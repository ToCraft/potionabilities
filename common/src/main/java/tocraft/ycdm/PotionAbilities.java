package tocraft.ycdm;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.ResourceLocation;
import tocraft.craftedcore.config.ConfigLoader;
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
	}

	public static ResourceLocation id(String name) {
		return new ResourceLocation(MODID, name);
	}
}
