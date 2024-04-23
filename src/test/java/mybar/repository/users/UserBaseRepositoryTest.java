package mybar.repository.users;

import mybar.repository.DbUnitBaseTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public abstract class UserBaseRepositoryTest extends DbUnitBaseTest {

    public static final String USERNAME_2 = "JohnDoe"; // to remove

    protected final int ROLES_CNT = 3;
    protected final int USERS_CNT = 5;
    protected final int USERS_HAS_ROLES_CNT = 7;

    @Autowired
    private ApplicationContext appContext;

    @Test
    @Order(Integer.MIN_VALUE)
    public void testPreconditions() {
        // do nothing, just check context loads
        Assertions.assertThat(appContext.getBean(RoleRepository.class)).isNotNull();
        Assertions.assertThat(appContext.getBean(UserRepository.class)).isNotNull();
    }

}