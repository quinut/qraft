package me.quinut.qraft

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID
import kotlin.random.Random

class BreadManager(private val plugin: Qraft) {

    private val claimedPlayers = mutableSetOf<UUID>()

    fun initialize() {
        // Start the announcement scheduler to run every 10 minutes
        object : BukkitRunnable() {
            override fun run() {
                sendBreadAnnouncement()
            }
        }.runTaskTimer(plugin, 0L, 10 * 60 * 20L) // 10 minutes * 60 seconds * 20 ticks
    }

    fun sendBreadAnnouncement() {
        // Clear the list of players who have claimed from the previous announcement
        claimedPlayers.clear()

        val message = Component.text("§e[안내] §f빵 드실래요? (클릭)")
            .clickEvent(ClickEvent.runCommand("/claimbread"))

        Bukkit.getServer().broadcast(message)
    }

    fun giveBread(player: Player) {
        if (claimedPlayers.contains(player.uniqueId)) {
            player.sendMessage("§c이미 빵을 받으셨습니다.")
            return
        }

        val amount = Random.nextInt(1, 6) // 1 to 5
        player.inventory.addItem(ItemStack(Material.BREAD, amount))
        player.sendMessage("§a빵을 ${amount}개 받았습니다!")
        claimedPlayers.add(player.uniqueId)
    }
}
