package io.github.lunastela.extra_discs.datagen;

import javax.annotation.Nullable;

import io.github.lunastela.extra_discs.DiscType;
import io.github.lunastela.extra_discs.ExtraDiscs;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public record TypedRecordHolder(
    DiscType discType,
    String discName,
    int comparatorOutput,
    float lengthInSeconds,
    @Nullable String appender,
    DeferredItem<Item> registryObject,
    DeferredHolder<SoundEvent, SoundEvent> soundHolder
) {
    public String getDiscID() {
        return ExtraDiscs.getDiscID(this.discType, this.discName);
    }
}