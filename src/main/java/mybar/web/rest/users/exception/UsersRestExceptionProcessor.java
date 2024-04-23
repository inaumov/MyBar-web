package mybar.web.rest.users.exception;

import lombok.extern.slf4j.Slf4j;
import mybar.app.bean.ErrorInfo;
import mybar.exception.users.EmailDuplicatedException;
import mybar.exception.users.UnknownUserException;
import mybar.exception.users.UserExistsException;
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
@RestControllerAdvice(basePackages = "mybar.web.rest.users")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UsersRestExceptionProcessor {

    private final MessageSource messageSource;

    @Autowired
    public UsersRestExceptionProcessor(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(EmailDuplicatedException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public ErrorInfo duplicatedEmail(HttpServletRequest req, EmailDuplicatedException ex) {
        log.warn("Duplicated email thrown:", ex);

        Locale locale = LocaleContextHolder.getLocale();
        String errorMessage = messageSource.getMessage("error.duplicated.email", null, locale);
        errorMessage = MessageFormat.format(errorMessage, ex.getEmail());
        String errorURL = req.getRequestURL().toString();

        return new ErrorInfo(errorURL, errorMessage);
    }

    @ExceptionHandler(UserExistsException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public ErrorInfo userExists(HttpServletRequest req, UserExistsException ex) {
        log.warn("User exists thrown:", ex);

        Locale locale = LocaleContextHolder.getLocale();
        String errorMessage = messageSource.getMessage("error.user.exists", null, locale);
        errorMessage = MessageFormat.format(errorMessage, ex.getUsername());
        String errorURL = req.getRequestURL().toString();

        return new ErrorInfo(errorURL, errorMessage);
    }

    @ExceptionHandler(UnknownUserException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ErrorInfo userUnknown(HttpServletRequest req, UnknownUserException ex) {
        log.warn("User unknown thrown:", ex);

        Locale locale = LocaleContextHolder.getLocale();
        String errorMessage = messageSource.getMessage("error.unknown.user", null, locale);
        errorMessage = MessageFormat.format(errorMessage, ex.getUsername());
        String errorURL = req.getRequestURL().toString();

        return new ErrorInfo(errorURL, errorMessage);
    }

    @ExceptionHandler(PasswordConfirmationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorInfo pwdConfirmation(HttpServletRequest req, PasswordConfirmationException ex) {
        log.warn("Password confirmation thrown:", ex);

        Locale locale = LocaleContextHolder.getLocale();
        String errorMessage = messageSource.getMessage("error.pwd.confirmation", null, locale);
        String errorURL = req.getRequestURL().toString();

        return new ErrorInfo(errorURL, errorMessage);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorInfo pwdCheck(HttpServletRequest req, InvalidPasswordException ex) {
        log.warn("Password check thrown:", ex);

        Locale locale = LocaleContextHolder.getLocale();
        String errorMessage = messageSource.getMessage("error.pwd.invalid", null, locale);
        String errorURL = req.getRequestURL().toString();

        return new ErrorInfo(errorURL, errorMessage);
    }

}