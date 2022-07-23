package tv.quaint.configs;

import net.streamline.api.configs.DatabaseConfig;
import net.streamline.api.configs.ModularizedConfig;
import net.streamline.api.configs.StorageUtils;
import tv.quaint.StreamlineMessaging;

public class Configs extends ModularizedConfig {
    public Configs() {
        super(StreamlineMessaging.getInstance(), "config.yml", true);
    }

    /*
    ===========================================================================

                                     CHANNELS

    ===========================================================================
     */

    public String defaultChat() {
        reloadResource();

        return resource.getString("chat-channels.default");
    }

    public boolean forceDefaultAlways() {
        reloadResource();

        return resource.getBoolean("chat-channels.force-default.always");
    }

    public boolean forceDefaultOnJoin() {
        reloadResource();

        return resource.getBoolean("chat-channels.force-default.on-join");
    }

    /*
    ===========================================================================

                                     MESSAGING

    ===========================================================================
     */

    public String messagingMessagePermissionFormatting() {
        reloadResource();

        return resource.getString("messaging.message.permissions.formatting");
    }

    public boolean messagingMessageUpdateSender() {
        reloadResource();

        return resource.getBoolean("messaging.message.update-reply-to.sender");
    }

    public boolean messagingMessageUpdateRecipient() {
        reloadResource();

        return resource.getBoolean("messaging.message.update-reply-to.recipient");
    }

    public String messagingReplyPermissionFormatting() {
        reloadResource();

        return resource.getString("messaging.reply.permissions.formatting");
    }

    public boolean messagingReplyUpdateSender() {
        reloadResource();

        return resource.getBoolean("messaging.reply.update-reply-to.sender");
    }

    public boolean messagingReplyUpdateRecipient() {
        reloadResource();

        return resource.getBoolean("messaging.reply.update-reply-to.recipient");
    }


    /*
    ===========================================================================

                                     CHATTERS

    ===========================================================================
     */


    public StorageUtils.StorageType savingUse() {
        reloadResource();

        return resource.getEnum("chatters.saving.use", StorageUtils.StorageType.class);
    }

    public String savingUri() {
        reloadResource();

        return resource.getString("chatters.saving.databases.connection-uri");
    }

    public String savingDatabase() {
        reloadResource();

        return resource.getString("chatters.saving.databases.database");
    }

    public String savingPrefix() {
        reloadResource();

        return resource.getString("chatters.saving.databases.prefix");
    }

    public DatabaseConfig getConfiguredDatabase() {
        StorageUtils.DatabaseType databaseType = null;
        if (savingUse().equals(StorageUtils.StorageType.MONGO)) databaseType = StorageUtils.DatabaseType.MONGO;
        if (savingUse().equals(StorageUtils.StorageType.MYSQL)) databaseType = StorageUtils.DatabaseType.MYSQL;
        if (databaseType == null) return null;

        return new DatabaseConfig(savingUri(), savingDatabase(), savingPrefix(), databaseType);
    }
}
