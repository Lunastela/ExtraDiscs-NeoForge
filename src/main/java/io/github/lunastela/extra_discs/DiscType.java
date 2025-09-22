package io.github.lunastela.extra_discs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public enum DiscType {
    OVERWORLD_DISC("overworld_music_disc", "game", "Overworld Music Disc", Rarity.RARE, "zombie"),
    CREATIVE_DISC("creative_music_disc", "game/creative", "Creative Music Disc", Rarity.RARE, "iron_golem"),
    UNDERWATER_DISC("underwater_music_disc", "game/water", "Underwater Music Disc", Rarity.RARE, "elder_guardian"),
    UNDERGROUND_DISC("underground_music_disc", "game", "Underground Music Disc", Rarity.RARE, "silverfish"),
    DEEP_DARK_DISC("deep_dark_music_disc", "game", "Warden Music Disc", Rarity.EPIC, "warden"),
    NETHER_DISC("nether_music_disc", "game/nether", "Nether Music Disc", Rarity.RARE, "ghast"),
    ALT_NETHER_DISC("alt_nether_music_disc", "game/nether", "Nether Music Disc", Rarity.RARE, "piglin_brute"),
    END_DISC("end_music_disc", "game/end", "End Music Disc", Rarity.RARE, "shulker"),
    MENU_DISC("menu_music_disc", "menu", "Menu Music Disc", Rarity.RARE, "enderman");

    public final String label;
    public final String filePath;
    public final TagKey<Item> itemTag;
    public final Rarity rarity;
    public final String englishLiteral;
    public final String defaultMobDrop;
    
    private DiscType(String label, String filePath, String defaultName, Rarity itemRarity, String defaultMobDrop) {
        this.label = label;
        this.filePath = filePath;
        this.rarity = itemRarity;
        this.englishLiteral = defaultName;
        this.defaultMobDrop = defaultMobDrop;
        this.itemTag = ItemTags.create(
            ResourceLocation.fromNamespaceAndPath(ExtraDiscs.MODID, label + "_drop")
        );
    }
}