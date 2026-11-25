package cl.oyl.users;

import static cl.oyl.users.UserDtos.*;

import cl.oyl.security.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController // Expone las rutas HTTP del servicio Usuarios.
public class UserController {
    private final UserRepository users; // Lee y guarda cuentas en H2.
    private final TokenService tokens; // Emite y valida sesiones.
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(); // Hashea contraseñas.

    public UserController(UserRepository users, TokenService tokens) {
        this.users = users;
        this.tokens = tokens;
    }

    // Convierte la entidad de base de datos a una respuesta sin token.
    private Account view(UserAccount user) {
        return new Account(user.id, user.alias, user.name, user.email, user.phone, user.address, user.role, null);
    }

    @GetMapping("/api/health") // Informa que Usuarios está activo.
    public java.util.Map<String, String> health() {
        return java.util.Map.of("status", "ok", "service", "users");
    }

    @PostMapping("/api/auth/register") // Valida, guarda y autentica una cuenta nueva.
    public Account register(@RequestBody Register request) {
        if (request == null || blank(request.alias()) || blank(request.name())
                || request.password() == null || request.password().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Nombre, alias y contraseña de al menos 8 caracteres son obligatorios");
        }

        String alias = request.alias().trim();
        if (users.findByAlias(alias).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Alias ocupado");
        }

        UserAccount account = new UserAccount();
        account.alias = alias;
        account.name = request.name().trim();
        account.email = clean(request.email());
        account.phone = clean(request.phone());
        account.address = clean(request.address());
        account.passwordHash = encoder.encode(request.password());
        return withToken(users.save(account));
    }

    @PostMapping("/api/auth/login") // Comprueba las credenciales y devuelve un token.
    public Account login(@RequestBody Credentials credentials) {
        if (credentials == null || blank(credentials.alias()) || credentials.password() == null) {
            throw unauthorized();
        }

        UserAccount account = users.findByAlias(credentials.alias().trim())
                .orElseThrow(UserController::unauthorized);
        if (!encoder.matches(credentials.password(), account.passwordHash)) {
            throw unauthorized();
        }
        return withToken(account);
    }

    @PutMapping("/api/users/{id}") // Actualiza solo el perfil dueño del token.
    public Account update(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorization,
            @RequestBody ProfileUpdate profile
    ) {
        requireSameUser(id, authorization);
        UserAccount account = users.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario inexistente"));
        if (profile == null || blank(profile.name())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }

        account.name = profile.name().trim();
        account.email = clean(profile.email());
        account.phone = clean(profile.phone());
        account.address = clean(profile.address());
        return view(users.save(account));
    }

    @GetMapping("/internal/users/{id}") // Consulta interna, no expuesta por el Gateway.
    public Account internal(@PathVariable Long id) {
        return users.findById(id).map(this::view)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario inexistente"));
    }

    // Añade el token firmado a la respuesta de login o registro.
    private Account withToken(UserAccount user) {
        Account account = view(user);
        return new Account(account.id(), account.alias(), account.name(), account.email(),
                account.phone(), account.address(), account.role(), tokens.issue(user.id, user.role));
    }

    // Evita que una cuenta cambie el perfil de otra persona.
    private void requireSameUser(Long id, String authorization) {
        try {
            if (tokens.verify(authorization).userId() != id) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes editar otro perfil");
            }
        } catch (IllegalArgumentException error) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, error.getMessage());
        }
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }
    private static String clean(String value) { return value == null ? "" : value.trim(); }
    private static ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Alias o contraseña incorrectos");
    }
}
