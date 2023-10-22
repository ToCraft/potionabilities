package tocraft.ycdm;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import tocraft.craftedcore.config.ConfigLoader;
import tocraft.craftedcore.platform.Platform;
import tocraft.ycdm.config.PotionAbilitiesConfig;
import tocraft.ycdm.network.NetworkHandler;

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
		if (Platform.getDist().isClient())
			new PotionAbilitiesClient().initialize();
		
		NetworkHandler.registerPacketReceiver();
	}

	public static ResourceLocation id(String name) {
		return new ResourceLocation(MODID, name);
	}
	
	public static <S extends Object> Optional<? extends HolderSet.ListBacked<S>> getHolders(S element, Registry<S> registry) {
		return registry.getHolder(registry.getId(element)).map((holder) -> {
				return HolderSet.direct(holder);
				});
	}
}
