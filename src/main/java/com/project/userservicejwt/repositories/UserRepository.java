package com.project.userservicejwt.repositories;


import com.project.userservicejwt.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Override
    Optional<User> findById(Long aLong);

    Optional<User> findByName(String username);


    @Query("SELECT u FROM users u JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmail(String email);

    @Override
    User save(User user);

    @Query("SELECT u FROM users u WHERE u.deleted = false")
    List<User> findAllActive();

    List<User> findAll();


}
