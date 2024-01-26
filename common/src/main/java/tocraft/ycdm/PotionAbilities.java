package tocraft.ycdm;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import tocraft.craftedcore.config.ConfigLoader;
import tocraft.craftedcore.platform.VersionChecker;
import tocraft.walkers.impl.PlayerDataProvider;
import tocraft.ycdm.command.PACommand;
import tocraft.ycdm.config.PotionAbilitiesConfig;
import tocraft.ycdm.network.NetworkHandler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public class PotionAbilities {

    public static final String MODID = "ycdm";
    private static final String MAVEN_URL = "https://maven.tocraft.dev/public/dev/tocraft/ycdm/maven-metadata.xml";
    public static final PotionAbilitiesConfig CONFIG = ConfigLoader.read(MODID, PotionAbilitiesConfig.class);
    public static boolean foundWalkers = false;

    public void initialize() {
        foundWalkers = Platform.getOptionalMod("walkers").isPresent();

        try {
            VersionChecker.registerMavenChecker(MODID, new URL(MAVEN_URL), Component.literal("PotionAbilities"));
        } catch (MalformedURLException ignored) {
        }

        if (Platform.getEnvironment() == Env.CLIENT)
            new PotionAbilitiesClient().initialize();

        NetworkHandler.registerPacketReceiver();
        new PACommand().initialize();
    }

    public static ResourceLocation id(String name) {
        return new ResourceLocation(MODID, name);
    }

    public static <S> Optional<? extends HolderSet.ListBacked<S>> getHolders(S element, Registry<S> registry) {
        return registry.getHolder(registry.getId(element)).map(HolderSet::direct);
    }

    /**
     * @return True, if woodwalkers isn't installed or if the player's got a second shape but is in first shape
     */
    public static boolean shapeConditions(Player player) {
        if (foundWalkers)
            return ((PlayerDataProvider) player).walkers$get2ndShape() != null && ((PlayerDataProvider) player).walkers$getCurrentShape() == null && ((PlayerDataProvider) player).walkers$getAbilityCooldown() <= 0;
        return true;
    }
}
