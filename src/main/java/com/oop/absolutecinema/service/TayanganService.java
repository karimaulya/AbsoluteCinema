package com.oop.absolutecinema.service;
import com.oop.absolutecinema.entity.Tayangan;
import java.util.List;

public interface TayanganService {
  Tayangan tambahTayangan(Tayangan tayanganBaru);
  List<Tayangan> getSemuaTayangan();
  Tayangan getTayanganById(String id);
}
