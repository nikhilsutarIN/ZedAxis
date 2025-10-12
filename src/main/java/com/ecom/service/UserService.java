package com.ecom.service;

import com.ecom.model.User;
import com.ecom.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User saveUser(User user) {
        Boolean userExists = userRepository.existsByEmail(user.getEmail());
        if (userExists) {
            return null;
        }

        user.setRole("ROLE_USER");
        user.setIsEnabled(false);

        String activateToken = UUID.randomUUID().toString();
        user.setActivateToken(activateToken);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User getUserByActivateToken(String token) {
        return userRepository.findByActivateToken(token);
    }

    public void updateUserActivateToken(String email, String token) {
        User user = userRepository.findByEmail(email);
        user.setActivateToken(token);
        userRepository.save(user);
    }

    public void activateUser(User user) {
        user.setIsEnabled(true);
        user.setActivateToken(null);
        userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    public Boolean updateAccountStatus(Integer id, Boolean status) {
        User existingUser = userRepository.findById(id).orElse(null);

        if (existingUser != null) {
            existingUser.setIsEnabled(status);
            userRepository.save(existingUser);
            return true;
        }

        return false;
    }

    public void updateUserResetToken(String email, String token) {
        User user = userRepository.findByEmail(email);
        user.setResetToken(token);
        userRepository.save(user);
    }

    public User getUserByResetToken(String token) {
        return userRepository.findByResetToken(token);
    }

    public void updateUserPassword(User existingUser, String password) {

        String newPassword = passwordEncoder.encode(password);

        existingUser.setPassword(newPassword);
        existingUser.setResetToken(null);

        userRepository.save(existingUser);
    }

    public User updateUserDetails(User user) {

        User existingUser = userRepository.findByEmail(user.getEmail());

        existingUser.setName(user.getName());
        existingUser.setMobile(user.getMobile());
        existingUser.setAddress(user.getAddress());
        existingUser.setEmail(user.getEmail());
        existingUser.setCity(user.getCity());
        existingUser.setState(user.getState());
        existingUser.setPincode(user.getPincode());

        User savedUser = userRepository.save(existingUser);

        return savedUser;
    }

    // Get Users By Role Pagination
    public Page<User> getUsersByRolePagination(String role, Integer pageNumber, Integer pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<User> page = userRepository.findByRole(role, pageable);

        return page;
    }

    // Get Users By Role Pagination
    public Page<User> getUsersByRolesPagination(String role, Integer pageNumber, Integer pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<User> page = userRepository.findByRole(role, pageable);

        return page;
    }

    // Save Admin
    public User saveAdmin(User user) {

        Boolean userExists = userRepository.existsByEmail(user.getEmail());
        if (userExists) {
            return null;
        }

        user.setRole("ROLE_ADMIN");
        user.setIsEnabled(true);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

}
