package tocraft.ycdm;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import tocraft.craftedcore.config.ConfigLoader;
import tocraft.craftedcore.events.common.PlayerEvents;
import tocraft.craftedcore.platform.Platform;
import tocraft.craftedcore.platform.VersionChecker;
import tocraft.walkers.impl.PlayerDataProvider;
import tocraft.ycdm.command.PACommand;
import tocraft.ycdm.config.PotionAbilitiesConfig;
import tocraft.ycdm.network.NetworkHandler;

public class PotionAbilities {

	public static final Logger LOGGER = LoggerFactory.getLogger(PotionAbilities.class);
	public static final String MODID = "ycdm";
	public static String versionURL = "https://raw.githubusercontent.com/ToCraft/potionabilities/1.20.2/gradle.properties";
	public static final PotionAbilitiesConfig CONFIG = ConfigLoader.read(MODID, PotionAbilitiesConfig.class);
	public static boolean foundWalkers = false;
	public static List<String> devs = new ArrayList<>();
	static {
		devs.add("1f63e38e-4059-4a4f-b7c4-0fac4a48e744");
	}
	
	public void initialize() {
		Platform.getMods().forEach(mod -> {
			if (mod.getModId().equals("walkers"))
				foundWalkers = true;
		});
		
		PlayerEvents.PLAYER_JOIN.register(player -> {
			String newestVersion = VersionChecker.checkForNewVersion(versionURL);
			if (newestVersion != null && !Platform.getMod(MODID).getVersion().equals(newestVersion))
				player.sendSystemMessage(Component.translatable("ycdm.update", newestVersion));
		});
			
		
		if (Platform.getDist().isClient())
			new PotionAbilitiesClient().initialize();
		
		NetworkHandler.registerPacketReceiver();
		new PACommand().initialize();
	}

	public static ResourceLocation id(String name) {
		return new ResourceLocation(MODID, name);
	}
	
	public static <S extends Object> Optional<? extends HolderSet.ListBacked<S>> getHolders(S element, Registry<S> registry) {
		return registry.getHolder(registry.getId(element)).map((holder) -> {
				return HolderSet.direct(holder);
				});
	}
	
	/**
     * 
     * @return True, if woodwalkers isn't installed or if the player's got a second shape but is in first shape
     */
    public static boolean shapeConditions(Player player) {
    	if (foundWalkers)
    		return ((PlayerDataProvider) player).walkers$get2ndShape() != null && ((PlayerDataProvider) player).walkers$getCurrentShape() == null && ((PlayerDataProvider) player).walkers$getAbilityCooldown() <= 0;
    	return true;
    };
}
