package io.github.lunastela.extra_discs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

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
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
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
                    return Component.translatable("item." + ExtraDiscs.MODID + "." + discType.label + ".name");
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

    /*
     * Disc Collection
     */
    public static final DeferredItem<Item> MINECRAFT = registerDisc(DiscType.OVERWORLD_DISC, "minecraft", 0, 254, "C418 - Minecraft");
    public static final DeferredItem<Item> CLARK = registerDisc(DiscType.OVERWORLD_DISC, "clark", 1, 191, "C418 - Clark");
    public static final DeferredItem<Item> SWEDEN = registerDisc(DiscType.OVERWORLD_DISC, "sweden", 2, 215, "C418 - Sweden");

    public static final DeferredItem<Item> BIOME_FEST = registerDisc(DiscType.CREATIVE_DISC, "biome_fest", 0, 377, "C418 - Biome Fest");
    public static final DeferredItem<Item> BLIND_SPOTS = registerDisc(DiscType.CREATIVE_DISC, "blind_spots", 1, 331, "C418 - Blind Spots");
    public static final DeferredItem<Item> HAUNT_MUSKIE = registerDisc(DiscType.CREATIVE_DISC, "haunt_muskie", 2, 360, "C418 - Haunt Muskie");
    public static final DeferredItem<Item> ARIA_MATH = registerDisc(DiscType.CREATIVE_DISC, "aria_math", 3, 309, "C418 - Aria Math");
    public static final DeferredItem<Item> DREITON = registerDisc(DiscType.CREATIVE_DISC, "dreiton", 4, 496, "C418 - Dreiton");
    public static final DeferredItem<Item> TASWELL = registerDisc(DiscType.CREATIVE_DISC, "taswell", 5, 514, "C418 - Taswell");

    public static final DeferredItem<Item> SUBWOOFER_LULLABY = registerDisc(DiscType.OVERWORLD_DISC, "subwoofer_lullaby", 0, 208, "C418 - Subwoofer Lullaby");
    public static final DeferredItem<Item> LIVING_MICE = registerDisc(DiscType.OVERWORLD_DISC, "living_mice", 1, 177, "C418 - Living Mice");
    public static final DeferredItem<Item> HAGGSTROM = registerDisc(DiscType.OVERWORLD_DISC, "haggstrom", 2, 204, "C418 - Haggstrom");
    public static final DeferredItem<Item> DANNY = registerDisc(DiscType.OVERWORLD_DISC, "danny", 3, 254, "C418 - Danny");

    public static final DeferredItem<Item> OREO = ITEM_REGISTRY.register("oreo", 
        () -> new Item(
            new Item.Properties()
                .stacksTo(64).rarity(Rarity.RARE)
                .food(
                    new FoodProperties.Builder()
                        .nutrition(2).saturationModifier(0.1f)
                        .alwaysEdible().build()
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
        }
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS)
            event.accept(OREO);
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
