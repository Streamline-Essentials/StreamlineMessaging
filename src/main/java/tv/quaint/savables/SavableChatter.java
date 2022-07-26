package tv.quaint.savables;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.SavableResource;
import net.streamline.api.savables.users.SavableUser;
import net.streamline.base.events.StreamlineChatEvent;
import tv.quaint.StreamlineMessaging;
import tv.quaint.configs.ConfiguredChatChannel;

import java.util.concurrent.ConcurrentHashMap;

public class SavableChatter extends SavableResource {
    @Getter
    private ConfiguredChatChannel currentChatChannel;
    @Getter @Setter
    private String replyTo;
    @Getter @Setter
    private String lastMessage;
    @Getter @Setter
    private String lastMessageSent;
    @Getter @Setter
    private String lastMessageReceived;
    @Getter @Setter
    private ConcurrentHashMap<ConfiguredChatChannel, Boolean> viewing = new ConcurrentHashMap<>();

    public void setCurrentChatChannel(ConfiguredChatChannel chatChannel) {
        if (! chatChannel.identifier().equals(StreamlineMessaging.getConfigs().defaultChat())) {
            if (StreamlineMessaging.getConfigs().forceDefaultAlways()) {
                ConfiguredChatChannel channel = StreamlineMessaging.getChatChannelConfig().getChatChannels().get(StreamlineMessaging.getConfigs().defaultChat());
                if (channel == null) return;
                chatChannel = channel;
            }
        }

        this.currentChatChannel = chatChannel;
    }

    public SavableChatter(String uuid) {
        this(uuid, true);
    }

    public SavableChatter(String uuid, boolean load) {
        super(uuid, ChatterManager.newStorageResourceUsers(uuid, SavableChatter.class));
        if (load) loadAfter();
    }

    @Override
    public void populateDefaults() {
        String chatIdentifier = storageResource.getOrSetDefault("chat-channel.identifier", StreamlineMessaging.getConfigs().defaultChat());
        ConfiguredChatChannel channel = StreamlineMessaging.getChatChannelConfig().getChatChannels().get(chatIdentifier);
        if (channel == null) {
            StreamlineMessaging.getInstance().logWarning("Tried to load a chat channel with identifier '" + chatIdentifier + "' for uuid '" + this.uuid + "', but found no suitable chat channels! Defaulting to none!");
            this.currentChatChannel = StreamlineMessaging.getChatChannelConfig().getChatChannels().get("none");
        } else {
            this.currentChatChannel = channel;
        }
        replyTo = storageResource.getOrSetDefault("messaging.reply-to", "null");
        lastMessage = storageResource.getOrSetDefault("messaging.last.normal", "");
        lastMessageSent = storageResource.getOrSetDefault("messaging.last.sent", "");
        lastMessageReceived = storageResource.getOrSetDefault("messaging.last.received", "");
    }

    @Override
    public void loadValues() {
        String chatIdentifier = storageResource.getOrSetDefault("chat-channel.identifier", currentChatChannel != null ? currentChatChannel.identifier() : StreamlineMessaging.getConfigs().defaultChat());
        ConfiguredChatChannel channel = StreamlineMessaging.getChatChannelConfig().getChatChannels().get(chatIdentifier);
        if (channel == null) {
            StreamlineMessaging.getInstance().logWarning("Tried to load a chat channel with identifier '" + chatIdentifier + "' for uuid '" + this.uuid + "', but found no suitable chat channels! Defaulting to none!");
            this.currentChatChannel = StreamlineMessaging.getChatChannelConfig().getChatChannels().get("none");
        } else {
            this.currentChatChannel = channel;
        }
        replyTo = storageResource.getOrSetDefault("messaging.reply-to", replyTo);
        lastMessage = storageResource.getOrSetDefault("messaging.last.normal", lastMessage);
        lastMessageSent = storageResource.getOrSetDefault("messaging.last.sent", lastMessageSent);
        lastMessageReceived = storageResource.getOrSetDefault("messaging.last.received", lastMessageReceived);
    }

    @Override
    public void saveAll() {
        storageResource.write("chat-channel.identifier", currentChatChannel.identifier());
        storageResource.write("messaging.reply-to", replyTo);
        storageResource.write("messaging.reply-to", replyTo);
        storageResource.write("messaging.last.normal", lastMessage);
        storageResource.write("messaging.last.sent", lastMessageSent);
        storageResource.write("messaging.last.received", lastMessageReceived);
    }

    public void loadAfter() {

    }

    public SavableUser replyToAsUser() {
        return ModuleUtils.getOrGetUser(getReplyTo());
    }

    public SavableUser asUser() {
        return ModuleUtils.getOrGetUser(this.uuid);
    }

    public void onReply(SavableUser recipient, String message) {
        SavableChatter other = ChatterManager.getOrGetChatter(recipient.uuid);
        if (StreamlineMessaging.getConfigs().messagingReplyUpdateSender()) setReplyTo(recipient.uuid);
        if (StreamlineMessaging.getConfigs().messagingReplyUpdateRecipient()) other.setReplyTo(this.uuid);
        setLastMessageSent(message);
        other.setLastMessageReceived(message);
    }

    public void onReply(SavableUser recipient, String message, String senderFormat, String recipientFormat) {
        if (this.uuid.equals(recipient.uuid)) {
            ModuleUtils.sendMessage(asUser(), StreamlineMessaging.getMessages().errorsMessagingSelf());
            return;
        }

        ModuleUtils.sendMessage(asUser(), senderFormat
                .replace("%this_other%", recipient.getName())
                .replace("%this_message%", message)
        );
        ModuleUtils.sendMessage(recipient, asUser(), recipientFormat
                .replace("%this_other%", recipient.getName())
                .replace("%this_message%", message)
        );

        onReply(recipient, message);
    }

    public void onMessage(SavableUser recipient, String message) {
        SavableChatter other = ChatterManager.getOrGetChatter(recipient.uuid);
        if (StreamlineMessaging.getConfigs().messagingMessageUpdateSender()) setReplyTo(recipient.uuid);
        if (StreamlineMessaging.getConfigs().messagingMessageUpdateRecipient()) other.setReplyTo(this.uuid);
        setLastMessageSent(message);
        other.setLastMessageReceived(message);
    }

    public void onMessage(SavableUser recipient, String message, String senderFormat, String recipientFormat) {
        if (this.uuid.equals(recipient.uuid)) {
            ModuleUtils.sendMessage(asUser(), StreamlineMessaging.getMessages().errorsMessagingSelf());
            return;
        }

        ModuleUtils.sendMessage(asUser(), senderFormat
                .replace("%this_other%", recipient.getName())
                .replace("%this_message%", message)
        );
        ModuleUtils.sendMessage(recipient, asUser(), recipientFormat
                .replace("%this_other%", recipient.getName())
                .replace("%this_message%", message)
        );

        onMessage(recipient, message);
    }

    public boolean onChannelMessage(StreamlineChatEvent event) {
        if (getCurrentChatChannel() == null) {
            ModuleUtils.sendMessage(asUser(), StreamlineMessaging.getMessages().errorsChannelIsNull());
            return false;
        }

        if (ModuleUtils.isCommand(event.getMessage())) {
//            ModuleUtils.runAs(event.getSender(), event.getMessage());
            return false;
        }
        if (getCurrentChatChannel().identifier().equals("none")) {
//            ModuleUtils.chatAs(event.getSender(), event.getMessage());
            return false;
        }

        if (! getCurrentChatChannel().formattingPermission().equals("") && ! ModuleUtils.hasPermission(asUser(), getCurrentChatChannel().formattingPermission())) {
            event.setMessage(ModuleUtils.stripColor(event.getMessage()));
        }

        if (! getCurrentChatChannel().accessPermission().equals("") && ! ModuleUtils.hasPermission(asUser(), getCurrentChatChannel().accessPermission())) {
            ModuleUtils.sendMessage(asUser(), StreamlineMessaging.getMessages().errorsChannelNoAccess());
            return false;
        }

        getCurrentChatChannel().sendMessageAs(asUser(), event.getMessage());
        return true;
    }

    public void setViewingEnabled(ConfiguredChatChannel channel, boolean isEnabled) {
        viewing.put(channel, isEnabled);
    }

    public boolean isViewing(ConfiguredChatChannel channel) {
        return getViewing().get(channel);
    }

    public boolean hasViewingPermission(ConfiguredChatChannel channel) {
        return ModuleUtils.hasPermission(ModuleUtils.getOrGetUser(this.uuid), (channel.viewingInfo().permission()));
    }

    public boolean canMessageMeFrom(ConfiguredChatChannel channel) {
        return hasViewingPermission(channel) && isViewing(channel);
    }

    public boolean canToggleViewing(ConfiguredChatChannel channel) {
        return ModuleUtils.hasPermission(ModuleUtils.getOrGetUser(this.uuid), (channel.viewingInfo().togglePermission()));
    }
}
