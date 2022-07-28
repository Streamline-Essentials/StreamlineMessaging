package tv.quaint.commands;

import net.streamline.api.command.ModuleCommand;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.modules.StreamlineModule;
import net.streamline.api.savables.users.SavableUser;
import net.streamline.base.configs.MainMessagesHandler;
import tv.quaint.StreamlineMessaging;
import tv.quaint.savables.ChatterManager;
import tv.quaint.savables.SavableChatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FriendCommand extends ModuleCommand {
    private String messageResultRequestSender;
    private String messageResultRequestOther;

    private String messageResultAcceptSender;
    private String messageResultAcceptOther;
    private String messageResultDenySender;
    private String messageResultDenyOther;
    private String messageResultList;

    public FriendCommand() {
        super(StreamlineMessaging.getInstance(),
                "friend",
                "streamline.command.friend.default",
                "f"
        );

        messageResultRequestSender = getCommandResource().getOrSetDefault("messages.result.request.sender", "&dYou &erequested %streamline_parse_%this_other%:::*/*streamline_user_formatted*/*% &eto be &dyour friend&8!");
        messageResultRequestOther = getCommandResource().getOrSetDefault("messages.result.request.other", "%streamline_user_formatted% &erequested to be &dyour friend&8! &eType &b/friend accept %streamline_user_absolute% &eto &aaccept&8!");
        messageResultAcceptSender = getCommandResource().getOrSetDefault("messages.result.accept.sender", "&dYou &aaccepted %streamline_parse_%this_other%:::*/*streamline_user_formatted*/*%&7'&es friend request&8!");
        messageResultAcceptOther = getCommandResource().getOrSetDefault("messages.result.accept.other", "%streamline_user_formatted% &aaccepted &dyour &efriend request&8!");
        messageResultDenySender = getCommandResource().getOrSetDefault("messages.result.deny.sender", "&dYou &cdenied %streamline_parse_%this_other%:::*/*streamline_user_formatted*/*%&7'&es friend request&8!");
        messageResultDenyOther = getCommandResource().getOrSetDefault("messages.result.deny.other", "%streamline_user_formatted% &cdenied &dyour &efriend request&8!");
        messageResultList = getCommandResource().getOrSetDefault("messages.result.list.sender", "&dYour &cfriends &7(&epage %this_page%&7)&8: %this_friends_list%");
    }

    @Override
    public void run(SavableUser savableUser, String[] strings) {
        String action;
        if (strings.length < 1) {
            action = "list";
        } else {
            action = strings[0];
        }

        SavableChatter chatter = ChatterManager.getOrGetChatter(savableUser.uuid);

        switch (action.toLowerCase(Locale.ROOT)) {
            case "list" -> {
                int page;
                if (strings.length < 2) {
                    page = 1;
                } else {
                    try {
                        page = Integer.parseInt(strings[1]);
                    } catch (Exception e) {
                        page = 1;
                        ModuleUtils.sendMessage(savableUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TYPE_NUMBER.get());
                    }
                }

                String p = chatter.getFriendsListPaged().get(page - 1);

                if (p == null) {
                    p = "";
                }

                ModuleUtils.sendMessage(savableUser, messageResultList
                        .replace("%this_page%", String.valueOf(page))
                        .replace("%this_friends_list%", p)
                );
            }
            case "add" -> {

            }
        }
    }

    @Override
    public List<String> doTabComplete(SavableUser savableUser, String[] strings) {
        if (strings.length <= 1) {
            return List.of(
                    "list",
                    "add",
                    "remove",
                    "accept",
                    "deny"
            );
        }
        if (strings.length == 2) {
            return ModuleUtils.getOnlinePlayerNames();
        }

        return new ArrayList<>();
    }
}
