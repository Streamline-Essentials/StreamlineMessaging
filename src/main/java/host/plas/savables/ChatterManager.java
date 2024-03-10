package host.plas.savables;

import host.plas.StreamlineMessaging;
import host.plas.configs.ConfiguredChatChannel;
import lombok.Getter;
import lombok.Setter;
import net.streamline.api.configs.given.GivenConfigs;
import net.streamline.api.data.console.StreamSender;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class ChatterManager {
    @Getter @Setter
    private static ConcurrentSkipListMap<String, SavableChatter> loadedChatters = new ConcurrentSkipListMap<>();

    public static void loadChatter(SavableChatter chatter) {
        loadedChatters.put(chatter.getUuid(), chatter);
        syncChatter(chatter);
    }

    public static void unloadChatter(SavableChatter chatter) {
        unloadChatter(chatter.getUuid());
    }

    public static void unloadChatter(String uuid) {
        loadedChatters.remove(uuid);
    }

    private static SavableChatter getChatter(String uuid) {
        return loadedChatters.get(uuid);
    }

    public static SavableChatter getOrGetChatter(String uuid) {
        SavableChatter chatter = getChatter(uuid);
        if (chatter != null) return chatter;

        StreamlineMessaging.getChatterDatabase().load(uuid).whenComplete((savableChatter, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                return;
            }

            if (savableChatter.isEmpty()) return;
            loadChatter(savableChatter.get());
        });

        return null;
    }

    public static SavableChatter getOrGetChatter(StreamSender user) {
        return getOrGetChatter(user.getUuid());
    }

    public static boolean isLoaded(String uuid) {
        return getChatter(uuid) != null;
    }

    public static void getAllUsersFromDatabase() {
        if (GivenConfigs.getMainDatabase() == null) return;

        StreamlineMessaging.getChatterDatabase().pullAllChatters().whenComplete((chatters, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                return;
            }

            chatters.forEach(ChatterManager::loadChatter);
        });
    }

    public static ConcurrentSkipListSet<SavableChatter> getLoadedChattersSet() {
        return new ConcurrentSkipListSet<>(loadedChatters.values());
    }

    public static void syncChatter(SavableChatter chatter) {
        if (GivenConfigs.getMainDatabase() == null) return;

        chatter.save();
    }

    public static void syncChatter(String uuid) {
        if (GivenConfigs.getMainDatabase() == null) return;

        if (! isLoaded(uuid)) return;
        syncChatter(getChatter(uuid));
    }

    public static void syncAllChatters() {
        getLoadedChattersSet().forEach(ChatterManager::syncChatter);
    }

    public static CompletableFuture<Boolean> userExists(String uuid) {
        return StreamlineMessaging.getChatterDatabase().exists(uuid);
    }

    public static ConcurrentSkipListSet<SavableChatter> getChattersViewingChannel(ConfiguredChatChannel channel) {
        ConcurrentSkipListSet<SavableChatter> r = new ConcurrentSkipListSet<>();

        getLoadedChatters().forEach((s, savableChatter) -> {
            if (savableChatter.canMessageMeFrom(channel)) r.add(savableChatter);
        });

        return r;
    }
}
