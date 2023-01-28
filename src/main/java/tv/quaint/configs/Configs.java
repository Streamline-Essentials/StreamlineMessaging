package tv.quaint.configs;

import net.streamline.api.configs.ModularizedConfig;
import tv.quaint.StreamlineMessaging;
import tv.quaint.storage.StorageUtils;
import tv.quaint.storage.resources.databases.configurations.DatabaseConfig;
import tv.quaint.thebase.lib.leonhard.storage.sections.FlatFileSection;

public class Configs extends ModularizedConfig {
    public Configs() {
        super(StreamlineMessaging.getInstance(), "config.yml", true);
    }

    @Override
    public void init() {
        if (getResource().contains("chatters")) {
            getResource().remove("chatters");
        }

        defaultChat();
        forceDefaultAlways();
        forceDefaultOnJoin();

        messagingMessagePermissionFormatting();
        messagingMessageUpdateSender();
        messagingMessageUpdateRecipient();

        messagingReplyPermissionFormatting();
        messagingReplyUpdateSender();
        messagingReplyUpdateRecipient();

        savingUse();
    }

    /*
    ===========================================================================

                                     CHANNELS

    ===========================================================================
     */

    public String defaultChat() {
        reloadResource();

        return getResource().getOrSetDefault("chat-channels.default", "none");
    }

    public boolean forceDefaultAlways() {
        reloadResource();

        return getResource().getOrSetDefault("chat-channels.force-default.always", false);
    }

    public boolean forceDefaultOnJoin() {
        reloadResource();

        return getResource().getOrSetDefault("chat-channels.force-default.on-join", true);
    }

    /*
    ===========================================================================

                                     MESSAGING

    ===========================================================================
     */

    public String messagingMessagePermissionFormatting() {
        reloadResource();

        return getResource().getOrSetDefault("messaging.message.permissions.formatting", "streamline.messaging.formatting.message");
    }

    public boolean messagingMessageUpdateSender() {
        reloadResource();

        return getResource().getOrSetDefault("messaging.message.update-reply-to.sender", true);
    }

    public boolean messagingMessageUpdateRecipient() {
        reloadResource();

        return getResource().getOrSetDefault("messaging.message.update-reply-to.recipient", true);
    }

    public String messagingReplyPermissionFormatting() {
        reloadResource();

        return getResource().getOrSetDefault("messaging.reply.permissions.formatting", "streamline.messaging.formatting.message");
    }

    public boolean messagingReplyUpdateSender() {
        reloadResource();

        return getResource().getOrSetDefault("messaging.reply.update-reply-to.sender", true);
    }

    public boolean messagingReplyUpdateRecipient() {
        reloadResource();

        return getResource().getOrSetDefault("messaging.reply.update-reply-to.recipient", true);
    }


    /*
    ===========================================================================

                                     CHATTERS

    ===========================================================================
     */

    public StorageUtils.SupportedStorageType savingUse() {
        reloadResource();

        return StorageUtils.SupportedStorageType.valueOf(getResource().getOrSetDefault("chatters.saving.use", StorageUtils.SupportedStorageType.YAML.toString()));
    }

    // FRIENDS

    public int friendInviteTime() {
        reloadResource();

        return getResource().getOrSetDefault("friends.invites.timeout", 600);
    }
}
