package cn.ussshenzhou.gravitywar.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author USS_Shenzhou
 */
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Redirect(method = "handleTakeItemEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;add(Lnet/minecraft/client/particle/Particle;)V"))
    private void redirectAddParticle(ParticleEngine particleEngine, Particle particle) {
    }
}
