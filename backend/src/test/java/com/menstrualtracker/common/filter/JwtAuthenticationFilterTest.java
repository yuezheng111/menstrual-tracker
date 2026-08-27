package com.menstrualtracker.common.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {

    private final JwtAuthenticationFilter filter =
            new JwtAuthenticationFilter(null, null, null, null, null);

    @Test
    void refreshTokenEndpointSkipsJwtValidation() {
        MockHttpServletRequest request =
                new MockHttpServletRequest("POST", "/api/auth/refresh-token");

        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void normalApiEndpointIsNotSkipped() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/records");

        assertThat(filter.shouldNotFilter(request)).isFalse();
    }
}
