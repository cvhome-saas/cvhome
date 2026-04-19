package com.asrevo.cvhome.cua.security;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.asrevo.cvhome.cua.domain.User;
import com.asrevo.cvhome.cua.repo.UserRepository;

@Service
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public JpaUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String clientId = getClientId();
        if (clientId == null) {
            throw new UsernameNotFoundException("Client ID not found in request");
        }

        User u = users.findByClientIdAndUsername(clientId, username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found for client " + clientId + " and username: " + username));

        return new SecurityUser(u);
    }

    private String getClientId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            // During OIDC authorization request, it's a query parameter
            String clientId = request.getParameter("client_id");
            if (clientId != null) {
                return clientId;
            }

            // If it's a post-login redirect, it might be in the saved request
            // But easier to check if we can get it from session or other places if
            // needed.
            // For form login, the client_id should have been passed or kept in session.
        }
        return null;
    }

}
