package host.plas.timers;

import net.streamline.api.scheduler.ModuleRunnable;
import host.plas.StreamlineMessaging;
import host.plas.savables.SavableChatter;
import host.plas.savables.ChatterManager;

public class ChatterSaver extends ModuleRunnable {
    public ChatterSaver() {
        super(StreamlineMessaging.getInstance(), 0L, 1200L);
    }

    @Override
    public void run() {
        for (SavableChatter chatter : ChatterManager.getLoadedChatters().values()) {
            chatter.save();

            if (StreamlineMessaging.getChatterDatabase() != null) {
                StreamlineMessaging.getChatterDatabase().save(chatter);
            }
        }
    }
}
