package tocraft.ycdm;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import tocraft.ycdm.gui.AbilityOverlayRenderer;
import tocraft.ycdm.network.ClientNetworking;
import tocraft.ycdm.tick.KeyPressHandler;

public class PotionAbilitiesClient {
    public static final KeyMapping ABILITY_KEY = new KeyMapping("key.ycdm_ability", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R, "key.categories.ycdm");

    public void initialize() {
        AbilityOverlayRenderer.register();

        KeyMappingRegistry.register(ABILITY_KEY);

        // Register event handlers
        ClientTickEvent.CLIENT_PRE.register(new KeyPressHandler());

        ClientNetworking.registerPacketHandlers();
    }
}
