package tv.quaint.ratapi;

import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.objects.AtomicString;
import net.streamline.api.placeholders.expansions.RATExpansion;
import net.streamline.api.placeholders.replaceables.IdentifiedReplaceable;
import net.streamline.api.placeholders.replaceables.IdentifiedUserReplaceable;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.StreamlineMessaging;
import tv.quaint.configs.ConfiguredChatChannel;
import tv.quaint.savables.ChatterManager;
import tv.quaint.savables.SavableChatter;
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

        new IdentifiedUserReplaceable(this, MatcherUtils.makeLiteral("channel_") + "(.*?)", (s, u) -> {
            SavableChatter chatter = ChatterManager.getOrGetChatter(u.getUuid());
            AtomicString string = new AtomicString(s.string());
            s.handledString().isolateIn(s.string()).forEach(str -> {
                string.set(startsWithChannel(str, chatter));
            });
            return string.get() == null ? s.string() : string.get();
        }).register();

        new IdentifiedUserReplaceable(this, MatcherUtils.makeLiteral("friends_with_") + "(.*?)", (s, u) -> {
            SavableChatter chatter = ChatterManager.getOrGetChatter(u.getUuid());
            AtomicString string = new AtomicString(s.string());
            s.handledString().isolateIn(s.string()).forEach(str -> {
                string.set(startsWithFriendsWith(str, chatter));
            });
            return string.get() == null ? s.string() : string.get();
        }).register();

        new IdentifiedUserReplaceable(this, "friend_invites_enabled", (s, u) -> {
            SavableChatter chatter = ChatterManager.getOrGetChatter(u.getUuid());

            return chatter.isAcceptingFriendRequests() ?
                    MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_TRUE.get() :
                    MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_FALSE.get();
        }).register();
    }

    public String startsWithFriendsWith(String s, SavableChatter chatter) {
        SavableChatter otherChatter = ChatterManager.getOrGetChatter(ModuleUtils.getUUIDFromName(s));
        return chatter.isMyBestFriend(otherChatter) ?
                MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_TRUE.get() :
                MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_FALSE.get();
    }

    public String startsWithChannel(String s, SavableChatter chatter) {
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
        return null;
    }
}
