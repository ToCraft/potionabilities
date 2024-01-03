package tocraft.ycdm.command;

import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.alchemy.Potion;
import tocraft.craftedcore.events.common.CommandEvents;
import tocraft.ycdm.PotionAbilities;
import tocraft.ycdm.impl.PAPlayerDataProvider;

import java.util.concurrent.atomic.AtomicBoolean;

public class PACommand {
	public void initialize() {
		CommandEvents.REGISTRATION.register((dispatcher, ctx, b) -> {
			LiteralCommandNode<CommandSourceStack> rootNode = Commands.literal("ycdm")
					.requires(source -> source.hasPermission(2)).build();
			
			LiteralCommandNode<CommandSourceStack> structures = Commands.literal("structures")
					.then(Commands.literal("clear")
							.then(Commands.argument("player", EntityArgument.players()).executes(context -> {
								clearStructures(context.getSource(), EntityArgument.getPlayer(context, "player"));
								return 1;
							})))
					.then(Commands.literal("get")
						.then(Commands.argument("player", EntityArgument.players()).executes(context -> {
							getStructures(context.getSource(), EntityArgument.getPlayer(context, "player"));
							return 1;
					})))
					.then(Commands.literal("add")
							.then(Commands.argument("player", EntityArgument.players())
									.then(Commands.argument("position", BlockPosArgument.blockPos()).executes(context -> {
										addStructure(context.getSource(), EntityArgument.getPlayer(context, "player"), BlockPosArgument.getBlockPos(context, "position"));
										return 1;
					}))))
					.then(Commands.literal("remove")
							.then(Commands.argument("player", EntityArgument.players())
									.then(Commands.argument("position", BlockPosArgument.blockPos()).executes(context -> {
										removeStructure(context.getSource(), EntityArgument.getPlayer(context, "player"), BlockPosArgument.getBlockPos(context, "position"));
										return 1;
					})))).build();
			
			LiteralCommandNode<CommandSourceStack> potion = Commands.literal("potion")
					.then(Commands.literal("clear").then(Commands.argument("player", EntityArgument.players()).executes(context -> {
						clearPotion(context.getSource(), EntityArgument.getPlayer(context, "player"));
						return 1;
					})))
					.then(Commands.literal("get").then(Commands.argument("player", EntityArgument.players()).executes(context -> {
						getPotion(context.getSource(), EntityArgument.getPlayer(context, "player"));
						return 1;
					})))
					.then(Commands.literal("set").then(Commands.argument("player", EntityArgument.players())
							.then(Commands.argument("potion", ResourceArgument.resource(ctx, Registries.POTION)).executes(context -> {
									setPotion(context.getSource(), EntityArgument.getPlayer(context, "player"), ResourceArgument.getResource(context, "potion", Registries.POTION).value());
									return 1;
					})))).build();
			
			rootNode.addChild(structures);
			rootNode.addChild(potion);

			dispatcher.getRoot().addChild(rootNode);
		});
	}
	
	public static void clearStructures(CommandSourceStack source, ServerPlayer player) {
		((PAPlayerDataProvider) player).getStructures().clear();
		source.sendSystemMessage(Component.translatable("ycdm.structures_clear", player.getDisplayName()));
	};

	public static void getStructures(CommandSourceStack source, ServerPlayer player) {
		source.sendSystemMessage(Component.translatable("ycdm.structures_get", player.getDisplayName()));

		for (BlockPos structure : ((PAPlayerDataProvider) player).getStructures()) {
			source.sendSystemMessage(Component.literal("X: " + structure.getX() + " Z: " + structure.getZ()));
		}
	};

	public static void addStructure(CommandSourceStack source, ServerPlayer player, BlockPos position) {
		((PAPlayerDataProvider) player).getStructures().add(position);
		source.sendSystemMessage(Component.translatable("ycdm.structure_add", player.getDisplayName(), position.getX(), position.getZ()));
	};

	public static void removeStructure(CommandSourceStack source, ServerPlayer player, BlockPos position) {
		boolean success = false;

		for (BlockPos structure : ((PAPlayerDataProvider) player).getStructures()) {
			if (structure.getX() == position.getX() && structure.getZ() == position.getZ()) {
				success = true;
				((PAPlayerDataProvider) player).getStructures().remove(structure);
			}
		};

		if (success )
			source.sendSystemMessage(Component.translatable("ycdm.structure_remove_success", player.getDisplayName(), position.getX(), position.getZ()));
		else
			source.sendSystemMessage(Component.translatable("ycdm.structure_remove_failure", player.getDisplayName(), position.getX(), position.getZ()));
	};
	
	public static void clearPotion(CommandSourceStack source, ServerPlayer player) {
		((PAPlayerDataProvider) player).setPotion("");
		source.sendSystemMessage(Component.translatable("ycdm.potion_clear", player.getDisplayName()));
	};
	
	public static void getPotion(CommandSourceStack source, ServerPlayer player) {
		if (((PAPlayerDataProvider) player).getPotion().isBlank()) {
			source.sendSystemMessage(Component.translatable("ycdm.potion_get_failed", player.getDisplayName()));
			return;
		}
		ResourceLocation potionId = new ResourceLocation(((PAPlayerDataProvider) player).getPotion());
		source.sendSystemMessage(Component.translatable("ycdm.potion_get_success", player.getDisplayName(), Component.translatable(potionId.toLanguageKey()).getString()));
	};

	public static void setPotion(CommandSourceStack source, ServerPlayer player, Potion potion) {
		ResourceLocation potionId = BuiltInRegistries.POTION.getKey(potion);
		((PAPlayerDataProvider) player).setPotion(potionId.getNamespace() + ":" + potionId.getPath());
		source.sendSystemMessage(Component.translatable("ycdm.potion_set", player.getDisplayName(), potionId.getNamespace() + ":" + potionId.getPath()));
	};
}
