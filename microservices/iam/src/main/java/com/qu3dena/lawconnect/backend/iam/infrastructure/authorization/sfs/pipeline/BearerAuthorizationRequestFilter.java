package com.qu3dena.lawconnect.backend.iam.infrastructure.authorization.sfs.pipeline;

import com.qu3dena.lawconnect.backend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.qu3dena.lawconnect.backend.iam.infrastructure.authorization.sfs.model.UsernamePasswordAuthenticationTokenBuilder;
import com.qu3dena.lawconnect.backend.iam.infrastructure.tokens.jwt.BearerTokenService;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

/**
 * Filter to authenticate requests with Bearer token
 * <p>
 *     This filter will authenticate requests with Bearer token.
 *     It will extract the token from the request and validate it.
 *     If the token is valid, it will set the user authentication in the security context.
 *     The user authentication will be set with the user details from the token.
 * </p>
 */
public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BearerAuthorizationRequestFilter.class);
    private final BearerTokenService tokenService;

    @Qualifier("defaultUserDetailsService")
    private final UserDetailsService userDetailsService;

    /**
     * Constructor
     * @param tokenService {@link BearerTokenService} Bearer token service
     * @param userDetailsService {@link UserDetailsService} User details service
     */
    public BearerAuthorizationRequestFilter(BearerTokenService tokenService, UserDetailsService userDetailsService) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Filter requests
     * <p>
     *     This method will filter the requests.
     *     It will extract the Bearer token from the request.
     *     If the token is valid, it will set the user authentication in the security context.
     *     The user authentication will be set with the user details from the token.
     *     If the token is not valid, it will log a warning.
     * </p>
     * @param request {@link HttpServletRequest} Request
     * @param response {@link HttpServletResponse} Response
     * @param filterChain {@link FilterChain} Filter chain
     * @throws ServletException If an error occurs
     * @throws IOException If an error occurs
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)  throws ServletException, IOException {
        try {
            // ──── 1) Extraer el header y el token ────────────────
            String bearerToken = tokenService.getBearerTokenFrom(request);
            if (bearerToken != null && tokenService.validateToken(bearerToken)) {
                // ──── 2) Obtener el username y cargar UserDetails ───
                String username = tokenService.getUsernameFromToken(bearerToken);
                UserDetailsImpl user = (UserDetailsImpl)
                        userDetailsService.loadUserByUsername(username);

                // ──── 3) Construir el Authentication y setear en contexto ───
                UsernamePasswordAuthenticationToken auth =
                        UsernamePasswordAuthenticationTokenBuilder.build(user, request);

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            // si bearerToken es null o inválido, no autenticamos y dejamos pasar
        } catch (Exception ex) {
            LOGGER.error("Cannot set user authentication: {}", ex.getMessage());
        }

        // ──── 4) Continuar la cadena de filtros ─────────────
        filterChain.doFilter(request, response);
    }
}
