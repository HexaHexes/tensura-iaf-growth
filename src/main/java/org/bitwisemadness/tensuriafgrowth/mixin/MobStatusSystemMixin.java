package org.bitwisemadness.tensuriafgrowth.mixin;

import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * TensuraRPGSystem exclusion for dragons - issue #31.
 *
 * <p>TensuraRPGSystem's status layer ({@code MobStatusSystem}) runs a
 * {@code +1%} HP-per-level bake on every {@code Mob} it initializes. Dragons
 * are not RPS levels; their HP is the pure Ice and Fire growth curve, so the
 * bake would fight the growth pin every time a dragon gains a level. The layer
 * funnels every mutating path through the static predicate
 * {@code MobStatusSystem.excluded(Mob)} - true today only for clone entities -
 * and that predicate is the single choke point, so this mixin makes it report
 * {@code true} for {@link DragonBaseEntity} as well.
 *
 * <p>This is <em>Path B</em>, not the {@code ep_initiate_excluded} tag
 * (Path A): that tag makes the join handler skip {@code updateEntityExistence}
 * for marked entities, which would strip dragons of their granted abilities -
 * breaths, resistances and the secondary elements. {@code excluded} only
 * suppresses the RPS stat bake, leaving Tensura's existence initialization
 * intact, and it keeps other mobs (non-dragon) on the {@code +1%/level} layer
 * exactly as before.
 *
 * <p>String-targeted and {@link Pseudo}: TensuraRPGSystem is not on this mod's
 * compile classpath (it is a pack member, not a declared dependency), and the
 * mixin is skipped harmlessly if the class is absent at runtime. When it <em>is</em>
 * present, {@code defaultRequire: 1} makes the injector hard-fail if the method
 * ever drifts, so a rename is caught in testing rather than silently unpinning
 * dragons.
 */
@Pseudo
@Mixin(targets = "com.japaeditor.tensuraevolution.core.MobStatusSystem", remap = false)
public final class MobStatusSystemMixin {
    /**
     * Dragons are reported as excluded from the RPS status/stat layer, so the
     * {@code +1% HP/level} bake never runs on them. Other mobs fall through to
     * the original clone-only logic untouched.
     */
    @Inject(
        method = "excluded(Lnet/minecraft/world/entity/Mob;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void tensura_iaf_growth$excludeDragons(Mob mob, CallbackInfoReturnable<Boolean> cir) {
        if (mob instanceof DragonBaseEntity) {
            cir.setReturnValue(true);
        }
    }
}
