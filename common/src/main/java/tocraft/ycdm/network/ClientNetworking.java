package tocraft.ycdm.network;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import tocraft.craftedcore.network.NetworkManager;
import tocraft.ycdm.impl.PAPlayerDataProvider;

public class ClientNetworking {
	public static void registerPAcketHandlers() {
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, NetworkHandler.SYNC_DATA, (packet, context) -> {
			assert context.getPlayer() != null;
			
			CompoundTag tag = packet.readNbt();
			PAPlayerDataProvider playerDataProvider = (PAPlayerDataProvider) context.getPlayer();
			playerDataProvider.setCooldown(tag.getInt("cooldown"));
			playerDataProvider.setPotion(tag.getString("potion"));
			
			List<BlockPos> structures = new ArrayList<BlockPos>();
			if ((ListTag) tag.get("structures") != null) {
				ListTag list = (ListTag) tag.get("structures");
				list.forEach(entry -> {
					if (entry instanceof CompoundTag) {
						int x = ((CompoundTag) entry).getInt("X");
						int z = ((CompoundTag) entry).getInt("Z");
						
						structures.add(new BlockPos(x, 0, z));
					}
				});
				
				playerDataProvider.setStructures(structures);
			}
		});
	}
}
