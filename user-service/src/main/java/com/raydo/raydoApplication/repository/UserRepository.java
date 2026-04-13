package com.raydo.raydoApplication.repository;

import com.raydo.raydoApplication.entity.Role;
import com.raydo.raydoApplication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);


}
