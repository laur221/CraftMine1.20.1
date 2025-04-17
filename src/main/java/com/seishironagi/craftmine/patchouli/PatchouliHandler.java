package com.seishironagi.craftmine.patchouli;

import com.seishironagi.craftmine.CraftMine;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;

/**
 * Ghid simplu folosind cărți standard din Minecraft
 * în loc de a depinde de moduri externe
 */
public class PatchouliHandler {
    public static void init() {
        // Verifică dacă sunt disponibile moduri opționale
        if (ModList.get().isLoaded("patchouli")) {
            registerPatchouliHandbook();
        } else {
            CraftMine.LOGGER.info("Patchouli nu este instalat, se folosește ghidul vanilla");
        }
    }
    
    /**
     * Creează și returnează un ghid simplu folosind cărți Minecraft standard
     */
    public static ItemStack createVanillaGuide() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.setHoverName(Component.translatable("item.craftmine.guide"));
        
        // Adaugă informații în NBT
        CompoundTag bookTag = book.getOrCreateTag();
        bookTag.putString("author", "CraftMine");
        bookTag.putString("title", "Ghid CraftMine");
        
        // Adaugă paginile de ghid
        bookTag.putBoolean("resolved", true);
        String[] pageContents = {
            Component.Serializer.toJson(Component.literal("Bun venit la CraftMine!\n\nAcest ghid te va ajuta să înțelegi cum să joci.")),
            Component.Serializer.toJson(Component.literal("Echipe:\n- Roșu: Minerii\n- Albastru: Exploratorii\n\nFiecare echipă are obiective specifice.")),
            Component.Serializer.toJson(Component.literal("Comenzi utile:\n/team - vezi echipa\n/time - vezi timpul rămas"))
        };
        
        // Create pages list tag
        ListTag pages = new ListTag();
        for (String content : pageContents) {
            pages.add(StringTag.valueOf(content));
        }
        
        bookTag.putString("generation", "0");
        bookTag.put("pages", pages);
        
        return book;
    }
    
    private static void registerPatchouliHandbook() {
        try {
            // Folosim reflection pentru a evita dependența hard-coded
            Class<?> apiClass = Class.forName("vazkii.patchouli.api.PatchouliAPI");
            
            // Use ResourceLocation.tryParse to avoid deprecation warnings
            ResourceLocation bookId = ResourceLocation.tryParse(CraftMine.MOD_ID + ":guide");
            
            // Trimitem mesaj IMC
            net.minecraftforge.fml.InterModComms.sendTo("patchouli", "register_book", 
                () -> bookId);
        } catch (Exception e) {
            CraftMine.LOGGER.error("Error registering Patchouli handbook", e);
        }
    }
}
