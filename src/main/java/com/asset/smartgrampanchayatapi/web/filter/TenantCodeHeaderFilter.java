package com.asset.smartgrampanchayatapi.web.filter;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import com.asset.smartgrampanchayatapi.district.routing.TenantCodeContext;
import com.asset.smartgrampanchayatapi.web.dto.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Requires {@value #HEADER_TENANT_CODE} on {@code /api/**} calls except CORS preflight, and except
 * {@code GET /api/tenants?tenantCode=...} (bootstrap lookup without a header).
 */
public class TenantCodeHeaderFilter extends OncePerRequestFilter {

    public static final String HEADER_TENANT_CODE = "X-Tenant-Code";

    private static final String TENANTS_PATH = "/api/tenants";
    private static final String PARAM_TENANT_CODE = "tenantCode";

    private final ObjectMapper objectMapper;

    public TenantCodeHeaderFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (!request.getServletPath().startsWith("/api")) {
            filterChain.doFilter(request, response);
            return;
        }

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isTenantBootstrapLookup(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String raw = request.getHeader(HEADER_TENANT_CODE);
        if (raw == null || raw.isBlank()) {
            sendMissingHeader(response, request);
            return;
        }

        TenantCodeContext.set(raw.trim());
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantCodeContext.clear();
        }
    }

    private static boolean isTenantBootstrapLookup(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        if (!TENANTS_PATH.equals(request.getServletPath())) {
            return false;
        }
        String p = request.getParameter(PARAM_TENANT_CODE);
        return p != null && !p.isBlank();
    }

    private void sendMissingHeader(HttpServletResponse response, HttpServletRequest request) throws IOException {
        ApiErrorResponse body = ApiErrorResponse.of(
                request,
                HttpStatus.BAD_REQUEST,
                "MISSING_TENANT_HEADER",
                "Required header '" + HEADER_TENANT_CODE + "' is missing or blank."
        );
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
