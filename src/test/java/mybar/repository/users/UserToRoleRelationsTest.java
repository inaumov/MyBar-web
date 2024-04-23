package mybar.repository.users;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import mybar.api.users.RoleName;
import mybar.domain.users.Role;
import mybar.domain.users.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DatabaseSetup("classpath:datasets/usersDataSet.xml")
@ContextConfiguration(classes = {RoleRepository.class, UserRepository.class})
public class UserToRoleRelationsTest extends UserBaseRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    public void testUserHasMoreThenOneRole() {
        User user = userRepository.getReferenceById(USERNAME_2);
        assertNotNull(user);
        Collection<Role> roles = user.getRoles();
        assertEquals(ROLES_CNT, roles.size());
        for (Role role : roles) {
            assertEquals(role, roleRepository.getReferenceById(role.getRoleName()));
        }
    }

    @Test
    public void testAddRole() {
        User user = userRepository.getReferenceById(USERNAME_2);
        assertNotNull(user);

        Role role = roleRepository.getReferenceById(RoleName.ROLE_ADMIN.name());
        assertNotNull(role);
        user.addRole(role);
        userRepository.save(user);
        commit();

        assertEquals(USERS_CNT, countRowsInTable("USERS"));
        assertEquals(ROLES_CNT, countRowsInTable("ROLES"));
        assertEquals(USERS_HAS_ROLES_CNT + 1, countRowsInTable("USERS_HAS_ROLES"));
    }

    @Test
    public void testRemoveRole() {
        User user = userRepository.getReferenceById(USERNAME_2);
        assertNotNull(user);

        int nmbOfRoles = user.getRoles().size();
        assertEquals(3, nmbOfRoles);
        user.getRoles().clear();
        userRepository.save(user);
        commit();

        assertEquals(USERS_CNT, countRowsInTable("USERS"));
        assertEquals(ROLES_CNT, countRowsInTable("ROLES"));
        assertEquals(USERS_HAS_ROLES_CNT - nmbOfRoles, countRowsInTable("USERS_HAS_ROLES"));
    }

    @Test
    public void testAddNullRole() {
        User user = userRepository.getReferenceById(USERNAME_2);
        assertNotNull(user);
        user.addRole(null);
        userRepository.save(user);
        commit();

        assertEquals(USERS_CNT, countRowsInTable("USERS"));
        assertEquals(ROLES_CNT, countRowsInTable("ROLES"));
        assertEquals(USERS_HAS_ROLES_CNT, countRowsInTable("USERS_HAS_ROLES"));
    }

}