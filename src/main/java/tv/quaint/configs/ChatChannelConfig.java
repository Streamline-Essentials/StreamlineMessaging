package tv.quaint.configs;

import net.streamline.api.configs.ModularizedConfig;
import tv.quaint.StreamlineMessaging;

import java.util.TreeMap;

public class ChatChannelConfig extends ModularizedConfig {
    public ChatChannelConfig() {
        super(StreamlineMessaging.getInstance(), "chat-channels.yml", true);
    }

    public TreeMap<String, ConfiguredChatChannel> getChatChannels() {
        reloadResource();

        TreeMap<String, ConfiguredChatChannel> r = new TreeMap<>();

        resource.singleLayerKeySet().forEach(a -> {
            try {
                ConfiguredChatChannel.Type t = resource.getEnum(a + ".type", ConfiguredChatChannel.Type.class);
                String accessPermission = resource.getString(a + ".permissions.access");
                String formattingPermission = resource.getString(a + ".permissions.formatting");
                String message = resource.getString(a + ".message");

                String viewBasePermission = resource.getString(a + ".view.permission");
                String viewTogglePermission = resource.getString(a + ".view.toggle.permission");
                ViewingInfo viewingInfo = new ViewingInfo(viewBasePermission, viewTogglePermission);

                ConfiguredChatChannel channel = new ConfiguredChatChannel(a, t, accessPermission, formattingPermission, message, viewingInfo);
                r.put(channel.identifier(), channel);
            } catch (Exception e) {
                StreamlineMessaging.getInstance().logWarning("Could not load chat channel with identifier '" + a + "' due to: " + e.getMessage());
            }
        });

        r.remove("none");
        r.put("none", new ConfiguredChatChannel("none", ConfiguredChatChannel.Type.LOCAL, "", "", "", new ViewingInfo("", "")));

        return r;
    }
}
