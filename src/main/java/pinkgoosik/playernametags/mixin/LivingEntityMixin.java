package pinkgoosik.playernametags.mixin;

import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pinkgoosik.playernametags.PlayerNametagsMod;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

	@Inject(method = "onStatusEffectApplied", at = @At("TAIL"))
	void onStatusEffectApplied(StatusEffectInstance effect, Entity source, CallbackInfo ci) {
		if(LivingEntity.class.cast(this) instanceof ServerPlayerEntity player && effect.getEffectType().equals(StatusEffects.INVISIBILITY)) {
			var holder = PlayerNametagsMod.updateHolder(player);
			holder.getElements().forEach(virtualElement -> {
				if(virtualElement instanceof ItemDisplayElement element) {
					element.setCustomNameVisible(false);
					element.tick();
				}
			});
		}
	}

	@Inject(method = "onStatusEffectRemoved", at = @At("TAIL"))
	void onStatusEffectRemoved(StatusEffectInstance effect, CallbackInfo ci) {
		if(LivingEntity.class.cast(this) instanceof ServerPlayerEntity player && effect.getEffectType().equals(StatusEffects.INVISIBILITY)) {
			var holder = PlayerNametagsMod.updateHolder(player);
			holder.getElements().forEach(virtualElement -> {
				if(virtualElement instanceof ItemDisplayElement element) {
					element.setCustomNameVisible(true);
					element.tick();
				}
			});
		}
	}
}
