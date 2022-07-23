package tv.quaint.commands;

import net.streamline.api.command.ModuleCommand;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.SavableUser;
import net.streamline.base.configs.MainMessagesHandler;
import tv.quaint.StreamlineMessaging;
import tv.quaint.configs.ConfiguredChatChannel;
import tv.quaint.savables.ChatterManager;
import tv.quaint.savables.SavableChatter;

import java.util.List;

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
    public void run(SavableUser savableUser, String[] strings) {
        String identifier;
        if (strings.length < 1) {
            identifier = "none";
        } else if (strings.length > 1) {
            ModuleUtils.sendMessage(savableUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_MANY.get());
            return;
        } else {
            identifier = strings[0];
        }

        SavableChatter chatter = ChatterManager.getOrGetChatter(savableUser.uuid);

        ConfiguredChatChannel channel = StreamlineMessaging.getChatChannelConfig().getChatChannels().get(identifier);
        chatter.setCurrentChatChannel(channel);

        if (chatter.getCurrentChatChannel().identifier().equals(identifier)) {
            ModuleUtils.sendMessage(savableUser, messageResultSuccess
                    .replace("%this_identifier%", identifier)
            );
        } else {
            ModuleUtils.sendMessage(savableUser, messageResultFailure
                    .replace("%this_identifier%", identifier)
            );
        }
    }

    @Override
    public List<String> doTabComplete(SavableUser savableUser, String[] strings) {
        return ModuleUtils.getOnlinePlayerNames();
    }
}