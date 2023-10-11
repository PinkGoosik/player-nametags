package pinkgoosik.playernametags.mixin;

import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pinkgoosik.playernametags.PlayerNametagsMod;

@Mixin(Entity.class)
public abstract class EntityMixin {

	@Inject(method = "setSneaking", at = @At("HEAD"))
	void onSneaking(boolean sneaking, CallbackInfo ci) {
		if(PlayerNametagsMod.config.enabled && Entity.class.cast(this) instanceof ServerPlayerEntity player && !player.isInvisible()) {
			var holder = PlayerNametagsMod.updateHolder(player);
			holder.getElements().forEach(virtualElement -> {
				if(virtualElement instanceof ItemDisplayElement element) {
					switch (PlayerNametagsMod.config.whenSneaking) {
						case "gray-out" -> {
							element.setSneaking(sneaking);
							element.tick();
						}
						case "hide" -> {
							element.setCustomNameVisible(!sneaking);
							element.tick();
						}
					}
				}
			});
		}
	}
}
