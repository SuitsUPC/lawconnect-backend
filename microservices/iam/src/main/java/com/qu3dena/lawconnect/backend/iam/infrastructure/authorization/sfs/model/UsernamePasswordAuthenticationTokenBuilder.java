package com.qu3dena.lawconnect.backend.iam.infrastructure.authorization.sfs.model;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

/**
 * Utility class for building {@link UsernamePasswordAuthenticationToken} instances
 * with additional web authentication details from an HTTP request.
 * <p>
 *     This builder simplifies the creation of authentication tokens for Spring Security,
 *     associating the authenticated principal and its authorities with the current request context.
 * </p>
 */
public class UsernamePasswordAuthenticationTokenBuilder {

    /**
     * Builds a {@link UsernamePasswordAuthenticationToken} for the given principal and HTTP request.
     * <p>
     *     The token is initialized with the provided principal, its authorities, and
     *     attaches web authentication details extracted from the request.
     * </p>
     *
     * @param principal the authenticated user details
     * @param request   the current HTTP servlet request
     * @return a fully initialized {@code UsernamePasswordAuthenticationToken}
     */
    public static UsernamePasswordAuthenticationToken build(UserDetailsImpl principal, HttpServletRequest request) {
        var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());

        usernamePasswordAuthenticationToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request));

        return usernamePasswordAuthenticationToken;
    }
}
