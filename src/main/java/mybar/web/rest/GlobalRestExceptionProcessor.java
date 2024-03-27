package mybar.web.rest;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import mybar.app.bean.ErrorInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.Locale;

@Slf4j
@RestControllerAdvice(basePackages = "mybar.web.rest")
@Order(value = 0)
public class GlobalRestExceptionProcessor {

    private final MessageSource messageSource;

    @Autowired
    public GlobalRestExceptionProcessor(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Throwable.class)
    public ErrorInfo handleThrowable(HttpServletRequest req, final Throwable ex) {
        log.error("Unexpected error", ex);

        Locale locale = LocaleContextHolder.getLocale();
        String errorMessage = messageSource.getMessage("internal.server.error", null, locale);

        String errorURL = req.getRequestURL().toString();
        return new ErrorInfo(errorURL, errorMessage);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorInfo requiredFields(HttpServletRequest req, IllegalArgumentException ex) {
        log.warn("Invalid property value:", ex);

        String errorURL = req.getRequestURL().toString();
        return new ErrorInfo(errorURL, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorInfo handleMethodArgumentValidationExceptions(HttpServletRequest req, MethodArgumentNotValidException exception, WebRequest webRequest) {
        List<ErrorInfo.ErrorField> errors = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ErrorInfo.ErrorField(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();

        Locale locale = LocaleContextHolder.getLocale();
        String errorMessage = messageSource.getMessage("invalid.parameter", null, locale);

        String errorURL = req.getRequestURL().toString();
        ErrorInfo errorInfo = new ErrorInfo(errorURL, errorMessage);
        errorInfo.setErrors(errors);
        return errorInfo;
    }

}
