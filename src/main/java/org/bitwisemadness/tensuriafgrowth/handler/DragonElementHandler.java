package org.bitwisemadness.tensuriafgrowth.handler;

import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import com.iafenvoy.iceandfire.entity.FireDragonEntity;
import com.iafenvoy.iceandfire.entity.IceDragonEntity;
import com.iafenvoy.iceandfire.entity.LightningDragonEntity;
import io.github.manasmods.tensura.ability.magic.Element;
import io.github.manasmods.tensura.damage.TensuraDamageSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Tags a dragon's melee hit with its natural element so Tensura treats the
 * bite as elemental damage: fire dragons deal flame damage, ice dragons deal
 * water damage, lightning dragons deal wind damage (Tensura has no lightning
 * element; wind is its closest match and is what the lightning branch's
 * secondary element grants as a skill).
 *
 * <p>Ice and Fire builds melee hits as {@code damageSources().mobAttack(this)},
 * a fresh untagged {@link DamageSource} per swing, so the tag has to be set on
 * the way in. {@link LivingIncomingDamageEvent} runs before every Tensura stage
 * that reads the element - the attacker's element boost and the defender's
 * elemental resistance - and Tensura never clears it again afterwards.
 *
 * <p>The handler only reacts to {@code mobAttack} sources, so breath weapons,
 * fire ticks and every other damage path keep whatever element they already
 * carry.
 */
public final class DragonElementHandler {
    private DragonElementHandler() {
    }

    /** Applied by {@code TensuraIafGrowth}'s constructor on the game bus. */
    public static void onLivingAttack(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        if (!source.is(DamageTypes.MOB_ATTACK) || !(source.getEntity() instanceof DragonBaseEntity dragon)) {
            return;
        }

        Element element = naturalElement(dragon);
        if (element != null && source instanceof TensuraDamageSource tensuraSource) {
            tensuraSource.tensura$setElement(element);
        }
    }

    /** The natural element of each dragon species, or null for a type with none. */
    private static Element naturalElement(DragonBaseEntity dragon) {
        if (dragon instanceof FireDragonEntity) {
            return Element.FLAME;
        }
        if (dragon instanceof IceDragonEntity) {
            return Element.WATER;
        }
        if (dragon instanceof LightningDragonEntity) {
            return Element.WIND;
        }
        return null;
    }
}
