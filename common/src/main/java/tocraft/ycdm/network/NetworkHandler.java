package tocraft.ycdm.network;

import java.util.UUID;

import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.alchemy.Potion;
import tocraft.craftedcore.network.NetworkManager;
import tocraft.ycdm.PotionAbilities;
import tocraft.ycdm.impl.PAPlayerDataProvider;

public class NetworkHandler {
	public static ResourceLocation ABILITY_USE = new ResourceLocation(PotionAbilities.MODID, "ability_use");
	
	public static void registerPacketReceiver() {
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, ABILITY_USE, (buf, context) -> {
			if (!PotionAbilities.shapeConditions(context.getPlayer()))
				return;
			UUID entityUUID = buf.readUUID();
			LivingEntity sendEntity = (LivingEntity) ((ServerPlayer)context.getPlayer()).serverLevel().getEntity(entityUUID);
			
			String potionId = ((PAPlayerDataProvider)context.getPlayer()).getPotion();
			Potion potion = BuiltInRegistries.POTION.get(new ResourceLocation(potionId));
            for (MobEffectInstance effect : potion.getEffects()) {
            	// otherwise /effect clear would cause troubles
            	MobEffectInstance effectInstance = new MobEffectInstance(effect.getEffect(), effect.getDuration(), effect.getAmplifier());
            	sendEntity.addEffect(effectInstance);
            }
		});
	}
	
	public static void sendAbilityUseRequest(UUID entityUUID) {
		FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
		
		packet.writeUUID(entityUUID);
		
		NetworkManager.sendToServer(ABILITY_USE, packet);
	}
}
