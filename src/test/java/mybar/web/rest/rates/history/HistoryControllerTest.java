package mybar.web.rest.rates.history;

import mybar.domain.History;
import mybar.service.rates.history.HistoryService;
import mybar.web.rest.ARestControllerTest;
import mybar.web.rest.rates.exception.RatesRestExceptionProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HistoryController.class)
@ContextConfiguration(
        classes = {
                HistoryController.class,
                RatesRestExceptionProcessor.class
        }
)
public class HistoryControllerTest extends ARestControllerTest {

    public static final String USERNAME = "bill";
    public static final String COCKTAIL_NAME = "B52";
    public static final int STARS = 7;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HistoryService historyServiceMock;

    @BeforeEach
    public void setup() {
    }

    @AfterEach
    public void tearDown() {
        reset(historyServiceMock);
    }

    @Test
    void test_getRatedCocktails() throws Exception {
        History history = new History();
        history.setName(COCKTAIL_NAME);
        history.setStars(STARS);
        history.setUsername(USERNAME);

        when(historyServiceMock.getHistoryForPeriod(any(LocalDate.class), any(LocalDate.class))).thenReturn(Collections.singletonList(history));

        this.mockMvc.perform(asAdmin(get("/v1/rates/history"))
                        .param("startDate", "2020-12-31")
                        .param("endDate", "2021-02-13"))

                .andDo(print())
                .andExpect(status()
                        .isOk())

                .andExpect(jsonPath("$[0].stars", is(STARS)))
                .andExpect(jsonPath("$[0].name", is(COCKTAIL_NAME)))
                .andExpect(jsonPath("$[0].username", is(USERNAME)));
    }

}