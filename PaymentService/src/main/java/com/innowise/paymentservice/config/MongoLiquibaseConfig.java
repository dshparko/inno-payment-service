package com.innowise.paymentservice.config;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import liquibase.ext.mongodb.database.MongoConnection;
import liquibase.ext.mongodb.database.MongoLiquibaseDatabase;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "spring.liquibase", name = "enabled", havingValue = "true")
public class MongoLiquibaseConfig implements CommandLineRunner {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.liquibase.change-log}")
    private String changelogFile;

    @Override
    public void run(String... args) throws Exception {
        validateChangelogExists();

        MongoClient mongoClient = createMongoClient();
        try {
            String dbName = extractDatabaseName();

            MongoDatabase mongoDatabase = getMongoDatabase(mongoClient, dbName);

            Liquibase liquibase = createLiquibase(mongoClient, mongoDatabase);

            runMigrations(liquibase);

        } finally {
            closeMongoClient(mongoClient);
        }

    }

    private void validateChangelogExists() {
        if (!new ClassPathResource(changelogFile).exists()) {
            throw new IllegalStateException(
                    "Changelog file not found on classpath: " + changelogFile
            );
        }
    }

    private MongoClient createMongoClient() {
        return MongoClients.create(mongoUri);
    }

    private String extractDatabaseName() {
        ConnectionString cs = new ConnectionString(mongoUri);
        String databaseName = cs.getDatabase();

        if (databaseName == null) {
            throw new IllegalStateException(
                    "MongoDB URI must include database name, e.g. mongodb://host:27017/paymentdb"
            );
        }

        return databaseName;
    }

    private MongoDatabase getMongoDatabase(MongoClient mongoClient, String dbName) {
        return mongoClient.getDatabase(dbName);
    }


    private Liquibase createLiquibase(MongoClient mongoClient, MongoDatabase mongoDatabase) {
        MongoConnection connection = new MongoConnection();
        connection.setMongoClient(mongoClient);
        connection.setMongoDatabase(mongoDatabase);

        MongoLiquibaseDatabase database = new MongoLiquibaseDatabase();
        database.setConnection(connection);

        return new Liquibase(
                changelogFile,
                new ClassLoaderResourceAccessor(),
                database
        );
    }

    private void runMigrations(Liquibase liquibase) throws LiquibaseException {
        liquibase.update(new Contexts(), new LabelExpression());
    }

    private void closeMongoClient(MongoClient mongoClient) {
        mongoClient.close();
    }

}
