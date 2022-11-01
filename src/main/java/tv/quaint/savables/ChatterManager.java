package tv.quaint.savables;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.savables.SavableResource;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.StreamlineMessaging;
import tv.quaint.configs.ConfiguredChatChannel;
import tv.quaint.storage.resources.StorageResource;
import tv.quaint.storage.resources.flat.FlatFileResource;
import tv.quaint.thebase.lib.leonhard.storage.Config;
import tv.quaint.thebase.lib.leonhard.storage.Json;
import tv.quaint.thebase.lib.leonhard.storage.Toml;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class ChatterManager {
    @Getter @Setter
    private static ConcurrentSkipListMap<String, SavableChatter> loadedChatters = new ConcurrentSkipListMap<>();

    public static void loadChatter(SavableChatter chatter) {
        loadedChatters.put(chatter.getUuid(), chatter);
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

        chatter = new SavableChatter(uuid);
        loadChatter(chatter);
        return chatter;
    }

    public static SavableChatter getOrGetChatter(StreamlineUser user) {
        return getOrGetChatter(user.getUuid());
    }

    public static StorageResource<?> newStorageResourceUsers(String uuid, Class<? extends SavableResource> clazz) {
        switch (StreamlineMessaging.getConfigs().savingUse()) {
            case YAML -> {
                return new FlatFileResource<>(Config.class, uuid + ".yml", StreamlineMessaging.getUsersFolder(), false);
            }
            case JSON -> {
                return new FlatFileResource<>(Json.class, uuid + ".json", StreamlineMessaging.getUsersFolder(), false);
            }
            case TOML -> {
                return new FlatFileResource<>(Toml.class, uuid + ".toml", StreamlineMessaging.getUsersFolder(), false);
            }
            case MONGO, SQLITE, MYSQL -> {
                return null;
            }
        }

        return null;
    }

    public static ConcurrentSkipListSet<SavableChatter> getChattersViewingChannel(ConfiguredChatChannel channel) {
        ConcurrentSkipListSet<SavableChatter> r = new ConcurrentSkipListSet<>();

        getLoadedChatters().forEach((s, savableChatter) -> {
            if (savableChatter.canMessageMeFrom(channel)) r.add(savableChatter);
        });

        return r;
    }
}
