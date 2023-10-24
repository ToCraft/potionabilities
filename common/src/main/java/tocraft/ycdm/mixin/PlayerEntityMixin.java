package tocraft.ycdm.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import tocraft.ycdm.PotionAbilities;
import tocraft.ycdm.impl.PAPlayerDataProvider;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PAPlayerDataProvider {
	@Unique
	private Integer potion = 0;
	@Unique
	private List<BlockPos> structures = new ArrayList<BlockPos>();
	
	// Stuff for giving potions
	private int distance = PotionAbilities.CONFIG.maxDistanceToStructure;
	private BlockPos nearest = null;

    private PlayerEntityMixin(EntityType<? extends LivingEntity> type, Level world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void serverTick(CallbackInfo info) {
    	// check if player is near temple and in liquid.
    	if ((Object) this instanceof ServerPlayer serverPlayer && serverPlayer.isInLiquid() && PotionAbilities.shapeConditions(serverPlayer)) {
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
				// ignore crashes to save time (otherwise it would need to check EVERY var from the code above if it's null.
	    		catch (Exception ignored) {
	    			// Re-assign values to ensure it works next time
	    			nearest = null;
	    			distance = PotionAbilities.CONFIG.maxDistanceToStructure;
	    		}; 			
    		});

    		if (nearest != null) {
    			// check if structure was already visited
    			for (BlockPos entry : structures) {
    				if (entry.getX() == nearest.getX() && entry.getZ() == nearest.getZ()) {
    					// Re-assign values to ensure it works next time
    					nearest = null;
    					distance = PotionAbilities.CONFIG.maxDistanceToStructure;
    					return;
    				}
    			}
    			
				Random random = new Random();
				potion = random.nextInt(0, BuiltInRegistries.POTION.size());
				structures.add(nearest);
			}
    		
    		// Re-assign values to ensure it works next time
			nearest = null;
			distance = PotionAbilities.CONFIG.maxDistanceToStructure;
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
		tag.putInt("potion",  potion);
		ListTag list = new ListTag();
		structures.forEach(entry -> {
			CompoundTag entryTag = new CompoundTag();
			entryTag.putInt("X", entry.getX());
			entryTag.putInt("Z", entry.getZ());
			list.add(entryTag);
		});
		if (list != null)
			tag.put("structures", list);
		return tag;
	}

	@Unique
	public void readData(CompoundTag tag) {
		structures.clear();
		potion = tag.getInt("id");
		if ((ListTag) tag.get("structures") != null) {
			ListTag list = (ListTag) tag.get("structures");
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
	@Override
	public void setPotion(Integer potion) {
		this.potion = potion;
	};
	
	@Unique
	@Override
	public Integer getPotion() {
		return potion;
	};
	
	@Unique
	@Override
	public void setStructures(List<BlockPos> structures) {
		this.structures = structures;
	};
	
	@Unique
	@Override
	public List<BlockPos> getStructures() {
		return structures;
	};
}
