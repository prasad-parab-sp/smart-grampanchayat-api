package com.asset.smartgrampanchayatapi.district.routing;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * JDBC {@link DataSource} that delegates to the {@link DistrictDataSourceRegistry} for the district bound via
 * {@link DistrictRoutingHolder}.
 */
public final class DistrictRoutingDataSource implements DataSource {

    private final DistrictDataSourceRegistry registry;

    public DistrictRoutingDataSource(DistrictDataSourceRegistry registry) {
        this.registry = registry;
    }

    private DataSource delegate() {
        return registry.getDataSource(DistrictRoutingHolder.getRequired());
    }

    @Override
    public Connection getConnection() throws SQLException {
        return delegate().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return delegate().getConnection(username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate().getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate().setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate().setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate().getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate().getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate().isWrapperFor(iface);
    }
}
