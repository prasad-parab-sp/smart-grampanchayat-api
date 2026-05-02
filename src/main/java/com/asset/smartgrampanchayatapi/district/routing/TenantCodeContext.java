package com.asset.smartgrampanchayatapi.district.routing;

/**
 * Tenant code for the current request, set from the {@code X-Tenant-Code} header by {@link com.asset.smartgrampanchayatapi.web.filter.TenantCodeHeaderFilter}.
 */
public final class TenantCodeContext {

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private TenantCodeContext() {
    }

    public static void set(String tenantCode) {
        CURRENT.set(tenantCode);
    }

    public static String get() {
        return CURRENT.get();
    }

    /** @throws IllegalStateException if no tenant code was bound for this request */
    public static String getRequired() {
        String code = CURRENT.get();
        if (code == null || code.isBlank()) {
            throw new IllegalStateException("No tenant code in request context (expected X-Tenant-Code header)");
        }
        return code;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
