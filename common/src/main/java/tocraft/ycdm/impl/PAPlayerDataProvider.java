package tocraft.ycdm.impl;

import net.minecraft.core.BlockPos;

import java.util.List;

public interface PAPlayerDataProvider {

    void ycdm$setPotion(String potion);

    String ycdm$getPotion();

    void ycdm$setStructures(List<BlockPos> structures);

    List<BlockPos> ycdm$getStructures();

    void ycdm$setCooldown(int cooldown);

    int ycdm$getCooldown();
}
