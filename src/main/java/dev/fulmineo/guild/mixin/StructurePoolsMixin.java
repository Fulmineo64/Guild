package dev.fulmineo.guild.mixin;

import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.structure.processor.StructureProcessorLists;
import net.minecraft.util.Identifier;
import com.mojang.datafixers.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructurePools.class)
public class StructurePoolsMixin {
    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static void registerMixin(StructurePool spool, CallbackInfoReturnable<StructurePool> cir) {
        StructurePool pool = spool;
       	tryAddElementToPool(new Identifier("village/plains/houses"), pool, "guild:village/plains/houses/guild", StructurePool.Projection.RIGID, 2);
       	tryAddElementToPool(new Identifier("village/desert/houses"), pool, "guild:village/plains/houses/guild", StructurePool.Projection.RIGID, 2);
       	tryAddElementToPool(new Identifier("village/savanna/houses"), pool, "guild:village/plains/houses/guild", StructurePool.Projection.RIGID, 2);
       	tryAddElementToPool(new Identifier("village/taiga/houses"), pool, "guild:village/plains/houses/guild", StructurePool.Projection.RIGID, 2);
       	tryAddElementToPool(new Identifier("village/snowy/houses"), pool, "guild:village/plains/houses/guild", StructurePool.Projection.RIGID, 2);
    }

	private static void tryAddElementToPool(Identifier targetPool, StructurePool pool, String elementId, StructurePool.Projection projection, int weight) {
        if(targetPool.equals(pool.getId())) {
            StructurePoolElement element = StructurePoolElement.ofProcessedLegacySingle(elementId, StructureProcessorLists.EMPTY).apply(projection);
            for (int i = 0; i < weight; i++) {
                ((StructurePoolAccess)pool).guild$getElements().add(element);
            }
            ((StructurePoolAccess)pool).guild$getElementCounts().add(Pair.of(element, weight));
        }
    }
}