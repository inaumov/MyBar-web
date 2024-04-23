package mybar.repository.users;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import mybar.api.users.RoleName;
import mybar.domain.users.Role;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DatabaseSetup("classpath:datasets/usersDataSet.xml")
@ContextConfiguration(classes = RoleRepository.class)
public class RoleRepositoryTest extends UserBaseRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    public void testSelectAllRoles() {
        int size = roleRepository.findAll().size();
        assertEquals(ROLES_CNT, size);
    }

    @Test
    public void testGetRole() {
        RoleName[] roleNames = RoleName.values();
        for (RoleName roleName : roleNames) {
            Role role = roleRepository.getReferenceById(roleName.name());
            assertNotNull(role);
            assertEquals(roleName.name(), role.getRoleName());
        }
    }

    @Test
    public void findByRoleNameIn() {
        List<Role> roles = roleRepository.findByRoleNameIn(Collections.singletonList(RoleName.ROLE_ADMIN.name()));
        assertEquals(1, roles.size());
    }
}