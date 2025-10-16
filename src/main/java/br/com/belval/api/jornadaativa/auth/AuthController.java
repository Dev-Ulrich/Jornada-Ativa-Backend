package br.com.belval.api.jornadaativa.auth;

import br.com.belval.api.jornadaativa.auth.dto.RegisterRequest;
import br.com.belval.api.jornadaativa.auth.dto.TokenResponse;
import br.com.belval.api.jornadaativa.model.entity.RoleName;
import br.com.belval.api.jornadaativa.model.entity.Usuarios;
import br.com.belval.api.jornadaativa.model.repository.RoleRepository;
import br.com.belval.api.jornadaativa.model.repository.UsuarioRepository;
import br.com.belval.api.jornadaativa.security.jwt.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwt;
    private final UsuarioRepository usuarios;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;

    public AuthController(
            AuthenticationConfiguration cfg,
            JwtService jwt,
            UsuarioRepository usuarios,
            RoleRepository roles,
            PasswordEncoder encoder
    ) throws Exception {
        this.authManager = cfg.getAuthenticationManager();
        this.jwt = jwt;
        this.usuarios = usuarios;
        this.roles = roles;
        this.encoder = encoder;
    }

    /**
     * Retorna informações do usuário autenticado (útil p/ front verificar role).
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String email = auth.getName();
        var uOpt = usuarios.findByEmail(email);
        var rolesDoUser = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList();

        // NUNCA use Map.of quando houver chance de null
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email", email);
        body.put("nome", uOpt.map(Usuarios::getNome).orElse(null));
        body.put("ftPerfil", uOpt.map(Usuarios::getFtPerfil).orElse(null));
        body.put("roles", rolesDoUser);

        return ResponseEntity.ok(body);
    }

    /**
     * Login com email/senha. Retorna { token: "..." }.
     * Aceita payload com "senha" (padrão do seu front) ou "password".
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> req) {
        String email = req.getOrDefault("email", "").trim();
        String senha = (req.get("senha") != null ? req.get("senha") : req.get("password"));

        if (email.isBlank() || senha == null || senha.isBlank()) {
            return ResponseEntity.status(400).body(Map.of("message", "Email e senha são obrigatórios"));
        }

        var uOpt = usuarios.findByEmail(email);
        if (uOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Credenciais inválidas"));
        }

        var u = uOpt.get();
        if (!encoder.matches(senha, u.getSenhaHash())) {
            return ResponseEntity.status(401).body(Map.of("message", "Credenciais inválidas"));
        }

        try {
            var auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, senha)
            );
            var token = jwt.generateToken((UserDetails) auth.getPrincipal());
            return ResponseEntity.ok(new TokenResponse(token));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body(Map.of("message", "Credenciais inválidas"));
        }
    }

    /**
     * Registro simples de usuário. Define ROLE_USER como padrão.
     */
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest req) {
        if (usuarios.findByEmail(req.email()).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("message", "E-mail já cadastrado"));
        }

        var u = new Usuarios();
        u.setNome(req.nome() != null && !req.nome().isBlank() ? req.nome() : req.email());
        u.setEmail(req.email());
        u.setSenhaHash(encoder.encode(req.password()));
        u.setGenero(req.genero() != null ? req.genero() : "N/D");
        u.setDataNascimento(req.dataNascimento() != null ? req.dataNascimento() : LocalDate.of(2000, 1, 1));
        u.setFtPerfil(req.ftPerfil() != null && !req.ftPerfil().isBlank() ? req.ftPerfil().trim() : null);
        u.setNivel(req.nivel() != null ? req.nivel() : "INICIANTE");
        u.setAltura(req.altura());
        u.setPeso(req.peso());

        // Garante que o Set de roles não é nulo
        if (u.getRoles() == null) {
            u.setRoles(new HashSet<>());
        }

        var roleName = req.role() != null ? req.role() : RoleName.ROLE_USER;
        roles.findByName(roleName).ifPresent(u.getRoles()::add);

        usuarios.save(u);
        return ResponseEntity.status(201).build();
    }
}