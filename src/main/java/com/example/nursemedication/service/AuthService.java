package com.example.nursemedication.service;

import com.example.nursemedication.model.User;
import com.example.nursemedication.model.Nurse;
import com.example.nursemedication.repository.UserRepository;
import com.example.nursemedication.repository.NurseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final NurseRepository nurseRepository;

    public AuthService(UserRepository userRepository, NurseRepository nurseRepository) {
        this.userRepository = userRepository;
        this.nurseRepository = nurseRepository;
    }

    public Object login(String username, String password, String role) {

        if (role.equalsIgnoreCase("Admin")) {
            // Admin login using username
            User admin = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Admin not found"));
            if (!admin.getPassword().equals(password)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
            }
            return admin;

        } else if (role.equalsIgnoreCase("Nurse")) {
            // Nurse login using employeeId
            Nurse nurse = nurseRepository.findByEmployeeId(username) // here username = employeeId
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Nurse not found"));
            if (!nurse.getPassword().equals(password)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
            }
            return nurse;

        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role");
        }
    }
}
