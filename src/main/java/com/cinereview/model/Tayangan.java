package com.cinereview.model;

import jakarta.persistence.*;

/**
 * Abstract Class Tayangan adalah kelas induk (parent class) untuk
 * semua jenis tayangan hiburan dalam sistem CineReview.
 *
 * Demonstrasi Pilar OOP:
 * - INHERITANCE  : Film dan SerialTV mewarisi semua atribut di sini
 * - ENCAPSULATION: Semua atribut private, diakses via getter/setter
 * - ABSTRACTION  : Method tampilkanDetail() bersifat abstract (wajib di-override)
 * - POLYMORPHISM : Implementasi tampilkanDetail() berbeda di setiap kelas anak
 *
 * Catatan: Kelas ini menggunakan @MappedSuperclass agar JPA (Orang 3)
 * bisa memetakan atribut ini ke tabel database masing-masing kelas anak.
 */
@MappedSuperclass
public abstract class Tayangan implements Rateable {

    // =========================================================
    // ATRIBUT — Semua PRIVATE (Demonstrasi ENCAPSULATION)
    // =========================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String judul;

    @Column(length = 1000)
    private String sinopsis;

    @Column(nullable = false)
    private int tahunRilis;

    /**
     * totalSkor dan jumlahReviewer adalah atribut SENSITIF.
     * Tidak boleh diubah langsung dari luar kelas.
     * Perubahan hanya bisa dilakukan melalui method tambahReview().
     */
    private double totalSkor = 0.0;
    private int jumlahReviewer = 0;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    // Constructor kosong wajib ada untuk JPA
    protected Tayangan() {
    }

    public Tayangan(String judul, String sinopsis, int tahunRilis) {
        this.judul = judul;
        this.sinopsis = sinopsis;
        this.tahunRilis = tahunRilis;
    }

    // =========================================================
    // METHOD INTI — Logika kalkulasi rating (ENCAPSULATION)
    // =========================================================

    /**
     * Menambahkan skor review baru dan memperbarui totalSkor.
     * Method ini adalah SATU-SATUNYA cara yang diizinkan untuk
     * mengubah nilai totalSkor dan jumlahReviewer dari luar.
     *
     * Validasi: skor harus antara 1 dan 5.
     *
     * @param skor nilai bintang yang diberikan reviewer (1–5)
     * @throws IllegalArgumentException jika skor di luar rentang 1–5
     */
    public void tambahReview(int skor) {
        if (skor < 1 || skor > 5) {
            throw new IllegalArgumentException(
                "Skor review harus antara 1 dan 5. Skor diterima: " + skor
            );
        }
        this.totalSkor += skor;
        this.jumlahReviewer++;
    }

    /**
     * Implementasi dari interface Rateable.
     * Rumus: rata-rata = totalSkor / jumlahReviewer
     * Jika belum ada reviewer, kembalikan 0.0 untuk menghindari ArithmeticException.
     *
     * @return rating rata-rata, atau 0.0 jika belum ada ulasan
     */
    @Override
    public double hitungRatingRataRata() {
        if (jumlahReviewer == 0) {
            return 0.0;
        }
        return totalSkor / jumlahReviewer;
    }

    /**
     * Method abstract yang WAJIB di-override oleh kelas anak.
     * Demonstrasi POLYMORPHISM: Film akan menampilkan durasi,
     * SerialTV akan menampilkan jumlah musim & episode.
     *
     * @return String berisi detail spesifik tayangan
     */
    public abstract String tampilkanDetail();

    // =========================================================
    // GETTER — Akses data dari luar (ENCAPSULATION)
    // =========================================================

    public Long getId() {
        return id;
    }

    public String getJudul() {
        return judul;
    }

    public String getSinopsis() {
        return sinopsis;
    }

    public int getTahunRilis() {
        return tahunRilis;
    }

    public double getTotalSkor() {
        return totalSkor;
    }

    public int getJumlahReviewer() {
        return jumlahReviewer;
    }

    // =========================================================
    // SETTER — Dengan validasi (ENCAPSULATION)
    // =========================================================

    public void setJudul(String judul) {
        if (judul == null || judul.trim().isEmpty()) {
            throw new IllegalArgumentException("Judul tidak boleh kosong.");
        }
        this.judul = judul;
    }

    public void setSinopsis(String sinopsis) {
        this.sinopsis = sinopsis;
    }

    public void setTahunRilis(int tahunRilis) {
        if (tahunRilis < 1888 || tahunRilis > 2100) {
            // Tahun 1888 adalah perkiraan film pertama di dunia
            throw new IllegalArgumentException(
                "Tahun rilis tidak valid: " + tahunRilis
            );
        }
        this.tahunRilis = tahunRilis;
    }

    // TIDAK ADA setter untuk totalSkor dan jumlahReviewer.
    // Keduanya hanya bisa diubah melalui method tambahReview().

    // =========================================================
    // OVERRIDE toString untuk debugging
    // =========================================================

    @Override
    public String toString() {
        return "Tayangan{" +
                "id=" + id +
                ", judul='" + judul + '\'' +
                ", tahunRilis=" + tahunRilis +
                ", rating=" + String.format("%.2f", hitungRatingRataRata()) +
                '}';
    }
}
