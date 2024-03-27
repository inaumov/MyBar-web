package mybar.web.rest.bar;

import lombok.extern.slf4j.Slf4j;
import mybar.api.bar.IMenu;
import mybar.app.RestBeanConverter;
import mybar.app.bean.bar.MenuBean;
import mybar.service.bar.CocktailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/menu")
@Slf4j
public class MenuController {

    private final CocktailsService cocktailsService;
    private final MessageSource messageSource;

    @Autowired
    public MenuController(CocktailsService cocktailsService, MessageSource messageSource) {
        this.cocktailsService = cocktailsService;
        this.messageSource = messageSource;
    }

    //-------------------Retrieve Menu List--------------------------------------------------------

    @GetMapping
    public ResponseEntity<List<MenuBean>> listAllMenuItems() {
        log.info("Fetching menu items list...");

        Collection<IMenu> allMenuItems = cocktailsService.getAllMenuItems();
        if (allMenuItems.isEmpty()) {
            log.info("Menu list is empty.");
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        }
        // converted into response beans
        List<MenuBean> menuBeans = toResponseWithTranslations(allMenuItems);
        for (MenuBean menuBean : menuBeans) {
            Link link = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CocktailsController.class)
            .allCocktails(menuBean.getName())).withRel("allCocktails");
            menuBean.add(link);
        }
        return new ResponseEntity<>(menuBeans, HttpStatus.OK);
    }

    private List<MenuBean> toResponseWithTranslations(Collection<IMenu> menuItems) {
        List<MenuBean> convertedList = new ArrayList<>();
        Locale locale = LocaleContextHolder.getLocale();
        for (IMenu menu : menuItems) {
            MenuBean from = RestBeanConverter.toMenuBean(menu);
            from.setTranslation(messageSource.getMessage(menu.getName(), null, locale));
            convertedList.add(from);
        }
        log.info("Found {} items in menu list.", menuItems.size());
        return convertedList;
    }

}