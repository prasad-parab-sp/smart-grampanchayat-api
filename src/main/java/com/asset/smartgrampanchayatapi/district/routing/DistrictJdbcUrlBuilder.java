package com.asset.smartgrampanchayatapi.district.routing;

import com.asset.smartgrampanchayatapi.master.jpa.model.District;

final class DistrictJdbcUrlBuilder {

    private DistrictJdbcUrlBuilder() {
    }

    static String buildUrl(District district) {
        String host = district.getDbHost().trim();
        int port = district.getDbPort();
        String db = district.getDbName().trim();
        String base = String.format("jdbc:postgresql://%s:%d/%s", host, port, db);
        if (Boolean.TRUE.equals(district.getDbSslEnabled())) {
            // prefer: negotiate SSL when the server supports it (e.g. RDS); else fall back instead of failing with
            // “The server does not support SSL” from sslmode=require on plain local Postgres / Docker Postgres.
            return base + "?sslmode=prefer";
        }
        return base + "?sslmode=disable";
    }
}
