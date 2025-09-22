package io.github.lunastela.extra_discs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = ExtraDiscs.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ExtraDiscsConfig {
    public static Map<String, DiscType> mobDropMap = new HashMap<String, DiscType>();

    private static Map<DiscType, ModConfigSpec.ConfigValue<List<? extends String>>> entityBuilderList = 
        new HashMap<DiscType, ModConfigSpec.ConfigValue<List<? extends String>>>();

    private static ModConfigSpec.Builder createBuilder() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        for (DiscType discType : DiscType.values()) {
            entityBuilderList.put(discType,
                builder.comment("What mobs should drop " + discType.englishLiteral + "s.")
                .translation("item." + ExtraDiscs.MODID + "." + discType.label)
                .defineListAllowEmpty(
                    discType.label + "_entities", List.of(discType.defaultMobDrop), 
                    () -> "", ExtraDiscsConfig::validateEntityName
                )
            );
        }
        return builder;
    }

    private static boolean validateEntityName(final Object obj) {
        return obj instanceof String entityName && BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(entityName));
    }
    private static final ModConfigSpec.Builder BUILDER = createBuilder();

    private static final ModConfigSpec.BooleanValue DISC_11_DROP = BUILDER
        .comment("Whether players drop Disc 11 when dying to a Skeleton")
        .define("disc_eleven_drop", true);

    private static final ModConfigSpec.DoubleValue DISC_11_DROP_CHANCE = BUILDER
        .comment("The percentage chance for Disc 11 to drop when being killed by a Skeleton")
        .defineInRange("disc_eleven_drop_rate", 0.05, 0, 1);

    // private static final ModConfigSpec.BooleanValue OREO_ITEM_ENABLED = BUILDER
    //     .translation("item." + ExtraDiscs.MODID + ".oreo")
    //     .comment("Whether to enable the Oreo item as a result of combining two discs in a crafting table.")
    //     .define("enableOreoItem", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean discElevenDrop = true;
    public static double discElevenDropRate = 0.05;
    // public static boolean enableOreoItem = true;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        for (DiscType discType : entityBuilderList.keySet()) {
            ModConfigSpec.ConfigValue<List<? extends String>> entityConfig = entityBuilderList.get(discType);
            List<String> correspondingEntities = entityConfig.get().stream().collect(Collectors.toList());
            for (int i = 0; i < correspondingEntities.size(); i++) {
                mobDropMap.put("entities/" + correspondingEntities.get(i), discType);
            }
        }
        discElevenDrop = DISC_11_DROP.get();
        discElevenDropRate = DISC_11_DROP_CHANCE.get();
        // enableOreoItem = OREO_ITEM_ENABLED.get();
    }
}
