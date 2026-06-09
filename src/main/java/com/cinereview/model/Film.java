package com.cinereview.model;

import jakarta.persistence.*;

/**
 * Class Film adalah kelas turunan (child class) dari Tayangan.
 * Merepresentasikan Film Layar Lebar dalam sistem CineReview.
 *
 * Demonstrasi Pilar OOP:
 * - INHERITANCE  : Mewarisi semua atribut dari Tayangan (id, judul, sinopsis, dll)
 * - POLYMORPHISM : Override tampilkanDetail() dengan versi khusus Film (menampilkan durasi)
 *
 * @Entity dan @Table dibutuhkan oleh JPA (akan dikonfigurasi lebih lanjut oleh Orang 3)
 */
@Entity
@Table(name = "film")
public class Film extends Tayangan {

    // =========================================================
    // ATRIBUT KHUSUS FILM — Private (ENCAPSULATION)
    // =========================================================

    /**
     * Durasi film dalam satuan menit.
     * Atribut ini TIDAK ADA di SerialTV, hanya di Film.
     */
    @Column(nullable = false)
    private int durasiMenit;

    private String genre;

    private String sutradara;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    // Constructor kosong wajib ada untuk JPA
    public Film() {
        super();
    }

    public Film(
        String judul,
        String sinopsis,
        int tahunRilis,
        int durasiMenit,
        String genre,
        String sutradara
    ) {
        super(judul, sinopsis, tahunRilis);
        this.durasiMenit = durasiMenit;
        this.genre = genre;
        this.sutradara = sutradara;
    }

    // =========================================================
    // OVERRIDE METHOD ABSTRACT — Demonstrasi POLYMORPHISM
    // =========================================================

    /**
     * Implementasi tampilkanDetail() khusus untuk Film.
     * Menampilkan informasi durasi dalam format jam dan menit.
     *
     * Berbeda dengan SerialTV yang menampilkan jumlah musim & episode.
     * Inilah inti dari POLYMORPHISM: method yang sama, perilaku berbeda.
     *
     * @return String detail spesifik Film
     */
    @Override
    public String tampilkanDetail() {
        int jam = durasiMenit / 60;
        int menit = durasiMenit % 60;

        return String.format(
            "[FILM] %s (%d)\n" +
            "Genre    : %s\n" +
            "Sutradara: %s\n" +
            "Durasi   : %d jam %d menit\n" +
            "Rating   : %.2f / 5.00 (%d ulasan)",
            getJudul(),
            getTahunRilis(),
            genre,
            sutradara,
            jam,
            menit,
            hitungRatingRataRata(),
            getJumlahReviewer()
        );
    }

    // =========================================================
    // GETTER & SETTER (ENCAPSULATION)
    // =========================================================

    public int getDurasiMenit() {
        return durasiMenit;
    }

    public void setDurasiMenit(int durasiMenit) {
        if (durasiMenit <= 0) {
            throw new IllegalArgumentException(
                "Durasi film harus lebih dari 0 menit."
            );
        }
        this.durasiMenit = durasiMenit;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getSutradara() {
        return sutradara;
    }

    public void setSutradara(String sutradara) {
        this.sutradara = sutradara;
    }
}
