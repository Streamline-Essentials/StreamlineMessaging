package tv.quaint.configs;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.command.CommandHandler;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import org.jetbrains.annotations.NotNull;
import tv.quaint.StreamlineMessaging;
import tv.quaint.events.ChannelMessageEvent;
import tv.quaint.savables.ChatterManager;
import tv.quaint.utils.StringUtils;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public class ConfiguredChatChannel implements Comparable<ConfiguredChatChannel> {
    @Override
    public int compareTo(@NotNull ConfiguredChatChannel o) {
        return getIdentifier().compareTo(o.getIdentifier());
    }

    public enum Type {
        ROOM,
        GLOBAL,
        LOCAL,
        ;
    }

    @Getter @Setter
    private String identifier;
    @Getter @Setter
    private Type type;
    @Getter @Setter
    private String prefix;
    @Getter @Setter
    private String accessPermission;
    @Getter @Setter
    private String formattingPermission;
    @Getter @Setter
    private String message;
    @Getter @Setter
    private ViewingInfo viewingInfo;
    @Getter @Setter
    private String commandBase;

    public ConfiguredChatChannel(String identifier, Type type, String prefix, String accessPermission, String formattingPermission, String message, ViewingInfo viewingInfo, String commandBase) {
        this.identifier = identifier;
        this.type = type;
        this.prefix = prefix;
        this.accessPermission = accessPermission;
        this.formattingPermission = formattingPermission;
        this.message = message;
        this.viewingInfo = viewingInfo;
        this.commandBase = commandBase;
    }

    public void sendMessageAs(StreamlineUser user, String message) {
        if (ModuleUtils.isCommand(message)) ModuleUtils.runAs(user, message);
        if (getIdentifier().equals("none")) {
            ModuleUtils.chatAs(user, message);
            return;
        }
        if (! ModuleUtils.hasPermission(user, getAccessPermission())) return;

        String realMessage = message;
        if (! user.hasPermission(getFormattingPermission()))
            realMessage = ModuleUtils.stripColor(message);
        else realMessage = message;

        String correctlyFormattedMessage = getMessage().replace("%this_message%", realMessage);

        switch (getType()) {
            case ROOM:
                ChatterManager.getChattersViewingChannel(this).forEach(a -> {
                    ModuleUtils.sendMessage(a.asUser(), user, correctlyFormattedMessage);
                });
                break;
            case LOCAL:
                ModuleUtils.getUsersOn(user.getLatestServer()).forEach(a -> {
                    ModuleUtils.sendMessage(a, user, correctlyFormattedMessage);
                });
                break;
            case GLOBAL:
                ModuleUtils.getLoadedUsersSet().forEach(a -> {
                    ModuleUtils.sendMessage(a, user, correctlyFormattedMessage);
                });
                break;
        }

        ModuleUtils.fireEvent(new ChannelMessageEvent(this, user, message));
    }

    public void setupCommand() {
        if (getIdentifier().equals("none")) return;

        ChatChannelCommandHelper commandHelper = new ChatChannelCommandHelper(this);

        CommandHandler.unregisterModuleCommand(commandHelper);
        CommandHandler.registerModuleCommand(commandHelper);

        StreamlineMessaging.getInstance().logInfo("Registered command for channel: " + getIdentifier());
    }

    public static class ChatChannelCommandHelper extends ModuleCommand {
        @Getter @Setter
        private ConfiguredChatChannel channel;

        public ChatChannelCommandHelper(ConfiguredChatChannel channel) {
            super(StreamlineMessaging.getInstance(), channel.getCommandBase(), channel.getAccessPermission());

            setChannel(channel);
        }

        @Override
        public void run(StreamlineUser streamlineUser, String[] strings) {
            if (strings.length == 0) {
                ModuleUtils.sendMessage(streamlineUser, "Usage: /" + channel.getCommandBase() + " <message>");
                return;
            }

            channel.sendMessageAs(streamlineUser, StringUtils.argsToString(strings));
        }

        @Override
        public ConcurrentSkipListSet<String> doTabComplete(StreamlineUser streamlineUser, String[] strings) {
            return new ConcurrentSkipListSet<>(List.of("<message>"));
        }
    }
}
