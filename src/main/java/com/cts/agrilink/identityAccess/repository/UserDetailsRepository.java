package com.cts.agrilink.identityAccess.repository;

import com.cts.agrilink.identityAccess.model.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
 
public interface UserDetailsRepository extends JpaRepository<UserDetails, Integer> {
    Optional<UserDetails> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByRole_RoleId(Integer roleId);
}
