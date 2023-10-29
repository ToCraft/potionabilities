package tocraft.ycdm.events;

import net.minecraft.server.level.ServerPlayer;
import tocraft.craftedcore.events.Event;
import tocraft.craftedcore.events.EventBuilder;

public interface PotionAbilityEvents {
	Event<UnlockPotionCallback> UNLOCK_POTION = EventBuilder.createEventResult(UnlockPotionCallback.class);
	
	interface UnlockPotionCallback {
		Event.Result unlock(ServerPlayer player);
	}
}
