package dev.fulmineo.guild.mixin;

import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.structure.processor.StructureProcessorLists;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.fulmineo.guild.feature.ModifiableStructurePool;

@Mixin(StructurePools.class)
public class StructurePoolsMixin {
    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static void registerMixin(StructurePool spool, CallbackInfoReturnable<StructurePool> cir) {
        StructurePool pool = spool;
        pool = tryAddElementToPool(new Identifier("village/plains/houses"), pool, "guild:village/plains/houses/guild", StructurePool.Projection.RIGID, 2);
        pool = tryAddElementToPool(new Identifier("village/desert/houses"), pool, "guild:village/plains/houses/guild", StructurePool.Projection.RIGID, 2);
        pool = tryAddElementToPool(new Identifier("village/savanna/houses"), pool, "guild:village/plains/houses/guild", StructurePool.Projection.RIGID, 2);
        pool = tryAddElementToPool(new Identifier("village/taiga/houses"), pool, "guild:village/plains/houses/guild", StructurePool.Projection.RIGID, 2);
        pool = tryAddElementToPool(new Identifier("village/snowy/houses"), pool, "guild:village/plains/houses/guild", StructurePool.Projection.RIGID, 2);

        cir.setReturnValue(BuiltinRegistries.add(BuiltinRegistries.STRUCTURE_POOL, pool.getId(), pool));
        cir.cancel();
    }

	private static StructurePool tryAddElementToPool(Identifier targetPool, StructurePool pool, String elementId, StructurePool.Projection projection, int weight) {
        if(targetPool.equals(pool.getId())) {
            ModifiableStructurePool modPool = new ModifiableStructurePool(pool);
            modPool.addStructurePoolElement(StructurePoolElement.ofProcessedLegacySingle(elementId, StructureProcessorLists.EMPTY).apply(projection), weight);
            return modPool.getStructurePool();
        }
        return pool;
    }
}