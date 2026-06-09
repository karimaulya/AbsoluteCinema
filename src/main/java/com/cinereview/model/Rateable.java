package com.cinereview.model;

/**
 * Interface Rateable mendefinisikan kontrak untuk entitas
 * yang dapat diberi rating oleh pengguna.
 *
 * Demonstrasi Pilar OOP: ABSTRACTION
 * Interface ini menyembunyikan detail implementasi perhitungan
 * dan hanya mengekspos "apa yang bisa dilakukan" (bukan "bagaimana caranya").
 *
 * Di masa depan, interface ini bisa diimplementasikan juga oleh
 * entitas lain seperti Aktor atau Sutradara.
 */
public interface Rateable {

    /**
     * Menghitung dan mengembalikan nilai rating rata-rata.
     * Setiap kelas yang mengimplementasikan interface ini
     * wajib mendefinisikan sendiri cara perhitungannya.
     *
     * @return nilai rata-rata rating dalam bentuk double (misalnya: 4.5)
     */
    double hitungRatingRataRata();
}
