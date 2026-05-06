package com.asset.smartgrampanchayatapi.district.service.routing;

import java.util.Optional;
import java.util.function.Function;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.asset.smartgrampanchayatapi.district.routing.DistrictRoutingHolder;
import com.asset.smartgrampanchayatapi.exception.DistrictShardUnavailableException;
import com.asset.smartgrampanchayatapi.master.jpa.model.District;
import com.asset.smartgrampanchayatapi.master.jpa.model.MasterTenant;
import com.asset.smartgrampanchayatapi.master.jpa.repository.MasterTenantRepository;

/**
 * Resolves {@code tenantCode} on the master database and runs read logic on the correct district shard with
 * {@link DistrictRoutingHolder} bound for the duration of the callback.
 */
@Service
public class TenantShardRoutingService {

    private final MasterTenantRepository masterTenantRepository;

    public TenantShardRoutingService(MasterTenantRepository masterTenantRepository) {
        this.masterTenantRepository = masterTenantRepository;
    }

    public Optional<TenantShardRoutingContext> resolveForShard(String tenantCode) {
        if (tenantCode == null || tenantCode.isBlank()) {
            return Optional.empty();
        }
        String code = tenantCode.trim();
        Optional<MasterTenant> master = masterTenantRepository.findByTenantCode(code);
        if (master.isEmpty()) {
            return Optional.empty();
        }
        District district = master.get().getDistrict();
        if (district == null) {
            return Optional.empty();
        }
        return Optional.of(new TenantShardRoutingContext(district, master.get().getId(), code));
    }

    /**
     * Binds the district shard for {@code tenantCode}, runs the callback, then clears routing.
     * <p>
     * Callback returns {@link Optional}: use it directly for lookups (e.g. repository {@code find}), or wrap a plain
     * value with {@link Optional#of(Object)} (e.g. {@code ctx -> Optional.of(service.save(...))}).
     * When {@code tenantCode} does not resolve on the master DB, the result is {@link Optional#empty()} without
     * invoking the callback.
     * </p>
     * <p>
     * Must bind routing before {@code @Transactional} runs on shard services: the transaction opens JDBC before the
     * proxied method body executes; {@link com.asset.smartgrampanchayatapi.district.routing.DistrictRoutingDataSource}
     * needs the holder set first.
     * </p>
     */
    public <T> Optional<T> runOnShard(
            String tenantCode,
            String shardUnavailableMessage,
            Function<TenantShardRoutingContext, Optional<T>> shardRead
    ) {
        return resolveForShard(tenantCode).flatMap(ctx -> {
            DistrictRoutingHolder.bind(ctx.district());
            try {
                return shardRead.apply(ctx);
            } catch (DataAccessException e) {
                throw new DistrictShardUnavailableException(shardUnavailableMessage, e);
            } finally {
                DistrictRoutingHolder.clear();
            }
        });
    }
}
