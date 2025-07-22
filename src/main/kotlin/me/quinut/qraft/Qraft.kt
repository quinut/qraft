package me.quinut.qraft

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import java.util.*

class Qraft : JavaPlugin(), Listener {

    // A set to keep track of players currently in combat.
    private val combatTag = mutableSetOf<UUID>()
    // A map to store the scheduled tasks that remove players from combat.
    private val combatTasks = mutableMapOf<UUID, BukkitTask>()
    // Duration in seconds a player remains in combat after the last hit.
    private val combatTagDuration: Long = 15

    
    
    private lateinit var breadManager: BreadManager
    private lateinit var playerRespawnListener: PlayerRespawnListener

    override fun onEnable() {
        // Register the event listeners in this class.
        server.pluginManager.registerEvents(this, this)
        
        
        breadManager = BreadManager(this)
        breadManager.initialize()
        playerRespawnListener = PlayerRespawnListener(this)
        server.pluginManager.registerEvents(playerRespawnListener, this)


        
        
        getCommand("randombread")?.setExecutor(this)
        getCommand("claimbread")?.setExecutor(this)
        

        logger.info("Qraft plugin enabled!")
    }

    override fun onDisable() {
        // Clean up tasks when the plugin is disabled.
        combatTasks.values.forEach { it.cancel() }
        combatTasks.clear()
        combatTag.clear()
        logger.info("Qraft plugin disabled!")
    }

    

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        when (command.name.lowercase(Locale.getDefault())) {
            
            
            "randombread" -> {
                breadManager.sendBreadAnnouncement()
                sender.sendMessage("Bread announcement sent.")
                return true
            }
            "claimbread" -> {
                if (sender is Player) {
                    breadManager.giveBread(sender)
                    return true
                } else {
                    sender.sendMessage("This command can only be run by a player.")
                    return false
                }
            }
            
        }
        return super.onCommand(sender, command, label, args)
    }

    @EventHandler
    fun onPlayerDamage(event: EntityDamageByEntityEvent) {
        // Check if both the attacker and the victim are players.
        if (event.damager is Player && event.entity is Player) {
            val attacker = event.damager as Player
            val victim = event.entity as Player

            // Tag both players.
            tagPlayer(attacker)
            tagPlayer(victim)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        // If the player disconnects while in combat...
        if (combatTag.contains(player.uniqueId)) {
            // ...kill them.
            player.health = 0.0
            // Announce it to the server.
            Bukkit.broadcastMessage("${player.name}은 전투 중 등을 보였고, 죽었습니다.")
            // Clean up their combat tag info.
            removeCombatTag(player.uniqueId)
        }
    }

    private fun tagPlayer(player: Player) {
        val playerUUID = player.uniqueId

        // If the player is already in combat, cancel the old task to reset the timer.
        combatTasks[playerUUID]?.cancel()

        // Add the player to the combat set.
        if (!combatTag.contains(playerUUID)) {
            combatTag.add(playerUUID)
            player.sendMessage("전투 중입니다. 접속 종료하지 마세요.")
        }

        // Schedule a new task to remove the player from combat after the duration.
        combatTasks[playerUUID] = server.scheduler.runTaskLater(this, Runnable {
            removeCombatTag(playerUUID)
            player.sendMessage("더 이상 전투 중이 아닙니다.")
        }, combatTagDuration * 20L) // 20 ticks = 1 second
    }

    private fun removeCombatTag(playerUUID: UUID) {
        combatTag.remove(playerUUID)
        combatTasks.remove(playerUUID)?.cancel()
    }

    
}