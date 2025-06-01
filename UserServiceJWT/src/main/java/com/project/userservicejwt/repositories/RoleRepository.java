

package com.project.userservicejwt.repositories;

import com.project.userservicejwt.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByValue(String value);

    @Override
    Role save(Role role);
}
