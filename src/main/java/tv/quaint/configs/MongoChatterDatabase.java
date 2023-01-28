package tv.quaint.configs;

import tv.quaint.storage.resources.databases.configurations.DatabaseConfig;
import tv.quaint.storage.resources.databases.specific.MongoResource;

public class MongoChatterDatabase extends MongoResource {
    public MongoChatterDatabase(DatabaseConfig config) {
        super(config);
    }
}
