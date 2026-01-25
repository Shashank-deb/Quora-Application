package com.example.quoraapplication.config;

import com.example.quoraapplication.security.CustomUserDetailsService;
import com.example.quoraapplication.security.JwtAuthenticationEntryPoint;
import com.example.quoraapplication.security.JwtAuthenticationFilter;
import com.example.quoraapplication.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true
)
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtTokenProvider tokenProvider;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                          JwtTokenProvider tokenProvider) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.tokenProvider = tokenProvider;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenProvider, userDetailsService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/favicon.ico", "/**/*.png", "/**/*.gif", "/**/*.svg", "/**/*.jpg", "/**/*.html", "/**/*.css", "/**/*.js").permitAll()
                        .requestMatchers("/api/v*/auth/**").permitAll()
                        .requestMatchers("/api/v*/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v*/questions/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v*/questions").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v*/questions/**").hasAnyRole("ADMIN", "MODERATOR")

                        .requestMatchers(HttpMethod.GET, "/api/v*/answers/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v*/answers").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v*/answers/**").authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/v*/tags/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v*/tags").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v*/tags/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/v*/users/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v*/users/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v*/users/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v*/users/**").hasAnyRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/v*/comments/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v*/comments").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v*/comments/**").authenticated()

                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:3001",
                "http://localhost:8080"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}