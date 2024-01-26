package tocraft.ycdm.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.alchemy.Potion;
import tocraft.ycdm.impl.PAPlayerDataProvider;

public class PACommand {
    public void initialize() {
        CommandRegistrationEvent.EVENT.register((dispatcher, ctx, b) -> {
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
        ((PAPlayerDataProvider) player).ycdm$getStructures().clear();
        source.sendSystemMessage(Component.translatable("ycdm.structures_clear", player.getDisplayName()));
    }

    public static void getStructures(CommandSourceStack source, ServerPlayer player) {
        if (((PAPlayerDataProvider) player).ycdm$getStructures().isEmpty()) {
            source.sendSystemMessage(Component.translatable("ycdm.structures_get_failure", player.getDisplayName()));
            return;
        }

        source.sendSystemMessage(Component.translatable("ycdm.structures_get_success", player.getDisplayName()));

        for (BlockPos structure : ((PAPlayerDataProvider) player).ycdm$getStructures()) {
            source.sendSystemMessage(Component.translatable("ycdm.structures_get", blockPosToComponent(structure)));
        }
    }

    public static void addStructure(CommandSourceStack source, ServerPlayer player, BlockPos position) {
        ((PAPlayerDataProvider) player).ycdm$getStructures().add(position);

        source.sendSystemMessage(Component.translatable("ycdm.structure_add", player.getDisplayName(), blockPosToComponent(position)));
    }

    public static void removeStructure(CommandSourceStack source, ServerPlayer player, BlockPos position) {
        boolean success = false;

        for (BlockPos structure : ((PAPlayerDataProvider) player).ycdm$getStructures()) {
            if (structure.getX() == position.getX() && structure.getZ() == position.getZ()) {
                success = true;
                ((PAPlayerDataProvider) player).ycdm$getStructures().remove(structure);
            }
        }

        Component positionComponent = blockPosToComponent(position);

        if (success)
            source.sendSystemMessage(Component.translatable("ycdm.structure_remove_success", player.getDisplayName(), positionComponent));
        else
            source.sendSystemMessage(Component.translatable("ycdm.structure_remove_failure", player.getDisplayName(), positionComponent));
    }

    public static void clearPotion(CommandSourceStack source, ServerPlayer player) {
        ((PAPlayerDataProvider) player).ycdm$setPotion("");
        source.sendSystemMessage(Component.translatable("ycdm.potion_clear", player.getDisplayName()));
    }

    public static void getPotion(CommandSourceStack source, ServerPlayer player) {
        if (((PAPlayerDataProvider) player).ycdm$getPotion().isBlank()) {
            source.sendSystemMessage(Component.translatable("ycdm.potion_get_failed", player.getDisplayName()));
            return;
        }
        ResourceLocation potionId = new ResourceLocation(((PAPlayerDataProvider) player).ycdm$getPotion());
        source.sendSystemMessage(Component.translatable("ycdm.potion_get_success", player.getDisplayName(), Component.translatable(potionId.toLanguageKey()).getString()));
    }
    public static void setPotion(CommandSourceStack source, ServerPlayer player, Potion potion) {
        ResourceLocation potionId = BuiltInRegistries.POTION.getKey(potion);
        ((PAPlayerDataProvider) player).ycdm$setPotion(potionId.getNamespace() + ":" + potionId.getPath());
        source.sendSystemMessage(Component.translatable("ycdm.potion_set", player.getDisplayName(), potionId.getNamespace() + ":" + potionId.getPath()));
    }

    private static Component blockPosToComponent(BlockPos blockPos) {
        return ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", blockPos.getX(), "~", blockPos.getZ())).withStyle((style) -> style.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + blockPos.getX() + " ~ " + blockPos.getZ())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip"))));
    }
}
