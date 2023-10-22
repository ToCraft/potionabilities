package tocraft.ycdm.tick;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import tocraft.craftedcore.events.client.ClientTickEvents;
import tocraft.ycdm.PotionAbilitiesClient;
import tocraft.ycdm.network.NetworkHandler;

public class KeyPressHandler implements ClientTickEvents.Client {

	@Override
	public void tick(Minecraft client) {
		assert client.player != null;

		if (PotionAbilitiesClient.ABILITY_KEY.consumeClick()) {
			HitResult hit = client.hitResult;
			if (hit instanceof EntityHitResult && ((EntityHitResult)hit).getEntity() instanceof LivingEntity entityLiving)
					NetworkHandler.sendAbilityUseRequest(entityLiving.getUUID());
			else
				NetworkHandler.sendAbilityUseRequest(client.player.getUUID());
		}
	}
}
