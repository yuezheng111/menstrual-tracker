package com.menstrualtracker.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Resolves the client IP without trusting client-supplied forwarding headers.
 * Forwarded headers are only accepted when the request arrives from a proxy
 * listed in app.security.trusted-proxies.
 */
@Component
public class ClientIpResolver {

    private final List<IpAddressMatcher> trustedProxies;

    public ClientIpResolver(@Value("${app.security.trusted-proxies:127.0.0.1,::1}") String trustedProxies) {
        this.trustedProxies = Arrays.stream(trustedProxies.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(IpAddressMatcher::new)
                .toList();
    }

    public String resolve(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        boolean fromTrustedProxy = trustedProxies.stream().anyMatch(matcher -> matcher.matches(remoteAddr));
        if (fromTrustedProxy) {
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (StringUtils.hasText(forwardedFor) && !"unknown".equalsIgnoreCase(forwardedFor)) {
                String[] entries = forwardedFor.split(",");
                for (int i = entries.length - 1; i >= 0; i--) {
                    String candidate = entries[i].trim();
                    if (StringUtils.hasText(candidate) && !"unknown".equalsIgnoreCase(candidate)) {
                        return candidate;
                    }
                }
            }
            String realIp = request.getHeader("X-Real-IP");
            if (StringUtils.hasText(realIp) && !"unknown".equalsIgnoreCase(realIp)) {
                return realIp;
            }
        }
        return remoteAddr;
    }
}
