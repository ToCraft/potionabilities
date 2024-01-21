package tocraft.ycdm.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import tocraft.ycdm.impl.PAPlayerDataProvider;

import java.util.ArrayList;
import java.util.List;

public class ClientNetworking {
    public static void registerPacketHandlers() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, NetworkHandler.SYNC_DATA, (packet, context) -> {
            assert context.getPlayer() != null;

            // prevent crashes
            if (!(context.getPlayer() instanceof PAPlayerDataProvider playerDataProvider))
                return;

            CompoundTag tag = packet.readNbt();
            assert tag != null;
            playerDataProvider.ycdm$setCooldown(tag.getInt("cooldown"));
                playerDataProvider.ycdm$setPotion(tag.getString("potion"));

            List<BlockPos> structures = new ArrayList<BlockPos>();
            if (tag.get("structures") != null) {
                ListTag list = (ListTag) tag.get("structures");
                assert list != null;
                list.forEach(entry -> {
                    if (entry instanceof CompoundTag) {
                        int x = ((CompoundTag) entry).getInt("X");
                        int z = ((CompoundTag) entry).getInt("Z");

                        structures.add(new BlockPos(x, 0, z));
                    }
                });

                playerDataProvider.ycdm$setStructures(structures);
            }
        });
    }
}
