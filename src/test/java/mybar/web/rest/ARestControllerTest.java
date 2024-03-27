package mybar.web.rest;

import mybar.web.config.MyBarConfiguration;
import mybar.web.config.auth.ResourceServerConfiguration;
import mybar.web.rest.bar.exception.BarRestExceptionProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(BarRestExceptionProcessor.class)
@ContextConfiguration(
        classes = {
                UsersServiceTestConfig.class,
                ResourceServerConfiguration.class,
                MyBarConfiguration.class,
                GlobalRestExceptionProcessor.class
        }
)
public abstract class ARestControllerTest {

    public static final String USER = "user";
    public static final String ADMIN = "admin";

    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    @Autowired
    protected MockMvc mockMvc;

    protected static MockHttpServletRequestBuilder asUser(MockHttpServletRequestBuilder mockHttpServletRequestBuilder) {
        return mockHttpServletRequestBuilder
                .with(user(USER).password(USER).roles(ROLE_USER))
                .contentType(MediaType.APPLICATION_JSON);
    }

    protected static MockHttpServletRequestBuilder asAdmin(MockHttpServletRequestBuilder mockHttpServletRequestBuilder) {
        return mockHttpServletRequestBuilder
                .with(user(ADMIN).password(ADMIN).roles(ROLE_ADMIN))
                .contentType(MediaType.APPLICATION_JSON);
    }

}
