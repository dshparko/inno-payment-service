package com.innowise.paymentservice.config;

import liquibase.command.CommandScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * @ClassName Mongo
 * @Description Executes Liquibase migrations for MongoDB on application startup,
 * only if Liquibase is explicitly enabled via configuration
 * @Author dshparko
 * @Date 05.11.2025 14:54
 * @Version 1.0
 */
@Component
@ConditionalOnProperty(prefix = "spring.liquibase", name = "enabled", havingValue = "true")
public class MongoLiquibaseConfig implements CommandLineRunner {

    @Value("${spring.liquibase.change-log}")
    private String changelogFile;

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Override
    public void run(String... args) throws Exception {
        CommandScope liquibaseCommand = new CommandScope("update");

        liquibaseCommand.addArgumentValue("changelogFile", changelogFile);
        liquibaseCommand.addArgumentValue("url", mongoUri);

        liquibaseCommand.execute();
    }

}


