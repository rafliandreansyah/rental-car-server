package com.rentalcar.server.security;

import com.rentalcar.server.entity.UserRoleEnum;
import com.rentalcar.server.handler.CustomAccessDeniedHandler;
import com.rentalcar.server.handler.CustomAuthenticationEntryPointHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;

@EnableWebSecurity
@Configuration
//@RequiredArgsConstructor
public class SecurityConfiguration {

    @Qualifier("handlerExceptionResolver")
    private final HandlerExceptionResolver exceptionResolver;
    private final UserDetailsService userDetailsService;
    //private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPointHandler customAuthenticationEntryPointHandler;
    private final JwtService jwtService;

    public SecurityConfiguration(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver, UserDetailsService userDetailsService, CustomAccessDeniedHandler customAccessDeniedHandler, CustomAuthenticationEntryPointHandler customAuthenticationEntryPointHandler, JwtService jwtService) {
        this.exceptionResolver = exceptionResolver;
        this.userDetailsService = userDetailsService;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.customAuthenticationEntryPointHandler = customAuthenticationEntryPointHandler;
        this.jwtService = jwtService;
    }

    /*@Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
            return new JwtAuthenticationFilter(exceptionResolver, userDetailsService, jwtService);
        }
    */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                new AntPathRequestMatcher("/api/v1/users", HttpMethod.POST.name()),
                                new AntPathRequestMatcher("/api/v1/users", HttpMethod.GET.name()),
                                new AntPathRequestMatcher("/api/v1/users/**", HttpMethod.DELETE.name()),
                                new AntPathRequestMatcher("/api/v1/cars", HttpMethod.POST.name()),
                                new AntPathRequestMatcher("/api/v1/cars/**", HttpMethod.DELETE.name()),
                                new AntPathRequestMatcher("/api/v1/cars/**", HttpMethod.PATCH.name()),
                                new AntPathRequestMatcher("/api/v1/transactions", HttpMethod.GET.name()),
                                new AntPathRequestMatcher("/api/v1/app/**")
                        ).hasAnyAuthority(UserRoleEnum.ADMIN.name())
                        .requestMatchers(
                                new AntPathRequestMatcher("/api/v1/users/**"),
                                new AntPathRequestMatcher("/api/v1/cars/**"),
                                new AntPathRequestMatcher("/api/v1/transactions/**")
                        )
                        .authenticated()
                        .anyRequest()
                        .permitAll()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer
                        .accessDeniedHandler(customAccessDeniedHandler)
                        .authenticationEntryPoint(customAuthenticationEntryPointHandler))
                .addFilterBefore(new JwtAuthenticationFilter(exceptionResolver, userDetailsService, jwtService), UsernamePasswordAuthenticationFilter.class)
                .build();

    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
