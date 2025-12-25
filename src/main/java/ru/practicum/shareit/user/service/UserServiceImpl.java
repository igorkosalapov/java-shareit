package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {

        checkEmailUnique(userDto.getEmail(), null);

        User user = UserMapper.toModel(userDto);
        return UserMapper.toDto(userRepository.create(user));
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (userDto.getEmail() != null) {
            checkEmailUnique(userDto.getEmail(), userId);
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }

        return UserMapper.toDto(userRepository.update(user));
    }

    @Override
    public UserDto findById(Long userId) {
        return UserMapper.toDto(
                userRepository.findById(userId)
                        .orElseThrow(() -> new NotFoundException("Пользователь не найден"))
        );
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    public void delete(Long userId) {
        userRepository.delete(userId);
    }

    @Override
    public User findByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    private void checkEmailUnique(String email, Long excludeUserId) {
        for (User existing : userRepository.findAll()) {
            if (existing.getEmail() == null) continue;

            if (existing.getEmail().equals(email)
                    && (excludeUserId == null || !existing.getId().equals(excludeUserId))) {
                throw new ConflictException("Email already exists");
            }
        }
    }

}
