package host.plas.ratapi;

import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.data.console.StreamSender;
import net.streamline.api.data.players.StreamPlayer;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.placeholders.expansions.RATExpansion;
import net.streamline.api.placeholders.replaceables.IdentifiedReplaceable;
import net.streamline.api.placeholders.replaceables.IdentifiedUserReplaceable;
import host.plas.StreamlineMessaging;
import host.plas.configs.ConfiguredChatChannel;
import host.plas.savables.SavableChatter;
import host.plas.savables.ChatterManager;
import tv.quaint.utils.MatcherUtils;

public class MessagingExpansion extends RATExpansion {
    public MessagingExpansion() {
        super(new RATExpansionBuilder("messaging"));
    }

    @Override
    public void init() {
        new IdentifiedReplaceable(this, "loaded_chatters", (s) -> String.valueOf(ChatterManager.getLoadedChatters().size())).register();
        new IdentifiedReplaceable(this, "loaded_channels", (s) -> String.valueOf(StreamlineMessaging.getChatChannelConfig().getChatChannels().size())).register();
        new IdentifiedReplaceable(this, "default_channel", (s) -> StreamlineMessaging.getConfigs().defaultChat()).register();

        new IdentifiedUserReplaceable(this, MatcherUtils.makeLiteral("channel_") + "(.*?)", 1, (s, u) -> {
            SavableChatter chatter = ChatterManager.getOrGetChatter(u.getUuid());
            String string = startsWithChannel(s.get(), chatter);
            return string == null ? s.string() : string;
        }).register();

        new IdentifiedUserReplaceable(this, MatcherUtils.makeLiteral("friends_with_") + "(.*?)", 1, (s, u) -> {
            SavableChatter chatter = ChatterManager.getOrGetChatter(u.getUuid());
            String string = startsWithFriendsWith(s.get(), chatter);
            return string == null ? s.string() : string;
        }).register();

        new IdentifiedUserReplaceable(this, "friend_invites_enabled", (s, u) -> {
            SavableChatter chatter = ChatterManager.getOrGetChatter(u.getUuid());

            return chatter.isAcceptingFriendRequests() ?
                    MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_TRUE.get() :
                    MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_FALSE.get();
        }).register();
    }

    public String startsWithFriendsWith(String s, SavableChatter chatter) {
        String uuid = ModuleUtils.getUUIDFromName(s).orElse(null);
        if (uuid == null) return "";
        SavableChatter otherChatter = ChatterManager.getOrGetChatter(uuid);
        return chatter.isMyBestFriend(otherChatter) ?
                MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_TRUE.get() :
                MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_FALSE.get();
    }

    public String startsWithChannel(String s, SavableChatter chatter) {
        ConfiguredChatChannel channel = chatter.getCurrentChatChannel();
        if (channel == null) return null;
        if (s.equals("identifier")) {
            return channel.getIdentifier();
        }
        if (s.equals("type")) {
            return channel.getType().toString();
        }
        if (s.equals("permission_access")) {
            return channel.getAccessPermission();
        }
        if (s.equals("permission_formatting")) {
            return channel.getFormattingPermission();
        }
        if (s.equals("message")) {
            return channel.getMessage();
        }
        return null;
    }
}
