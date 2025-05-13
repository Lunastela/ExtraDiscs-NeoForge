package io.github.lunastela.extra_discs.datagen;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import io.github.lunastela.extra_discs.DiscType;
import io.github.lunastela.extra_discs.ExtraDiscs;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxSong;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredItem;

@EventBusSubscriber(modid = ExtraDiscs.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    // Thank you Mojang for removing this in 1.21.1, your days are numbered.
    public static final TagKey<Item> MUSIC_DISC_TAG = TagKey.create(Registries.ITEM, ResourceLocation.tryBuild("c", "music_discs"));

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeServer(), new ItemModelProvider(packOutput, ExtraDiscs.MODID, existingFileHelper) {
            @Override
            protected void registerModels() {
                for (int i = 0; i < ExtraDiscs.typedRecordHolderList.size(); i++)
                    basicItem(ExtraDiscs.typedRecordHolderList.get(i).registryObject().get());
                basicItem(ExtraDiscs.OREO.get());
            }
        });

        BlockTagsProvider blockTagGenerator = generator.addProvider(event.includeServer(),
                new BlockTagsProvider(packOutput, lookupProvider, ExtraDiscs.MODID, existingFileHelper){
                    @Override protected void addTags(Provider arg0) {}});
        generator.addProvider(event.includeServer(), new ItemTagsProvider(packOutput, lookupProvider, blockTagGenerator.contentsGetter(), ExtraDiscs.MODID, existingFileHelper) {
            @Override
            protected void addTags(Provider arg0) {
                for (int i = 0; i < ExtraDiscs.typedRecordHolderList.size(); i++) {
                    tag(ExtraDiscs.typedRecordHolderList.get(i).discType().itemTag).add(ExtraDiscs.typedRecordHolderList.get(i).registryObject().get());
                    tag(MUSIC_DISC_TAG).add(ExtraDiscs.typedRecordHolderList.get(i).registryObject().get());
                }
            }
        });

        generator.addProvider(event.includeClient(), new LanguageProvider(packOutput, ExtraDiscs.MODID, "en_us") {
            @Override
            protected void addTranslations() {
                for (DiscType discType : DiscType.values())
                    add("item." + ExtraDiscs.MODID + "." + discType.label + ".name", discType.englishLiteral);
                add("item." + ExtraDiscs.MODID + ".oreo", "Oreo");

                for (int i = 0; i < ExtraDiscs.typedRecordHolderList.size(); i++) {
                    TypedRecordHolder recordHolder = ExtraDiscs.typedRecordHolderList.get(i);
                    add("item." + ExtraDiscs.MODID + "." + recordHolder.getDiscID() + ".desc", recordHolder.englishLiteral());
                }
            }
        });

        generator.addProvider(true, new SoundDefinitionsProvider(packOutput, ExtraDiscs.MODID, existingFileHelper) {
            @Override
            public void registerSounds() {
                for (int i = 0; i < ExtraDiscs.typedRecordHolderList.size(); i++) {
                    TypedRecordHolder recordHolder = ExtraDiscs.typedRecordHolderList.get(i);
                    SoundDefinition.Sound recordSound = sound(ResourceLocation.fromNamespaceAndPath("minecraft", "music/" + recordHolder.discType().filePath
                        + (recordHolder.appender() == null ? "" : ("/" + recordHolder.appender())) + "/" + recordHolder.discName())).stream();
                    add(ResourceLocation.fromNamespaceAndPath(ExtraDiscs.MODID, recordHolder.discName()), SoundDefinition.definition().with(recordSound));
                }
            } 
        });

        RegistrySetBuilder jukeboxSongBuilder = new RegistrySetBuilder();
        jukeboxSongBuilder.add(Registries.JUKEBOX_SONG, bootstrap -> {
            for (int i = 0; i < ExtraDiscs.typedRecordHolderList.size(); i++) {
                TypedRecordHolder recordHolder = ExtraDiscs.typedRecordHolderList.get(i);
                bootstrap.register(
                    ExtraDiscs.createSong(recordHolder.discName()),
                    new JukeboxSong(
                        recordHolder.soundHolder().getDelegate(),
                        Component.translatable("item." + ExtraDiscs.MODID + "." + recordHolder.getDiscID() + ".desc"),
                        recordHolder.lengthInSeconds(),
                        recordHolder.comparatorOutput()
                    )
                );
            }
        });
        generator.addProvider(event.includeServer(), 
            new DatapackBuiltinEntriesProvider(packOutput, lookupProvider, jukeboxSongBuilder, Set.of(ExtraDiscs.MODID))
        );
    }
}
