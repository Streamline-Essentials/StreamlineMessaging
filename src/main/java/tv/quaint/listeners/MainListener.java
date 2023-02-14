package tv.quaint.listeners;

import net.streamline.api.events.server.LoginCompletedEvent;
import net.streamline.api.events.server.LoginEvent;
import net.streamline.api.events.server.StreamlineChatEvent;
import tv.quaint.StreamlineMessaging;
import tv.quaint.configs.ConfiguredChatChannel;
import tv.quaint.events.BaseEventListener;
import tv.quaint.events.processing.BaseEventPriority;
import tv.quaint.events.processing.BaseProcessor;
import tv.quaint.savables.ChatterManager;
import tv.quaint.savables.SavableChatter;

import java.util.concurrent.atomic.AtomicBoolean;

public class MainListener implements BaseEventListener {
    public MainListener() {
    }

    @BaseProcessor(priority = BaseEventPriority.LOWEST)
    public void onChat(StreamlineChatEvent event) {
        if (event.isCanceled()) return;

        AtomicBoolean handled = new AtomicBoolean(false);
        StreamlineMessaging.getChatChannelConfig().getChatChannels().forEach((s, chatChannel) -> {
            if (chatChannel.getIdentifier().equals("none")) return;
            if (handled.get()) return;
            if (chatChannel.getPrefix().equals("")) return;
            if (! event.getMessage().startsWith(chatChannel.getPrefix())) return;

            String message = event.getMessage().substring(chatChannel.getPrefix().length());

            chatChannel.sendMessageAs(event.getSender(), message);
            handled.set(true);
        });
        if (handled.get()) {
            event.setCanceled(true);
            return;
        }

        SavableChatter chatter = ChatterManager.getOrGetChatter(event.getSender().getUuid());
        event.setCanceled(chatter.onChannelMessage(event));
    }

    @BaseProcessor
    public void onJoin(LoginCompletedEvent event) {
        SavableChatter chatter = ChatterManager.getOrGetChatter(event.getResource().getUuid());
        ChatterManager.getChatterFromDatabase(chatter);

        if (StreamlineMessaging.getConfigs().forceDefaultOnJoin()) {
            ConfiguredChatChannel channel = StreamlineMessaging.getChatChannelConfig().getChatChannels().get(StreamlineMessaging.getConfigs().defaultChat());
            if (channel == null) return;
            chatter.setCurrentChatChannel(channel);
        }
    }
}
