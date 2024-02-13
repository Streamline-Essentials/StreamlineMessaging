package host.plas.savables;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.events.server.StreamlineChatEvent;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.SavableResource;
import net.streamline.api.savables.users.StreamlineUser;
import host.plas.StreamlineMessaging;
import host.plas.configs.ConfiguredChatChannel;
import tv.quaint.storage.resources.StorageResource;
import tv.quaint.storage.resources.flat.FlatFileResource;
import host.plas.timers.FriendInviteExpiry;
import tv.quaint.utils.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class SavableChatter extends SavableResource {
    @Override
    public StorageResource<?> getStorageResource() {
        return super.getStorageResource();
    }

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
    private ConcurrentSkipListMap<Date, String> friends = new ConcurrentSkipListMap<>();
    @Getter @Setter
    private ConcurrentSkipListMap<String, FriendInviteExpiry> friendInvites = new ConcurrentSkipListMap<>();
    @Getter @Setter
    private ConcurrentSkipListMap<Date, String> bestFriends = new ConcurrentSkipListMap<>();
    @Getter @Setter
    private boolean acceptingFriendRequests = true;

    public void setCurrentChatChannel(ConfiguredChatChannel chatChannel) {
        if (! chatChannel.getIdentifier().equals(StreamlineMessaging.getConfigs().defaultChat())) {
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
        String chatIdentifier = getStorageResource().getOrSetDefault("chat-channel.identifier", StreamlineMessaging.getConfigs().defaultChat());
        ConfiguredChatChannel channel = StreamlineMessaging.getChatChannelConfig().getChatChannels().get(chatIdentifier);
        if (channel == null) {
            StreamlineMessaging.getInstance().logWarning("Tried to load a chat channel with identifier '" + chatIdentifier + "' for uuid '" + this.getUuid() + "', but found no suitable chat channels! Defaulting to none!");
            this.currentChatChannel = StreamlineMessaging.getChatChannelConfig().getChatChannels().get("none");
        } else {
            this.currentChatChannel = channel;
        }
        replyTo = getStorageResource().getOrSetDefault("messaging.reply-to", "null");
        lastMessage = getStorageResource().getOrSetDefault("messaging.last.normal", "");
        lastMessageSent = getStorageResource().getOrSetDefault("messaging.last.sent", "");
        lastMessageReceived = getStorageResource().getOrSetDefault("messaging.last.received", "");

        this.viewing = new ConcurrentHashMap<>();
        String v = getStorageResource().getOrSetDefault("chat-channel.specific.viewing", getDefaultChannelsViewing());
        List<String> viewing = Arrays.asList(v.split(";"));
        viewing.forEach(s -> {
            if (s == null) return;
            if (s.isEmpty()) return;
            if (s.isBlank()) return;
            String[] split = s.split(",", 2);
            if (split.length != 2) return;
            ConfiguredChatChannel chatChannel = StreamlineMessaging.getChatChannelConfig().getChatChannels().get(split[0]);
            if (chatChannel == null) return;

            boolean is = false;
            try {
                is = Boolean.parseBoolean(split[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.viewing.put(chatChannel, is);
        });
        friends = getFriendMapFromResource("friends.list", new ConcurrentSkipListMap<>());
        List<String> invites = getStringListFromResource("friends.invites.list", new ArrayList<>());
        friendInvites = new ConcurrentSkipListMap<>();
        invites.forEach(s -> {
            friendInvites.put(s, new FriendInviteExpiry(ChatterManager.getOrGetChatter(s), this, StreamlineMessaging.getConfigs().friendInviteTime()));
        });
        bestFriends = getFriendMapFromResource("friends.best.list", new ConcurrentSkipListMap<>());
        acceptingFriendRequests = getStorageResource().getOrSetDefault("friends.invites.accepting", true);
    }

    public List<String> getStringListFromResource(String key, List<String> def){
        String defString = StringUtils.listToString(def, ",");
        try {
            String s = getStorageResource().getOrSetDefault(key, defString);
            return StringUtils.stringToList(s, ",");
        } catch (ClassCastException e) {
            try {
                return getStorageResource().getOrSetDefault(key, def);
            } catch (ClassCastException error) {
                if (getStorageResource() instanceof FlatFileResource<?>) {
                    FlatFileResource<?> flatFileResource = (FlatFileResource<?>) getStorageResource();

                    flatFileResource.getResource().remove(key);
                }
                getStorageResource().write(key, defString);
                return StringUtils.stringToList(defString, ",");
            }
        }
    }

    public ConcurrentSkipListMap<Date, String> getFriendMapFromResource(String key, ConcurrentSkipListMap<Long, String> def){
        ConcurrentSkipListSet<String> entriesStrings = new ConcurrentSkipListSet<>();
        def.forEach((aLong, s) -> {
           entriesStrings.add(aLong + "!!" + s);
        });

        List<String> split = getStringListFromResource(key, new ArrayList<>(entriesStrings));
        ConcurrentSkipListMap<Date, String> map = new ConcurrentSkipListMap<>();
        split.forEach(s -> {
            String[] split1 = s.split("!!");
            if (split1.length != 2) return;
            try {
                long l = Long.parseLong(split1[0]);
                map.put(new Date(l), split1[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return map;
    }

    public static String getDefaultChannelsViewing() {
        StringBuilder builder = new StringBuilder();
        StreamlineMessaging.getChatChannelConfig().getChatChannels().forEach((s, configuredChatChannel) -> {
            if (configuredChatChannel.getIdentifier().equals(StreamlineMessaging.getConfigs().defaultChat())) {
                builder.append(s).append(",").append(true).append(";"); // default chat channel is always true
                return;
            }
            builder.append(s).append(",").append(false).append(";");
        });
        return builder.toString();
    }

    public static String getCurrentChannelsViewing(ConcurrentHashMap<ConfiguredChatChannel, Boolean> viewing) {
        StringBuilder builder = new StringBuilder();
        viewing.forEach((configuredChatChannel, aBoolean) -> {
            builder.append(configuredChatChannel.getIdentifier()).append(",").append(aBoolean).append(";");
        });
        return builder.toString();
    }

    public static ConcurrentSkipListMap<Date, String> getFriendsFromList(List<String> strings) {
        ConcurrentSkipListMap<Date, String> map = new ConcurrentSkipListMap<>();

        strings.forEach(s -> {
            try {
                String[] split = s.split(",", 2);
                if (split.length != 2) return;
                map.put(new Date(Long.parseLong(split[0])), split[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return map;
    }

    public static ConcurrentSkipListMap<Date, String> getBestFriendsFromList(Map<String, String> strings) {
        ConcurrentSkipListMap<Date, String> map = new ConcurrentSkipListMap<>();

        strings.forEach((s, s2) -> {
            try {
                map.put(new Date(Long.parseLong(s)), s2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return map;
    }

    public static List<String> friendsToList(ConcurrentSkipListMap<Date, String> map) {
        List<String> list = new ArrayList<>();

        map.forEach((date, s) -> {
            list.add(date.getTime() + "," + s);
        });

        return list;
    }

    public static List<String> bestFriendsToList(ConcurrentSkipListMap<Date, String> map) {
        List<String> list = new ArrayList<>();

        map.forEach((date, s) -> {
            list.add(date.getTime() + "," + s);
        });

        return list;
    }

    @Override
    public void loadValues() {
        String chatIdentifier = getStorageResource().getOrSetDefault("chat-channel.identifier", currentChatChannel != null ? currentChatChannel.getIdentifier() : StreamlineMessaging.getConfigs().defaultChat());
        ConfiguredChatChannel channel = StreamlineMessaging.getChatChannelConfig().getChatChannels().get(chatIdentifier);
        if (channel == null) {
            StreamlineMessaging.getInstance().logWarning("Tried to load a chat channel with identifier '" + chatIdentifier + "' for uuid '" + this.getUuid() + "', but found no suitable chat channels! Defaulting to none!");
            this.currentChatChannel = StreamlineMessaging.getChatChannelConfig().getChatChannels().get("none");
        } else {
            this.currentChatChannel = channel;
        }
        replyTo = getStorageResource().getOrSetDefault("messaging.reply-to", replyTo);
        lastMessage = getStorageResource().getOrSetDefault("messaging.last.normal", lastMessage);
        lastMessageSent = getStorageResource().getOrSetDefault("messaging.last.sent", lastMessageSent);
        lastMessageReceived = getStorageResource().getOrSetDefault("messaging.last.received", lastMessageReceived);

        this.viewing = new ConcurrentHashMap<>();
        String v = getStorageResource().getOrSetDefault("chat-channel.specific.viewing", getDefaultChannelsViewing());
        List<String> viewing = Arrays.asList(v.split(";"));
        viewing.forEach(s -> {
            if (s == null) return;
            if (s.isEmpty()) return;
            if (s.isBlank()) return;
            String[] split = s.split(",", 2);
            if (split.length != 2) return;
            ConfiguredChatChannel chatChannel = StreamlineMessaging.getChatChannelConfig().getChatChannels().get(split[0]);
            if (chatChannel == null) return;

            boolean is = false;
            try {
                is = Boolean.parseBoolean(split[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.viewing.put(chatChannel, is);
        });
        friends = getFriendMapFromResource("friends.list", new ConcurrentSkipListMap<>());
        List<String> invites = getStringListFromResource("friends.invites.list", new ArrayList<>());
        friendInvites = new ConcurrentSkipListMap<>();
        invites.forEach(s -> {
            friendInvites.put(s, new FriendInviteExpiry(ChatterManager.getOrGetChatter(s), this, StreamlineMessaging.getConfigs().friendInviteTime()));
        });
        bestFriends = getFriendMapFromResource("friends.best.list", new ConcurrentSkipListMap<>());
        acceptingFriendRequests = getStorageResource().getOrSetDefault("friends.invites.accepting", true);
    }

    @Override
    public void saveAll() {
        getStorageResource().write("chat-channel.identifier", currentChatChannel.getIdentifier());
        getStorageResource().write("messaging.reply-to", replyTo);
        getStorageResource().write("messaging.reply-to", replyTo);
        getStorageResource().write("messaging.last.normal", lastMessage);
        getStorageResource().write("messaging.last.sent", lastMessageSent);
        getStorageResource().write("messaging.last.received", lastMessageReceived);

        getStorageResource().write("chat-channel.specific.viewing", getCurrentChannelsViewing(getViewing()));

        StringBuilder builder = new StringBuilder();
        getFriends().forEach((date, s) -> {
            builder.append(date.getTime()).append("!!").append(s).append(",");
        });
        if (builder.length() > 0) builder.deleteCharAt(builder.length() - 1);
        getStorageResource().write("friends.list", builder.toString());

        StringBuilder builder2 = new StringBuilder();
        getFriendInvites().forEach((s, friendInviteExpiry) -> {
            builder2.append(s).append(",");
        });
        if (builder2.length() > 0) builder2.deleteCharAt(builder2.length() - 1);
        getStorageResource().write("friends.invites.list", builder2.toString());

        StringBuilder builder3 = new StringBuilder();
        getBestFriends().forEach((date, s) -> {
            builder3.append(date.getTime()).append("!!").append(s).append(",");
        });
        if (builder3.length() > 0) builder3.deleteCharAt(builder3.length() - 1);
        getStorageResource().write("friends.best.list", builder3.toString());

        getStorageResource().write("friends.invites.accepting", acceptingFriendRequests);

        getStorageResource().sync();
    }

    public void loadAfter() {

    }

    public StreamlineUser replyToAsUser() {
        return ModuleUtils.getOrGetUser(getReplyTo());
    }

    public StreamlineUser asUser() {
        return ModuleUtils.getOrGetUser(this.getUuid());
    }

    public void onReply(StreamlineUser recipient, String message) {
        SavableChatter other = ChatterManager.getOrGetChatter(recipient.getUuid());
        if (StreamlineMessaging.getConfigs().messagingReplyUpdateSender()) setReplyTo(recipient.getUuid());
        if (StreamlineMessaging.getConfigs().messagingReplyUpdateRecipient()) other.setReplyTo(this.getUuid());
        setLastMessageSent(message);
        other.setLastMessageReceived(message);
    }

    public void onReply(StreamlineUser recipient, String message, String senderFormat, String recipientFormat) {
        if (this.getUuid().equals(recipient.getUuid())) {
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

    public void onMessage(StreamlineUser recipient, String message) {
        SavableChatter other = ChatterManager.getOrGetChatter(recipient.getUuid());
        if (StreamlineMessaging.getConfigs().messagingMessageUpdateSender()) setReplyTo(recipient.getUuid());
        if (StreamlineMessaging.getConfigs().messagingMessageUpdateRecipient()) other.setReplyTo(this.getUuid());
        setLastMessageSent(message);
        other.setLastMessageReceived(message);
    }

    public void onMessage(StreamlineUser recipient, String message, String senderFormat, String recipientFormat) {
        if (this.getUuid().equals(recipient.getUuid())) {
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
        if (getCurrentChatChannel().getIdentifier().equals("none")) {
//            ModuleUtils.chatAs(event.getSender(), event.getMessage());
            return false;
        }

        if (! getCurrentChatChannel().getFormattingPermission().equals("") && ! ModuleUtils.hasPermission(asUser(), getCurrentChatChannel().getFormattingPermission())) {
            event.setMessage(ModuleUtils.stripColor(event.getMessage()));
        }

        if (! getCurrentChatChannel().getAccessPermission().equals("") && ! ModuleUtils.hasPermission(asUser(), getCurrentChatChannel().getAccessPermission())) {
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
        return ModuleUtils.hasPermission(ModuleUtils.getOrGetUser(this.getUuid()), channel.getViewingInfo().getPermission());
    }

    public boolean canMessageMeFrom(ConfiguredChatChannel channel) {
        return hasViewingPermission(channel) && isViewing(channel);
    }

    public boolean canToggleViewing(ConfiguredChatChannel channel) {
        return ModuleUtils.hasPermission(ModuleUtils.getOrGetUser(this.getUuid()), (channel.getViewingInfo().getTogglePermission()));
    }

    public void addFriend(SavableChatter chatter) {
        this.removeInviteTo(chatter);
        getFriends().put(new Date(), chatter.getUuid());
        if (StreamlineMessaging.getMessages().friendsAddSend()) ModuleUtils.sendMessage(this.getUuid(), StreamlineMessaging.getMessages().friendsAddMessage().replace("%this_other%", chatter.getUuid()));
        StreamlineMessaging.getInstance().logInfo("%streamline_parse_" + this.getUuid() + ":::*/*streamline_user_formatted*/*% just added %streamline_parse_" + getUuid() + ":::*/*streamline_user_formatted*/*% as a friend!");
    }

    public void addFriend(String uuid) {
        this.addFriend(ChatterManager.getOrGetChatter(uuid));
    }

    public void addFriendOther(String uuid) {
        SavableChatter chatter = ChatterManager.getOrGetChatter(uuid);
        chatter.addFriend(this);
    }

    public void removeFriend(SavableChatter chatter) {
        this.removeInviteTo(chatter);
        getFriends().remove(getFriendedAt(chatter.getUuid()));
        if (StreamlineMessaging.getMessages().friendsRemoveSend()) ModuleUtils.sendMessage(this.getUuid(), StreamlineMessaging.getMessages().friendsRemoveMessage().replace("%this_other%", chatter.getUuid()));
        StreamlineMessaging.getInstance().logInfo("%streamline_parse_" + this.getUuid() + ":::*/*streamline_user_formatted*/*% just removed %streamline_parse_" + getUuid() + ":::*/*streamline_user_formatted*/*% as a friend!");
    }

    public void removeFriend(String uuid) {
        this.removeFriend(ChatterManager.getOrGetChatter(uuid));
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
        return chatter.getFriendedAt(this.getUuid());
    }

    public boolean isMyFriend(SavableChatter chatter) {
        return this.isMyFriend(chatter.getUuid());
    }

    public boolean isMyFriend(String uuid) {
        return getFriends().containsValue(uuid);
    }

    public boolean isMeFriendOf(String uuid) {
        SavableChatter chatter = ChatterManager.getOrGetChatter(uuid);
        return chatter.isMyFriend(this.getUuid());
    }

    public ConcurrentSkipListMap<Integer, String> getFriendsListPaged() {
        double times = Math.ceil(getFriends().size() / ((double) StreamlineMessaging.getMessages().friendsListMaxPerPage()));

        ConcurrentSkipListMap<Integer, String> r = new ConcurrentSkipListMap<>();

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
            StreamlineUser user = ModuleUtils.getOrGetUser(uuid);
            if (i == start + StreamlineMessaging.getMessages().friendsListMaxPerPage() - 1) {
                builder.append(ModuleUtils.replaceAllPlayerBungee(user, StreamlineMessaging.getMessages().friendsListEntryLast()));
            } else {
                builder.append(ModuleUtils.replaceAllPlayerBungee(user, StreamlineMessaging.getMessages().friendsListEntryNotLast()));
            }
        }

        return builder.toString();
    }

    public boolean isAlreadyFriendInvited(SavableChatter chatter) {
        return isAlreadyFriendInvited(chatter.getUuid());
    }

    public boolean isAlreadyFriendInvited(String uuid) {
        return getFriendInvites().containsKey(uuid);
    }

    public void addInviteTo(SavableChatter chatter) {
        getFriendInvites().put(chatter.getUuid(), new FriendInviteExpiry(this, chatter, StreamlineMessaging.getConfigs().friendInviteTime()));
    }

    public void removeInviteTo(SavableChatter chatter) {
        if (isAlreadyFriendInvited(chatter)) {
            FriendInviteExpiry prev = getFriendInvites().remove(chatter.getUuid());
            prev.cancel();
        }
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

    public void addBestFriend(SavableChatter chatter) {
        this.removeInviteTo(chatter);
        getBestFriends().put(new Date(), chatter.getUuid());
        if (StreamlineMessaging.getMessages().bestFriendsAddSend()) ModuleUtils.sendMessage(this.getUuid(), StreamlineMessaging.getMessages().bestFriendsAddMessage().replace("%this_other%", chatter.getUuid()));
        StreamlineMessaging.getInstance().logInfo("%streamline_parse_" + this.getUuid() + ":::*/*streamline_user_formatted*/*% just added %streamline_parse_" + getUuid() + ":::*/*streamline_user_formatted*/*% as a best friend!");
    }

    public void addBestFriend(String uuid) {
        this.addBestFriend(ChatterManager.getOrGetChatter(uuid));
    }

    public void addBestFriendOther(String uuid) {
        SavableChatter chatter = ChatterManager.getOrGetChatter(uuid);
        chatter.addBestFriend(this);
    }

    public void removeBestFriend(SavableChatter chatter) {
        this.removeInviteTo(chatter);
        getBestFriends().remove(getBestFriendedAt(chatter.getUuid()));
        if (StreamlineMessaging.getMessages().bestFriendsRemoveSend()) ModuleUtils.sendMessage(this.getUuid(), StreamlineMessaging.getMessages().bestFriendsRemoveMessage().replace("%this_other%", chatter.getUuid()));
        StreamlineMessaging.getInstance().logInfo("%streamline_parse_" + this.getUuid() + ":::*/*streamline_user_formatted*/*% just removed %streamline_parse_" + getUuid() + ":::*/*streamline_user_formatted*/*% as a best friend!");
    }

    public void removeBestFriend(String uuid) {
        this.removeBestFriend(ChatterManager.getOrGetChatter(uuid));
    }

    public void removeBestFriendOther(String uuid) {
        SavableChatter chatter = ChatterManager.getOrGetChatter(uuid);
        chatter.removeBestFriend(this);
    }

    public Date getBestFriendedAt(String uuid) {
        for (Date date : getBestFriends().keySet()) {
            if (getBestFriends().get(date).equals(uuid)) return date;
        }

        return null;
    }

    public Date getMeBestFriendedAtOf(String uuid) {
        SavableChatter chatter = ChatterManager.getOrGetChatter(uuid);
        return chatter.getBestFriendedAt(this.getUuid());
    }

    public boolean isMyBestFriend(SavableChatter chatter) {
        return this.isMyBestFriend(chatter.getUuid());
    }

    public boolean isMyBestFriend(String uuid) {
        return getBestFriends().containsValue(uuid);
    }

    public boolean isMeBestFriendOf(String uuid) {
        SavableChatter chatter = ChatterManager.getOrGetChatter(uuid);
        return chatter.isMyBestFriend(this.getUuid());
    }

    public ConcurrentSkipListMap<Integer, String> getBestFriendsListPaged() {
        double times = Math.ceil(getBestFriends().size() / ((double) StreamlineMessaging.getMessages().bestFriendsListMaxPerPage()));

        ConcurrentSkipListMap<Integer, String> r = new ConcurrentSkipListMap<>();

        for (int i = 0; i < times; i ++) {
            r.put(i, getBestFriendsListFrom(i * 5));
        }

        return r;
    }

    public String getBestFriendsListFrom(int start) {
        StringBuilder builder = new StringBuilder();

        for (int i = start; i < getBestFriends().size(); i ++) {
            if (i >= start + StreamlineMessaging.getMessages().bestFriendsListMaxPerPage()) continue;
            String uuid = new ArrayList<>(getBestFriends().values()).get(i);
            StreamlineUser user = ModuleUtils.getOrGetUser(uuid);
            if (i == start + StreamlineMessaging.getMessages().bestFriendsListMaxPerPage() - 1) {
                builder.append(ModuleUtils.replaceAllPlayerBungee(user, StreamlineMessaging.getMessages().bestFriendsListEntryLast()));
            } else {
                builder.append(ModuleUtils.replaceAllPlayerBungee(user, StreamlineMessaging.getMessages().bestFriendsListEntryNotLast()));
            }
        }

        return builder.toString();
    }
}
