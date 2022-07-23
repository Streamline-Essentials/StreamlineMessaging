package tv.quaint.timers;

import net.streamline.api.scheduler.ModuleRunnable;
import tv.quaint.StreamlineMessaging;
import tv.quaint.savables.ChatterManager;
import tv.quaint.savables.SavableChatter;

public class ChatterSaver extends ModuleRunnable {
    public ChatterSaver() {
        super(StreamlineMessaging.getInstance(), 0L, 1200L);
    }

    @Override
    public void run() {
        for (SavableChatter chatter : ChatterManager.getLoadedChatters().values()) {
            chatter.saveAll();
        }
    }
}
