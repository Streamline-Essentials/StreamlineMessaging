package host.plas;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.command.CommandHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.modules.SimpleModule;
import net.streamline.api.utils.UserUtils;
import host.plas.configs.ChatChannelConfig;
import host.plas.configs.Configs;
import host.plas.configs.Messages;
import host.plas.commands.ChannelCommand;
import host.plas.commands.FriendCommand;
import host.plas.commands.MessageCommand;
import host.plas.commands.ReplyCommand;
import host.plas.listeners.MainListener;
import host.plas.ratapi.MessagingExpansion;
import host.plas.savables.ChatterManager;
import tv.quaint.storage.resources.databases.DatabaseResource;
import net.streamline.thebase.lib.pf4j.PluginWrapper;
import host.plas.timers.ChatterSaver;
import host.plas.timers.ChatterSyncer;

import java.io.File;
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

    @Getter @Setter
    static DatabaseResource<?> chatterDatabase;

    public StreamlineMessaging(PluginWrapper wrapper) {
        super(wrapper);
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

        new ChatterSyncer();

        ChatterManager.getOrGetChatter(UserUtils.getConsole());


        new ChannelCommand().register();
        new MessageCommand().register();
        new ReplyCommand().register();
        new FriendCommand().register();
    }

    @Override
    public void onDisable() {
        ChatterManager.getLoadedChatters().forEach((s, savableChatter) -> {
            savableChatter.saveAll();
            savableChatter.getStorageResource().push();
            ChatterManager.syncChatter(savableChatter);
        });
        ChatterManager.setLoadedChatters(new ConcurrentSkipListMap<>());

        getMessagingExpansion().stop();

        CommandHandler.getLoadedModuleCommands().forEach((s, moduleCommand) -> {
            if (! s.equals(getIdentifier())) return;

            if (moduleCommand != null) {
                moduleCommand.unregister();
            }
        });
    }
}
