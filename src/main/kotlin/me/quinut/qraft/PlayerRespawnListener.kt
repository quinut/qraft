package me.quinut.qraft

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Statistic
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack
import java.util.Random

class PlayerRespawnListener(private val plugin: Qraft) : Listener {

    private val random = Random()

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val player = event.player
        val deathCount = player.getStatistic(Statistic.DEATHS)

        // Give the player a cake
        player.inventory.addItem(ItemStack(Material.CAKE))

        // Give random colored candles based on death count
        val candleMaterials = Material.entries.filter { it.name.endsWith("_CANDLE") && it.name != "CANDLE" }
        if (candleMaterials.isNotEmpty()) {
            val randomCandleMaterial = candleMaterials[random.nextInt(candleMaterials.size)]
            player.inventory.addItem(ItemStack(randomCandleMaterial, deathCount))
        }

        // Send the action bar message
        player.sendActionBar(net.kyori.adventure.text.Component.text("새로 태어나셨네요!"))
        player.sendMessage("§f${deathCount + 1}번째 삶을 시작해 볼까요?")
    }
}
