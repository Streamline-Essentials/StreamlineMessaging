package tv.quaint.configs;

import net.streamline.api.configs.ModularizedConfig;
import tv.quaint.StreamlineMessaging;

public class Messages extends ModularizedConfig {
    public Messages() {
        super(StreamlineMessaging.getInstance(), "messages.yml", true);
    }

    public String errorsChannelIsNull() {
        reloadResource();

        return resource.getString("errors.channel.is-null");
    }

    public String errorsChannelNoAccess() {
        reloadResource();

        return resource.getString("errors.channel.no-access");
    }
}
