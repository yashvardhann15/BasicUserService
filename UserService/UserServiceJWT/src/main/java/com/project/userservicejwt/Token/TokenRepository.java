package com.project.userservicejwt.Token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

//    @Query("select t from Token t where t.user.email = :username and (t.expired = false or t.revoked = false) order by t.createdAt")
//    List<Token> findAllValidTokensByUser(String username);

    Optional<Token> findByToken(String token);
}
