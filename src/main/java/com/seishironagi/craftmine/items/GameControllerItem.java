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
import net.minecraftforge.fml.DistExecutor;

public class GameControllerItem extends Item {
    public GameControllerItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                .fireResistant() // Prevents accidental loss
        );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // Separate client-side GUI opening
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> openClientGui());
        }

        // Prevent any server-side container from opening
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide);
    }

    @OnlyIn(Dist.CLIENT)
    private void openClientGui() {
        // Open the modern GUI directly on the client side
        Minecraft.getInstance().setScreen(new GameControllerScreen());
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item." + CraftMine.MOD_ID + ".game_controller");
    }
}
