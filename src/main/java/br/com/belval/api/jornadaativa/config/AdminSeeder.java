package br.com.belval.api.jornadaativa.config;

import br.com.belval.api.jornadaativa.model.entity.RoleName;
import br.com.belval.api.jornadaativa.model.entity.Usuarios;
import br.com.belval.api.jornadaativa.model.entity.Role;
import br.com.belval.api.jornadaativa.model.repository.UsuarioRepository;
import br.com.belval.api.jornadaativa.model.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;   // 👈 IMPORT necessário
import java.util.Optional;

@Configuration
public class AdminSeeder {

    @Bean
    CommandLineRunner seedAdmin(
            UsuarioRepository usuarios,
            RoleRepository roles,              // 👈 repository da Role
            PasswordEncoder encoder,
            @Value("${ADMIN_EMAIL:}") String email,
            @Value("${ADMIN_PASSWORD:}") String senhaPlano,
            @Value("${ADMIN_NOME:Jornada Ativa Admin}") String nome,
            @Value("${ADMIN_GENERO:Outro}") String genero,
            @Value("${ADMIN_DATA_NASC:2000-01-01}") String dataNascStr,
            @Value("${ADMIN_FOTO:}") String ftPerfil,
            @Value("${ADMIN_NIVEL:Iniciante}") String nivel,
            @Value("${ADMIN_ALTURA:1.75}") BigDecimal altura,
            @Value("${ADMIN_PESO:70.5}") BigDecimal peso,
            @Value("${ADMIN_ROLE:ROLE_ADMIN}") String roleStr
    ) {
        return args -> {
            try {
                // 0) guarda de segurança: só roda se tiver email e senha
                if (email == null || email.isBlank() || senhaPlano == null || senhaPlano.isBlank()) return;
                if (usuarios.findByEmail(email).isPresent()) return;

                // 1) normaliza a ROLE para RoleName (ROLE_XYZ)
                String normalized = (roleStr == null ? "" : roleStr.trim().toUpperCase());
                if (!normalized.startsWith("ROLE_")) normalized = "ROLE_" + normalized;
                RoleName roleName = RoleName.valueOf(normalized); // lança se inválida → bom para detectar erro

                // 2) garante que a Role exista (sem usar construtor)
                Role roleEntity = roles.findByName(roleName)      // 👈 se seu método é outro, ajuste aqui
                        .orElseGet(() -> {
                            Role r = new Role();        // usa no-arg + setter
                            // ⚠️ Se o campo do enum na entidade NÃO se chama "name",
                            // troque para o setter correto (ex.: setRoleName / setNome / setValor etc.)
                            r.setName(roleName);
                            return roles.save(r);
                        });

                // 3) parse da data
                LocalDate dataNasc = LocalDate.now();
                try {
                    dataNasc = LocalDate.parse(dataNascStr, DateTimeFormatter.ISO_DATE);
                } catch (DateTimeParseException ignored) {}

                // 4) cria o usuário admin
                Usuarios u = new Usuarios();
                u.setNome(nome);
                u.setEmail(email);
                u.setSenhaHash(encoder.encode(senhaPlano));
                u.setGenero(genero);
                u.setDataNascimento(dataNasc);
                u.setFtPerfil((ftPerfil != null && !ftPerfil.isBlank()) ? ftPerfil : null);
                u.setNivel(nivel);
                u.setAltura(altura);
                u.setPeso(peso);

                // (opcional) mantém coluna string sincronizada, se existir no seu modelo:
                u.setRole(roleName.name());

                // 5) adiciona na coleção de roles (evita NPE inicializando)
                if (u.getRoles() == null) u.setRoles(new HashSet<>());
                u.getRoles().add(roleEntity);

                usuarios.save(u);
                System.out.println(">>> ADMIN criado: " + email + " com " + roleName.name());
            } catch (Exception e) {
                // Não derrube a app por causa do seeder
                System.err.println(">>> [WARN] AdminSeeder falhou: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        };
    }
}
