package br.com.belval.api.jornadaativa.security.config;

import br.com.belval.api.jornadaativa.security.jwt.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(
            JwtAuthFilter jwtAuthFilter,
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults()) // usa o bean corsConfigurationSource()
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ⭐ Preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // ⭐ Público
                        .requestMatchers("/health", "/actuator/health", "/auth/**").permitAll()
                        // (opcional) Swagger
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * CORS configurável por properties:
     * - app.cors.allowed-origins: lista de origens fixas (ex.: https://jornada-ativa.vercel.app)
     * - app.cors.allowed-origin-patterns: padrões (ex.: https://*.vercel.app)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:}") String origins,
            @Value("${app.cors.allowed-origin-patterns:}") String originPatterns,
            @Value("${app.cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}") String methods,
            @Value("${app.cors.allowed-headers:Authorization,Content-Type,Accept,Origin}") String headers,
            @Value("${app.cors.exposed-headers:Authorization}") String exposed,
            @Value("${app.cors.allow-credentials:true}") boolean allowCreds
    ) {
        var cfg = new CorsConfiguration();

        // ✅ Preferir patterns (permite previews tipo https://algo-xxx.vercel.app)
        if (originPatterns != null && !originPatterns.isBlank()) {
            cfg.setAllowedOriginPatterns(Arrays.stream(originPatterns.split("\\s*,\\s*")).toList());
        } else if (origins != null && !origins.isBlank()) {
            cfg.setAllowedOrigins(Arrays.stream(origins.split("\\s*,\\s*")).toList());
        }

        cfg.setAllowedMethods(Arrays.stream(methods.split("\\s*,\\s*")).toList());
        cfg.setAllowedHeaders(Arrays.stream(headers.split("\\s*,\\s*")).toList());
        cfg.setExposedHeaders(Arrays.stream(exposed.split("\\s*,\\s*")).toList());
        cfg.setAllowCredentials(allowCreds);

        var src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }
    }