package tocraft.ycdm.fabric;

import net.fabricmc.api.ModInitializer;
import tocraft.ycdm.PotionAbilities;

public class PotionAbilitiesFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        new PotionAbilities().initialize();
    }
}
