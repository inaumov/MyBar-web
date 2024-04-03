package mybar.repository.users;

import mybar.domain.users.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

    List<Role> findByRoleNameIn(List<String> roleNames);
}