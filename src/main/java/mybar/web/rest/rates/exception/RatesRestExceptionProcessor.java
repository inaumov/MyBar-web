package mybar.web.rest.rates.exception;

import lombok.extern.slf4j.Slf4j;
import mybar.exception.CocktailNotFoundException;
import mybar.app.bean.ErrorInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.Locale;

@Slf4j
@RestControllerAdvice(basePackages = "mybar.web.rest.rates")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RatesRestExceptionProcessor {

    private final MessageSource messageSource;

    @Autowired
    public RatesRestExceptionProcessor(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(CocktailNotFoundException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public ErrorInfo cocktailNotFound(HttpServletRequest req, CocktailNotFoundException ex) {
        log.warn("Cocktail not found thrown:", ex);

        Locale locale = LocaleContextHolder.getLocale();
        String errorMessage = messageSource.getMessage("error.could.not.rate.unknown.cocktail", null, locale);
        errorMessage = MessageFormat.format(errorMessage, ex.getCocktailId());
        String errorURL = req.getRequestURL().toString();

        return new ErrorInfo(errorURL, errorMessage);
    }

}