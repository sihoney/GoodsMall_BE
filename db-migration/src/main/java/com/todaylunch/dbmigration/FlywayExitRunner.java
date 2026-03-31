package com.todaylunch.dbmigration;

import java.sql.Connection;
import java.util.Arrays;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

@Component
public class FlywayExitRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FlywayExitRunner.class);

    private final ConfigurableApplicationContext applicationContext;
    private final DataSource dataSource;
    private final Environment environment;

    public FlywayExitRunner(
            ConfigurableApplicationContext applicationContext,
            DataSource dataSource,
            Environment environment
    ) {
        this.applicationContext = applicationContext;
        this.dataSource = dataSource;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean baselineOnMigrate = environment.getProperty("spring.flyway.baseline-on-migrate", Boolean.class, true);
        String baselineVersion = environment.getProperty("spring.flyway.baseline-version", "0");
        String[] schemas = splitCsv(environment.getProperty("spring.flyway.schemas", "payment,settlement"));
        String[] locations = splitCsv(environment.getProperty("spring.flyway.locations", "classpath:db/migration"));

        log.info("Starting explicit Flyway migration");

        MigrateResult migrateResult = Flyway.configure()
                .dataSource(dataSource)
                .baselineOnMigrate(baselineOnMigrate)
                .baselineVersion(baselineVersion)
                .schemas(schemas)
                .locations(locations)
                .load()
                .migrate();

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

    private void runSeedScriptsIfEnabled() {
        boolean seedEnabled = environment.getProperty("app.seed.enabled", Boolean.class, false);
        if (!seedEnabled) {
            log.info("Seed execution is disabled. Skipping seed scripts.");
            return;
        }

        String[] seedLocations = splitCsv(environment.getProperty("app.seed.locations", ""));
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

    private String[] splitCsv(String value) {
        if (value == null || value.isBlank()) {
            return new String[0];
        }
        return Arrays.stream(value.split("\\s*,\\s*"))
                .filter(entry -> entry != null && !entry.isBlank())
                .toArray(String[]::new);
    }
}
