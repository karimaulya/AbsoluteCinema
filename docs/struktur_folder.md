```text
AbsoluteCinema/
├── .github/                 # (Opsional) Buat nyimpen template Pull Request biar seragam
├── docs/                    # Folder buat nyimpen Class Diagram, spesifikasi proyek, dll.
├── src/
│   ├── main/
│   │   ├── java/com/oop/absolutecinema/
│   │   │   ├── AbsoluteCinema.java  # File utama buat nge-run Spring Boot
│   │   │   ├── config/              # Konfigurasi Security, Swagger, dll.
│   │   │   ├── controller/          # File REST API (TayanganController, ReviewController)
│   │   │   ├── dto/                 # Data Transfer Object biar data aman pas dikirim
│   │   │   ├── entity/              # Class OOP: Tayangan, Film, SerialTV, User, Review
│   │   │   ├── repository/          # Interface Spring Data JPA buat interaksi ke database
│   │   │   └── service/             # Logika bisnis & antarmuka (TayanganService, dll.)
│   │   └── resources/
│   │       ├── application.yml      # Konfigurasi koneksi database, port, dll.
│   │       ├── static/              # Tempat file CSS, JS, dan gambar buat frontend
│   │       └── templates/           # File HTML/Thymeleaf buat tampilan web
│   └── test/
│       └── java/com/oop/absolutecinema/
│           ├── controller/          # Testing buat endpoint
│           └── service/             # Testing buat logika bisnis (Unit Test)
├── .gitignore               # PENTING: Biar file sampah/lokal nggak ikut ke-push
├── pom.xml                  # Daftar library/dependency (Spring Web, JPA, MySQL, dll.)
└── README.md                # Halaman depan GitHub: Penjelasan proyek & cara jalannya
```
