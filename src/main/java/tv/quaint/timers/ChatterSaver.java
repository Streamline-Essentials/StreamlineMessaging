package tv.quaint.timers;

import net.streamline.api.scheduler.ModuleRunnable;
import tv.quaint.StreamlineMessaging;
import tv.quaint.savables.ChatterManager;
import tv.quaint.savables.SavableChatter;
import tv.quaint.storage.resources.cache.CachedResource;
import tv.quaint.storage.resources.cache.CachedResourceUtils;

public class ChatterSaver extends ModuleRunnable {
    public ChatterSaver() {
        super(StreamlineMessaging.getInstance(), 0L, 1200L);
    }

    @Override
    public void run() {
        for (SavableChatter chatter : ChatterManager.getLoadedChatters().values()) {
            chatter.saveAll();

            if (StreamlineMessaging.getChatterDatabase() != null) {
                if (! StreamlineMessaging.getChatterDatabase().exists(StreamlineMessaging.getChatterDatabase().getConfig().getTablePrefix() + "chatter")) {
                    CachedResourceUtils.pushToDatabase(StreamlineMessaging.getChatterDatabase().getConfig().getTablePrefix() + "chatter",
                            (CachedResource<?>) chatter.getStorageResource(), StreamlineMessaging.getChatterDatabase());
                }
            }
        }
    }
}
