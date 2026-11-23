package com.projeto.emprestimo.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final TokenService tokenService;

    public SecurityConfig(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login", "/error").permitAll()
                        .requestMatchers("/h2-console/**").hasRole("ADMIN")
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/equipamentos/**", "/emprestimos/**", "/colaboradores/**")
                        .hasAnyRole("ADMIN", "COLABORADOR")
                        .requestMatchers(HttpMethod.POST, "/emprestimos")
                        .hasAnyRole("ADMIN", "COLABORADOR")
                        .requestMatchers(HttpMethod.PUT, "/emprestimos/*/devolucao")
                        .hasAnyRole("ADMIN", "COLABORADOR")
                        .anyRequest().hasRole("ADMIN"))
                .exceptionHandling(erro -> erro.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(new FiltroJwt(tokenService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public UserDetailsService usuarios(
            PasswordEncoder encoder,
            @Value("${app.admin.usuario}") String adminUsuario,
            @Value("${app.admin.senha}") String adminSenha,
            @Value("${app.colaborador.usuario}") String colaboradorUsuario,
            @Value("${app.colaborador.senha}") String colaboradorSenha
    ) {
        UserDetails admin = User.withUsername(adminUsuario)
                .password(encoder.encode(adminSenha))
                .roles("ADMIN")
                .build();

        UserDetails colaborador = User.withUsername(colaboradorUsuario)
                .password(encoder.encode(colaboradorSenha))
                .roles("COLABORADOR")
                .build();

        return new InMemoryUserDetailsManager(admin, colaborador);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}