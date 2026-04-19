package com.todaylunch.dbmigration;

import com.todaylunch.dbmigration.config.MigrationFlywayProperties;
import com.todaylunch.dbmigration.config.SeedProperties;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.output.MigrateResult;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

@Component
public class FlywayExitRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FlywayExitRunner.class);

    private final ConfigurableApplicationContext applicationContext;
    private final DataSource dataSource;
    private final MigrationFlywayProperties flywayProperties;
    private final SeedProperties seedProperties;

    public FlywayExitRunner(
            ConfigurableApplicationContext applicationContext,
            DataSource dataSource,
            MigrationFlywayProperties flywayProperties,
            SeedProperties seedProperties
    ) {
        this.applicationContext = applicationContext;
        this.dataSource = dataSource;
        this.flywayProperties = flywayProperties;
        this.seedProperties = seedProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        String[] locations = resolveLocations();

        logConfiguredSchemasIfPresent();
        log.info(
                "Starting explicit Flyway migration. historySchema=default, locations={}, baselineOnMigrate={}, baselineVersion={}",
                Arrays.toString(locations),
                flywayProperties.isBaselineOnMigrate(),
                flywayProperties.getBaselineVersion()
        );

        FluentConfiguration configuration = Flyway.configure()
                .dataSource(dataSource)
                .baselineOnMigrate(flywayProperties.isBaselineOnMigrate())
                .baselineVersion(flywayProperties.getBaselineVersion())
                .locations(locations);

        MigrateResult migrateResult = configuration.load().migrate();

        log.info(
                "Flyway migration finished. initialSchemaVersion={}, targetSchemaVersion={}, migrationsExecuted={}",
                migrateResult.initialSchemaVersion,
                migrateResult.targetSchemaVersion,
                migrateResult.migrationsExecuted
        );

        runSeedScriptsIfEnabled();

        int exitCode = SpringApplication.exit(applicationContext, () -> 0);
        System.exit(exitCode);
    }

    private void logConfiguredSchemasIfPresent() {
        List<String> configuredSchemas = flywayProperties.getSchemas();
        if (configuredSchemas == null || configuredSchemas.isEmpty()) {
            return;
        }
        log.info(
                "Configured spring.flyway.schemas={} but current runner intentionally does not pass schemas() to Flyway. "
                        + "This keeps schema creation owned by versioned migrations such as V1/V2 and avoids pre-creating history schemas.",
                configuredSchemas
        );
    }

    private void runSeedScriptsIfEnabled() {
        if (!seedProperties.isEnabled()) {
            log.info("Seed execution is disabled. Skipping seed scripts.");
            return;
        }

        String[] seedLocations = resolveSeedLocations();
        if (seedLocations.length == 0) {
            log.info("Seed execution is enabled, but no seed locations are configured.");
            return;
        }

        log.info("Running seed scripts. locations={}", Arrays.toString(seedLocations));
        try (Connection connection = dataSource.getConnection()) {
            for (String location : seedLocations) {
                Resource resource = applicationContext.getResource(location);
                if (!resource.exists()) {
                    throw new IllegalStateException("Seed resource not found: " + location);
                }
                ScriptUtils.executeSqlScript(connection, resource);
                log.info("Completed seed script: {}", location);
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to execute seed scripts.", exception);
        }
    }

    private String[] resolveLocations() {
        List<String> configuredLocations = flywayProperties.getLocations();
        if (configuredLocations == null || configuredLocations.isEmpty()) {
            return new String[]{"classpath:db/migration"};
        }
        return configuredLocations.stream()
                .filter(location -> location != null && !location.isBlank())
                .toArray(String[]::new);
    }

    private String[] resolveSeedLocations() {
        List<String> configuredLocations = seedProperties.getLocations();
        if (configuredLocations == null || configuredLocations.isEmpty()) {
            return new String[0];
        }
        return configuredLocations.stream()
                .filter(location -> location != null && !location.isBlank())
                .toArray(String[]::new);
    }
}
