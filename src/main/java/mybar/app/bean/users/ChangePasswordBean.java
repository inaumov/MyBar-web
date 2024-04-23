package mybar.app.bean.users;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Size;

@Getter
@Setter
public class ChangePasswordBean {

    @Size(min = 4)
    private String newPassword;
}
