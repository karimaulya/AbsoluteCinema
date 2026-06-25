package com.oop.absolutecinema.service;

import com.oop.absolutecinema.DTO.UserDTO;

public interface UserService {
  UserDTO.Response getProfile(Long id);
}
