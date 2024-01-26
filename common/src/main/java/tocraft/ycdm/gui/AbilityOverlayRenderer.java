package tocraft.ycdm.gui;

import dev.architectury.event.events.client.ClientGuiEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Items;
import tocraft.craftedcore.gui.TimerOverlayRenderer;
import tocraft.ycdm.PotionAbilities;
import tocraft.ycdm.impl.PAPlayerDataProvider;

public class AbilityOverlayRenderer {

    public static void register() {
        ClientGuiEvent.RENDER_HUD.register((matrices, delta) -> {
            Minecraft client = Minecraft.getInstance();
            LocalPlayer player = client.player;

            assert player != null;
            if (((PAPlayerDataProvider) player).ycdm$getPotion().isBlank()) {
                return;
            }

            TimerOverlayRenderer.register(matrices, ((PAPlayerDataProvider) player).ycdm$getCooldown(), PotionAbilities.CONFIG.cooldownTicks, Items.SPLASH_POTION);
        });
    }
}
