package tv.quaint.savables;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.SLAPI;
import net.streamline.api.configs.given.GivenConfigs;
import net.streamline.api.savables.SavableResource;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.StreamlineMessaging;
import tv.quaint.configs.ConfiguredChatChannel;
import tv.quaint.storage.StorageUtils;
import tv.quaint.storage.resources.StorageResource;
import tv.quaint.storage.resources.cache.CachedResource;
import tv.quaint.storage.resources.cache.CachedResourceUtils;
import tv.quaint.storage.resources.databases.configurations.DatabaseConfig;
import tv.quaint.storage.resources.flat.FlatFileResource;
import tv.quaint.thebase.lib.leonhard.storage.Config;
import tv.quaint.thebase.lib.leonhard.storage.Json;
import tv.quaint.thebase.lib.leonhard.storage.Toml;
import tv.quaint.thebase.lib.mongodb.MongoClient;

import java.io.File;
import java.sql.Connection;
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

    public static void getChatterFromDatabase(SavableChatter chatter) {
        CachedResource<?> cachedResource = (CachedResource<?>) chatter.getStorageResource();
        String tableName = SLAPI.getMainDatabase().getConfig().getTablePrefix() + "chatters";

        try {
            boolean changed = false;
            switch (GivenConfigs.getMainConfig().savingUseType()) {
                case MYSQL, SQLITE, MONGO -> {
                    CachedResourceUtils.updateCache(tableName, cachedResource.getDiscriminatorKey(), cachedResource.getDiscriminatorAsString(), cachedResource, SLAPI.getMainDatabase());
                    changed = true;
                }
            }
            if (changed) chatter.loadValues();
        } catch (Exception e) {
            syncChatter(chatter);
        }
    }

    public static boolean isLoaded(String uuid) {
        return getChatter(uuid) != null;
    }

    public static void getChatterFromDatabase(String uuid) {
        if (! isLoaded(uuid)) return;
        getChatterFromDatabase(getChatter(uuid));
    }

    public static void getAllUsersFromDatabase() {
        getLoadedChattersSet().forEach(ChatterManager::getChatterFromDatabase);
    }

    public static ConcurrentSkipListSet<SavableChatter> getLoadedChattersSet() {
        return new ConcurrentSkipListSet<>(loadedChatters.values());
    }

    public static void syncChatter(SavableChatter chatter) {
        switch (GivenConfigs.getMainConfig().savingUseType()) {
            case MONGO, MYSQL, SQLITE -> {
                CachedResource<?> cachedResource = (CachedResource<?>) chatter.getStorageResource();
                String tableName = SLAPI.getMainDatabase().getConfig().getTablePrefix() + "chatter";
                CachedResourceUtils.pushToDatabase(tableName, cachedResource, SLAPI.getMainDatabase());
            }
        }
    }

    public static void syncChatter(String uuid) {
        if (! isLoaded(uuid)) return;
        syncChatter(getChatter(uuid));
    }

    public static void syncAllChatters() {
        getLoadedChattersSet().forEach(ChatterManager::syncChatter);
    }

    public static boolean userExists(String uuid) {
        StorageUtils.SupportedStorageType type = GivenConfigs.getMainConfig().savingUseType();
        DatabaseConfig config = GivenConfigs.getMainConfig().getConfiguredDatabase();
        File userFolder = SLAPI.getUserFolder();
        switch (type) {
            case YAML:
                File[] files = userFolder.listFiles();
                if (files == null) return false;

                for (File file : files) {
                    if (file.getName().equals(uuid + ".yml")) return true;
                }
                return false;
            case JSON:
                File[] files2 = userFolder.listFiles();
                if (files2 == null) return false;

                for (File file : files2) {
                    if (file.getName().equals(uuid + ".json")) return true;
                }
                return false;
            case TOML:
                File[] files3 = userFolder.listFiles();
                if (files3 == null) return false;

                for (File file : files3) {
                    if (file.getName().equals(uuid + ".toml")) return true;
                }
                return false;
            case MONGO:
            case MYSQL:
            case SQLITE:
                return SLAPI.getMainDatabase().exists(SLAPI.getMainDatabase().getConfig().getTablePrefix() + "chatters", "uuid", uuid);
            default:
                return false;
        }
    }

    public static StorageResource<?> newStorageResourceUsers(String uuid, Class<? extends SavableResource> clazz) {
        switch (StreamlineMessaging.getConfigs().savingUse()) {
            case YAML:
                return new FlatFileResource<>(Config.class, uuid + ".yml", StreamlineMessaging.getUsersFolder(), false);
            case JSON:
                return new FlatFileResource<>(Json.class, uuid + ".json", StreamlineMessaging.getUsersFolder(), false);
            case TOML:
                return new FlatFileResource<>(Toml.class, uuid + ".toml", StreamlineMessaging.getUsersFolder(), false);
            case MONGO:
                return new CachedResource<>(MongoClient.class, "uuid", uuid);
            case MYSQL:
            case SQLITE:
                return new CachedResource<>(Connection.class, "uuid", uuid);
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
