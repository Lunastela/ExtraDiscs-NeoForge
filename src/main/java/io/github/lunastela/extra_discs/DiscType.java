package io.github.lunastela.extra_discs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public enum DiscType {
    OVERWORLD_DISC("overworld_music_disc", "game", "Overworld Music Disc", Rarity.RARE),
    CREATIVE_DISC("creative_music_disc", "game/creative", "Creative Music Disc", Rarity.RARE),
    UNDERWATER_DISC("underwater_music_disc", "game/water", "Underwater Music Disc", Rarity.RARE),
    UNDERGROUND_DISC("underground_music_disc", "game", "Underground Music Disc", Rarity.RARE),
    DEEP_DARK_DISC("deep_dark_music_disc", "game", "Warden Music Disc", Rarity.RARE),
    NETHER_DISC("nether_music_disc", "game/nether", "Nether Music Disc", Rarity.RARE),
    ALT_NETHER_DISC("alt_nether_music_disc", "game/nether", "Nether Music Disc", Rarity.RARE),
    END_DISC("end_music_disc", "game/end", "End Music Disc", Rarity.RARE),
    MENU_DISC("menu_music_disc", "menu", "Menu Music Disc", Rarity.RARE);

    public final String label;
    public final String filePath;
    public final TagKey<Item> itemTag;
    public final Rarity rarity;
    public final String englishLiteral;
    private DiscType(String label, String filePath, String defaultName, Rarity itemRarity) {
        this.label = label;
        this.filePath = filePath;
        this.rarity = itemRarity;
        this.englishLiteral = defaultName;
        this.itemTag = ItemTags.create(
            ResourceLocation.fromNamespaceAndPath(ExtraDiscs.MODID, label + "_drop")
        );
    }
}