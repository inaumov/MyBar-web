package mybar.rest.actuator;

import mybar.rest.ApiTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static mybar.rest.Constants.TEST_USERNAME;
import static mybar.rest.Constants.USER_PASS;


class ActuatorApiTest extends ApiTest {

    @Test
    void testActuatorUpAndRunning() {
        givenAuthenticated(TEST_USERNAME, USER_PASS)
                .get("http://localhost:8080/api/bar/actuator")
                .then()
                .assertThat()
                .statusCode(200)
                .and()
                .body("_links", Matchers.hasKey("health"))
                .body("_links", Matchers.hasKey("info"))
                .body("_links", Matchers.hasKey("metrics"));

    }

}
