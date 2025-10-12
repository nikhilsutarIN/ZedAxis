package com.ecom.repo;

import com.ecom.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    public Boolean existsByEmail(String email);

    public User findByEmail(String email);

    public List<User> findByRole(String role);

    public User findByResetToken(String token);

    public Page<User> findByRole(String role, Pageable pageable);

    public User findByActivateToken(String activateToken);


}
