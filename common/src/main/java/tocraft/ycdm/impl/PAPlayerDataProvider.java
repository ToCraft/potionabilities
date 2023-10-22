package tocraft.ycdm.impl;

import java.util.List;

import net.minecraft.core.BlockPos;

public interface PAPlayerDataProvider {
	
	void setPotion(Integer potion);
	Integer getPotion();
	void setStructures(List<BlockPos> structures);
	List<BlockPos> getStructures();
}
