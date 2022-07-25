package tv.quaint;

import lombok.Getter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.modules.SimpleModule;
import net.streamline.api.modules.dependencies.Dependency;
import net.streamline.api.placeholder.RATExpansion;
import tv.quaint.commands.ChannelCommand;
import tv.quaint.commands.MessageCommand;
import tv.quaint.commands.ReplyCommand;
import tv.quaint.configs.ChatChannelConfig;
import tv.quaint.configs.Configs;
import tv.quaint.configs.Messages;
import tv.quaint.listeners.MainListener;
import tv.quaint.ratapi.MessagingExpansion;
import tv.quaint.timers.ChatterSaver;

import java.io.File;
import java.util.Collections;
import java.util.List;

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

    @Override
    public String identifier() {
        return "streamline-messaging";
    }

    @Override
    public List<String> authors() {
        return List.of("Quaint");
    }

    @Override
    public List<Dependency> dependencies() {
        return Collections.emptyList();
    }

    @Override
    public void registerCommands() {
        setCommands(List.of(
                new ChannelCommand(),
                new MessageCommand(),
                new ReplyCommand())
        );
    }

    @Override
    public void onLoad() {
        instance = this;
        usersFolder = new File(getDataFolder(), "users" + File.separator);
        usersFolder.mkdirs();

        RATExpansion expansion = ModuleUtils.getRATAPI().getExpansionByIdentifier("messaging");
        while (expansion != null) {
            ModuleUtils.getRATAPI().unregisterExpansion(expansion);
            expansion = ModuleUtils.getRATAPI().getExpansionByIdentifier("messaging");
        }
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
    }
}
