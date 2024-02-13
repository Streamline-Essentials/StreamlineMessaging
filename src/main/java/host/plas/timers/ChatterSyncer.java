package host.plas.timers;

import net.streamline.api.scheduler.ModuleRunnable;
import host.plas.StreamlineMessaging;
import host.plas.savables.ChatterManager;

public class ChatterSyncer extends ModuleRunnable {
    public ChatterSyncer() {
        super(StreamlineMessaging.getInstance(), 0, 6000);
    }

    @Override
    public void run() {
        ChatterManager.syncAllChatters();
    }
}
