package host.plas.commands;

import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import host.plas.StreamlineMessaging;
import host.plas.savables.SavableChatter;
import host.plas.savables.ChatterManager;

import java.util.concurrent.ConcurrentSkipListSet;

public class MessageCommand extends ModuleCommand {
    private String messageSender;
    private String messageRecipient;

    public MessageCommand() {
        super(StreamlineMessaging.getInstance(),
                "message",
                "streamline.command.message",
                "msg", "w", "tell", "whisper"
        );

        messageSender = this.getCommandResource().getOrSetDefault("messages.success.sender",
                "&8[&dYOU &7(&e%streamline_user_server%&7) &9&l>> &d%streamline_parse_%this_other%:::*/*streamline_user_formatted*/*%&8] &7%this_message%");
        messageRecipient = this.getCommandResource().getOrSetDefault("messages.success.recipient",
                "&8[&d%streamline_user_formatted% &7(&e%streamline_user_server%&7) &9&l>> &dYOU&8] &7%this_message%");
    }

    @Override
    public void run(StreamlineUser streamlineUser, String[] strings) {
        if (strings.length < 2) {
            ModuleUtils.sendMessage(streamlineUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
            return;
        }

        String username = strings[0];

        StreamlineUser other = ModuleUtils.getOrGetUserByName(username);
        if (other == null) {
            ModuleUtils.sendMessage(streamlineUser, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
            return;
        }

        String message = ModuleUtils.argsToStringMinus(strings, 0);

        SavableChatter chatter = ChatterManager.getOrGetChatter(streamlineUser.getUuid());
        chatter.onMessage(other, message, messageSender, messageRecipient);
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamlineUser StreamlineUser, String[] strings) {
        return ModuleUtils.getOnlinePlayerNames();
    }
}
