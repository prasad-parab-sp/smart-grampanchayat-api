package com.asset.smartgrampanchayatapi.district.routing;

import com.asset.smartgrampanchayatapi.master.jpa.model.District;

/**
 * Holds the district whose shard datasource should be used for the current thread (typically one HTTP request scope).
 */
public final class DistrictRoutingHolder {

    private static final ThreadLocal<District> CURRENT = new ThreadLocal<>();

    private DistrictRoutingHolder() {
    }

    public static void bind(District district) {
        if (district == null) {
            throw new IllegalArgumentException("district");
        }
        CURRENT.set(district);
    }

    public static District getRequired() {
        District district = CURRENT.get();
        if (district == null) {
            throw new IllegalStateException("No district bound for shard access");
        }
        return district;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
