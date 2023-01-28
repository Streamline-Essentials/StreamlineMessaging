package tv.quaint.configs;

import tv.quaint.storage.resources.databases.configurations.DatabaseConfig;
import tv.quaint.storage.resources.databases.specific.MySQLResource;

public class MySQLChatterDatabase extends MySQLResource {
    public MySQLChatterDatabase(DatabaseConfig config) {
        super(config);
    }
}
