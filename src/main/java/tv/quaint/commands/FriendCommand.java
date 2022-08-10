package tv.quaint.commands;

import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.StreamlineMessaging;
import tv.quaint.savables.ChatterManager;
import tv.quaint.savables.SavableChatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
    private String messageResultTeleportSender;
    private String messageResultTeleportOther;
    private String messageResultBestCheck;
    private String messageResultToggle;

    private String messageErrorOfflineOther;

    private String permissionListOthers;
    private String permissionTeleport;

    public FriendCommand() {
        super(StreamlineMessaging.getInstance(),
                "friend",
                "streamline.command.friend.default",
                "f"
        );

        messageResultRequestSender = getCommandResource().getOrSetDefault("messages.result.request.sender", "&dYou &erequested %streamline_parse_%this_other%:::*/*streamline_user_formatted*/*% &eto be &dyour friend&8!");
        messageResultRequestOther = getCommandResource().getOrSetDefault("messages.result.request.other", "%streamline_user_formatted% &erequested to be &dyour friend&8! &eType &b/friend accept %streamline_user_absolute% &eto &aaccept&8!");

        messageResultAcceptSender = getCommandResource().getOrSetDefault("messages.result.accept.sender", "&dYou &aaccepted %streamline_parse_%this_other%:::*/*streamline_user_formatted*/*%&7'&es friend request&8!");
        messageResultAcceptOther = getCommandResource().getOrSetDefault("messages.result.accept.other", "%streamline_parse_%this_sender%:::*/*streamline_user_formatted*/*% &aaccepted &dyour &efriend request&8!");

        messageResultDenySender = getCommandResource().getOrSetDefault("messages.result.deny.sender", "&dYou &cdenied %streamline_parse_%this_other%:::*/*streamline_user_formatted*/*%&7'&es friend request&8!");
        messageResultDenyOther = getCommandResource().getOrSetDefault("messages.result.deny.other", "%streamline_parse_%this_sender%:::*/*streamline_user_formatted*/*% &cdenied &dyour &efriend request&8!");

        messageResultRemoveSender = getCommandResource().getOrSetDefault("messages.result.remove.sender", "&dYou &cremoved %streamline_parse_%this_other%:::*/*streamline_user_formatted*/*% &eas a friend&8!");
        messageResultRemoveOther = getCommandResource().getOrSetDefault("messages.result.remove.other", "%streamline_parse_%this_sender%:::*/*streamline_user_formatted*/*% &cremoved &dyou &eas a friend&8!");

        messageResultListSelf = getCommandResource().getOrSetDefault("messages.result.list.sender", "&dYour &cfriends &7(&epage %this_page%&7)&8:%newline%%this_friends_list%");
        messageResultListOther = getCommandResource().getOrSetDefault("messages.result.list.other", "&d%streamline_parse_%this_other%:::*/*streamline_user_formatted*/*%&7'&es &cfriends &7(&epage %this_page%&7)&8:%newline%%this_friends_list%");

        messageResultTeleportSender = getCommandResource().getOrSetDefault("messages.result.teleport.sender", "&dYou &eteleported to &d%streamline_parse_%this_other%:::*/*streamline_user_formatted*/*%&8!");
        messageResultTeleportOther = getCommandResource().getOrSetDefault("messages.result.teleport.other", "&d%streamline_parse_%this_other%:::*/*streamline_user_formatted*/*% &eteleported to &dyou&8!");

        messageResultBestCheck = getCommandResource().getOrSetDefault("messages.result.best.check", "&eIs %streamline_parse_%this_other%:::*/*streamline_user_formatted*/*% &dyour &cbest friend&8: %messaging_friends_with_%this_other%%");

        messageResultToggle = getCommandResource().getOrSetDefault("messages.result.toggle", "&eAccepting friend invites&8: %messaging_friend_invites_enabled%");

        messageErrorOfflineOther = getCommandResource().getOrSetDefault("messages.errors.player.offline", "&cThat player is offline!");

        permissionListOthers = getCommandResource().getOrSetDefault("basic.permissions.others", "streamline.command.friend.others");
        permissionTeleport = getCommandResource().getOrSetDefault("basic.permissions.teleport.others", "streamline.command.friend.teleport.others");
    }

    @Override
    public void run(StreamlineUser StreamlineUser, String[] strings) {
        String action;
        if (strings.length < 1) {
            action = "list";
        } else {
            action = strings[0];
        }

        SavableChatter chatter = ChatterManager.getOrGetChatter(StreamlineUser.getUUID());

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
                        ModuleUtils.sendMessage(StreamlineUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TYPE_NUMBER.get());
                    }
                }

                if (strings.length > 3) {
                    ModuleUtils.sendMessage(StreamlineUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_MANY.get());
                    return;
                }

                if (strings.length == 3) {
                    if (! ModuleUtils.hasPermission(StreamlineUser, permissionListOthers)) {
                        ModuleUtils.sendMessage(StreamlineUser, MainMessagesHandler.MESSAGES.INVALID.PERMISSIONS.get());
                        return;
                    }

                    StreamlineUser other = ModuleUtils.getOrGetUserByName(strings[2]);
                    if (other == null) {
                        ModuleUtils.sendMessage(StreamlineUser, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
                        return;
                    }

                    chatter = ChatterManager.getOrGetChatter(other);

                    String p = chatter.getFriendsListPaged().get(page - 1);

                    if (p == null) {
                        p = "";
                    }

                    ModuleUtils.sendMessage(StreamlineUser, getWithOther(StreamlineUser, messageResultListOther, other)
                            .replace("%this_page%", String.valueOf(page))
                            .replace("%this_friends_list%", p)
                    );
                } else {
                    String p = chatter.getFriendsListPaged().get(page - 1);

                    if (p == null) {
                        p = "";
                    }

                    ModuleUtils.sendMessage(StreamlineUser, messageResultListSelf
                            .replace("%this_page%", String.valueOf(page))
                            .replace("%this_friends_list%", p)
                    );
                }
            }
            case "add", "accept" -> {
                if (strings.length < 2) {
                    ModuleUtils.sendMessage(StreamlineUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
                    return;
                }

                String username = strings[1];
                if (Objects.equals(username, StreamlineUser.getLatestName())) {
                    ModuleUtils.sendMessage(StreamlineUser, StreamlineMessaging.getMessages().errorsFriendSelfInvite());
                    return;
                }

                StreamlineUser other = ModuleUtils.getOrGetUserByName(username);
                if (other == null) {
                    ModuleUtils.sendMessage(StreamlineUser, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
                    return;
                }

                SavableChatter otherChatter = ChatterManager.getOrGetChatter(other.getUUID());
                if (chatter.isMyFriend(otherChatter)) {
                    ModuleUtils.sendMessage(StreamlineUser, StreamlineMessaging.getMessages().friendsAlreadyFriends());
                    return;
                }

                if (otherChatter.isAlreadyFriendInvited(chatter)) {
                    chatter.addFriend(otherChatter);
                    otherChatter.addFriend(chatter);

                    ModuleUtils.sendMessage(StreamlineUser, getWithOther(StreamlineUser, messageResultAcceptSender, other));
                    ModuleUtils.sendMessage(other, getWithOther(StreamlineUser, messageResultAcceptOther, other));
                } else {
                    if (chatter.isAlreadyFriendInvited(otherChatter)) {
                        ModuleUtils.sendMessage(StreamlineUser, StreamlineMessaging.getMessages().friendsAlreadyInvited());
                        return;
                    }
                    if (! otherChatter.isAcceptingFriendRequests()) {
                        ModuleUtils.sendMessage(StreamlineUser, StreamlineMessaging.getMessages().friendsToggledOffInvites());
                        return;
                    }
                    chatter.addInviteTo(otherChatter);

                    ModuleUtils.sendMessage(StreamlineUser, getWithOther(StreamlineUser, messageResultRequestSender, other));
                    ModuleUtils.sendMessage(other, getWithOther(StreamlineUser, messageResultRequestOther, other));
                }
            }
            case "remove", "deny" -> {
                if (strings.length < 2) {
                    ModuleUtils.sendMessage(StreamlineUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
                    return;
                }

                String username = strings[1];
                if (Objects.equals(username, StreamlineUser.getLatestName())) {
                    ModuleUtils.sendMessage(StreamlineUser, StreamlineMessaging.getMessages().errorsFriendSelfInvite());
                    return;
                }

                StreamlineUser other = ModuleUtils.getOrGetUserByName(username);
                if (other == null) {
                    ModuleUtils.sendMessage(StreamlineUser, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
                    return;
                }

                SavableChatter otherChatter = ChatterManager.getOrGetChatter(other.getUUID());

                if (chatter.isAlreadyFriendInvited(other.getUUID())) {
                    if (chatter.isMyFriend(otherChatter)) {
                        ModuleUtils.sendMessage(StreamlineUser, StreamlineMessaging.getMessages().friendsAlreadyFriends());
                        return;
                    }

                    chatter.removeInviteTo(otherChatter);
                    otherChatter.removeInviteTo(chatter);

                    ModuleUtils.sendMessage(StreamlineUser, getWithOther(StreamlineUser, messageResultDenySender, other));
                    ModuleUtils.sendMessage(other, getWithOther(StreamlineUser, messageResultDenyOther, other));
                } else {
                    if (! chatter.isMyFriend(otherChatter)) {
                        ModuleUtils.sendMessage(StreamlineUser, StreamlineMessaging.getMessages().friendsAlreadyNotFriends());
                        return;
                    }

                    chatter.removeInviteTo(otherChatter);
                    otherChatter.removeInviteTo(chatter);
                    chatter.removeFriend(otherChatter);
                    otherChatter.removeFriend(chatter);

                    ModuleUtils.sendMessage(StreamlineUser, getWithOther(StreamlineUser, messageResultRemoveSender, other));
                    ModuleUtils.sendMessage(other, getWithOther(StreamlineUser, messageResultRemoveOther, other));
                }
            }
            case "teleport" -> {
                if (strings.length < 2) {
                    ModuleUtils.sendMessage(StreamlineUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
                    return;
                }

                String username = strings[1];
                StreamlineUser other = ModuleUtils.getOrGetUserByName(username);
                if (other == null) {
                    ModuleUtils.sendMessage(StreamlineUser, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
                    return;
                }

                if (! other.isOnline()) {
                    ModuleUtils.sendMessage(StreamlineUser, getWithOther(StreamlineUser, messageErrorOfflineOther, other));
                    return;
                }

                ModuleUtils.connect(StreamlineUser, other.getLatestName());
                ModuleUtils.sendMessage(StreamlineUser, getWithOther(StreamlineUser, messageResultTeleportSender, other));
                ModuleUtils.sendMessage(other, getWithOther(StreamlineUser, messageResultTeleportOther, other));
            }
            case "best" -> {
                if (strings.length < 2) {
                    ModuleUtils.sendMessage(StreamlineUser, MainMessagesHandler.MESSAGES.INVALID.ARGUMENTS_TOO_FEW.get());
                    return;
                }

                String username = strings[1];
                StreamlineUser other = ModuleUtils.getOrGetUserByName(username);
                if (other == null) {
                    ModuleUtils.sendMessage(StreamlineUser, MainMessagesHandler.MESSAGES.INVALID.USER_OTHER.get());
                    return;
                }

                SavableChatter otherChatter = ChatterManager.getOrGetChatter(other.getUUID());
                if (chatter.isMyFriend(otherChatter)) {
                    ModuleUtils.sendMessage(StreamlineUser, StreamlineMessaging.getMessages().friendsAlreadyFriends());
                    return;
                }

                if (strings.length < 3) {
                    ModuleUtils.sendMessage(StreamlineUser, getWithOther(StreamlineUser, messageResultBestCheck, other));
                    return;
                }

                switch (strings[2]) {
                    case "check" -> {
                        ModuleUtils.sendMessage(StreamlineUser, getWithOther(StreamlineUser, messageResultBestCheck, other));
                    }
                    case "add" -> {
                        if (chatter.isMyBestFriend(otherChatter)) {
                            ModuleUtils.sendMessage(StreamlineUser, StreamlineMessaging.getMessages().friendsAlreadyBestFriends());
                            return;
                        }

                        chatter.addBestFriend(otherChatter);
                    }
                    case "remove" -> {
                        if (! chatter.isMyBestFriend(otherChatter)) {
                            ModuleUtils.sendMessage(StreamlineUser, StreamlineMessaging.getMessages().friendsAlreadyNotBestFriends());
                            return;
                        }

                        chatter.removeBestFriend(otherChatter);
                    }
                }

                if (otherChatter.isAlreadyFriendInvited(chatter)) {
                    chatter.addFriend(otherChatter);
                    otherChatter.addFriend(chatter);

                    ModuleUtils.sendMessage(StreamlineUser, getWithOther(StreamlineUser, messageResultAcceptSender, other));
                    ModuleUtils.sendMessage(other, getWithOther(StreamlineUser, messageResultAcceptOther, other));
                } else {
                    if (chatter.isAlreadyFriendInvited(otherChatter)) {
                        ModuleUtils.sendMessage(StreamlineUser, StreamlineMessaging.getMessages().friendsAlreadyInvited());
                        return;
                    }
                    chatter.addInviteTo(otherChatter);

                    ModuleUtils.sendMessage(StreamlineUser, getWithOther(StreamlineUser, messageResultRequestSender, other));
                    ModuleUtils.sendMessage(other, getWithOther(StreamlineUser, messageResultRequestOther, other));
                }
            }
            case "toggle" -> {
                chatter.setAcceptingFriendRequests(! chatter.isAcceptingFriendRequests());
                ModuleUtils.sendMessage(StreamlineUser, messageResultToggle);
            }
        }
    }

    @Override
    public List<String> doTabComplete(StreamlineUser StreamlineUser, String[] strings) {
        if (strings.length <= 1) {
            return List.of(
                    "list",
                    "add",
                    "remove",
                    "accept",
                    "deny",
                    "teleport",
                    "best",
                    "toggle"
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
            if (ModuleUtils.equalsAny(strings[0], List.of("best"))) {
                return List.of("remove", "add", "toggle", "check");
            }
        }

        return new ArrayList<>();
    }
}
