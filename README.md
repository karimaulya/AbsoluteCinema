# 🎬 Absolute Cinema
**Sistem Review & Rating Film Berbasis Object-Oriented Programming (OOP)**

Repositori ini merupakan *backend service* untuk aplikasi **Absolute Cinema**. Proyek ini adalah sebuah platform *backend API* (dengan antarmuka Thymeleaf) yang memungkinkan pengguna untuk mencari, melihat detail, memberikan rating, dan menulis ulasan tayangan hiburan. Proyek ini dirancang khusus untuk memenuhi kriteria mata kuliah OOP menggunakan Java dan Spring Boot.

---

## 💡 Penerapan 4 Pilar OOP
Fokus utama sistem ini adalah kalkulasi rating otomatis secara *real-time* saat ulasan masuk, dengan mendemonstrasikan empat pilar OOP:
* **Inheritance:** `Class` anak seperti `Film` (memiliki atribut `durasiMenit`) dan `SerialTV` (memiliki atribut `jumlahMusim` dan `totalEpisode`) mewarisi atribut dari `Abstract Class` induk `Tayangan`.
* **Encapsulation:** Semua variabel di-set `private`. Atribut sensitif seperti `totalSkor` dan `jumlahReviewer` tidak bisa diubah langsung, melainkan melalui *method* internal `tambahReview(int skor)`.
* **Polymorphism:** Penerapan *Method Overriding* pada fungsi `tampilkanDetail()` yang menghasilkan *output* berbeda untuk objek `Film` dan `SerialTV`.
* **Abstraction:** Menggunakan `Interface` bernama `Rateable` yang memuat kontrak fungsi `hitungRatingRataRata()`.

---

## 🛠️ Tech Stack & Arsitektur
Proyek ini dibangun menggunakan:
* **Bahasa Pemrograman:** Java
* **Framework & Interactor:** Spring Boot dan Spring Data JPA
* **Database & Tampilan:** MySQL dan Thymeleaf / HTML

### Arsitektur Sistem Layering
Diagram ini menunjukkan alur data dari *Frontend* hingga tersimpan ke *Backend*.

```mermaid
graph LR
    subgraph UI/View Layer [Tim View: Orang 9 & 10]
        A[Halaman Thymeleaf / HTML]
    end

    subgraph Controller Layer [Tim API: Orang 7 & 8]
        B(TayanganController & \n ReviewController)
    end

    subgraph Service Layer [Tim Logic: Orang 5 & 6]
        C{TayanganService & \n ReviewService}
    end

    subgraph Repository & Model [Tim Core: Orang 1, 2, 3, 4]
        D[Spring Data JPA]
        E((Database MySQL))
    end

    A -- "HTTP Request (Klik/Submit)" --> B
    B -- "Kirim DTO" --> C
    C -- "Terapkan Logika OOP" --> D
    D -- "Query SQL Otomatis" --> E
    E -- "Kembalikan Data" --> D
    D -- "Kembalikan Objek/Entity" --> C
    C -- "Kirim DTO" --> B
    B -- "Render Tampilan" --> A
```

---

## 📊 Class Diagram Komprehensif
Diagram kelas ini telah disesuaikan untuk mencakup pembagian tugas ke-12 anggota tim, mulai dari Core Model, DTO, Repository, Service Layer, Controller, hingga Security.

```mermaid
classDiagram
    %% CORE & DOMAIN MODEL
    class Rateable {
        <<interface>>
        +hitungRatingRataRata() double
    }

    class Tayangan {
        <<abstract entity>>
        -String id
        -String judul
        -String sinopsis
        -int tahunRilis
        -int totalSkor
        -int jumlahReviewer
        +tambahReview(int skor) void
        +tampilkanDetail() void
        +getTotalSkor() int
        +getJumlahReviewer() int
    }

    class Film {
        <<entity>>
        -int durasiMenit
        +tampilkanDetail() void
    }

    class SerialTV {
        <<entity>>
        -int jumlahMusim
        -int totalEpisode
        +tampilkanDetail() void
    }

    class User {
        <<entity>>
        -String id
        -String username
        -String password
        -String role
    }

    class Review {
        <<entity>>
        -String id
        -int skor
        -String teks
        -User user
        -Tayangan tayangan
    }

    %% DATA TRANSFER OBJECT (DTO)
    class FilmDTO {
        <<DTO>>
        +String judul
        +String sinopsis
        +int durasiMenit
    }
    class ReviewDTO {
        <<DTO>>
        +int skor
        +String teks
        +String userId
        +String tayanganId
    }
    class UserDTO {
        <<DTO>>
        +String username
        +String password
    }

    %% REPOSITORY LAYER
    class UserRepository {
        <<interface>>
        +findByUsername(String username) User
    }
    class ReviewRepository {
        <<interface>>
        +findByTayanganId(String id) List~Review~
    }
    class FilmRepository {
        <<interface>>
        +findByJudul(String judul) List~Film~
    }
    class SerialTVRepository {
        <<interface>>
    }

    %% SERVICE LAYER
    class TayanganService {
        <<interface>>
        +tambahTayangan(Tayangan tayangan)
        +getSemuaTayangan()
        +getTayanganById(String id)
    }
    class TayanganServiceImpl {
        <<Service>>
        -FilmRepository filmRepo
        -SerialTVRepository serialTvRepo
    }

    class ReviewService {
        <<interface>>
        +tambahReview(ReviewDTO reviewDto)
    }
    class ReviewServiceImpl {
        <<Service>>
        -ReviewRepository reviewRepo
        -TayanganService tayanganService
        -UserRepository userRepo
    }

    %% CONTROLLER LAYER & SECURITY
    class TayanganController {
        <<RestController>>
        -TayanganService tayanganService
        +tampilkanKatalog()
        +tampilkanDetail()
    }
    class ReviewController {
        <<RestController>>
        -ReviewService reviewService
        +submitReview(ReviewDTO dto)
    }
    class SecurityConfig {
        <<Configuration>>
        +securityFilterChain()
        +passwordEncoder()
    }

    %% RELATIONSHIPS
    Rateable <|.. Tayangan : implements
    Tayangan <|-- Film : extends
    Tayangan <|-- SerialTV : extends

    Tayangan "1" -- "*" Review : memiliki (OneToMany)
    User "1" -- "*" Review : menulis (OneToMany)
    Review "*" --> "1" User : dimiliki (ManyToOne)
    Review "*" --> "1" Tayangan : terkait pada (ManyToOne)

    UserRepository ..> User : manages
    ReviewRepository ..> Review : manages
    FilmRepository ..> Film : manages
    SerialTVRepository ..> SerialTV : manages

    TayanganService <|.. TayanganServiceImpl : implements
    ReviewService <|.. ReviewServiceImpl : implements
    TayanganServiceImpl --> FilmRepository : uses
    TayanganServiceImpl --> SerialTVRepository : uses
    ReviewServiceImpl --> ReviewRepository : uses
    ReviewServiceImpl --> UserRepository : uses
    ReviewServiceImpl --> TayanganService : uses

    TayanganController --> TayanganService : uses
    ReviewController --> ReviewService : uses

    ReviewController ..> ReviewDTO : receives
    TayanganController ..> FilmDTO : receives
    SecurityConfig ..> User : secures
```

---

## 🗺️ Alur Logika Sistem (Flowcharts)

### 1. User Journey Utama
Alur dari sudut pandang *User* saat membuka aplikasi dari awal sampai selesai memberi ulasan.
```mermaid
flowchart TD
    A([Buka Aplikasi]) --> B{Sudah punya akun?}
    B -- Belum --> C[Masuk Halaman Register]
    B -- Sudah --> D[Masuk Halaman Login]
    C -->|Submit Data| D
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
Alur ini menerapkan enkapsulasi untuk menghitung rata-rata skor saat ulasan baru ditambahkan.
```mermaid
flowchart TD
    A([Mulai Tambah Ulasan]) --> B[/Terima Input: ID Tayangan, ID User, Skor 1-5, Teks/]
    B --> C[Validasi: Cek Tayangan & User di Database]
    C --> D{Apakah data valid?}
    D -- Tidak --> E[Lempar Exception: Data Tidak Valid]
    D -- Ya --> F{Apakah User sudah pernah \n mereview tayangan ini?}
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

## 👥 Pembagian Tugas Kelompok (12 Orang)
Tim dibagi menjadi 3 sub-tim utama: Backend Core, API & Integration, serta Support.

### A. Sub-Tim Backend Core (OOP & Domain Model)
* **Orang 1:** Core Architect - Membuat abstract class `Tayangan`, kelas turunan `Film` dan `SerialTV`, serta interface `Rateable`. 
* **Orang 2:** Domain Specialist - Membuat kelas entitas `User` dan `Review` beserta logika *encapsulation* dan rumus matematika kalkulasi rating.
* **Orang 3:** Database Engineer - Mengonfigurasi Spring Data JPA dan mendesain relasi antar objek (*One-to-Many*, dll).
* **Orang 4:** Repository Layer - Membuat semua antarmuka repository dan *custom query*.

### B. Sub-Tim Service & API (Logic & Controller)
* **Oya (Orang 5):** Service Layer (Catalog Logic) - Membuat `TayanganService` beserta implementasinya untuk mengatur logika bisnis CRUD.
* **Orang 6:** Service Layer (Review Logic) - Membuat `ReviewService` untuk logika unggah ulasan dan pencegahan duplikasi.
* **Orang 7:** Controller Layer - Membuat `TayanganController` dan `ReviewController` untuk REST API.
* **Orang 8:** DTO & Security - Membuat Data Transfer Object dan mengonfigurasi Spring Security.

### C. Sub-Tim View & Integrasi, Testing, & DevOps
* **Orang 9:** UI Developer (Katalog & Detail) - Membuat halaman HTML/Thymeleaf untuk daftar dan detail film.
* **Orang 10:** UI Developer (Form & Auth) - Membuat halaman form review dan visual login/register.
* **Orang 11:** Quality Assurance - Membuat Unit Testing menggunakan JUnit/Mockito.
* **Orang 12:** Project Manager - Mengatur Git, dokumentasi Swagger, dan menyusun materi presentasi.
````</Configuration></RestController></RestController></Service></Service></DTO></DTO></DTO>
