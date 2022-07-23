package tv.quaint.configs;

import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.SavableUser;
import tv.quaint.savables.ChatterManager;

import java.util.List;

public record ConfiguredChatChannel(String identifier, Type type, String accessPermission, String formattingPermission,
                                    String message
) {
    public enum Type {
        ROOM,
        GLOBAL,
        LOCAL,
        ;
    }

    public void sendMessageAs(SavableUser user, String message) {
        if (identifier().equals("none")) return;

        switch (type()) {
            case ROOM -> {
                ChatterManager.getChattersInChannel(this).forEach(a -> {
                    ModuleUtils.sendMessage(a.asUser(), user, message().replace("%this_message%", message));
                });
            }
            case LOCAL -> {
                ModuleUtils.getUsersOn(user.latestServer).forEach(a -> {
                    ModuleUtils.sendMessage(a, user, message().replace("%this_message%", message));
                });
            }
            case GLOBAL -> {
                ModuleUtils.getLoadedUsers().forEach(a -> {
                    if (! a.online) return;
                    ModuleUtils.sendMessage(a, user, message().replace("%this_message%", message));
                });
            }
        }
    }
}
