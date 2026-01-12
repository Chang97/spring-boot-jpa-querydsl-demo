package com.demo.api.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing // (필요 시 auditorAwareRef="auditorAware")
public class JpaAuditingConfig {
    /**
     * 기본: 인증체계 없음 → 항상 "system"
     */
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of("system");
    }

    /*
    // Security 있는 환경 예시 (Spring Security 6.x)
    // - 인증되어 있으면 principal 이름 사용, 아니면 "system"으로 대체
    // - AnonymousAuthenticationToken 제외
    @Bean
    public AuditorAware<String> securityAuditorAware() {
        return () -> {
        var ctx = org.springframework.security.core.context.SecurityContextHolder.getContext();
        var auth = ctx != null ? ctx.getAuthentication() : null;

        if (auth != null && auth.isAuthenticated()
            && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {

            // 1) 기본: Authentication#getName()
            String username = auth.getName();

            // 2) 필요 시 principal 타입 분기
            // Object principal = auth.getPrincipal();
            // if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            //   username = ud.getUsername();
            // } else if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User ou) {
            //   username = (String) ou.getAttributes().getOrDefault("preferred_username", ou.getName());
            // }

            return Optional.ofNullable(username).filter(s -> !s.isBlank());
        }
        return Optional.of("system");
        };
    }
    */

}
