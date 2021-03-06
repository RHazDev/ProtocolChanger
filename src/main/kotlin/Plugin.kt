package hazae41.minecraft.fakeprotocol

import hazae41.minecraft.kotlin.bungee.*
import hazae41.minecraft.kotlin.catch
import hazae41.minecraft.kotlin.ex
import hazae41.minecraft.kotlin.lowerCase
import net.md_5.bungee.api.event.ProxyPingEvent
import net.md_5.bungee.event.EventPriority.*

class Plugin: BungeePlugin() {
    override fun onEnable() {
        update(67965)
        init(Config)
        listen()
    }
}

fun Plugin.placeholders(s: String) = s
    .replace("%max%", proxy.config.playerLimit.toString())
    .replace("%online%", proxy.onlineCount.toString())
    .replace("&", "§")

object Config: PluginConfigFile("config"){
    val allowed by intList("allowed")
    val priority by string("priority", "highest")
    val name by string("name")
    val protocols get() = config.getSection("protocols")!!
}

fun Plugin.process(e: ProxyPingEvent) = e.response?.version?.also{
    val version = e.connection.version
    if(version in Config.allowed) {
        it.protocol = version
    }
    else {
        var name = Config.protocols.getString(version.toString())
        if(name.isBlank()) name = Config.name
        it.name = placeholders(name)
        it.protocol = 0
    }
}

fun Plugin.listen() {
    infix fun String.eic(other: String) = equals(other, ignoreCase = true)
    listen<ProxyPingEvent>(LOWEST){
        if(Config.priority eic "lowest") process(it)
    }
    listen<ProxyPingEvent>(LOW){
        if(Config.priority eic "low") process(it)
    }
    listen<ProxyPingEvent>(NORMAL){
        if(Config.priority eic "normal") process(it)
    }
    listen<ProxyPingEvent>(HIGH){
        if(Config.priority eic "high") process(it)
    }
    listen<ProxyPingEvent>(HIGHEST){
        if(Config.priority eic "highest") process(it)
    }
}