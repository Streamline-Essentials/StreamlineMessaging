package tv.quaint.configs;

import tv.quaint.storage.resources.databases.configurations.DatabaseConfig;
import tv.quaint.storage.resources.databases.specific.SQLiteResource;

public class SQLiteChatterDatabase extends SQLiteResource {
    public SQLiteChatterDatabase(DatabaseConfig config) {
        super(config);
    }
}
