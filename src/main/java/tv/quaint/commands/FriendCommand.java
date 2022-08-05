package tv.quaint.commands;

import net.streamline.api.command.ModuleCommand;
import net.streamline.api.modules.ModuleUtils;
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
    private String messageResultRemoveSender;
    private String messageResultRemoveOther;
    private String messageResultListSelf;
    private String messageResultListOther;

    private String permissionListOthers;

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
        messageResultRemoveSender = getCommandResource().getOrSetDefault("messages.result.remove.sender", "&dYou &cremoved %streamline_parse_%this_other%:::*/*streamline_user_formatted*/*% &eas a friend&8!");
        messageResultRemoveOther = getCommandResource().getOrSetDefault("messages.result.remove.other", "%streamline_user_formatted% &cremoved &dyou &eas a friend&8!");
        messageResultListSelf = getCommandResource().getOrSetDefault("messages.result.list.sender", "&dYour &cfriends &7(&epage %this_page%&7)&8: %this_friends_list%");
        messageResultListOther = getCommandResource().getOrSetDefault("messages.result.list.other", "&d%streamline_parse_%this_other%:::*/*streamline_user_formatted*/*%&7'&es &cfriends &7(&epage %this_page%&7)&8: %this_friends_list%");

        permissionListOthers = getCommandResource().getOrSetDefault("basic.permissions.others", "streamline.command.friend.others");
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

                if (strings.length > 3) {
                    ModuleUtils.sendMessage(savableUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_MANY.get());
                    return;
                }

                if (strings.length == 3) {
                    if (! ModuleUtils.hasPermission(savableUser, permissionListOthers)) {
                        ModuleUtils.sendMessage(savableUser, MainMessagesHandler.MESSAGES.INVALID.PERMISSIONS.get());
                        return;
                    }

                    SavableUser other = ModuleUtils.getOrGetUserByName(strings[2]);
                    if (other == null) {
                        ModuleUtils.sendMessage(savableUser, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
                        return;
                    }

                    chatter = ChatterManager.getOrGetChatter(other);

                    String p = chatter.getFriendsListPaged().get(page - 1);

                    if (p == null) {
                        p = "";
                    }

                    ModuleUtils.sendMessage(savableUser, getWithOther(savableUser, messageResultListOther, other)
                            .replace("%this_page%", String.valueOf(page))
                            .replace("%this_friends_list%", p)
                    );
                } else {
                    String p = chatter.getFriendsListPaged().get(page - 1);

                    if (p == null) {
                        p = "";
                    }

                    ModuleUtils.sendMessage(savableUser, messageResultListSelf
                            .replace("%this_page%", String.valueOf(page))
                            .replace("%this_friends_list%", p)
                    );
                }
            }
            case "add", "accept" -> {
                if (strings.length < 2) {
                    ModuleUtils.sendMessage(savableUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
                    return;
                }

                String username = strings[1];
                SavableUser other = ModuleUtils.getOrGetUserByName(username);
                if (other == null) {
                    ModuleUtils.sendMessage(savableUser, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
                    return;
                }

                SavableChatter otherChatter = ChatterManager.getOrGetChatter(other.uuid);
                if (chatter.isMyFriend(otherChatter)) {
                    ModuleUtils.sendMessage(savableUser, StreamlineMessaging.getMessages().friendsAlreadyFriends());
                    return;
                }

                if (otherChatter.isAlreadyFriendInvited(chatter)) {
                    chatter.addFriend(otherChatter);
                    otherChatter.addFriend(chatter);

                    ModuleUtils.sendMessage(savableUser, getWithOther(savableUser, messageResultAcceptSender, other));
                    ModuleUtils.sendMessage(other, getWithOther(savableUser, messageResultAcceptOther, other));
                } else {
                    if (chatter.isAlreadyFriendInvited(otherChatter)) {
                        ModuleUtils.sendMessage(savableUser, StreamlineMessaging.getMessages().friendsAlreadyInvited());
                        return;
                    }
                    chatter.addInviteTo(otherChatter);

                    ModuleUtils.sendMessage(savableUser, getWithOther(savableUser, messageResultRequestSender, other));
                    ModuleUtils.sendMessage(other, getWithOther(savableUser, messageResultRequestOther, other));
                }
            }
            case "remove", "deny" -> {
                if (strings.length < 2) {
                    ModuleUtils.sendMessage(savableUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
                    return;
                }

                String username = strings[1];
                SavableUser other = ModuleUtils.getOrGetUserByName(username);
                if (other == null) {
                    ModuleUtils.sendMessage(savableUser, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
                    return;
                }

                SavableChatter otherChatter = ChatterManager.getOrGetChatter(other.uuid);

                if (chatter.isAlreadyFriendInvited(other.uuid)) {
                    if (chatter.isMyFriend(otherChatter)) {
                        ModuleUtils.sendMessage(savableUser, StreamlineMessaging.getMessages().friendsAlreadyFriends());
                        return;
                    }

                    chatter.removeInviteTo(otherChatter);
                    otherChatter.removeInviteTo(chatter);
                    chatter.addFriend(otherChatter);
                    otherChatter.addFriend(chatter);

                    ModuleUtils.sendMessage(savableUser, getWithOther(savableUser, messageResultDenySender, other));
                    ModuleUtils.sendMessage(other, getWithOther(savableUser, messageResultDenyOther, other));
                } else {
                    if (! chatter.isMyFriend(otherChatter)) {
                        ModuleUtils.sendMessage(savableUser, StreamlineMessaging.getMessages().friendsAlreadyNotFriends());
                        return;
                    }

                    chatter.removeInviteTo(otherChatter);
                    otherChatter.removeInviteTo(chatter);
                    chatter.removeFriend(otherChatter);
                    otherChatter.removeFriend(chatter);

                    ModuleUtils.sendMessage(savableUser, getWithOther(savableUser, messageResultRemoveSender, other));
                    ModuleUtils.sendMessage(other, getWithOther(savableUser, messageResultRemoveOther, other));
                }
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
            if (ModuleUtils.equalsAny(strings[0], List.of("add", "remove", "accept", "deny"))) {
                return ModuleUtils.getOnlinePlayerNames();
            }
            if (ModuleUtils.equalsAny(strings[0], List.of("list"))) {
                return List.of("1", "2", "3");
            }
        }

        if (strings.length == 3) {
            if (ModuleUtils.equalsAny(strings[0], List.of("list"))) {
                return ModuleUtils.getOnlinePlayerNames();
            }
        }

        return new ArrayList<>();
    }
}
