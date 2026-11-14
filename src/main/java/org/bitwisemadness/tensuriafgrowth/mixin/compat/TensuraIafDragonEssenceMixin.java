package org.bitwisemadness.tensuriafgrowth.mixin.compat;

import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import io.github.manasmods.tensura.registry.item.TensuraMobDropItems;
import org.bitwisemadness.tensuriafgrowth.config.DragonEpConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ported from TenSura Compat: Ice and Fire (MinhEragon), retargeted to
 * IceAndFireCE 2.2 (issue #80): a dragon within the configured stage window
 * may drop Tensura's Dragon Essence on death - a per-death roll against the
 * configured chance, after the vanilla loot (the injected tail).
 *
 * <p>The stage window and chance ride this mod's own config
 * ({@code [dragon_essence]} in the common toml); the original's separate
 * config file is absorbed into it. IceAndFireCE 2.2 hooks this on
 * {@code dropFromLootTable} - the dragon's own loot path - where the
 * original used vanilla's {@code dropCustomDeathLoot}; same roll point,
 * same conditions. The original mixin also carried a TamableAnimal
 * superclass bridge and a narrowed level() override - neither is needed
 * for the drop itself, so the port keeps only the behavior.</p>
 */
@Mixin(value = DragonBaseEntity.class, remap = true)
public class TensuraIafDragonEssenceMixin {

    @Inject(method = "dropFromLootTable", at = @At("TAIL"))
    private void tensuraIaf$dropDragonEssence(CallbackInfo ci) {
        DragonBaseEntity dragon = (DragonBaseEntity) (Object) this;
        int stage = dragon.getDragonStage();
        if (stage < DragonEpConfig.ESSENCE_MIN_STAGE.get()
                || stage > DragonEpConfig.ESSENCE_MAX_STAGE.get()) {
            return;
        }
        if (dragon.getRandom().nextFloat() >= DragonEpConfig.ESSENCE_CHANCE.get()) {
            return;
        }
        dragon.spawnAtLocation(TensuraMobDropItems.DRAGON_ESSENCE.get());
    }
}
