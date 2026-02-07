package com.training.repo;

import com.training.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface AccountRepo extends JpaRepository<Account, Long> {
    List<Account> findAllByUser_UserId(Long userId);
}
