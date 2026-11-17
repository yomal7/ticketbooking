package com.yomal7.ticketbooking.user;

import com.yomal7.ticketbooking.common.ConflictException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse create(CreateUserRequest request) {
        try {
            return UserResponse.from(userRepository.save(new User(request.email(), "placeholder-week5")));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Email already registered: " + request.email());
        }
    }
}