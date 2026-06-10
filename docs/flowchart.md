# Dokumentasi Alur Keseluruhan Proyek Absolute Cinema
**Proyek:** Absolute Cinema (Sistem Review & Rating Film Berbasis OOP)
**Teknologi:** Java, Spring Boot, Thymeleaf, MySQL

Dokumen ini memuat arsitektur sistem dan alur kerja (flowchart) utama yang menghubungkan seluruh komponen aplikasi yang dikerjakan oleh 12 anggota tim.

---

## 1. Arsitektur Sistem (Bagaimana Semua Tim Bekerja Sama)
Diagram ini menunjukkan alur data dari layar pengguna (*Frontend*) hingga tersimpan ke *Database* (*Backend*).

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

## 2. Alur Pengguna Utama (User Journey)
Ini adalah alur dari sudut pandang *User* saat membuka aplikasi Absolute Cinema dari awal sampai selesai memberi ulasan.

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

---

## 3. Flowchart Autentikasi (Register & Login)
**Penanggung Jawab:** Orang 8 & 10
Alur logika ketika pengguna mendaftarkan akun baru atau mencoba masuk.

```mermaid
flowchart TD
    A([Mulai Register/Login]) --> B[/Terima Username & Password/]
    
    B --> C{Aksi yang dipilih?}
    
    C -- Register --> D[Cek apakah Username sudah ada?]
    D -- Ya --> E[Lempar Exception: Username Terpakai]
    D -- Tidak --> F[Enkripsi Password & Simpan User Baru]
    F --> G([Sukses Register, Arahkan ke Login])
    
    C -- Login --> H[Cari User berdasarkan Username]
    H --> I{Apakah User ada & Password cocok?}
    I -- Tidak --> J[Lempar Exception: Kredensial Salah]
    I -- Ya --> K[Buat Sesi Login / Generate Token]
    
    K --> L([Sukses Login, Masuk Beranda])
    
    E --> M([Selesai - Error])
    J --> M
```

---

## 4. Flowchart Tambah Ulasan & Hitung Rating Otomatis
**Penanggung Jawab:** Orang 6 & Orang 2
Ini adalah fitur utama proyek OOP ini. Menerapkan enkapsulasi untuk menghitung rata-rata skor saat ulasan baru ditambahkan.

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
