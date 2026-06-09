package com.cinereview.model;

import jakarta.persistence.*;

/**
 * Class SerialTV adalah kelas turunan (child class) dari Tayangan.
 * Merepresentasikan Serial TV dalam sistem CineReview.
 *
 * Demonstrasi Pilar OOP:
 * - INHERITANCE  : Mewarisi semua atribut dari Tayangan (id, judul, sinopsis, dll)
 * - POLYMORPHISM : Override tampilkanDetail() dengan versi khusus SerialTV
 *                  (menampilkan jumlah musim & total episode, bukan durasi)
 */
@Entity
@Table(name = "serial_tv")
public class SerialTV extends Tayangan {

    // =========================================================
    // ATRIBUT KHUSUS SERIAL TV — Private (ENCAPSULATION)
    // =========================================================

    /**
     * jumlahMusim dan totalEpisode adalah atribut yang TIDAK ADA di Film.
     * Ini yang membedakan SerialTV dari Film secara struktural.
     */
    @Column(nullable = false)
    private int jumlahMusim;

    @Column(nullable = false)
    private int totalEpisode;

    private String jaringanTV; // Contoh: "Netflix", "HBO", "Disney+"

    private boolean masihBerjalan; // true jika serial masih on-going

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    // Constructor kosong wajib ada untuk JPA
    public SerialTV() {
        super();
    }

    public SerialTV(
        String judul,
        String sinopsis,
        int tahunRilis,
        int jumlahMusim,
        int totalEpisode,
        String jaringanTV,
        boolean masihBerjalan
    ) {
        super(judul, sinopsis, tahunRilis);
        this.jumlahMusim = jumlahMusim;
        this.totalEpisode = totalEpisode;
        this.jaringanTV = jaringanTV;
        this.masihBerjalan = masihBerjalan;
    }

    // =========================================================
    // OVERRIDE METHOD ABSTRACT — Demonstrasi POLYMORPHISM
    // =========================================================

    /**
     * Implementasi tampilkanDetail() khusus untuk SerialTV.
     * Menampilkan jumlah musim, total episode, dan status tayang.
     *
     * Bandingkan dengan Film.tampilkanDetail() yang menampilkan durasi.
     * Satu method (tampilkanDetail()), dua perilaku berbeda = POLYMORPHISM.
     *
     * @return String detail spesifik SerialTV
     */
    @Override
    public String tampilkanDetail() {
        String statusTayang = masihBerjalan
            ? "Masih Berjalan (On-Going)"
            : "Sudah Tamat";

        return String.format(
            "[SERIAL TV] %s (%d)\n" +
            "Jaringan : %s\n" +
            "Musim    : %d musim, %d total episode\n" +
            "Status   : %s\n" +
            "Rating   : %.2f / 5.00 (%d ulasan)",
            getJudul(),
            getTahunRilis(),
            jaringanTV,
            jumlahMusim,
            totalEpisode,
            statusTayang,
            hitungRatingRataRata(),
            getJumlahReviewer()
        );
    }

    // =========================================================
    // GETTER & SETTER (ENCAPSULATION)
    // =========================================================

    public int getJumlahMusim() {
        return jumlahMusim;
    }

    public void setJumlahMusim(int jumlahMusim) {
        if (jumlahMusim <= 0) {
            throw new IllegalArgumentException(
                "Jumlah musim harus minimal 1."
            );
        }
        this.jumlahMusim = jumlahMusim;
    }

    public int getTotalEpisode() {
        return totalEpisode;
    }

    public void setTotalEpisode(int totalEpisode) {
        if (totalEpisode <= 0) {
            throw new IllegalArgumentException(
                "Total episode harus minimal 1."
            );
        }
        this.totalEpisode = totalEpisode;
    }

    public String getJaringanTV() {
        return jaringanTV;
    }

    public void setJaringanTV(String jaringanTV) {
        this.jaringanTV = jaringanTV;
    }

    public boolean isMasihBerjalan() {
        return masihBerjalan;
    }

    public void setMasihBerjalan(boolean masihBerjalan) {
        this.masihBerjalan = masihBerjalan;
    }
}
