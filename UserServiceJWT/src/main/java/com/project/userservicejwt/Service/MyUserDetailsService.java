package com.project.userservicejwt.Service;

import com.project.userservicejwt.models.User;
import com.project.userservicejwt.models.UserPrincipal;
import com.project.userservicejwt.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByEmail(username);
        if(user.isEmpty()) throw new UsernameNotFoundException("No user found with username: " + username);

        return new UserPrincipal(user.get());
    }
}
