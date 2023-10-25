package tocraft.ycdm.command;

import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import tocraft.craftedcore.events.common.CommandEvents;
import tocraft.ycdm.impl.PAPlayerDataProvider;

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
					}))).build();
			
			LiteralCommandNode<CommandSourceStack> potion = Commands.literal("potion")
					.then(Commands.literal("clear").then(Commands.argument("player", EntityArgument.players()).executes(context -> {
						clearPotion(context.getSource(), EntityArgument.getPlayer(context, "player"));
						return 1;
					})))
					.then(Commands.literal("get").then(Commands.argument("player", EntityArgument.players()).executes(context -> {
						getPotion(context.getSource(), EntityArgument.getPlayer(context, "player"));
						return 1;
					}))).build();
			
			rootNode.addChild(structures);
			rootNode.addChild(potion);

			dispatcher.getRoot().addChild(rootNode);
		});
	}
	
	public static void clearStructures(CommandSourceStack source, ServerPlayer player) {
		((PAPlayerDataProvider) player).getStructures().clear();
		source.sendSystemMessage(Component.translatable("ycdm.structure_clear", player.getDisplayName()));
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
}
