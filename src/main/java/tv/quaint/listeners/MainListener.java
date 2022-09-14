package tv.quaint.listeners;

import net.streamline.api.events.EventPriority;
import net.streamline.api.events.EventProcessor;
import net.streamline.api.events.StreamlineListener;
import net.streamline.api.events.server.LoginEvent;
import net.streamline.api.events.server.StreamlineChatEvent;
import tv.quaint.StreamlineMessaging;
import tv.quaint.configs.ConfiguredChatChannel;
import tv.quaint.savables.ChatterManager;
import tv.quaint.savables.SavableChatter;

public class MainListener implements StreamlineListener {
    @EventProcessor(priority = EventPriority.LOWEST)
    public void onChat(StreamlineChatEvent event) {
        if (event.isCanceled()) return;

        SavableChatter chatter = ChatterManager.getOrGetChatter(event.getSender().getUuid());
        event.setCanceled(chatter.onChannelMessage(event));
    }

    @EventProcessor
    public void onJoin(LoginEvent event) {
        SavableChatter chatter = ChatterManager.getOrGetChatter(event.getResource().getUuid());

        if (StreamlineMessaging.getConfigs().forceDefaultOnJoin()) {
            ConfiguredChatChannel channel = StreamlineMessaging.getChatChannelConfig().getChatChannels().get(StreamlineMessaging.getConfigs().defaultChat());
            if (channel == null) return;
            chatter.setCurrentChatChannel(channel);
        }
    }
}
