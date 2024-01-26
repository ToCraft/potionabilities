package tocraft.ycdm.tick;

import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import tocraft.ycdm.PotionAbilitiesClient;
import tocraft.ycdm.network.NetworkHandler;

public class KeyPressHandler implements ClientTickEvent.Client {

    @Override
    public void tick(Minecraft client) {
        assert client.player != null;

        if (PotionAbilitiesClient.ABILITY_KEY.consumeClick()) {

            if (client.hitResult instanceof EntityHitResult ehit && ehit.getEntity() instanceof LivingEntity entityLiving)
                NetworkHandler.sendAbilityUseRequest(entityLiving.getUUID());
            else
                NetworkHandler.sendAbilityUseRequest(client.player.getUUID());
        }
    }
}
