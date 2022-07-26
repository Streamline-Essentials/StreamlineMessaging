package tv.quaint.configs;

import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.events.ChannelMessageEvent;
import tv.quaint.savables.ChatterManager;

public record ConfiguredChatChannel(String identifier, Type type, String prefix, String accessPermission, String formattingPermission,
                                    String message, ViewingInfo viewingInfo
) {
    public enum Type {
        ROOM,
        GLOBAL,
        LOCAL,
        ;
    }

    public void sendMessageAs(StreamlineUser user, String message) {
        if (ModuleUtils.isCommand(message)) ModuleUtils.runAs(user, message);
        if (identifier().equals("none")) {
            ModuleUtils.chatAs(user, message);
            return;
        }

        switch (type()) {
            case ROOM -> {
                ChatterManager.getChattersViewingChannel(this).forEach(a -> {
                    ModuleUtils.sendMessage(a.asUser(), user, message().replace("%this_message%", message));
                });
            }
            case LOCAL -> {
                ModuleUtils.getUsersOn(user.getLatestServer()).forEach(a -> {
                    ModuleUtils.sendMessage(a, user, message().replace("%this_message%", message));
                });
            }
            case GLOBAL -> {
                ModuleUtils.getLoadedUsersSet().forEach(a -> {
                    ModuleUtils.sendMessage(a, user, message().replace("%this_message%", message));
                });
            }
        }

        ModuleUtils.fireEvent(new ChannelMessageEvent(this, user, message));
    }
}
