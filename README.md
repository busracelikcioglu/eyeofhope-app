# eyeofhope-app
 
EyeofHope - Görme Engelliler İçin Sesli Navigasyon Rehberi
## Genel Bakış
EyeofHope, görme engelli bireyler için sesli navigasyon rehberi sağlayan bir mobil uygulamadır. Bu uygulama, kullanıcıların sesli komutlar ile istedikleri hedefi girmelerini ve gerçek zamanlı sesli yönlendirme talimatları almalarını sağlar. Aynı zamanda gürültülü ortamlarda titreşimli geri bildirim ve rota sapmalarında uyarılar sunarak görme engelli kullanıcıların güvenli ve bağımsız bir şekilde seyahat etmelerine yardımcı olur.

## Özellikler
Sesli Komut Girişi: Kullanıcılar, sesli komutlar ile hedeflerini belirleyebilir.
Gerçek Zamanlı Navigasyon: Adım adım sesli yönlendirme talimatları sağlar.
Özel Haritalar ve İşaretçiler: Rotalar ve işaretçili özel haritalar oluşturur.
Titreşimli Geri Bildirim: Gürültülü ortamlarda titreşimli bildirimler sunar.
Rota Sapma Uyarıları: Rota sapmalarında kullanıcıyı uyarır.
Engel Tespiti: Rota üzerindeki engelleri algılar ve kullanıcıyı bilgilendirir.
Kullanılan Teknolojiler
Programlama Dilleri: Kotlin ve Java
Makine Öğrenimi: Görüntü işleme ve engel tespiti için TensorFlow ve MobileNetV1
Android UI: Jetpack Compose ve XML
GPS ve Haritalar: Konum takibi ve haritalama için GPS verileri kullanılır

## Sistem Mimarisi
Uygulama, çeşitli ana modüllere ayrılmıştır:
Konum Takibi: Kullanıcının mevcut konumunu GPS verileri ile belirler.
Harita Gösterimi: Kullanıcının konumunu ve rotasını harita üzerinde gösterir.
Ses İşleme: Kullanıcı sesli komutlarını eyleme dönüştürür.
Sesli Rehberlik: Gerçek zamanlı, adım adım navigasyon talimatları sağlar.
Titreşimli Geri Bildirim: Gürültülü ortamlarda titreşimli bildirimler sunar.
Engel Tespiti: MobileNetV1 kullanarak engelleri tespit eder ve kullanıcıyı bilgilendirir.

