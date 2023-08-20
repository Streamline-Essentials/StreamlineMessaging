package tv.quaint.commands;

import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.StreamlineMessaging;
import tv.quaint.configs.ConfiguredChatChannel;
import tv.quaint.savables.ChatterManager;
import tv.quaint.savables.SavableChatter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public class ChannelCommand extends ModuleCommand {
    private String messageResultSuccess;
    private String messageResultFailure;

    public ChannelCommand() {
        super(StreamlineMessaging.getInstance(),
                "channel",
                "streamline.command.channel",
                "chan", "chat", "chat-channel"
        );

        messageResultSuccess = this.getCommandResource().getOrSetDefault("messages.result",
                "&eYou set your &cchat channel &eto &7'&a%messaging_channel_identifier%&7'&8.");
        messageResultFailure = this.getCommandResource().getOrSetDefault("messages.result",
                "&eYou attempted to set your &cchat channel &eto &7'&a%this_identifier%&7'&8, &ebut was set to &7'&a%messaging_channel_identifier%&7' &edue to an &cerror&8.");
    }

    @Override
    public void run(StreamlineUser streamlineUser, String[] strings) {
        try {
            String identifier;
            if (strings[0].equals("")) {
                identifier = "none";
            } else if (strings.length > 1) {
                ModuleUtils.sendMessage(streamlineUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_MANY.get());
                return;
            } else {
                identifier = strings[0];
            }

            SavableChatter chatter = ChatterManager.getOrGetChatter(streamlineUser.getUuid());
            if (chatter == null) {
                ModuleUtils.sendMessage(streamlineUser, MainMessagesHandler.MESSAGES.INVALID.USER_SELF.get());
                return;
            }

            ConfiguredChatChannel channel = StreamlineMessaging.getChatChannelConfig().getChatChannel(identifier);
            if (channel == null) {
                ModuleUtils.sendMessage(streamlineUser, messageResultFailure
                        .replace("%this_identifier%", identifier));
                return;
            }

            chatter.setCurrentChatChannel(channel);

            if (chatter.getCurrentChatChannel().getIdentifier().equals(identifier)) {
                ModuleUtils.sendMessage(streamlineUser, messageResultSuccess
                        .replace("%this_identifier%", identifier)
                );
            } else {
                ModuleUtils.sendMessage(streamlineUser, messageResultFailure
                        .replace("%this_identifier%", identifier)
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            streamlineUser.sendMessage(MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TYPE_DEFAULT.get());
        }
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamlineUser StreamlineUser, String[] strings) {
        if (strings.length <= 1) {
            ConcurrentSkipListSet<String> r = new ConcurrentSkipListSet<>();

            StreamlineMessaging.getChatChannelConfig().getChatChannels().forEach((a, b) -> {
                if (ModuleUtils.hasPermission(StreamlineUser, b.getAccessPermission())) r.add(a);
            });

            return r;
        }

        return new ConcurrentSkipListSet<>();
    }
}
