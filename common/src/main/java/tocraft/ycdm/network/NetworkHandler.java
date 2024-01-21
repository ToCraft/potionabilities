package tocraft.ycdm.network;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.alchemy.Potion;
import tocraft.ycdm.PotionAbilities;
import tocraft.ycdm.impl.PAPlayerDataProvider;

import java.util.UUID;

public class NetworkHandler {
    public static ResourceLocation ABILITY_USE = PotionAbilities.id("ability_use");
    public static ResourceLocation SYNC_DATA = PotionAbilities.id("sync_data");

    public static void registerPacketReceiver() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ABILITY_USE, (buf, context) -> {
            if (((PAPlayerDataProvider) context.getPlayer()).ycdm$getCooldown() <= 0) {
                if (!PotionAbilities.shapeConditions(context.getPlayer()))
                    return;
                UUID entityUUID = buf.readUUID();
                LivingEntity sendEntity = (LivingEntity) ((ServerPlayer) context.getPlayer()).serverLevel().getEntity(entityUUID);
                assert sendEntity != null;

                String potionId = ((PAPlayerDataProvider) context.getPlayer()).ycdm$getPotion();
                Potion potion = BuiltInRegistries.POTION.get(new ResourceLocation(potionId));
                for (MobEffectInstance effect : potion.getEffects()) {
                    // otherwise /effect clear would cause troubles
                    MobEffectInstance effectInstance = new MobEffectInstance(effect.getEffect(), effect.getDuration(), effect.getAmplifier());
                    sendEntity.addEffect(effectInstance);
                }

                sendEntity.playSound(SoundEvents.SPLASH_POTION_THROW);

                ((PAPlayerDataProvider) context.getPlayer()).ycdm$setCooldown(PotionAbilities.CONFIG.cooldownTicks);
            }
        });
    }

    public static void sendAbilityUseRequest(UUID entityUUID) {
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());

        packet.writeUUID(entityUUID);

        NetworkManager.sendToServer(ABILITY_USE, packet);
    }

    public static void syncData(ServerPlayer player) {
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());

        CompoundTag tag = new CompoundTag();
        tag.putInt("cooldown", ((PAPlayerDataProvider) player).ycdm$getCooldown());
        tag.putString("potion", ((PAPlayerDataProvider) player).ycdm$getPotion());
        ListTag list = new ListTag();
        ((PAPlayerDataProvider) player).ycdm$getStructures().forEach(entry -> {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putInt("X", entry.getX());
            entryTag.putInt("Z", entry.getZ());
            list.add(entryTag);
        });
        if (!list.isEmpty())
            tag.put("structures", list);

        packet.writeNbt(tag);

        NetworkManager.sendToPlayer(player, SYNC_DATA, packet);
    }
}
