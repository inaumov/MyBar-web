package mybar.app.impl;

import org.springframework.security.authentication.CachingUserDetailsService;
import org.springframework.stereotype.Service;

@Service("userDetailsService")
public class UserDetailsService extends CachingUserDetailsService {
    public UserDetailsService(UserDetailsServiceImpl delegate) {
        super(delegate);
    }

}
