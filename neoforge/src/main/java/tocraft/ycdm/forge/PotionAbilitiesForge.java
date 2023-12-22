package tocraft.ycdm.forge;

import net.neoforged.fml.common.Mod;
import tocraft.ycdm.PotionAbilities;

@Mod(PotionAbilities.MODID)
public class PotionAbilitiesForge {

	public PotionAbilitiesForge() {
		new PotionAbilities().initialize();
	}
}
