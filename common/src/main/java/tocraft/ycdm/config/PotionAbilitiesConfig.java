package tocraft.ycdm.config;

import java.util.ArrayList;
import java.util.List;

import tocraft.craftedcore.config.Config;

public class PotionAbilitiesConfig implements Config {
	@SuppressWarnings("serial")
	public List<String> structures = new ArrayList<String>() {
		{
			add("jungle_pyramid");
		}
	};
	public int maxDistanceToStructure = 50;
}
