package io.github.pylonmc.rebar.block.base

import io.github.pylonmc.rebar.Rebar
import io.github.pylonmc.rebar.block.BlockListener
import io.github.pylonmc.rebar.block.BlockListener.logEventHandleErr
import io.github.pylonmc.rebar.block.BlockStorage
import io.github.pylonmc.rebar.block.context.BlockCreateContext
import io.github.pylonmc.rebar.event.api.MultiListener
import io.github.pylonmc.rebar.event.api.annotation.MultiHandlers
import io.github.pylonmc.rebar.event.api.annotation.UniversalHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockSpreadEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.jetbrains.annotations.ApiStatus

@Suppress("unused")
interface RebarFire {
    fun onDamageEntity(event: EntityDamageEvent, priority: EventPriority) {}
    fun onFireSpread(event: BlockSpreadEvent, priority: EventPriority) {}

    @ApiStatus.Internal
    companion object : MultiListener {
        @UniversalHandler
        private fun onDamageEntity(event: EntityDamageEvent, priority: EventPriority) {
            if (event.cause != EntityDamageEvent.DamageCause.FIRE) return

            val rebarBlock = BlockStorage.get(event.entity.location)
            if (rebarBlock !is RebarFire) {
                return
            }

            try {
                MultiHandlers.handleEvent(rebarBlock, "onDamageEntity", event, priority)
            } catch (e: Exception) {
                BlockListener.logEventHandleErr(event, e, rebarBlock)
            }
        }

        @UniversalHandler
        private fun onFireSpread(event: BlockSpreadEvent, priority: EventPriority) {
            val rebarBlock = BlockStorage.get(event.source)
            if (rebarBlock !is RebarFire) return

            try {
                MultiHandlers.handleEvent(rebarBlock, "onFireSpread", event, priority)
                
                if (!event.isCancelled) {
                    // place new fire
                    BlockStorage.placeBlock(
                        event.block,
                        rebarBlock.key,
                        BlockCreateContext.PluginGenerate(Rebar.javaPlugin, block = event.block)
                    )
                }
            } catch (e: Exception) {
                BlockListener.logEventHandleErr(event, e, rebarBlock)
            }
        }
    }
}