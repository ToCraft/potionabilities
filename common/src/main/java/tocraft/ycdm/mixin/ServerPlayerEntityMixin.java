package tocraft.ycdm.mixin;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tocraft.ycdm.impl.PAPlayerDataProvider;

@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void copyWalkersData(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        PAPlayerDataProvider oldData = ((PAPlayerDataProvider) oldPlayer);
        PAPlayerDataProvider newData = ((PAPlayerDataProvider) this);

        // Transfer data from the old ServerPlayer -> new ServerPlayer
        newData.ycdm$setPotion(oldData.ycdm$getPotion());
        newData.ycdm$setStructures(oldData.ycdm$getStructures());
        newData.ycdm$setCooldown(oldData.ycdm$getCooldown());
    }
}
