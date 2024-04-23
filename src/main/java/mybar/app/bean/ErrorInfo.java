package mybar.app.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ErrorInfo {

    private String url;
    private String errorMessage;
    private String code;
    private List<ErrorField> errors;

    public ErrorInfo(String url, String errorMessage) {
        this.url = url;
        this.errorMessage = errorMessage;
    }

    public ErrorInfo(String url, String errorMessage, String code) {
        this(url, errorMessage);
        this.code = code;
    }

    @Getter
    @AllArgsConstructor
    public static class ErrorField {
        private String field;
        private String errorMessage;
    }

}