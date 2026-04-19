package com.todaylunch.dbmigration.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.flyway")
public class MigrationFlywayProperties {

    private boolean baselineOnMigrate = true;
    private String baselineVersion = "0";
    private List<String> locations = List.of("classpath:db/migration");
    private List<String> schemas = List.of();

    public boolean isBaselineOnMigrate() {
        return baselineOnMigrate;
    }

    public void setBaselineOnMigrate(boolean baselineOnMigrate) {
        this.baselineOnMigrate = baselineOnMigrate;
    }

    public String getBaselineVersion() {
        return baselineVersion;
    }

    public void setBaselineVersion(String baselineVersion) {
        this.baselineVersion = baselineVersion;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public List<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }
}
