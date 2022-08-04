package tv.quaint.timers;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.modules.StreamlineModule;
import net.streamline.api.scheduler.ModuleRunnable;
import tv.quaint.StreamlineMessaging;
import tv.quaint.savables.SavableChatter;

public class FriendInviteExpiry extends ModuleRunnable {
    @Getter
    private final SavableChatter sender;
    @Getter
    private final SavableChatter invited;
    @Getter @Setter
    private int ticksLeft;

    public FriendInviteExpiry(SavableChatter sender, SavableChatter invited, int ticksLeft) {
        super(StreamlineMessaging.getInstance(), 0L, 1L);
        this.sender = sender;
        this.invited = invited;
        this.ticksLeft = ticksLeft;
    }

    @Override
    public void run() {
        sender.handleInviteExpiryEnd(this);
    }
}
