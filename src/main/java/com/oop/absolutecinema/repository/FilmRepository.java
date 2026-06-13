package com.oop.absolutecinema.repository;

import com.oop.absolutecinema.entity.Film;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
  public interface FilmRepository extends JpaRepository<Film, Long> {
    
    // Mencari film berdasarkan genre
    // Field 'genre' ada di Film.java
    List<Film> findByGenre(String genre);
    
    // Mencari film berdasarkan nama sutradara
    // Field 'sutradara' ada di Film.java
    List<Film> findBySutradara(String sutradara);

    // Mencari film berdasarkan tahun rilis
    // Field 'tahunRilis' diwarisi dari Tayangan.java
    List<Film> findByTahunRilis(int tahunRilis);

    // Mencari film berdasarkan judul, tidak case-sensitive
    // Field 'judul' diwarisi dari Tayangan.java
    List<Film> findByJudulContainingIgnoreCase(String judul);

    // Mencari film berdasarkan kombinasi genre DAN tahun rilis
    List<Film> findByGenreAndTahunRilis(String genre, int tahunRilis);
  }
