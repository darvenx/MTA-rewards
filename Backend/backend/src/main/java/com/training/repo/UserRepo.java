package com.training.repo;

import com.training.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User,Long> {
    Optional<User> findByUsernameAndPassword(String username, String password);

    Optional<User> findByUsernameOrEmailOrPhoneNumber
            (String username, String email,String phoneNumber);

    Optional<User> findByUsername(String username);
}
