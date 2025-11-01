package com.qu3dena.lawconnect.backend.iam.infrastructure.authorization.sfs.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qu3dena.lawconnect.backend.iam.domain.model.aggregates.UserAggregate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Implementation of {@link UserDetails} for Spring Security.
 * <p>
 * Encapsulates user information such as username, password, authorities, and account status flags.
 * Used by Spring Security for authentication and authorization.
 * </p>
 */
@Getter
@EqualsAndHashCode
public class UserDetailsImpl implements UserDetails {

    /**
     * The username identifying the user.
     */
    private final String username;

    /**
     * The password used to authenticate the user.
     * This field is ignored during JSON serialization.
     */
    @JsonIgnore
    private final String password;

    /**
     * Indicates whether the user is enabled.
     */
    private final boolean enabled;

    /**
     * Indicates whether the user's account is not locked.
     */
    private final boolean accountNonLocked;

    /**
     * Indicates whether the user's account is not expired.
     */
    private final boolean accountNonExpired;

    /**
     * Indicates whether the user's credentials are not expired.
     */
    private final boolean credentialsNonExpired;

    /**
     * The authorities granted to the user.
     */
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Constructs a new {@code UserDetailsImpl} with the provided username, password, and authorities.
     * All account status flags are set to {@code true} by default.
     *
     * @param username    the username identifying the user
     * @param password    the password for the user
     * @param authorities the authorities granted to the user
     */
    public UserDetailsImpl(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.accountNonExpired = true;
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;
        this.enabled = true;
    }


    /**
     * Builds a {@code UserDetailsImpl} instance from the supplied {@code UserAggregate}.
     *
     * <p>
     * Extracts the user's role and converts it to a {@code SimpleGrantedAuthority} to set up the user's authorities.
     * </p>
     *
     * @param user the user aggregate containing user details
     * @return a new {@code UserDetailsImpl} instance
     */
    public static UserDetailsImpl build(UserAggregate user) {
        var authorities = List.of(new SimpleGrantedAuthority(user.getRole().getName().name()));
        return new UserDetailsImpl(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
