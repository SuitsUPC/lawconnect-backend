package com.qu3dena.lawconnect.backend.iam.infrastructure.hashing.bcrypt;

import com.qu3dena.lawconnect.backend.iam.application.internal.outboundservices.hashing.HashingService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * BCrypt hashing service.
 * <p>
 *     This service is responsible for hashing and verifying passwords using BCrypt algorithm.
 *     It extends {@link HashingService} and {@link PasswordEncoder}.
 * </p>
 */
public interface BCryptHashingService extends HashingService, PasswordEncoder {
}
