package tv.quaint.configs;

import net.streamline.api.configs.DatabaseConfig;
import net.streamline.api.configs.ModularizedConfig;
import net.streamline.api.configs.StorageUtils;
import tv.quaint.StreamlineMessaging;

public class Configs extends ModularizedConfig {
    public Configs() {
        super(StreamlineMessaging.getInstance(), "config.yml", false);
    }

    /*
    ===========================================================================

                                     CHANNELS

    ===========================================================================
     */

    public String defaultChat() {
        reloadResource();

        return resource.getOrSetDefault("chat-channels.default", "none");
    }

    public boolean forceDefaultAlways() {
        reloadResource();

        return resource.getOrSetDefault("chat-channels.force-default.always", false);
    }

    public boolean forceDefaultOnJoin() {
        reloadResource();

        return resource.getOrSetDefault("chat-channels.force-default.on-join", true);
    }

    /*
    ===========================================================================

                                     MESSAGING

    ===========================================================================
     */

    public String messagingMessagePermissionFormatting() {
        reloadResource();

        return resource.getOrSetDefault("messaging.message.permissions.formatting", "streamline.messaging.formatting.message");
    }

    public boolean messagingMessageUpdateSender() {
        reloadResource();

        return resource.getOrSetDefault("messaging.message.update-reply-to.sender", true);
    }

    public boolean messagingMessageUpdateRecipient() {
        reloadResource();

        return resource.getOrSetDefault("messaging.message.update-reply-to.recipient", true);
    }

    public String messagingReplyPermissionFormatting() {
        reloadResource();

        return resource.getOrSetDefault("messaging.reply.permissions.formatting", "streamline.messaging.formatting.message");
    }

    public boolean messagingReplyUpdateSender() {
        reloadResource();

        return resource.getOrSetDefault("messaging.reply.update-reply-to.sender", true);
    }

    public boolean messagingReplyUpdateRecipient() {
        reloadResource();

        return resource.getOrSetDefault("messaging.reply.update-reply-to.recipient", true);
    }


    /*
    ===========================================================================

                                     CHATTERS

    ===========================================================================
     */


    public StorageUtils.StorageType savingUse() {
        reloadResource();

        return StorageUtils.StorageType.valueOf(resource.getOrSetDefault("chatters.saving.use", StorageUtils.StorageType.YAML.toString()));
    }

    public String savingUri() {
        reloadResource();

        return resource.getOrSetDefault("chatters.saving.databases.connection-uri", "mongodb://<user>:<pass>@<host>:<port>/?authSource=admin&readPreference=primary&appname=StreamlineGroups&ssl=false");
    }

    public String savingDatabase() {
        reloadResource();

        return resource.getOrSetDefault("chatters.saving.databases.database", "streamline_chatters");
    }

    public String savingPrefix() {
        reloadResource();

        return resource.getOrSetDefault("chatters.saving.databases.prefix", "sl_");
    }

    public DatabaseConfig getConfiguredDatabase() {
        StorageUtils.DatabaseType databaseType = null;
        if (savingUse().equals(StorageUtils.StorageType.MONGO)) databaseType = StorageUtils.DatabaseType.MONGO;
        if (savingUse().equals(StorageUtils.StorageType.MYSQL)) databaseType = StorageUtils.DatabaseType.MYSQL;
        if (databaseType == null) return null;

        return new DatabaseConfig(savingUri(), savingDatabase(), savingPrefix(), databaseType);
    }

    // FRIENDS

    public int friendInviteTime() {
        reloadResource();

        return resource.getOrSetDefault("friends.invites.timeout", 600);
    }
}
