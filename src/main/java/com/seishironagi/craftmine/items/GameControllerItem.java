package com.seishironagi.craftmine.items;

import com.seishironagi.craftmine.CraftMine;
import com.seishironagi.craftmine.gui.GameControllerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;

public class GameControllerItem extends Item {
    public GameControllerItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (level.isClientSide) {
            openGameControllerScreen();
        }
        
        return InteractionResultHolder.success(itemstack);
    }
    
    @OnlyIn(Dist.CLIENT)
    private void openGameControllerScreen() {
        Minecraft.getInstance().setScreen(new GameControllerScreen(Component.literal("Game Controller")));
    }
    
    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item." + CraftMine.MODID + ".game_controller");
    }
}
