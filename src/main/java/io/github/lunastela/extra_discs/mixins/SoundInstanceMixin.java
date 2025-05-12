package io.github.lunastela.extra_discs.mixins;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.lunastela.extra_discs.ExtraDiscs;
import io.github.lunastela.extra_discs.MonoWrapper;
import net.minecraft.Util;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.JOrbisAudioStream;
import net.minecraft.client.sounds.LoopingAudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.sounds.SoundSource;

@Mixin(SoundInstance.class)
public interface SoundInstanceMixin {

    @Inject(
        method = "getStream(Lnet/minecraft/client/sounds/SoundBufferLibrary;Lnet/minecraft/client/resources/sounds/Sound;Z)Ljava/util/concurrent/CompletableFuture;",
        at = @At(value = "RETURN"), cancellable = true
    )
    private void returnMonoAudioStream(SoundBufferLibrary soundBuffer, Sound sound, boolean looping, CallbackInfoReturnable<CompletableFuture<AudioStream>> cir) {
        SoundInstance soundInstance = (SoundInstance) (Object) this;
        if (!soundInstance.isRelative() 
        && (soundInstance.getAttenuation() != Attenuation.NONE)
        && (soundInstance.getSource() == SoundSource.RECORDS)) {
            CompletableFuture<AudioStream> futureAudioStream = CompletableFuture.supplyAsync(() -> {
                try {
                    InputStream inputStream = soundBuffer.resourceManager.open(sound.getPath());
                    return (AudioStream) (new MonoWrapper(new JOrbisAudioStream(inputStream)));
                } catch (IOException var4) {
                    throw new CompletionException(var4);
                }
            }, Util.nonCriticalIoPool());
            cir.setReturnValue(futureAudioStream);
        }
    }
}
