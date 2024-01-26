package tocraft.ycdm.config;

import tocraft.craftedcore.config.Config;
import tocraft.craftedcore.config.annotions.Synchronize;

import java.util.ArrayList;
import java.util.List;

public class PotionAbilitiesConfig implements Config {
    public List<String> structures = new ArrayList<String>() {
        {
            add("jungle_pyramid");
        }
    };
    public int maxDistanceToStructure = 50;
    @Synchronize
    public int cooldownTicks = 600;
}
