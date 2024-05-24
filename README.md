# eyeofhope-app
 
EyeofHope - Görme Engelliler İçin Sesli Navigasyon Rehberi
Geliştiriciler:

Feyza MIYNAT
Bilişim Sistemleri Mühendisliği, Kocaeli Üniversitesi
211307084@kocaeli.edu.tr

Büşra ÇELİKÇİOĞLU
Bilişim Sistemleri Mühendisliği, Kocaeli Üniversitesi
211307093@kocaeli.edu.tr

Batuhan KOÇASLAN
Bilişim Sistemleri Mühendisliği, Kocaeli Üniversitesi
211307095@kocaeli.edu.tr

Genel Bakış
EyeofHope, görme engelli bireyler için sesli navigasyon rehberi sağlayan bir mobil uygulamadır. Bu uygulama, kullanıcıların sesli komutlar ile istedikleri hedefi girmelerini ve gerçek zamanlı sesli yönlendirme talimatları almalarını sağlar. Aynı zamanda gürültülü ortamlarda titreşimli geri bildirim ve rota sapmalarında uyarılar sunarak görme engelli kullanıcıların güvenli ve bağımsız bir şekilde seyahat etmelerine yardımcı olur.

Özellikler
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
Sistem Mimarisi
Uygulama, çeşitli ana modüllere ayrılmıştır:

Konum Takibi: Kullanıcının mevcut konumunu GPS verileri ile belirler.
Harita Gösterimi: Kullanıcının konumunu ve rotasını harita üzerinde gösterir.
Ses İşleme: Kullanıcı sesli komutlarını eyleme dönüştürür.
Sesli Rehberlik: Gerçek zamanlı, adım adım navigasyon talimatları sağlar.
Titreşimli Geri Bildirim: Gürültülü ortamlarda titreşimli bildirimler sunar.
Engel Tespiti: MobileNetV1 kullanarak engelleri tespit eder ve kullanıcıyı bilgilendirir.
Kurulum
Uygulamayı kurmak ve çalıştırmak için:

Bu depo'yu yerel makinenize klonlayın:
```sh git clone https://github.com/your-repo/eyeofhope.git

Projeyi Android Studio'da açın.
Projeyi Gradle dosyaları ile senkronize edin.
Uygulamayı bir Android cihaz veya emülatör üzerinde derleyin ve çalıştırın.
Kullanım
Uygulamayı Başlatın: EyeofHope'u Android cihazınızda açın.
Sesli Komut: Mikrofon düğmesine basın ve gitmek istediğiniz yeri söyleyin.
Talimatları Takip Edin: Hedefinize güvenli bir şekilde ulaşmak için sesli yönlendirme talimatlarını dinleyin.
Uyarılar ve Geri Bildirim: Titreşimli uyarılara ve rota sapma bildirimlerine dikkat edin.
Geliştirme Zorlukları
EyeofHope'u geliştirirken ekip olarak karşılaştığımız zorluklar arasında şunlar yer alıyor:

Farklı veri formatlarına sahip çeşitli API'leri entegre etmek ve güncellemeleri yönetmek.
Uygulamanın kararlılığını ve performansını sağlamak.
Mobil platformlara özgü karmaşık yapıları ele almak.
Bu zorluklar, dikkatli dokümantasyon incelemeleri, yoğun hata ayıklama ve alternatif çözümler araştırarak aşılmıştır.

Ekibe Katkıları
EyeofHope projesi, ekibimize birçok fayda sağlamıştır:

Takım çalışması ve işbirliği becerilerinin geliştirilmesi.
Mobil geliştirme konusunda teknik bilgi ve pratik deneyimin artırılması.
Proje zorluklarını aşarak problem çözme yeteneklerinin güçlenmesi.
Referanslar
WeWALK Erişilebilir Navigasyon Uygulaması
BlindEye - Blind Android Projesi GitHub
Udemy'de Android O Mobil Uygulama Geliştirme Kursu
EyeofHope'un görme engelli bireylerin hareketliliğini ve bağımsızlığını artırmasını umuyoruz. Herhangi bir soru veya destek için geliştiriciler ile sağlanan e-posta adresleri üzerinden iletişime geçebilirsiniz.
