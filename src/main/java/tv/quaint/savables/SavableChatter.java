package tv.quaint.savables;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.SavableResource;
import net.streamline.api.savables.users.SavableUser;
import net.streamline.base.events.StreamlineChatEvent;
import tv.quaint.StreamlineMessaging;
import tv.quaint.configs.ConfiguredChatChannel;
import tv.quaint.timers.FriendInviteExpiry;

import java.util.*;
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
    @Getter @Setter
    private TreeMap<Date, String> friends = new TreeMap<>();
    @Getter @Setter
    private TreeMap<String, FriendInviteExpiry> friendInvites = new TreeMap<>();

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
        viewing = new ConcurrentHashMap<>();
        StreamlineMessaging.getChatChannelConfig().getChatChannels().forEach((s, c) -> {
            viewing.put(c, storageResource.getOrSetDefault("chat-channel.specific." + s + ".viewing", true));
        });
        friends = new TreeMap<>(storageResource.getOrSetDefault("friends.list", new HashMap<>()));
        friendInvites = new TreeMap<>();
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
        storageResource.singleLayerKeySet("chat-channel.specific").forEach(a -> {
            ConfiguredChatChannel chatChannel = StreamlineMessaging.getChatChannelConfig().getChatChannels().get(a);
            if (chatChannel == null) return;
            boolean viewing = storageResource.getOrSetDefault("chat-channel.specific." + a + ".viewing", true);
            getViewing().put(chatChannel, viewing);
        });
        storageResource.singleLayerKeySet("friends.list").forEach(a -> {
            Date date = new Date(Long.parseLong(a));
            getFriends().put(date, storageResource.getOrSetDefault("friends.list." + a, "null"));
        });
        storageResource.singleLayerKeySet("friends.invites.list").forEach(a -> {
            getFriendInvites().put(a, new FriendInviteExpiry(this, ChatterManager.getOrGetChatter(a), storageResource.getOrSetDefault("friends.invites.list." + a, StreamlineMessaging.getConfigs().friendInviteTime())));
        });
    }

    @Override
    public void saveAll() {
        storageResource.write("chat-channel.identifier", currentChatChannel.identifier());
        storageResource.write("messaging.reply-to", replyTo);
        storageResource.write("messaging.reply-to", replyTo);
        storageResource.write("messaging.last.normal", lastMessage);
        storageResource.write("messaging.last.sent", lastMessageSent);
        storageResource.write("messaging.last.received", lastMessageReceived);
        getViewing().forEach((s, b) -> {
            storageResource.write("chat-channel.specific." + s.identifier() + ".viewing", b);
        });
        getFriends().forEach((date, s) -> {
            storageResource.write("friends.list." + date.getTime(), s);
        });
        getFriendInvites().forEach((s, expiry) -> {
            storageResource.write("friends.invites.list." + s, expiry.getTicksLeft());
        });
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
        getViewing().putIfAbsent(channel, true);
        return true;
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

    public void addFriend(SavableChatter chatter) {
        this.addFriend(chatter.uuid);
    }

    public void addFriend(String uuid) {
        getFriends().put(new Date(), uuid);
        ModuleUtils.sendMessage(this.uuid, StreamlineMessaging.getMessages().friendsAddMessage().replace("%this_other%", uuid));
        StreamlineMessaging.getInstance().logInfo("%streamline_parse_" + this.uuid + ":::*/*streamline_user_formatted*/*% just added %streamline_parse_" + uuid + ":::*/*streamline_user_formatted*/*% as a friend!");
    }

    public void addFriendOther(String uuid) {
        SavableChatter chatter = ChatterManager.getOrGetChatter(uuid);
        chatter.addFriend(this);
    }

    public void removeFriend(SavableChatter chatter) {
        this.addFriend(chatter.uuid);
    }

    public void removeFriend(String uuid) {
        getFriends().remove(getFriendedAt(uuid));
        ModuleUtils.sendMessage(this.uuid, StreamlineMessaging.getMessages().friendsRemoveMessage().replace("%this_other%", uuid));
        StreamlineMessaging.getInstance().logInfo("%streamline_parse_" + this.uuid + ":::*/*streamline_user_formatted*/*% just removed %streamline_parse_" + uuid + ":::*/*streamline_user_formatted*/*% as a friend!");
    }

    public void removeFriendOther(String uuid) {
        SavableChatter chatter = ChatterManager.getOrGetChatter(uuid);
        chatter.removeFriend(this);
    }

    public Date getFriendedAt(String uuid) {
        for (Date date : getFriends().keySet()) {
            if (getFriends().get(date).equals(uuid)) return date;
        }

        return null;
    }

    public Date getMeFriendedAtOf(String uuid) {
        SavableChatter chatter = ChatterManager.getOrGetChatter(uuid);
        return chatter.getFriendedAt(this.uuid);
    }

    public boolean isMyFriend(SavableChatter chatter) {
        return this.isMyFriend(chatter.uuid);
    }

    public boolean isMyFriend(String uuid) {
        return getFriends().containsValue(uuid);
    }

    public boolean isMeFriendOf(String uuid) {
        SavableChatter chatter = ChatterManager.getOrGetChatter(uuid);
        return chatter.isMyFriend(this.uuid);
    }

    public TreeMap<Integer, String> getFriendsListPaged() {
        double times = Math.ceil(getFriends().size() / ((double) StreamlineMessaging.getMessages().friendsListMaxPerPage()));

        TreeMap<Integer, String> r = new TreeMap<>();

        for (int i = 0; i < times; i ++) {
            r.put(i, getFriendsListFrom(i * 5));
        }

        return r;
    }

    public String getFriendsListFrom(int start) {
        StringBuilder builder = new StringBuilder();

        for (int i = start; i < getFriends().size(); i ++) {
            if (i >= start + StreamlineMessaging.getMessages().friendsListMaxPerPage()) continue;
            String uuid = new ArrayList<>(getFriends().values()).get(i);
            SavableUser user = ModuleUtils.getOrGetUser(uuid);
            if (i == start + StreamlineMessaging.getMessages().friendsListMaxPerPage() - 1) {
                builder.append(ModuleUtils.replaceAllPlayerBungee(user, StreamlineMessaging.getMessages().friendsListEntryLast()));
            } else {
                builder.append(ModuleUtils.replaceAllPlayerBungee(user, StreamlineMessaging.getMessages().friendsListEntryNotLast()));
            }
        }

        return builder.toString();
    }

    public boolean isAlreadyFriendInvited(SavableChatter chatter) {
        return isAlreadyFriendInvited(chatter.uuid);
    }

    public boolean isAlreadyFriendInvited(String uuid) {
        return getFriendInvites().containsKey(uuid);
    }

    public void addInviteTo(SavableChatter chatter) {
        getFriendInvites().put(chatter.uuid, new FriendInviteExpiry(this, chatter, StreamlineMessaging.getConfigs().friendInviteTime()));
    }

    public void removeInviteTo(SavableChatter chatter) {
        getFriendInvites().remove(chatter.uuid);
    }

    public void handleInviteExpiryEnd(FriendInviteExpiry expiry) {
        removeInviteTo(expiry.getInvited());
        ModuleUtils.sendMessage(asUser(), StreamlineMessaging.getMessages().friendInviteTimeoutSender()
                .replace("%this_other%", expiry.getInvited().asUser().getName())
        );
        ModuleUtils.sendMessage(expiry.getInvited().asUser(), StreamlineMessaging.getMessages().friendInviteTimeoutInvited()
                .replace("%this_other%", asUser().getName())
        );
    }
}
