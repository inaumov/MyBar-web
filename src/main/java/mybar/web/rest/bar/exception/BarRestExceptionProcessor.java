package mybar.web.rest.bar.exception;

import lombok.extern.slf4j.Slf4j;
import mybar.app.bean.ErrorInfo;
import mybar.exception.BottleNotFoundException;
import mybar.exception.CocktailNotFoundException;
import mybar.exception.UniqueCocktailNameException;
import mybar.exception.UnknownBeverageException;
import mybar.exception.UnknownIngredientsException;
import mybar.exception.UnknownMenuException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.Locale;

@Slf4j
@RestControllerAdvice(basePackages = "mybar.web.rest.bar")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BarRestExceptionProcessor {

    private final MessageSource messageSource;

    @Autowired
    public BarRestExceptionProcessor(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(BottleNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ErrorInfo bottleNotFound(HttpServletRequest req, BottleNotFoundException ex) {
        log.warn("Bottle not found thrown:", ex);

        Locale locale = LocaleContextHolder.getLocale();
        String errorMessage = messageSource.getMessage("error.no.bottle.id", null, locale);
        errorMessage += " ";
        errorMessage += ex.getBottleId();
        String errorURL = req.getRequestURL().toString();

        return new ErrorInfo(errorURL, errorMessage);
    }

    @ExceptionHandler(CocktailNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ErrorInfo cocktailNotFound(HttpServletRequest req, CocktailNotFoundException ex) {
        log.warn("Cocktail not found thrown:", ex);

        Locale locale = LocaleContextHolder.getLocale();
        String errorMessage = messageSource.getMessage("error.no.cocktail.id", null, locale);
        errorMessage += " ";
        errorMessage += ex.getCocktailId();
        String errorURL = req.getRequestURL().toString();

        return new ErrorInfo(errorURL, errorMessage);
    }

    @ExceptionHandler(UniqueCocktailNameException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorInfo cocktailUniqueName(HttpServletRequest req, UniqueCocktailNameException ex) {
        log.warn("Unique cocktail name thrown:", ex);

        Locale locale = LocaleContextHolder.getLocale();
        String errorMessage = messageSource.getMessage("error.unique.cocktail.name", null, locale);
        errorMessage = MessageFormat.format(errorMessage, ex.getName());
        String errorURL = req.getRequestURL().toString();

        return new ErrorInfo(errorURL, errorMessage);
    }

    @ExceptionHandler(UnknownMenuException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorInfo unknownMenuName(HttpServletRequest req, UnknownMenuException ex) {
        log.warn("Unknown menu name thrown:", ex);

        Locale locale = LocaleContextHolder.getLocale();
        String errorMessage = messageSource.getMessage("error.unknown.menu.name", null, locale);
        errorMessage = MessageFormat.format(errorMessage, ex.getName());
        String errorURL = req.getRequestURL().toString();

        return new ErrorInfo(errorURL, errorMessage);
    }

    @ExceptionHandler(UnknownBeverageException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorInfo unknownBeverage(HttpServletRequest req, UnknownBeverageException ex) {
        log.warn("Unknown beverage thrown:", ex);

        Locale locale = LocaleContextHolder.getLocale();
        String errorMessage = messageSource.getMessage("error.beverage.unknown", null, locale);
        errorMessage = MessageFormat.format(errorMessage, ex.getId());
        String errorURL = req.getRequestURL().toString();

        return new ErrorInfo(errorURL, errorMessage);
    }

    @ExceptionHandler(UnknownIngredientsException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorInfo unknownIngredients(HttpServletRequest req, UnknownIngredientsException ex) {
        log.warn("Unknown ingredient thrown:", ex);

        Locale locale = LocaleContextHolder.getLocale();
        String errorMessage = messageSource.getMessage("error.ingredients.unknown", null, locale);
        errorMessage = MessageFormat.format(errorMessage, ex.getIds());
        String errorURL = req.getRequestURL().toString();

        return new ErrorInfo(errorURL, errorMessage);
    }

}