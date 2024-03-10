package host.plas.events;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.events.modules.ModuleEvent;
import net.streamline.api.data.console.StreamSender;
import host.plas.configs.ConfiguredChatChannel;
import host.plas.StreamlineMessaging;

@Setter
@Getter
public class ChannelMessageEvent extends ModuleEvent {
    private ConfiguredChatChannel chatChannel;
    private StreamSender sender;
    private String message;

    public ChannelMessageEvent(ConfiguredChatChannel chatChannel, StreamSender sender, String message) {
        super(StreamlineMessaging.getInstance());
        setChatChannel(chatChannel);
        setSender(sender);
        setMessage(message);
    }
}
