package tocraft.ycdm;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import tocraft.craftedcore.config.ConfigLoader;
import tocraft.craftedcore.events.common.PlayerEvents;
import tocraft.ycdm.config.PotionAbilitiesConfig;

public class PotionAbilities {

	public static final Logger LOGGER = LoggerFactory.getLogger(PotionAbilities.class);
	public static final String MODID = "ycdm";
	public static String versionURL = "https://raw.githubusercontent.com/ToCraft/potionabilities/arch-1.20.1/gradle.properties";
	public static final PotionAbilitiesConfig CONFIG = ConfigLoader.read(MODID, PotionAbilitiesConfig.class);
	public static List<String> devs = new ArrayList<>();

	static {
		devs.add("1f63e38e-4059-4a4f-b7c4-0fac4a48e744");
	}

	public void initialize() {
		PlayerEvents.PLAYER_JOIN.register(player -> {
			
			Registry<Structure> registry = player.serverLevel().registryAccess().registryOrThrow(Registries.STRUCTURE);
			
			Structure structure = registry.get(ResourceKey.create(Registries.STRUCTURE, new ResourceLocation("jungle_pyramid")));
			
			HolderSet<Structure> holderSet = getStructureHolders(structure, registry).orElseThrow();
			ServerLevel serverLevel = player.serverLevel();
			
			BlockPos nearest = serverLevel.getChunkSource().getGenerator().findNearestMapStructure(serverLevel, holderSet, player.blockPosition(), 100, false).getFirst();
			
			if (nearest != null) {
				LOGGER.warn(nearest.toString());
				
				// get distance from player to structure. Ignores the height.
				int distance = player.blockPosition().distManhattan(new BlockPos(nearest.getX(), player.getBlockY(), nearest.getZ()));
				LOGGER.info("distance: " + distance);
			}
		});
	}

	public static ResourceLocation id(String name) {
		return new ResourceLocation(MODID, name);
	}
	
	private static Optional<? extends HolderSet.ListBacked<Structure>> getStructureHolders(Structure structure, Registry<Structure> structureRegistry) {
		return structureRegistry.getHolder(structureRegistry.getId(structure)).map((holder) -> {
				return HolderSet.direct(holder);
				});
	}
}
