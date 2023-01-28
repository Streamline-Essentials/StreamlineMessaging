package tv.quaint.timers;

import net.streamline.api.scheduler.ModuleRunnable;
import tv.quaint.StreamlineMessaging;
import tv.quaint.savables.ChatterManager;

public class ChatterSyncer extends ModuleRunnable {
    public ChatterSyncer() {
        super(StreamlineMessaging.getInstance(), 0, 6000);
    }

    @Override
    public void run() {
        ChatterManager.syncAllChatters();
    }
}
