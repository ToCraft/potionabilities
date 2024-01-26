package tocraft.ycdm.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.minecraft.server.level.ServerPlayer;

public interface PotionAbilityEvents {
    Event<UnlockPotionCallback> UNLOCK_POTION = EventFactory.createEventResult(UnlockPotionCallback.class);

    interface UnlockPotionCallback {
        EventResult unlock(ServerPlayer player);
    }
}
