package com.asset.smartgrampanchayatapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

import com.asset.smartgrampanchayatapi.district.config.DistrictShardProperties;
import com.asset.smartgrampanchayatapi.master.config.DistrictCredentialEncryptionProperties;
import com.asset.smartgrampanchayatapi.master.jpa.model.District;
import com.asset.smartgrampanchayatapi.master.jpa.model.MasterTenant;

@SpringBootApplication
@EntityScan(basePackageClasses = { MasterTenant.class, District.class })
@EnableConfigurationProperties({ DistrictShardProperties.class, DistrictCredentialEncryptionProperties.class })
public class SmartGrampanchayatApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartGrampanchayatApiApplication.class, args);
    }

}
