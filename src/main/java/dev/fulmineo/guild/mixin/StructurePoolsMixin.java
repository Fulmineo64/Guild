package dev.fulmineo.guild.mixin;

import net.minecraft.registry.Registerable;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import com.mojang.datafixers.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructurePools.class)
public class StructurePoolsMixin {
    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static void registerMixin(Registerable<StructurePool> structurePoolsRegisterable, String id, StructurePool pool, CallbackInfoReturnable<StructurePool> cir) {
       	tryAddElementToPool("village/plains/houses", id, pool, "guild:village/plains/houses/guild", StructurePool.Projection.RIGID, 2);
       	tryAddElementToPool("village/desert/houses", id, pool, "guild:village/plains/houses/guild", StructurePool.Projection.RIGID, 2);
       	tryAddElementToPool("village/savanna/houses", id, pool, "guild:village/plains/houses/guild", StructurePool.Projection.RIGID, 2);
       	tryAddElementToPool("village/taiga/houses", id, pool, "guild:village/plains/houses/guild", StructurePool.Projection.RIGID, 2);
       	tryAddElementToPool("village/snowy/houses", id, pool, "guild:village/plains/houses/guild", StructurePool.Projection.RIGID, 2);
    }

    // TODO: Check if this works
    private static void tryAddElementToPool(String targetPool, String currentPool, StructurePool pool, String elementId, StructurePool.Projection projection, int weight) {
        if (targetPool.equals(currentPool)) {
            StructurePoolElement element = StructurePoolElement.ofLegacySingle(elementId).apply(projection);
            for (int i = 0; i < weight; i++) {
                ((StructurePoolAccess)pool).guild$getElementCounts().add(Pair.of(element, weight));
            }
        }
    }
}