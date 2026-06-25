package com.oop.absolutecinema.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.oop.absolutecinema.DTO.UserDTO;
import com.oop.absolutecinema.entity.User;
import com.oop.absolutecinema.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void testGetProfile_Sukses() {
        User userTiruan = mock(User.class);
        when(userTiruan.getId()).thenReturn(1L);
        when(userTiruan.getUsername()).thenReturn("RaditAssegaf");
        when(userRepository.findById(1L)).thenReturn(Optional.of(userTiruan));

        UserDTO.Response hasil = userService.getProfile(1L);

        assertNotNull(hasil);
        assertEquals(1L, hasil.getId());
        assertEquals("RaditAssegaf", hasil.getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetProfile_TidakDitemukan() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.getProfile(99L);
        });

        assertEquals("User dengan ID 99 tidak ditemukan.", exception.getMessage());
    }
}