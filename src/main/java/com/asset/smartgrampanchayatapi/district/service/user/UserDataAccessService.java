package com.asset.smartgrampanchayatapi.district.service.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asset.smartgrampanchayatapi.district.jpa.model.ShardUser;
import com.asset.smartgrampanchayatapi.district.jpa.repository.ShardUserRepository;

@Service
public class UserDataAccessService {

    private final ShardUserRepository shardUserRepository;

    public UserDataAccessService(ShardUserRepository shardUserRepository) {
        this.shardUserRepository = shardUserRepository;
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public Optional<ShardUser> findByTenantIdAndPhone(UUID tenantId, String phone) {
        return shardUserRepository.findByTenantIdAndPhone(tenantId, phone);
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public Optional<ShardUser> findByTenantIdAndEmailIgnoreCase(UUID tenantId, String email) {
        return shardUserRepository.findByTenantIdAndEmailIgnoreCase(tenantId, email);
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public List<ShardUser> findAllByTenantId(UUID tenantId) {
        return shardUserRepository.findAllByTenantId(tenantId);
    }

    @Transactional(transactionManager = "districtTransactionManager")
    public ShardUser save(ShardUser user) {
        return shardUserRepository.save(user);
    }
}
