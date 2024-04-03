package mybar.repository.users;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import mybar.api.users.RoleName;
import mybar.domain.users.Role;
import mybar.domain.users.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DatabaseSetup("classpath:datasets/usersDataSet.xml")
@ContextConfiguration(classes = UserRepository.class)
public class UserRepositoryTest extends UserBaseRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindUserById() {
        User user = userRepository.getReferenceById(USERNAME_2);
        assertNotNull(user);
        assertTrue(user.isActive());
        assertEquals("JohnDoe", user.getUsername());
        assertEquals("John", user.getName());
        assertEquals("Doe", user.getSurname());
        assertEquals("john.doe@mybar.com", user.getEmail());

        assertEquals("JhnD", user.getPassword());
    }

    @Test
    public void testSelectAllUsers() {
        List<User> all = userRepository.findAll();
        assertEquals(USERS_CNT, all.size());
    }

    @ExpectedDatabase(
            assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED,
            value = "classpath:datasets/expected/users-create.xml", table = "USERS")
    @Test
    public void testSaveUser() {
        User user = new User();
        user.setUsername("phlp111");
        user.setPassword("Passport");
        user.setName("Philippe");
        user.setSurname("Prescott");
        user.setEmail("mail@prescott.com");
        user.setActive(true);
        Role roleRefAnalyst = testEntityManager.find(Role.class, RoleName.ROLE_ADMIN.name());
        user.setRoles(Collections.singletonList(roleRefAnalyst));

        User saved = userRepository.save(user);
        commit();

        assertNotNull(saved);
    }

    @ExpectedDatabase(
            assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED,
            value = "classpath:datasets/expected/users-update.xml", table = "USERS")
    @Test
    public void testUpdateUser() {
        User user = userRepository.getReferenceById("analyst");
        assertNotNull(user);
        user.setName("Johny");
        user.setSurname("Walker");
        user.setEmail("mail@johnyw.com");
        Role roleRefUser = testEntityManager.find(Role.class, RoleName.ROLE_USER.name());
        user.addRole(roleRefUser);
        User updated = userRepository.save(user);
        commit();
        assertNotNull(updated);
    }

    @Test
    public void testFindByEmail() {
        User user = userRepository.findByEmail("super@mybar.com");
        assertNotNull(user);
        assertEquals("super", user.getUsername());
        assertEquals("super@mybar.com", user.getEmail());
    }

}