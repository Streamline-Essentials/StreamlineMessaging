package tv.quaint.configs;

import net.streamline.api.configs.ModularizedConfig;
import tv.quaint.StreamlineMessaging;

import java.util.TreeMap;

public class ChatChannelConfig extends ModularizedConfig {
    public ChatChannelConfig() {
        super(StreamlineMessaging.getInstance(), "chat-channels.yml", true);
    }

    @Override
    public void init() {

    }

    public TreeMap<String, ConfiguredChatChannel> getChatChannels() {
        reloadResource();

        TreeMap<String, ConfiguredChatChannel> r = new TreeMap<>();

        getResource().singleLayerKeySet().forEach(a -> {
            try {
                ConfiguredChatChannel.Type t = ConfiguredChatChannel.Type.valueOf(getResource().getOrSetDefault(a + ".type", ConfiguredChatChannel.Type.ROOM.toString()));
                String prefix = getResource().getOrSetDefault(a + ".prefix", "");
                String accessPermission = getResource().getOrSetDefault(a + ".permissions.access", "streamline.messaging.chats." + a + ".access");
                String formattingPermission = getResource().getOrSetDefault(a + ".permissions.formatting", "streamline.messaging.chats." + a + ".formatting");
                String message = getResource().getOrSetDefault(a + ".message", "%this_message%");

                String viewBasePermission = getResource().getOrSetDefault(a + ".view.permission", "streamline.messaging.chats." + a + ".view");
                String viewTogglePermission = getResource().getOrSetDefault(a + ".view.toggle.permission", "streamline.messaging.chats." + a + ".toggle");
                ViewingInfo viewingInfo = new ViewingInfo(viewBasePermission, viewTogglePermission);

                ConfiguredChatChannel channel = new ConfiguredChatChannel(a, t, prefix, accessPermission, formattingPermission, message, viewingInfo);
                r.put(channel.identifier(), channel);
            } catch (Exception e) {
                StreamlineMessaging.getInstance().logWarning("Could not load chat channel with identifier '" + a + "' due to: " + e.getMessage());
            }
        });

        r.remove("none");
        r.put("none", new ConfiguredChatChannel("none", ConfiguredChatChannel.Type.LOCAL, "-", "", "", "", new ViewingInfo("", "")));

        return r;
    }
}
