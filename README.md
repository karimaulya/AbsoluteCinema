# Absolute Cinema
**Sistem Review & Rating Film Berbasis Object-Oriented Programming (OOP)**

Repositori ini merupakan backend service untuk aplikasi Absolute Cinema. Proyek ini adalah sebuah platform backend API (dengan antarmuka web) yang memungkinkan pengguna untuk mencari tayangan, melihat detail, memberikan rating, menulis ulasan, serta mengelola profil. Proyek ini dirancang khusus untuk memenuhi kriteria dan mendemonstrasikan pemahaman mata kuliah Pemrograman Berorientasi Objek (OOP) menggunakan bahasa Java dan framework Spring Boot.

---

## Fitur Utama
* **Kalkulasi Rating Otomatis:** Perhitungan rata-rata skor tayangan secara real-time menggunakan prinsip enkapsulasi OOP saat ulasan baru masuk.
* **Autentikasi & Keamanan:** Sistem login dan registrasi yang diamankan dengan Spring Security, password hashing (BCrypt), dan verifikasi akun berbasis Email OTP.
* **Unggah Gambar (ImageKit CDN):** Fitur unggah gambar ke ImageKit.io (CDN eksternal) sehingga menghasilkan URL publik yang disimpan di database — Railway-safe dan tidak hilang saat redeploy.
* **Import dari TMDB:** Admin dapat mencari film/serial di The Movie Database (themoviedb.org) dan mengimpornya ke katalog dalam satu klik — metadata (judul, sinopsis, tahun, genre, sutradara, jumlah musim/episode, poster) terisi otomatis. API key TMDB tetap di server dan tidak pernah terekspos ke browser.
* **Katalog Dinamis:** Pemisahan entitas antara Film dan Serial TV dengan properti dan detail yang spesifik untuk masing-masing jenis tayangan.
* **PWA (Progressive Web App):** aplikasi dapat diinstal sebagai PWA di Android/desktop dan tetap menampilkan halaman katalog meski koneksi terputus.

---

## Penerapan 4 Pilar OOP
Fokus utama arsitektur sistem ini adalah mendemonstrasikan empat pilar utama OOP:

* **Inheritance:** Class anak seperti `Film` (memiliki atribut durasiMenit) dan `SerialTV` (memiliki atribut jumlahMusim dan totalEpisode) mewarisi atribut dasar dari Abstract Class induk `Tayangan`.
* **Encapsulation:** Semua variabel di-set private. Atribut sensitif seperti totalSkor dan jumlahReviewer tidak bisa diubah langsung dari luar, melainkan harus melalui method internal `tambahReview(int skor)`.
* **Polymorphism:** Penerapan Method Overriding pada fungsi `tampilkanDetail()` yang menghasilkan output informasi yang berbeda untuk objek Film dan SerialTV.
* **Abstraction:** Menggunakan Interface bernama `Rateable` yang memuat kontrak fungsi `hitungRatingRataRata()`.

---

## Tech Stack & Arsitektur
Proyek ini dibangun menggunakan teknologi berikut:
* **Bahasa Pemrograman:** Java
* **Framework Backend:** Spring Boot, Spring Security, Spring Data JPA
* **Database:** MySQL
* **Mail Server:** Java Mail Sender (untuk pengiriman OTP)
* **View Template:** Thymeleaf / HTML

### Arsitektur Sistem Layering
Diagram ini menunjukkan alur data dari Frontend hingga tersimpan ke Database.

```mermaid
graph LR
    subgraph UI/View Layer
        A[Halaman Thymeleaf / HTML]
    end

    subgraph Controller Layer
        B(REST Controllers & UI Controllers)
    end

    subgraph Service Layer
        C{Service Logic & File Handling}
    end

    subgraph Repository & Model
        D[Spring Data JPA]
        E((Database MySQL))
    end

    A -- "HTTP Request" --> B
    B -- "Kirim DTO" --> C
    C -- "Terapkan Logika OOP" --> D
    D -- "Query SQL Otomatis" --> E
    E -- "Kembalikan Data" --> D
    D -- "Kembalikan Objek/Entity" --> C
    C -- "Kirim DTO" --> B
    B -- "Render Tampilan" --> A
```

---

## Class Diagram Komprehensif
Diagram kelas ini telah disesuaikan untuk mencakup struktur lapisan (layer) arsitektur secara lengkap.

**1. Domain Model (Entity & DTO Layer)**
![Class Diagram Domain Model](docs/Domain%20Model.png)

**2. Business Model (Service & Repository Layer)**
![Class Diagram Business Model](docs/Business%20Logic.png)

**3. API & Presentation (Controller & Security)**
![Class Diagram API & Presentation](docs/API%20Presentation.png)

---

## Alur Logika Sistem (Flowcharts)

### 1. User Journey Utama
Alur dari sudut pandang pengguna saat membuka aplikasi hingga memberikan ulasan.

```mermaid
flowchart TD
    A([Buka Aplikasi]) --> B{Sudah punya akun?}
    B -- Belum --> C[Masuk Halaman Register]
    B -- Sudah --> D[Masuk Halaman Login]
    C -->|Kirim OTP & Verifikasi| D
    D -->|Login Sukses| E[Masuk Halaman Utama / Katalog]
    E --> F[Cari & Pilih Tayangan]
    F --> G[Buka Halaman Detail Tayangan]
    G --> H{Ingin Beri Ulasan?}
    H -- Tidak --> I([Selesai / Kembali ke Katalog])
    H -- Ya --> J[Isi Form Bintang 1-5 & Komentar]
    J --> K[Sistem Memvalidasi & Menghitung Ulang Rating]
    K --> G
```

### 2. Logika Hitung Rating Otomatis (OOP Core)
Alur ini menerapkan enkapsulasi untuk mencegah manipulasi skor secara langsung.

```mermaid
flowchart TD
    A([Mulai Tambah Ulasan]) --> B[/Terima Input: ID Tayangan, ID User, Skor 1-5, Teks/]
    B --> C[Validasi: Cek Tayangan & User di Database]
    C --> D{Apakah data valid?}
    D -- Tidak --> E[Lempar Exception: Data Tidak Valid]
    D -- Ya --> F{Apakah User sudah pernah mereview?}
    F -- Ya --> G[Lempar Exception: Dilarang Review Ganda]
    F -- Tidak --> H[Buat Objek Review Baru]
    H --> I[Panggil method tayangan.tambahReview]
    I --> J[Logika OOP: Skor ditambahkan, dibagi jumlah reviewer]
    J --> K[Simpan Objek Review & Update Tayangan ke Database]
    K --> L([Selesai - Ulasan & Rating Berhasil Diperbarui])
    E --> M([Selesai - Error])
    G --> M
```

---

## Pembagian Tugas Kelompok (12 Orang)
Tim dibagi menjadi 3 divisi utama untuk memastikan pengembangan yang terstruktur.

| Divisi | Peran | Deskripsi Tugas Utama |
| :--- | :--- | :--- |
| **Backend Core** | Orang 1 | Membuat abstract class `Tayangan`, turunannya, dan interface `Rateable`. |
| | Orang 2 | Membuat entitas `User`, `Review`, dan logika enkapsulasi kalkulasi rating. |
| | Orang 3 | Mengonfigurasi Spring Data JPA dan mendesain relasi entitas. |
| | Orang 4 | Membuat seluruh antarmuka Repository dan custom query. |
| **Service & API** | Orang 5 | Membuat `TayanganService` untuk logika bisnis CRUD katalog film. |
| | Orang 6 | Membuat `ReviewService` untuk logika ulasan dan pembuatan `FileStorageService`. |
| | Orang 7 | Membuat kumpulan Controller untuk API tayangan, ulasan, dan integrasi upload. |
| | Orang 8 | Membuat DTO, konfigurasi Spring Security, dan logika Email OTP. |
| **View & QA** | Orang 9 | Membuat halaman Thymeleaf untuk antarmuka katalog dan detail. |
| | Orang 10 | Membuat antarmuka form interaktif dan halaman autentikasi/profil. |
| | Orang 11 | Membuat Unit Testing untuk validasi logika menggunakan JUnit/Mockito. |
| | Orang 12 | Project Manager, pengelola Git, dan penyusun dokumentasi Swagger. |

---

## Dokumentasi API

Untuk menguji API endpoint secara langsung, sistem ini telah terintegrasi dengan Swagger UI.
* **Swagger UI Endpoint:** `http://localhost:8080/swagger-ui/index.html`
* **OpenAPI Specification:** Silakan lihat file `openapi.yaml` pada direktori root proyek ini.

---

## Deployment (Railway)

Aplikasi merupakan Progressive Web App (PWA) yang dapat dipasang (installable) dari domain Railway. Semua rahasia dibaca dari environment variables — tidak ada kredensial yang di-hardcode di repo.

### Environment Variables yang harus diset

| Variabel | Wajib | Keterangan |
| :--- | :---: | :--- |
| `DB_URL` | Ya | JDBC URL MySQL (mis. `jdbc:mysql://...:3306/dbname`). |
| `DB_USER` | Ya | User database MySQL. |
| `DB_PASS` | Ya | Password database MySQL. |
| `PORT` | Otomatis | Diset otomatis Railway; default `8080` jika kosong. |
| `MAIL_USERNAME` | Ya* | Email Gmail untuk mengirim OTP (pakai Gmail App Password, bukan password biasa). |
| `MAIL_PASSWORD` | Ya* | App Password Gmail. \*Wajib bila fitur register/forgot-password digunakan. |
| `IMAGEKIT_PRIVATE_KEY` | Ya** | Private key ImageKit untuk upload server-side. |
| `IMAGEKIT_PUBLIC_KEY` | Opsional | Public key ImageKit (untuk referensi). |
| `IMAGEKIT_URL_ENDPOINT` | Opsional | URL endpoint ImageKit (mis. `https://ik.imagekit.io/...`). |
| `TMDB_API_KEY` | Opsional*** | API key The Movie Database (themoviedb.org) untuk fitur Import di halaman admin. Dapatkan gratis setelah mendaftar & request API key di Settings → API. |
| `TMDB_BASE_URL` | Opsional | Override base URL TMDB (default `https://api.themoviedb.org/3`). |
| `TMDB_IMAGE_BASE` | Opsional | Override base URL poster (default `https://image.tmdb.org/t/p/w500`). |
| `TMDB_LANGUAGE` | Opsional | Bahasa respons TMDB (default `id-ID`). |

> **Catatan:** Aplikasi tetap booting walau kredensial mail/ImageKit/TMDB kosong; error hanya muncul saat fitur terkait benar-benar dipakai. \*\*\*TMDB dibutuhkan hanya saat admin memakai fitur "Import dari TMDB" di `/admin`; katalog manual tetap berfungsi tanpanya.

### Catatan Database
Skema dibuat otomatis oleh Hibernate (`ddl-auto: update`). Kolom `email` dan `aktif` pada tabel `users` ditambahkan via ALTER dengan `DEFAULT TRUE`, sehingga user lama tidak terkunci. Jalankan `database_seed.sql` hanya pada database kosong untuk data contoh.

---

## Project Structure
```text
com.oop.absolutecinema
│
├── AbsoluteCinema.java                  # Entry point aplikasi Spring Boot
│
├── config/
│   └── SecurityConfig.java              # Konfigurasi Spring Security & role-based access
│
├── controller/
│   ├── AdminController.java             # Manajemen tayangan (admin only)
│   ├── AuthController.java              # Register, login, verifikasi OTP
│   ├── ReviewController.java            # Submit ulasan & rating
│   ├── TayanganController.java          # REST API katalog film & serial
│   ├── TmdbController.java              # Import metadata dari TMDB API
│   ├── UIController.java                # Routing halaman Thymeleaf
│   ├── UploadController.java            # Upload poster ke ImageKit CDN
│   └── UserController.java              # Profil pengguna
│
├── DTO/
│   ├── FilmDTO.java                     # Transfer data Film
│   ├── ReviewDTO.java                   # Transfer data Review (Request/Response)
│   ├── TmdbDTO.java                     # Transfer data hasil pencarian TMDB
│   └── UserDTO.java                     # Transfer data User
│
├── entity/
│   ├── Tayangan.java                    # Abstract class induk (OOP: Abstraction)
│   ├── Film.java                        # Extends Tayangan (OOP: Inheritance)
│   ├── SerialTV.java                    # Extends Tayangan (OOP: Inheritance)
│   ├── Rateable.java                    # Interface kalkulasi rating (OOP: Abstraction)
│   ├── Review.java                      # Entity ulasan — relasi User ↔ Tayangan
│   └── User.java                        # Entity akun pengguna
│
├── exception/
│   ├── DataTidakDitemukanException.java # Thrown saat data tidak ditemukan (404)
│   └── JudulDuplikatException.java      # Thrown saat judul tayangan sudah ada
│
├── repository/
│   ├── FilmRepository.java              # JPA Repository untuk Film
│   ├── ReviewRepository.java            # JPA Repository untuk Review
│   ├── SerialTVRepository.java          # JPA Repository untuk SerialTV
│   └── UserRepository.java              # JPA Repository untuk User
│
└── service/
    ├── TayanganService.java             # Interface service tayangan
    ├── TayanganServiceImpl.java         # Implementasi logika bisnis tayangan
    ├── ReviewService.java               # Interface service review
    ├── ReviewServiceImpl.java           # Implementasi kalkulasi rating (tambahReview)
    ├── UserService.java                 # Interface service user
    ├── UserServiceImpl.java             # Implementasi manajemen akun
    ├── AuthService.java                 # Logika autentikasi & verifikasi OTP
    ├── EmailService.java                # Kirim email via Brevo HTTP API
    ├── OTPMailService.java              # Generate & validasi kode OTP
    ├── ImageKitService.java             # Upload & manajemen gambar ke CDN
    ├── TmdbService.java                 # Fetch & import data dari TMDB
    ├── CustomUserDetails.java           # Wrapper User untuk Spring Security
    └── CustomUserDetailsService.java    # Load user by username (Spring Security)
```
---
