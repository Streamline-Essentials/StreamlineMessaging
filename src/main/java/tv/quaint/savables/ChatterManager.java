package tv.quaint.savables;

import de.leonhard.storage.Config;
import de.leonhard.storage.Json;
import de.leonhard.storage.Toml;
import lombok.Getter;
import lombok.Setter;
import net.streamline.api.configs.*;
import net.streamline.api.savables.SavableResource;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.StreamlineMessaging;
import tv.quaint.configs.ConfiguredChatChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ChatterManager {
    @Getter @Setter
    private static ConcurrentHashMap<String, SavableChatter> loadedChatters = new ConcurrentHashMap<>();

    public static void loadChatter(SavableChatter chatter) {
        loadedChatters.put(chatter.uuid, chatter);
    }

    public static void unloadChatter(SavableChatter chatter) {
        unloadChatter(chatter.uuid);
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
        return getOrGetChatter(user.getUUID());
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
            case MONGO -> {
                return new MongoResource(StreamlineMessaging.getConfigs().getConfiguredDatabase(), clazz.getSimpleName(), "uuid", uuid);
            }
            case MYSQL -> {
                return new MySQLResource(StreamlineMessaging.getConfigs().getConfiguredDatabase(), new SQLCollection(clazz.getSimpleName(), "uuid", uuid));
            }
        }

        return null;
    }

    public static List<SavableChatter> getChattersViewingChannel(ConfiguredChatChannel channel) {
        List<SavableChatter> r = new ArrayList<>();

        for (SavableChatter chatter : getLoadedChatters().values()) {
            if (chatter.canMessageMeFrom(channel)) r.add(chatter);
        }

        return r;
    }
}
