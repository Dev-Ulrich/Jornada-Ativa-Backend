package br.com.belval.api.jornadaativa.config;


import br.com.belval.api.jornadaativa.model.entity.Role;
import br.com.belval.api.jornadaativa.model.entity.RoleName;
import br.com.belval.api.jornadaativa.model.entity.Usuarios;
import br.com.belval.api.jornadaativa.model.repository.UsuarioRepository;
import br.com.belval.api.jornadaativa.model.service.RoleService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class AdminSeeder {


    @Bean
    CommandLineRunner seedAdmin(
            UsuarioRepository usuarios,
            PasswordEncoder encoder,
            RoleService roleService,
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
            // só roda se as envs mínimas existirem
            if (email == null || email.isBlank() || senhaPlano == null || senhaPlano.isBlank()) return;
            if (usuarios.findByEmail(email).isPresent()) return;

            LocalDate dataNasc = LocalDate.parse(dataNascStr, DateTimeFormatter.ISO_DATE);

            RoleName roleName = toRoleName(roleStr);
            Role roleEntity = roleService.getOrThrow(roleName);


            var u = new Usuarios();
            u.setNome(nome);
            u.setEmail(email);
            u.setSenhaHash(encoder.encode(senhaPlano));
            u.setGenero(genero);
            u.setDataNascimento(dataNasc);
            u.setFtPerfil((ftPerfil != null && !ftPerfil.isBlank()) ? ftPerfil : null);
            u.setNivel(nivel);
            u.setAltura(altura);
            u.setPeso(peso);

            // 1) coluna string (se você mantém essa coluna)
            u.setRole(roleName.name());
            // 2) coleção de roles (ESSENCIAL para authorities)
            if (u.getRoles() == null) {
                // garanta que sua entidade inicializa a coleção; se não, faça aqui:
                u.setRoles(new java.util.HashSet<>());
            }
            u.getRoles().add(roleEntity);

            usuarios.save(u);
            System.out.println(">>> ADMIN criado: " + email + " com " + roleName.name());
        };
    }

    // mesma ideia do seu service
    private RoleName toRoleName(String roleStr) {
        String normalized = (roleStr == null ? "" : roleStr.trim().toUpperCase());
        if (!normalized.startsWith("ROLE_")) normalized = "ROLE_" + normalized;
        return RoleName.valueOf(normalized); // lança erro se inválido (ok)
    }
}