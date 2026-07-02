package com.oop.absolutecinema.service;

import com.oop.absolutecinema.DTO.UserDTO;
import com.oop.absolutecinema.entity.User;
import com.oop.absolutecinema.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
  
  @Autowired
  private UserRepository userRepository;
  
  @Override
  @Transactional(readOnly = true)
  public UserDTO.Response getProfile(Long id) {
    // Validasi id null untuk keamanan tipe non-null
    if (id == null) {
      throw new RuntimeException("ID tidak boleh null.");
    }

    // Cari user berdasarkan Long id
    // Jika ID tidak ditemukan, sistem otomatis menghentikan proses dan melempar RuntimeException
    User user = userRepository.findById(id)
      .orElseThrow(() -> new RuntimeException(
        "User dengan ID " + id + " tidak ditemukan."
      ));

    // Kembalikan data yang berhasil diambil dalam bentuk UserDTO.Response
    return new UserDTO.Response(
      user.getId(),
      user.getUsername(),
      user.getRole(),
      "Profil berhasil dimuat."
    );
  }
}
