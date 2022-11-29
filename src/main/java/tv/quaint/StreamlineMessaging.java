package tv.quaint;

import lombok.Getter;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.modules.SimpleModule;
import org.pf4j.PluginWrapper;
import tv.quaint.commands.ChannelCommand;
import tv.quaint.commands.FriendCommand;
import tv.quaint.commands.MessageCommand;
import tv.quaint.commands.ReplyCommand;
import tv.quaint.configs.ChatChannelConfig;
import tv.quaint.configs.Configs;
import tv.quaint.configs.Messages;
import tv.quaint.listeners.MainListener;
import tv.quaint.ratapi.MessagingExpansion;
import tv.quaint.savables.ChatterManager;
import tv.quaint.timers.ChatterSaver;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

public class StreamlineMessaging extends SimpleModule {
    @Getter
    static StreamlineMessaging instance;

    @Getter
    static Configs configs;
    @Getter
    static Messages messages;
    @Getter
    static ChatChannelConfig chatChannelConfig;

    @Getter
    static MainListener mainListener;

    @Getter
    static File usersFolder;

    @Getter
    static ChatterSaver chatterSaver;

    @Getter
    static MessagingExpansion messagingExpansion;

    public StreamlineMessaging(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void registerCommands() {
        setCommands(List.of(
                new ChannelCommand(),
                new MessageCommand(),
                new ReplyCommand(),
                new FriendCommand()
        ));
    }

    @Override
    public void onLoad() {
        instance = this;
        usersFolder = new File(getDataFolder(), "users" + File.separator);
        usersFolder.mkdirs();
        messagingExpansion = new MessagingExpansion();
    }

    @Override
    public void onEnable() {
        configs = new Configs();
        messages = new Messages();
        chatChannelConfig = new ChatChannelConfig();

        chatterSaver = new ChatterSaver();

        mainListener = new MainListener();
        ModuleUtils.listen(mainListener, this);
        getMessagingExpansion().init();
    }

    @Override
    public void onDisable() {
        ChatterManager.getLoadedChatters().forEach((s, savableChatter) -> {
            savableChatter.saveAll();
            savableChatter.getStorageResource().push();
        });
        ChatterManager.setLoadedChatters(new ConcurrentSkipListMap<>());

        getMessagingExpansion().stop();
    }
}
