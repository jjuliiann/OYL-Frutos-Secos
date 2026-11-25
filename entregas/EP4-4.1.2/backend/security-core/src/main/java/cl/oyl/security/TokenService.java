package cl.oyl.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Set;

/** Emite y verifica JWT estándar; Android solo transporta el Bearer. */
public class TokenService {
    private static final String ISSUER = "oyl-users";
    private static final Set<String> ROLES = Set.of("ADMIN", "CLIENT");
    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final long lifetimeSeconds;

    public TokenService(String secret, long lifetimeSeconds) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32)
            throw new IllegalArgumentException("OYL_JWT_SECRET debe tener al menos 32 bytes");
        this.lifetimeSeconds = lifetimeSeconds;
        algorithm = Algorithm.HMAC256(secret);
        verifier = JWT.require(algorithm).withIssuer(ISSUER).withClaimPresence("exp").build();
    }

    public String issue(long userId, String role) {
        if (userId < 1 || role == null || !ROLES.contains(role)) throw new IllegalArgumentException("Identidad inválida");
        var now = Instant.now();
        return JWT.create().withIssuer(ISSUER).withSubject(Long.toString(userId)).withClaim("role", role)
                .withIssuedAt(now).withExpiresAt(now.plusSeconds(lifetimeSeconds)).sign(algorithm);
    }

    public Identity verify(String bearer) {
        if (bearer == null || !bearer.startsWith("Bearer ")) throw new IllegalArgumentException("Falta token de sesión");
        try {
            var jwt = verifier.verify(bearer.substring(7));
            long id = Long.parseLong(jwt.getSubject());
            String role = jwt.getClaim("role").asString();
            if (id < 1 || role == null || !ROLES.contains(role)) throw new IllegalArgumentException("Identidad inválida");
            return new Identity(id, role, jwt.getExpiresAtAsInstant().getEpochSecond());
        } catch (JWTVerificationException | NumberFormatException e) {
            throw new IllegalArgumentException("Sesión inválida o vencida", e);
        }
    }
    public record Identity(long userId, String role, long expiresAt) {}
}
