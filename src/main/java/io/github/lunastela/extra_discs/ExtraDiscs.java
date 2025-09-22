package io.github.lunastela.extra_discs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.mojang.datafixers.Typed;
import com.mojang.logging.LogUtils;

import io.github.lunastela.extra_discs.datagen.TypedRecordHolder;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.SetItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(ExtraDiscs.MODID)
public class ExtraDiscs
{
    public static final String MODID = "extra_discs";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEM_REGISTRY = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<SoundEvent> SOUND_REGISTRY = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MODID);
    public static final DeferredRegister<JukeboxSong> JUKEBOX_SONG_REGISTRY = DeferredRegister.create(Registries.JUKEBOX_SONG, MODID);

    public static final List<TypedRecordHolder> typedRecordHolderList = new ArrayList<>();
    public static String getDiscID(DiscType discType, String discName) {
        return discType.label + "_" + discName;
    }

    public static ResourceKey<JukeboxSong> createSong(String name) {
        return ResourceKey.create(Registries.JUKEBOX_SONG, ResourceLocation.fromNamespaceAndPath(MODID, name));
    }

    public static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MODID, name);
        return SOUND_REGISTRY.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static DeferredItem<Item> registerDisc(DiscType discType, String discName, int comparatorStrength, float lengthInSeconds, String englishLiteral, @Nullable String appender) {
        DeferredHolder<SoundEvent, SoundEvent> soundEvent = registerSoundEvent(discName);
        DeferredItem<Item> discItem = ITEM_REGISTRY.register(
            getDiscID(discType, discName),
            () -> new Item(
                new Item.Properties()
                    .jukeboxPlayable(createSong(discName))
                    .rarity(discType.rarity)
                    .stacksTo(1)
            ){
                @Override
                public Component getName(ItemStack stack) {
                    return Component.translatable("item." + ExtraDiscs.MODID + "." + discType.label);
                }
            }
        );

        TypedRecordHolder recordHolder = new TypedRecordHolder(
            discType, discName, comparatorStrength,
            lengthInSeconds, appender, discItem,
            soundEvent, englishLiteral
        );
        typedRecordHolderList.add(recordHolder);
        return discItem;
    }

    public static DeferredItem<Item> registerDisc(DiscType discType, String discName, int comparatorStrength, float lengthInSeconds, String englishLiteral) {
        return registerDisc(discType, discName, comparatorStrength, lengthInSeconds, englishLiteral, null);
    }

    /* Volume Alpha */

    // "Calm"
    public static final DeferredItem<Item> MINECRAFT = registerDisc(DiscType.OVERWORLD_DISC, "minecraft", 0, 254, "C418 - Minecraft");
    public static final DeferredItem<Item> CLARK = registerDisc(DiscType.OVERWORLD_DISC, "clark", 1, 191, "C418 - Clark");
    public static final DeferredItem<Item> SWEDEN = registerDisc(DiscType.OVERWORLD_DISC, "sweden", 2, 215, "C418 - Sweden");

    // "Hal"
    public static final DeferredItem<Item> SUBWOOFER_LULLABY = registerDisc(DiscType.OVERWORLD_DISC, "subwoofer_lullaby", 0, 208, "C418 - Subwoofer Lullaby");
    public static final DeferredItem<Item> LIVING_MICE = registerDisc(DiscType.OVERWORLD_DISC, "living_mice", 1, 177, "C418 - Living Mice");
    public static final DeferredItem<Item> HAGGSTROM = registerDisc(DiscType.OVERWORLD_DISC, "haggstrom", 2, 204, "C418 - Haggstrom");
    public static final DeferredItem<Item> DANNY = registerDisc(DiscType.OVERWORLD_DISC, "danny", 3, 254, "C418 - Danny");

    // "Nuance"
    public static final DeferredItem<Item> KEY = registerDisc(DiscType.OVERWORLD_DISC, "key", 0, 65, "C418 - Key");
    public static final DeferredItem<Item> OXYGENE = registerDisc(DiscType.OVERWORLD_DISC, "oxygene", 1, 65, "C418 - Oxygène");

    // "Piano"
    public static final DeferredItem<Item> DRY_HANDS = registerDisc(DiscType.OVERWORLD_DISC, "dry_hands", 0, 68, "C418 - Dry Hands");
    public static final DeferredItem<Item> WET_HANDS = registerDisc(DiscType.OVERWORLD_DISC, "wet_hands", 1, 90, "C418 - Wet Hands");
    public static final DeferredItem<Item> MICE_ON_VENUS = registerDisc(DiscType.OVERWORLD_DISC, "mice_on_venus", 2, 281, "C418 - Mice on Venus");

    /* Volume Beta */

    // "Creative"
    public static final DeferredItem<Item> BIOME_FEST = registerDisc(DiscType.CREATIVE_DISC, "biome_fest", 0, 377, "C418 - Biome Fest");
    public static final DeferredItem<Item> BLIND_SPOTS = registerDisc(DiscType.CREATIVE_DISC, "blind_spots", 1, 331, "C418 - Blind Spots");
    public static final DeferredItem<Item> HAUNT_MUSKIE = registerDisc(DiscType.CREATIVE_DISC, "haunt_muskie", 2, 360, "C418 - Haunt Muskie");
    public static final DeferredItem<Item> ARIA_MATH = registerDisc(DiscType.CREATIVE_DISC, "aria_math", 3, 309, "C418 - Aria Math");
    public static final DeferredItem<Item> DREITON = registerDisc(DiscType.CREATIVE_DISC, "dreiton", 4, 496, "C418 - Dreiton");
    public static final DeferredItem<Item> TASWELL = registerDisc(DiscType.CREATIVE_DISC, "taswell", 5, 514, "C418 - Taswell");

    // "Nether"
    public static final DeferredItem<Item> CONCRETE_HALLS = registerDisc(DiscType.NETHER_DISC, "concrete_halls", 0, 253, "C418 - Concrete Halls");
    public static final DeferredItem<Item> DEAD_VOXEL = registerDisc(DiscType.NETHER_DISC, "dead_voxel", 1, 295, "C418 - Dead Voxel");
    public static final DeferredItem<Item> WARMTH = registerDisc(DiscType.NETHER_DISC, "warmth", 2, 238, "C418 - Warmth");
    public static final DeferredItem<Item> BALLAD_OF_THE_CATS = registerDisc(DiscType.NETHER_DISC, "ballad_of_the_cats", 3, 274, "C418 - Ballad of the Cats");

    // End Music
    public static final DeferredItem<Item> THE_END = registerDisc(DiscType.END_DISC, "the_end", 0, 903, "C418 - The End");
    public static final DeferredItem<Item> BOSS = registerDisc(DiscType.END_DISC, "boss", 1, 346, "C418 - Boss");
    public static final DeferredItem<Item> ALPHA = registerDisc(DiscType.END_DISC, "alpha", 2, 602, "C418 - Alpha");

    // Menu Music
    public static final DeferredItem<Item> MUTATION = registerDisc(DiscType.MENU_DISC, "mutation", 0, 184, "C418 - Mutation");
    public static final DeferredItem<Item> MOOG_CITY_2 = registerDisc(DiscType.MENU_DISC, "moog_city_2", 1, 179, "C418 - Moog City 2");
    public static final DeferredItem<Item> BEGINNING_2 = registerDisc(DiscType.MENU_DISC, "beginning_2", 2, 175, "C418 - Beginning 2");
    public static final DeferredItem<Item> FLOATING_TREES = registerDisc(DiscType.MENU_DISC, "floating_trees", 3, 244, "C418 - Floating Trees");

    /* Update Aquatic */

    public static final DeferredItem<Item> AXOLOTL = registerDisc(DiscType.UNDERWATER_DISC, "axolotl", 0, 303, "C418 - Axolotl");
    public static final DeferredItem<Item> DRAGON_FISH = registerDisc(DiscType.UNDERWATER_DISC, "dragon_fish", 1, 373, "C418 - Dragon Fish");
    public static final DeferredItem<Item> SHUNIJI = registerDisc(DiscType.UNDERWATER_DISC, "shuniji", 2, 244, "C418 - Shuniji");

    /* Nether Update */

    public static final DeferredItem<Item> CHRYSOPOEIA = registerDisc(DiscType.ALT_NETHER_DISC, "chrysopoeia", 4, 243, "Lena Raine - Chrysopoeia", "crimson_forest");
    public static final DeferredItem<Item> RUBEDO = registerDisc(DiscType.ALT_NETHER_DISC, "rubedo", 5, 312, "Lena Raine - Rubedo", "nether_wastes");
    public static final DeferredItem<Item> SO_BELOW = registerDisc(DiscType.ALT_NETHER_DISC, "so_below", 6, 319, "Lena Raine - So Below", "soulsand_valley");

    /* Caves & Cliffs */

    public static final DeferredItem<Item> STAND_TALL = registerDisc(DiscType.UNDERGROUND_DISC, "stand_tall", 0, 308, "Lena Raine - Stand Tall");
    public static final DeferredItem<Item> LEFT_TO_BLOOM = registerDisc(DiscType.UNDERGROUND_DISC, "left_to_bloom", 1, 342, "Lena Raine - Left to Bloom");
    public static final DeferredItem<Item> ANCESTRY = registerDisc(DiscType.DEEP_DARK_DISC, "ancestry", 2, 343, "Lena Raine - Ancestry");
    public static final DeferredItem<Item> WENDING = registerDisc(DiscType.UNDERGROUND_DISC, "wending", 3, 314, "Lena Raine - Wending");
    public static final DeferredItem<Item> INFINITE_AMETHYST = registerDisc(DiscType.UNDERGROUND_DISC, "infinite_amethyst", 4, 271, "Lena Raine - Infinite Amethyst"); // Peak
    public static final DeferredItem<Item> ONE_MORE_DAY = registerDisc(DiscType.UNDERGROUND_DISC, "one_more_day", 5, 278, "Lena Raine - One More Day");
    public static final DeferredItem<Item> FLOATING_DREAM = registerDisc(DiscType.UNDERGROUND_DISC, "floating_dream", 6, 206, "Kumi Tanioka - Floating Dream");
    public static final DeferredItem<Item> COMFORTING_MEMORIES = registerDisc(DiscType.OVERWORLD_DISC, "comforting_memories", 7, 275, "Kumi Tanioka - Comforting Memories");
    public static final DeferredItem<Item> AN_ORDINARY_DAY = registerDisc(DiscType.UNDERGROUND_DISC, "an_ordinary_day", 3, 331, "Kumi Tanioka - An Ordinary Day");

    /* The Wild Update */

    public static final DeferredItem<Item> FIREBUGS = registerDisc(DiscType.UNDERGROUND_DISC, "firebugs", 0, 312, "Lena Raine - Firebugs", "swamp");
    public static final DeferredItem<Item> AERIE = registerDisc(DiscType.OVERWORLD_DISC, "aerie", 1, 296, "Lena Raine - Aerie", "swamp");
    public static final DeferredItem<Item> LABYRINTHINE = registerDisc(DiscType.UNDERGROUND_DISC, "labyrinthine", 2, 324, "Lena Raine - Labyrinthine", "swamp");

    /* Trails & Tales */

    public static final DeferredItem<Item> ECHO_IN_THE_WIND = registerDisc(DiscType.UNDERGROUND_DISC, "echo_in_the_wind", 0, 296, "Aaron Cherof - Echo in the Wind");
    public static final DeferredItem<Item> A_FAMILIAR_ROOM = registerDisc(DiscType.OVERWORLD_DISC, "a_familiar_room", 1, 241, "Aaron Cherof - A Familiar Room");
    public static final DeferredItem<Item> BROMELIAD = registerDisc(DiscType.OVERWORLD_DISC, "bromeliad", 2, 312, "Aaron Cherof - Bromeliad");
    public static final DeferredItem<Item> CRESCENT_DUNES = registerDisc(DiscType.OVERWORLD_DISC, "crescent_dunes", 3, 248, "Aaron Cherof - Crescent Dunes");

    /* Tricky Trials */

    public static final DeferredItem<Item> FEATHERFALL = registerDisc(DiscType.OVERWORLD_DISC, "featherfall", 0, 345, "Aaron Cherof - Featherfall");
    public static final DeferredItem<Item> WATCHER = registerDisc(DiscType.OVERWORLD_DISC, "watcher", 1, 332, "Aaron Cherof - Watcher");
    public static final DeferredItem<Item> PUZZLEBOX = registerDisc(DiscType.OVERWORLD_DISC, "puzzlebox", 2, 299, "Aaron Cherof - Puzzlebox");
    public static final DeferredItem<Item> KOMOREBI = registerDisc(DiscType.OVERWORLD_DISC, "komorebi", 3, 287, "Kumi Tanioka - komorebi");
    public static final DeferredItem<Item> POKOPOKO = registerDisc(DiscType.UNDERGROUND_DISC, "pokopoko", 4, 304, "Kumi Tanioka - pokopoko");
    public static final DeferredItem<Item> YAKUSOKU = registerDisc(DiscType.OVERWORLD_DISC, "yakusoku", 5, 271, "Kumi Tanioka - yakusoku");
    public static final DeferredItem<Item> DEEPER = registerDisc(DiscType.UNDERGROUND_DISC, "deeper", 6, 303, "Lena Raine - Deeper");
    public static final DeferredItem<Item> ELD_UNKNOWN = registerDisc(DiscType.UNDERGROUND_DISC, "eld_unknown", 7, 296, "Lena Raine - Eld Unknown");
    public static final DeferredItem<Item> ENDLESS = registerDisc(DiscType.UNDERGROUND_DISC, "endless", 8, 402, "Lena Raine - Endless");

    // Extra
    public static final Supplier<SoundEvent> CREATOR_SOUND = registerSoundEvent("creator_remix");
    public static final ResourceKey<JukeboxSong> CREATOR_JUKEBOX_EVENT = ResourceKey.create(
        Registries.JUKEBOX_SONG, ResourceLocation.fromNamespaceAndPath(MODID, "creator_remix")
    );
    public static final DeferredItem<Item> CREATOR_REMIX = ITEM_REGISTRY.register(
        "music_disc_creator_remix", () -> new Item(
            new Item.Properties()
                .jukeboxPlayable(CREATOR_JUKEBOX_EVENT)
                .rarity(Rarity.EPIC)
                .fireResistant()
                .stacksTo(1)
                .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
        )
    );

    // Oreo
    public static final Supplier<SoundEvent> OREO_SOUND = registerSoundEvent("oreo");
    public static final ResourceKey<JukeboxSong> OREO_JUKEBOX_EVENT = ResourceKey.create(
        Registries.JUKEBOX_SONG, ResourceLocation.fromNamespaceAndPath(MODID, "oreo")
    );

    public static final DeferredItem<Item> OREO = ITEM_REGISTRY.register(
        "oreo", () -> new Item(
            new Item.Properties()
                .stacksTo(64)
                .rarity(Rarity.RARE)
                .jukeboxPlayable(OREO_JUKEBOX_EVENT)
                .food(
                    new FoodProperties.Builder()
                        .nutrition(2).alwaysEdible()
                        .saturationModifier(0.1f)
                        .build()
                )
        )
    );

    public ExtraDiscs(IEventBus modEventBus, ModContainer modContainer) {
        ITEM_REGISTRY.register(modEventBus);
        SOUND_REGISTRY.register(modEventBus);
        JUKEBOX_SONG_REGISTRY.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreativeTab);

        modContainer.registerConfig(ModConfig.Type.COMMON, ExtraDiscsConfig.SPEC);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    private void addCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            for (int i = 0; i < typedRecordHolderList.size(); i++)
                event.accept(typedRecordHolderList.get(i).registryObject());
            event.accept(CREATOR_REMIX);
        }
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS)
            event.accept(OREO);
    }

    @SubscribeEvent
    public void onLightningStrikeCreator(EntityStruckByLightningEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ItemEntity itemEntity) {
            if (itemEntity.getItem().is(Items.MUSIC_DISC_CREATOR)) {
                ItemStack itemStack = new ItemStack(CREATOR_REMIX.get());
                itemEntity.setItem(itemStack);
                event.setCanceled(true);
            } else if (itemEntity.getItem().is(CREATOR_REMIX))
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        String mobKey = event.getName().getPath();
        @Nullable DiscType currentType = ExtraDiscsConfig.mobDropMap.get(mobKey);
        if (currentType != null) {
            LootPool.Builder poolExtension = LootPool.lootPool()
                .name("extra_discs_injected")
                .setRolls(ConstantValue.exactly(1))
                .add(TagEntry.expandTag(currentType.itemTag))
                .when(LootItemEntityPropertyCondition.hasProperties(
                    LootContext.EntityTarget.ATTACKER,
                    EntityPredicate.Builder.entity().of(EntityType.SKELETON)
                ));
            event.getTable().addPool(poolExtension.build());
        }

        // 11 Special Rare Drop
        if (ExtraDiscsConfig.discElevenDrop && mobKey.matches("entities/player")) {
            LootPool.Builder poolExtension = LootPool.lootPool().setRolls(ConstantValue.exactly(1));
            poolExtension.add(TagEntry.expandTag(ItemTags.create(ResourceLocation.fromNamespaceAndPath(ExtraDiscs.MODID, "music_disc_11_drop"))))
                .when(LootItemEntityPropertyCondition.hasProperties(
                    LootContext.EntityTarget.ATTACKER,
                    EntityPredicate.Builder.entity().of(EntityType.SKELETON)
                ))
                .when(LootItemRandomChanceCondition.randomChance((float) ExtraDiscsConfig.discElevenDropRate));
            event.getTable().addPool(poolExtension.build());
        }
    }
}
