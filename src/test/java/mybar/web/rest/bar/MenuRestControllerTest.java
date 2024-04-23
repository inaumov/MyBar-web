package mybar.web.rest.bar;

import mybar.dto.bar.MenuDto;
import mybar.service.bar.CocktailsService;
import mybar.web.rest.ARestControllerTest;
import mybar.web.rest.bar.exception.BarRestExceptionProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MenuController.class)
@ContextConfiguration(
        classes = {
                MenuController.class,
                BarRestExceptionProcessor.class
        }
)
public class MenuRestControllerTest extends ARestControllerTest {
    public static final String basePath = "/v1/menu";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CocktailsService cocktailsServiceMock;

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() throws Exception {
        reset(cocktailsServiceMock);
    }

    @Test
    public void listAllMenuItems_noAuthRequired_And_Should_ReturnAllMenuEntries() throws Exception {

        final MenuDto first = new MenuDto();
        first.setId(1);
        first.setName("shot");

        final MenuDto second = new MenuDto();
        second.setId(2);
        second.setName("long");

        when(cocktailsServiceMock.getAllMenuItems()).thenReturn(List.of(first, second));

        mockMvc.perform(get(basePath))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$[0].name", is("shot")))
                .andExpect(jsonPath("$[0].translation", equalTo("Test Shot")))
                .andExpect(jsonPath("$[1].name", is("long")))
                .andExpect(jsonPath("$[1].translation", equalTo("Test Long")));

        verify(cocktailsServiceMock, times(1)).getAllMenuItems();
        verifyNoMoreInteractions(cocktailsServiceMock);
    }

}