package tv.quaint.configs;

import net.streamline.api.configs.ModularizedConfig;
import tv.quaint.StreamlineMessaging;

public class Messages extends ModularizedConfig {
    public Messages() {
        super(StreamlineMessaging.getInstance(), "messages.yml", true);
    }

    public String errorsMessagingSelf() {
        reloadResource();

        return resource.getString("errors.messaging.self");
    }

    public String errorsChannelIsNull() {
        reloadResource();

        return resource.getString("errors.channel.is-null");
    }

    public String errorsChannelNoAccess() {
        reloadResource();

        return resource.getString("errors.channel.no-access");
    }

    public String friendsAlreadyFriends() {
        reloadResource();

        return resource.getString("friends.already.friends");
    }

    public String friendsAlreadyNotFriends() {
        reloadResource();

        return resource.getString("friends.already.not-friends");
    }

    public String friendsAddMessage() {
        reloadResource();

        return resource.getString("friends.add");
    }

    public String friendsRemoveMessage() {
        reloadResource();

        return resource.getString("friends.remove");
    }

    public int friendsListMaxPerPage() {
        reloadResource();

        return resource.getInt("friends.list.max-per-page");
    }

    public String friendsListEntryNotLast() {
        reloadResource();

        return resource.getString("friends.list.entry.not-last");
    }

    public String friendsListEntryLast() {
        reloadResource();

        return resource.getString("friends.list.entry.last");
    }

    public String friendInviteTimeoutSender() {
        reloadResource();

        return resource.getString("friends.invite.timeout.sender");
    }

    public String friendInviteTimeoutInvited() {
        reloadResource();

        return resource.getString("friends.invite.timeout.invited");
    }
}
