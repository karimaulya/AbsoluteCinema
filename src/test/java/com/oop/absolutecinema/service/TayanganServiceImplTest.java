package com.oop.absolutecinema.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.oop.absolutecinema.entity.Tayangan;
import com.oop.absolutecinema.entity.Film;
import com.oop.absolutecinema.entity.SerialTV;
import com.oop.absolutecinema.exception.DataTidakDitemukanException;
import com.oop.absolutecinema.exception.JudulDuplikatException;
import com.oop.absolutecinema.repository.FilmRepository;
import com.oop.absolutecinema.repository.SerialTVRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

@ExtendWith(MockitoExtension.class)
public class TayanganServiceImplTest {

    @Mock
    private FilmRepository filmRepo;

    @Mock
    private SerialTVRepository serialTvRepo;

    @InjectMocks
    private TayanganServiceImpl tayanganService;

    @Test
    public void testLihatSemuaTayangan_Sukses() {
        // GIVEN: Membuat data tiruan untuk Film dan SerialTV
        List<Film> listFilm = new ArrayList<>();
        Film filmTiruan = mock(Film.class);
        listFilm.add(filmTiruan);

        List<SerialTV> listSerial = new ArrayList<>();
        SerialTV serialTiruan = mock(SerialTV.class);
        listSerial.add(serialTiruan);

        // Atur agar repo mengembalikan data tiruan
        when(filmRepo.findAll()).thenReturn(listFilm);
        when(serialTvRepo.findAll()).thenReturn(listSerial);

        // WHEN: Jalankan fungsi gabungan
        List<Tayangan> hasil = tayanganService.lihatSemuaTayangan();

        // THEN: Total harus ada 2 tayangan terintegrasi
        assertNotNull(hasil);
        assertEquals(2, hasil.size());
        verify(filmRepo, times(1)).findAll();
        verify(serialTvRepo, times(1)).findAll();
    }

    @Test
    public void testTambahTayangan_ValidasiJudulKosong_HarusError() {
        // GIVEN: Membuat objek tayangan tanpa judul
        Tayangan tayanganTanpaJudul = mock(Tayangan.class);
        when(tayanganTanpaJudul.getJudul()).thenReturn("");

        // WHEN & THEN: Harus melempar IllegalArgumentException
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tayanganService.tambahTayangan(tayanganTanpaJudul);
        });

        assertEquals("Judul tayangan wajib diisi!", exception.getMessage());
    }

    @Test
    public void testTambahTayangan_FilmJudulDuplikat_HarusError() {
        // GIVEN: Siapkan Film tiruan dengan judul yang sudah ada
        Film filmBaru = mock(Film.class);
        when(filmBaru.getJudul()).thenReturn("Inception");

        List<Film> filmDitemukan = new ArrayList<>();
        filmDitemukan.add(filmBaru);

        // Atur agar ketika sistem mengecek judul ke database, datanya ternyata ketemu (duplikat)
        when(filmRepo.findByJudulContainingIgnoreCase("Inception")).thenReturn(filmDitemukan);

        // WHEN & THEN: Harus melempar JudulDuplikatException
        assertThrows(JudulDuplikatException.class, () -> {
            tayanganService.tambahTayangan(filmBaru);
        });
    }

    @Test
    public void testLihatTayanganBerdasarkanId_TidakDitemukan() {
        // GIVEN: ID 99L tidak ada di database film maupun serial tv
        when(filmRepo.findById(99L)).thenReturn(Optional.empty());
        when(serialTvRepo.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN: Harus melempar DataTidakDitemukanException
        DataTidakDitemukanException exception = assertThrows(DataTidakDitemukanException.class, () -> {
            tayanganService.lihatTayanganBerdasarkanId(99L);
        });

        assertEquals("Tayangan dengan ID 99 tidak ditemukan!", exception.getMessage());
    }
}