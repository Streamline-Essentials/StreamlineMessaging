package host.plas.events;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.events.modules.ModuleEvent;
import net.streamline.api.savables.users.StreamlineUser;
import host.plas.configs.ConfiguredChatChannel;
import host.plas.StreamlineMessaging;

public class ChannelMessageEvent extends ModuleEvent {
    @Getter @Setter
    private ConfiguredChatChannel chatChannel;
    @Getter @Setter
    private StreamlineUser sender;
    @Getter @Setter
    private String message;

    public ChannelMessageEvent(ConfiguredChatChannel chatChannel, StreamlineUser sender, String message) {
        super(StreamlineMessaging.getInstance());
        setChatChannel(chatChannel);
        setSender(sender);
        setMessage(message);
    }
}
