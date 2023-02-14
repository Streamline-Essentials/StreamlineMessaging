package tv.quaint.configs;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import org.jetbrains.annotations.NotNull;
import tv.quaint.events.ChannelMessageEvent;
import tv.quaint.savables.ChatterManager;

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

    public ConfiguredChatChannel(String identifier, Type type, String prefix, String accessPermission, String formattingPermission, String message, ViewingInfo viewingInfo) {
        this.identifier = identifier;
        this.type = type;
        this.prefix = prefix;
        this.accessPermission = accessPermission;
        this.formattingPermission = formattingPermission;
        this.message = message;
        this.viewingInfo = viewingInfo;
    }

    public void sendMessageAs(StreamlineUser user, String message) {
        if (ModuleUtils.isCommand(message)) ModuleUtils.runAs(user, message);
        if (getIdentifier().equals("none")) {
            ModuleUtils.chatAs(user, message);
            return;
        }

        switch (getType()) {
            case ROOM:
                ChatterManager.getChattersViewingChannel(this).forEach(a -> {
                    ModuleUtils.sendMessage(a.asUser(), user, getMessage().replace("%this_message%", message));
                });
                break;
            case LOCAL:
                ModuleUtils.getUsersOn(user.getLatestServer()).forEach(a -> {
                    ModuleUtils.sendMessage(a, user, getMessage().replace("%this_message%", message));
                });
                break;
            case GLOBAL:
                ModuleUtils.getLoadedUsersSet().forEach(a -> {
                    ModuleUtils.sendMessage(a, user, getMessage().replace("%this_message%", message));
                });
                break;
        }

        ModuleUtils.fireEvent(new ChannelMessageEvent(this, user, message));
    }
}
