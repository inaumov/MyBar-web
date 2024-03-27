package mybar.app.impl;

import mybar.api.users.IUser;
import mybar.service.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/*
 * Spring-security requires an implementation of UserDetailService.
 */
@Service
@Transactional(readOnly = true)
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Autowired
    public UserDetailsServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        IUser myBarUser = userService.findByUsername(username);

        if (myBarUser == null) {
            throw new UsernameNotFoundException("My Bar user=[" + username + "] was not found.");
        }

        final boolean enabled = myBarUser.isActive();
        final boolean accountNonExpired = true;
        final boolean credentialsNonExpired = true;
        final boolean accountNonLocked = true;

        return new User(
                myBarUser.getUsername(),
                myBarUser.getPassword(),
                enabled,
                accountNonExpired,
                credentialsNonExpired,
                accountNonLocked,
                createAuthorityList(myBarUser.getRoles())
        );
    }

    private static List<GrantedAuthority> createAuthorityList(Collection<String> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String roleName : roles) {
            authorities.add(new SimpleGrantedAuthority(roleName));
        }
        return authorities;
    }

}