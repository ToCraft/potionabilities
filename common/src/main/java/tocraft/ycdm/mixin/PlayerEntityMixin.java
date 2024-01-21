package tocraft.ycdm.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tocraft.ycdm.PotionAbilities;
import tocraft.ycdm.events.PotionAbilityEvents;
import tocraft.ycdm.impl.PAPlayerDataProvider;
import tocraft.ycdm.network.NetworkHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PAPlayerDataProvider {
    @Unique
    private String potion = "";
    @Unique
    private List<BlockPos> structures = new ArrayList<>();
    @Unique
    private int cooldown = 0;

    // Stuff for giving potions
    @Unique
    private int distance = PotionAbilities.CONFIG.maxDistanceToStructure;
    @Unique
    private BlockPos nearest = null;

    private PlayerEntityMixin(EntityType<? extends LivingEntity> type, Level world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void serverTick(CallbackInfo info) {
        // check if player is near temple and in liquid.
        if ((Object) this instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.isInLiquid() && PotionAbilities.shapeConditions(serverPlayer)) {
                ServerLevel serverLevel = serverPlayer.serverLevel();
                Registry<Structure> registry = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE);

                // get each structure from config
                PotionAbilities.CONFIG.structures.forEach(entry -> {
                    try {
                        Structure structure = registry.get(ResourceKey.create(Registries.STRUCTURE, new ResourceLocation(entry)));
                        HolderSet<Structure> holderSet = PotionAbilities.getHolders(structure, registry).orElseThrow();
                        BlockPos newNearest = serverLevel.getChunkSource().getGenerator().findNearestMapStructure(serverLevel, holderSet, serverPlayer.blockPosition(), PotionAbilities.CONFIG.maxDistanceToStructure, false).getFirst();
                        int newDistance = serverPlayer.blockPosition().distManhattan(new BlockPos(newNearest.getX(), serverPlayer.getBlockY(), newNearest.getZ()));
                        if (newDistance <= distance) {
                            distance = newDistance;
                            nearest = newNearest;
                        }
                    }
                    // ignore crashes to save time (otherwise it would need to check EVERY var from the code above if it's null.)
                    catch (Exception ignored) {
                        reassignValues();
                    }
                });

                if (nearest != null) {
                    // check if structure was already visited
                    for (BlockPos entry : structures) {
                        if (entry.getX() == nearest.getX() && entry.getZ() == nearest.getZ()) {
                            reassignValues();
                            return;
                        }
                    }

                    if (PotionAbilityEvents.UNLOCK_POTION.invoker().unlock(serverPlayer).isFalse()) {
                        reassignValues();
                        return;
                    }

                    Random random = new Random();
                    int potionId = random.nextInt(0, BuiltInRegistries.POTION.size());
                    ResourceLocation potionName = BuiltInRegistries.POTION.getKey(BuiltInRegistries.POTION.byId(potionId));
                    potion = potionName.getNamespace() + ":" + potionName.getPath();
                    structures.add(nearest);

                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false));
                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 10, false, false));

                    serverPlayer.playSound(SoundEvents.GENERIC_DRINK);
                }
                reassignValues();
            }

            NetworkHandler.syncData(serverPlayer);
            this.ycdm$setCooldown(Math.max(0, this.ycdm$getCooldown() - 1));
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void readNbt(CompoundTag tag, CallbackInfo info) {
        readData(tag.getCompound(PotionAbilities.MODID));
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void writeNbt(CompoundTag tag, CallbackInfo info) {
        tag.put(PotionAbilities.MODID, writeData(new CompoundTag()));
    }

    @Unique
    private CompoundTag writeData(CompoundTag tag) {
        tag.putInt("cooldown", cooldown);
        tag.putString("potion", potion);
        ListTag list = new ListTag();
        structures.forEach(entry -> {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putInt("X", entry.getX());
            entryTag.putInt("Z", entry.getZ());
            list.add(entryTag);
        });
        if (!list.isEmpty())
            tag.put("structures", list);
        return tag;
    }

    @Unique
    public void readData(CompoundTag tag) {
        structures.clear();
        cooldown = tag.getInt("cooldown");
        potion = tag.getString("potion");
        if (tag.get("structures") != null) {
            ListTag list = (ListTag) tag.get("structures");
            if (list != null)
                list.forEach(entry -> {
                    if (entry instanceof CompoundTag) {
                        int x = ((CompoundTag) entry).getInt("X");
                        int z = ((CompoundTag) entry).getInt("Z");

                        structures.add(new BlockPos(x, 0, z));
                    }
                });
        }
    }

    @Unique
    private void reassignValues() {
        // Re-assign values to ensure everything works next time
        nearest = null;
        distance = PotionAbilities.CONFIG.maxDistanceToStructure;
    }

    @Unique
    @Override
    public void ycdm$setPotion(String potion) {
        this.potion = potion;
    }

    @Unique
    @Override
    public String ycdm$getPotion() {
        return potion;
    }

    @Unique
    @Override
    public void ycdm$setStructures(List<BlockPos> structures) {
        this.structures = structures;
    }

    @Unique
    @Override
    public List<BlockPos> ycdm$getStructures() {
        return structures;
    }

    @Unique
    @Override
    public void ycdm$setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    @Unique
    @Override
    public int ycdm$getCooldown() {
        return cooldown;
    }
}
