package tv.quaint.ratapi;

import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.placeholder.RATExpansion;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.StreamlineMessaging;
import tv.quaint.configs.ConfiguredChatChannel;
import tv.quaint.savables.ChatterManager;
import tv.quaint.savables.SavableChatter;

public class MessagingExpansion extends RATExpansion {
    public MessagingExpansion() {
        super("messaging", "Quaint", "1.0");
    }

    @Override
    public String onLogic(String s) {
        if (s.equals("loaded_chatters")) return String.valueOf(ChatterManager.getLoadedChatters().size());
        if (s.equals("loaded_channels")) return String.valueOf(StreamlineMessaging.getChatChannelConfig().getChatChannels().size());
        if (s.equals("default_channel")) return StreamlineMessaging.getConfigs().defaultChat();
        return null;
    }

    @Override
    public String onRequest(StreamlineUser StreamlineUser, String s) {
        SavableChatter chatter = ChatterManager.getOrGetChatter(StreamlineUser.getUUID());
        if (s.startsWith("channel_")) {
            ConfiguredChatChannel channel = chatter.getCurrentChatChannel();
            if (channel == null) return null;
            if (s.equals("channel_identifier")) {
                return channel.identifier();
            }
            if (s.equals("channel_type")) {
                return channel.type().toString();
            }
            if (s.equals("channel_permission_access")) {
                return channel.accessPermission();
            }
            if (s.equals("channel_permission_formatting")) {
                return channel.formattingPermission();
            }
            if (s.equals("channel_message")) {
                return channel.message();
            }
        }
        if (s.startsWith("friends_with_")) {
            String username = s.substring("friends_with_".length());
            SavableChatter otherChatter = ChatterManager.getOrGetChatter(ModuleUtils.getUUIDFromName(username));
            if (s.equals("friends_with_")) {
                return chatter.isMyBestFriend(otherChatter) ?
                        MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_TRUE.get() :
                        MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_FALSE.get();
            }
        }
        if (s.equals("friend_invites_enabled")) {
            return chatter.isAcceptingFriendRequests() ?
                    MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_TRUE.get() :
                    MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_FALSE.get();
        }

        return null;
    }
}
