package com.example.yazgel2proje

import android.annotation.SuppressLint
import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicYuvToRGB
import android.telephony.SmsManager
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import com.example.eyeofhope.R
import org.tensorflow.lite.task.vision.detector.Detection
import java.nio.ByteBuffer


class FloatingOverMapIconService : Service(), ObjectDetectorHelper.DetectorListener {

    // UI
    private var wm: WindowManager? = null
    private var rootView: View? = null
    private var textureView: TextureView? = null

    // Camera2-related stuff
    private var cameraManager: CameraManager? = null
    private var previewSize: Size? = Size(640, 480)
    private var cameraDevice: CameraDevice? = null
    private var captureRequest: CaptureRequest? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null

    // Model Related
    private lateinit var objectDetectorHelper: ObjectDetectorHelper

    // Image Rotation
    private val ORIENTATIONS = SparseIntArray()

    // Floating icon
    private var windowManager: WindowManager? = null
    private var frameLayout: FrameLayout? = null
    private var surfaceView: SurfaceView? = null
    private var then = 0L
    private var emergency: MutableList<String> = mutableListOf()

    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {

        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) {}

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {}
    }

    private fun ARGBBitmap(img: Bitmap): Bitmap? {
        return img.copy(Bitmap.Config.ARGB_8888, true)
    }

    private fun imageToByteBuffer(image: Image): ByteBuffer? {
        val crop = image.cropRect
        val width = crop.width()
        val height = crop.height()
        val planes = image.planes
        val rowData = ByteArray(planes[0].rowStride)
        val bufferSize = width * height * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8
        val output = ByteBuffer.allocateDirect(bufferSize)
        var channelOffset = 0
        var outputStride = 0
        for (planeIndex in 0..2) {
            if (planeIndex == 0) {
                channelOffset = 0
                outputStride = 1
            } else if (planeIndex == 1) {
                channelOffset = width * height + 1
                outputStride = 2
            } else if (planeIndex == 2) {
                channelOffset = width * height
                outputStride = 2
            }
            val buffer = planes[planeIndex].buffer
            val rowStride = planes[planeIndex].rowStride
            val pixelStride = planes[planeIndex].pixelStride
            val shift = if (planeIndex == 0) 0 else 1
            val widthShifted = width shr shift
            val heightShifted = height shr shift
            buffer.position(rowStride * (crop.top shr shift) + pixelStride * (crop.left shr shift))
            for (row in 0 until heightShifted) {
                val length: Int
                if (pixelStride == 1 && outputStride == 1) {
                    length = widthShifted
                    buffer[output.array(), channelOffset, length]
                    channelOffset += length
                } else {
                    length = (widthShifted - 1) * pixelStride + 1
                    buffer[rowData, 0, length]
                    for (col in 0 until widthShifted) {
                        output.array()[channelOffset] = rowData[col * pixelStride]
                        channelOffset += outputStride
                    }
                }
                if (row < heightShifted - 1) {
                    buffer.position(buffer.position() + rowStride - length)
                }
            }
        }
        return output
    }



    private val imageListener = ImageReader.OnImageAvailableListener { reader ->


        //Log.d(TAG, "Got image: " + image?.width + " x " + image?.height)

        // Get the YUV data
        val image = reader?.acquireLatestImage()
        val yuvBytes: ByteBuffer? = image?.let { this.imageToByteBuffer(it) }


        // Convert YUV to RGB
        val rs = RenderScript.create(this)

        val bitmap =
            image?.let { Bitmap.createBitmap(it.width, image.height, Bitmap.Config.ARGB_8888) }

        if (bitmap != null) {
            val allocationRgb = Allocation.createFromBitmap(rs, bitmap)

            val allocationYuv = yuvBytes?.array()
                ?.let { Allocation.createSized(rs, Element.U8(rs), it.size) }
            if (yuvBytes != null) {
                allocationYuv?.copyFrom(yuvBytes.array())
            }

            val scriptYuvToRgb = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs))
            scriptYuvToRgb.setInput(allocationYuv)
            scriptYuvToRgb.forEach(allocationRgb)

            allocationRgb.copyTo(bitmap)

            // Release


            objectDetectorHelper.detect(bitmap, getRotationCompensation("0", false))

            allocationYuv?.destroy()
            allocationRgb?.destroy()
        }

        /*
        if (image != null) {
            val buffer: ByteBuffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.capacity())
            buffer.get(bytes)
            var bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)

            if (bitmapImage != null) {

                bitmapImage = ARGBBitmap(bitmapImage);
                objectDetectorHelper.detect(bitmapImage, getRotationCompensation("0", false));
            }
        }

         */

        // Release
        bitmap?.recycle()
        rs?.destroy()

        image?.close()

    }


    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     */
    @Throws(CameraAccessException::class)
    private fun getRotationCompensation(cameraId: String, isFrontFacing: Boolean): Int {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.

        val deviceRotation = resources.configuration.orientation
        var rotationCompensation = ORIENTATIONS.get(deviceRotation)

        // Get the device's sensor orientation.
        val cameraManager = this.getSystemService(CAMERA_SERVICE) as CameraManager
        val sensorOrientation = cameraManager
            .getCameraCharacteristics(cameraId)
            .get(CameraCharacteristics.SENSOR_ORIENTATION)!!

        rotationCompensation = if (isFrontFacing) {
            (sensorOrientation + rotationCompensation) % 360
        } else { // back-facing
            (sensorOrientation - rotationCompensation + 360) % 360
        }
        return rotationCompensation
    }


    private val stateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(currentCameraDevice: CameraDevice) {
            cameraDevice = currentCameraDevice
            createCaptureSession()
        }

        override fun onDisconnected(currentCameraDevice: CameraDevice) {
            currentCameraDevice.close()
            cameraDevice = null
        }

        override fun onError(currentCameraDevice: CameraDevice, error: Int) {
            currentCameraDevice.close()
            cameraDevice = null
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //super.onStartCommand(intent, flags, startId)

        when(intent?.action) {
            ACTION_START -> start()

        }

        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        objectDetectorHelper = ObjectDetectorHelper(
            context = this,
            objectDetectorListener = this)

        fillEmergency(
            ContactRetrievalHelper.getContactsByCustomLabel(
                applicationContext,
                "EyeOfHope"
            )
        )
        createFloatingBackButton()
        startForegroundFunction()

    }

    override fun onDestroy() {
        super.onDestroy()

        stopCamera()

        if (rootView != null)
            wm?.removeView(rootView)

        sendBroadcast(Intent(ACTION_STOPPED))
        windowManager!!.removeView(frameLayout)
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }



    private fun start() {

        initCam(1600, 1200)
    }

    @SuppressLint("MissingPermission")
    private fun initCam(width: Int, height: Int) {

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        var camId: String? = null

        for (id in cameraManager!!.cameraIdList) {
            val characteristics = cameraManager!!.getCameraCharacteristics(id)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                camId = id
                break
            }
        }

        if (camId != null) {
            cameraManager!!.openCamera(camId, stateCallback, null)
        }
    }

    private fun fillEmergency(e : MutableList<String>){
        emergency = e;
    }

    private fun startForegroundFunction() {

        val pendingIntent: PendingIntent =
            Intent(this, SearchOrFreeroam::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, FLAG_IMMUTABLE)
            }

        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getText(R.string.app_name))
            .setContentText(getText(R.string.app_name))
            .setSmallIcon(R.drawable.bg_service_icon)
            .setContentIntent(pendingIntent)
            .setTicker(getText(R.string.app_name))
            .build()

        startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    private fun createCaptureSession() {
        try {
            // Prepare surfaces we want to use in capture session
            val targetSurfaces = ArrayList<Surface>()

            // Prepare CaptureRequest that can be used with CameraCaptureSession
            val requestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {

                // Configure target surface for background processing (ImageReader)
                imageReader = ImageReader.newInstance(
                    previewSize!!.width, previewSize!!.height,
                    ImageFormat.YUV_420_888, 2
                )
                imageReader!!.setOnImageAvailableListener(imageListener, null)

                targetSurfaces.add(imageReader!!.surface)
                addTarget(imageReader!!.surface)

                // Set some additional parameters for the request
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
                //set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(5, 10))
            }

            // Prepare CameraCaptureSession
            cameraDevice!!.createCaptureSession(targetSurfaces,
                object : CameraCaptureSession.StateCallback() {

                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        // The camera is already closed
                        if (null == cameraDevice) {
                            return
                        }

                        captureSession = cameraCaptureSession
                        try {
                            // Now we can start capturing
                            captureRequest = requestBuilder!!.build()
                            captureSession!!.setRepeatingRequest(captureRequest!!, captureCallback, null)

                        } catch (e: CameraAccessException) {
                            Log.e(TAG, "createCaptureSession", e)
                        }

                    }

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                        Log.e(TAG, "createCaptureSession()")
                    }
                }, null
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, "createCaptureSession", e)
        }
    }

    private fun stopCamera() {
        try {
            captureSession?.close()
            captureSession = null

            cameraDevice?.close()
            cameraDevice = null

            imageReader?.close()
            imageReader = null

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }




    override fun onError(error: String) {
        Log.e("ObjectDetection", error)
    }

    override fun onResults(
        results: MutableList<Detection>?,
        inferenceTime: Long,
        imageHeight: Int,
        imageWidth: Int
    ) {
        // Handled in the class itself

    }



    @SuppressLint("MissingPermission", "ClickableViewAccessibility")
    private fun createFloatingBackButton() {

        //CurrentJobDetail.isFloatingIconServiceAlive = true;
        val LAYOUT_FLAG: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        surfaceView = SurfaceView(this)
        frameLayout = FrameLayout(this)
        val layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        layoutInflater.inflate(R.layout.custom_start_ride_back_button_over_map, frameLayout)
        val backOnMap: ImageView =
            frameLayout!!.findViewById<View>(R.id.custom_drawover_back_button) as ImageView
        backOnMap.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@FloatingOverMapIconService, SearchOrFreeroam::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)

            this@FloatingOverMapIconService.stopSelf()
        })
        backOnMap.setOnTouchListener(OnTouchListener { v, event ->
            val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
            if (System.currentTimeMillis() - then > 3000){
                v.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.EFFECT_HEAVY_CLICK));
            } else {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            }
            if (event.action == MotionEvent.ACTION_DOWN) {
                then = System.currentTimeMillis()

            } else if (event.action == MotionEvent.ACTION_UP) {
                if (System.currentTimeMillis() - then > 3000) {
                    val say = Speaking(this@FloatingOverMapIconService, "Calling Emergency")
                    var msg : String = "Help !!!"

                    val smsManager: SmsManager = SmsManager.getDefault()

                    for (phoneNumber in emergency) {
                        smsManager.sendTextMessage(phoneNumber, null, msg, null, null)
                    }

                    return@OnTouchListener true
                }
            }
            false
        })
        windowManager!!.addView(frameLayout, params)
    }


    companion object {

        val TAG = "CamService"

        var ACTION_START = "com.example.eyeofhope.action.START"
        var ACTION_STOPPED = "com.example.eyeofhope.action.STOPPED"

        val ONGOING_NOTIFICATION_ID = 6660
        val CHANNEL_ID = "cam_service_channel_id"
        val CHANNEL_NAME = "cam_service_channel_name"

    }

}