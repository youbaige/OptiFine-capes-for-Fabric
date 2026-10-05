package com.optifinecapes.mixin;

import com.mojang.authlib.GameProfile;
import com.optifinecapes.CapeService;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Supplier;

@Mixin(PlayerInfo.class)
public abstract class PlayerInfoMixin {
    @Shadow
    @Final
    private GameProfile profile;

    @Inject(method = "createSkinLookup", at = @At("HEAD"))
    private static void optifinecapes$requestCape(GameProfile profile, CallbackInfoReturnable<Supplier<PlayerSkin>> cir) {
        CapeService.request(profile);
    }

    @Inject(method = "getSkin", at = @At("TAIL"), cancellable = true)
    private void optifinecapes$overrideCape(CallbackInfoReturnable<PlayerSkin> cir) {
        String name = this.profile == null ? null : this.profile.name();
        if (name == null) {
            return;
        }
        Optional<Identifier> texture = CapeService.getCapeTexture(name);
        if (texture.isEmpty()) {
            return;
        }
        PlayerSkin old = cir.getReturnValue();
        Identifier id = texture.get();
        ClientAsset.Texture capeTexture = new ClientAsset.ResourceTexture(id, id);
        cir.setReturnValue(new PlayerSkin(old.body(), capeTexture, capeTexture, old.model(), old.secure()));
    }
}
