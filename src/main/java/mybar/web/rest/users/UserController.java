package mybar.web.rest.users;

import com.fasterxml.jackson.annotation.JsonView;
import mybar.api.users.IUser;
import mybar.api.users.IUserDetails;
import mybar.app.bean.users.*;
import mybar.service.users.UserService;
import mybar.web.rest.users.exception.PasswordConfirmationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/users")
public class UserController {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public UserController(PasswordEncoder passwordEncoder, UserService userService) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @JsonView(View.UserView.class)
    @GetMapping("/{username}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_USER')")
    public UserDetailsBean getUser(@PathVariable("username") String username) {
        IUserDetails user = userService.findByUsername(username);
        return BeanFactory.fromDetails(user);
    }

    @JsonView(View.UserView.class)
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_USER')")
    public UserDetailsBean updateUser(@RequestBody UserBean userBean) {
        IUserDetails user = userService.editUserInfo(userBean);
        return BeanFactory.fromDetails(user);
    }

    @PutMapping("/changePassword")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public void changePassword(Principal principal,
                               @RequestBody @Validated ChangePasswordBean passwordBean) {

        IUser user = userService.findByUsername(principal.getName());
        final String encodedPassword = passwordEncoder.encode(passwordBean.getNewPassword());
        userService.changePassword(user, encodedPassword);
    }

    @JsonView(View.UserView.class)
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDetailsBean register(@RequestHeader HttpHeaders headers,
                                    @RequestBody RegisterUserBean registerUserBean) {
        validatePasswordConfirmation(registerUserBean);

        registerUserBean.setPassword(passwordEncoder.encode(registerUserBean.getPassword()));

        IUserDetails user = userService.createUser(registerUserBean);
        return BeanFactory.fromDetails(user);
    }

    private void validatePasswordConfirmation(RegisterUserBean user) {
        if (user.getPasswordConfirm() == null || !Objects.equals(user.getPassword(), user.getPasswordConfirm())) {
            throw new PasswordConfirmationException();
        }
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_USER')")
    public void deactivate(Principal principal) {
        userService.deactivateUser(principal.getName());
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<MappingJacksonValue> getUsers() {
        List<IUser> users = userService.getAllRegisteredUsers();

        UserList userList = new UserList(BeanFactory.toFullUserList(users));

        MappingJacksonValue wrapper = new MappingJacksonValue(userList);
        wrapper.setSerializationView(View.AdminView.class);

        return new ResponseEntity<>(wrapper, HttpStatus.OK);
    }

}