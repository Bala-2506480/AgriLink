package com.cts.agrilink.identityAccess.repository;
import com.cts.agrilink.identityAccess.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
 
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
    Optional<UserRole> findByRoleName(String roleName);
}
