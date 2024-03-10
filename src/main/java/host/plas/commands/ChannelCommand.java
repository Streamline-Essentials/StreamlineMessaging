package host.plas.commands;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.data.console.StreamSender;
import host.plas.StreamlineMessaging;
import host.plas.configs.ConfiguredChatChannel;
import host.plas.savables.ChatterManager;
import host.plas.savables.SavableChatter;

import java.util.concurrent.ConcurrentSkipListSet;

@Getter @Setter
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
    public void run(StreamSender StreamSender, String[] strings) {
        try {
            String identifier;
            if (strings[0].equals("")) {
                identifier = "none";
            } else if (strings.length > 1) {
                ModuleUtils.sendMessage(StreamSender, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_MANY.get());
                return;
            } else {
                identifier = strings[0];
            }

            SavableChatter chatter = ChatterManager.getOrGetChatter(StreamSender.getUuid());
            if (chatter == null) {
                ModuleUtils.sendMessage(StreamSender, MainMessagesHandler.MESSAGES.INVALID.USER_SELF.get());
                return;
            }

            ConfiguredChatChannel channel = StreamlineMessaging.getChatChannelConfig().getChatChannel(identifier);
            if (channel == null) {
                ModuleUtils.sendMessage(StreamSender, messageResultFailure
                        .replace("%this_identifier%", identifier));
                return;
            }

            chatter.setCurrentChatChannel(channel);

            if (chatter.getCurrentChatChannel().getIdentifier().equals(identifier)) {
                ModuleUtils.sendMessage(StreamSender, messageResultSuccess
                        .replace("%this_identifier%", identifier)
                );
            } else {
                ModuleUtils.sendMessage(StreamSender, messageResultFailure
                        .replace("%this_identifier%", identifier)
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            StreamSender.sendMessage(MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TYPE_DEFAULT.get());
        }
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamSender StreamSender, String[] strings) {
        if (strings.length <= 1) {
            ConcurrentSkipListSet<String> r = new ConcurrentSkipListSet<>();

            StreamlineMessaging.getChatChannelConfig().getChatChannels().forEach((a, b) -> {
                if (ModuleUtils.hasPermission(StreamSender, b.getAccessPermission())) r.add(a);
            });

            return r;
        }

        return new ConcurrentSkipListSet<>();
    }
}
