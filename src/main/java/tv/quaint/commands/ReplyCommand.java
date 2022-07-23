package tv.quaint.commands;

import net.streamline.api.command.ModuleCommand;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.SavableUser;
import net.streamline.base.configs.MainMessagesHandler;
import tv.quaint.StreamlineMessaging;
import tv.quaint.savables.ChatterManager;
import tv.quaint.savables.SavableChatter;

import java.util.List;

public class ReplyCommand extends ModuleCommand {
    private String messageSender;
    private String messageRecipient;

    public ReplyCommand() {
        super(StreamlineMessaging.getInstance(),
                "reply",
                "streamline.command.reply",
                "re", "r"
        );

        messageSender = this.getCommandResource().getOrSetDefault("messages.success.sender",
                "&8[&dYOU &7(&e%streamline_user_server%&7) &9&l>> &d%streamline_parse_%this_other%:::*/*streamline_user_formatted*/*%&8] &7%this_message%");
        messageRecipient = this.getCommandResource().getOrSetDefault("messages.success.recipient",
                "&8[&d%streamline_user_formatted% &7(&e%streamline_user_server%&7) &9&l>> &dYOU&8] &7%this_message%");
    }

    @Override
    public void run(SavableUser savableUser, String[] strings) {
        if (strings.length < 1) {
            ModuleUtils.sendMessage(savableUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
            return;
        }
        String message = ModuleUtils.argsToString(strings);

        SavableChatter chatter = ChatterManager.getOrGetChatter(savableUser.uuid);
        SavableUser other = ModuleUtils.getOrGetUser(chatter.getReplyTo());
        if (other == null) {
            ModuleUtils.sendMessage(savableUser, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
            return;
        }
        chatter.onReply(other, message, messageSender, messageRecipient);
    }

    @Override
    public List<String> doTabComplete(SavableUser savableUser, String[] strings) {
        return ModuleUtils.getOnlinePlayerNames();
    }
}
