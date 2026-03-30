package com.todaylunch.dbmigration;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
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

        int exitCode = SpringApplication.exit(applicationContext, () -> 0);
        System.exit(exitCode);
    }

    private String[] splitCsv(String value) {
        return value.split("\\s*,\\s*");
    }
}
