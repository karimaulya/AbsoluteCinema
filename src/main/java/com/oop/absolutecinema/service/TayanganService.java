package com.oop.absolutecinema.service;
import com.oop.absolutecinema.entity.Tayangan;
import java.util.List;

public interface TayanganService {
  Tayangan tambahTayangan(Tayangan tayanganBaru);
  List<Tayangan> lihatSemuaTayangan();
  Tayangan lihatTayanganBerdasarkanId(Long id);
  Tayangan editTayangan(Long id, Tayangan dataEdit);
  void hapusTayangan(Long id);
}
