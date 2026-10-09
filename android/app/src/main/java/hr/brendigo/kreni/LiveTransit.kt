package hr.brendigo.kreni

import android.content.Context
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URI
import java.time.Instant
import kotlin.math.ceil

data class Vehicle(val id: String, val route: String, val mode: String,
    val lat: Double, val lon: Double, val observedAt: Long, val provider: String)
data class Arrival(val id: String, val stopId: String, val route: String, val mode: String,
    val scheduledAt: Long?, val predictedAt: Long?, val updatedAt: Long, val provider: String) {
    fun minutes(now: Long): Int? {
        val time = predictedAt ?: scheduledAt ?: return null
        return if (time < now - 30 || time > now + 7200) null
        else ceil((time - now).coerceAtLeast(0L) / 60.0).toInt()
    }
    fun estimated(now: Long) = predictedAt != null && updatedAt <= now + 15 &&
        now - updatedAt in 0..90
}
data class LiveData(val city: String, val provider: String, val generatedAt: Long,
    val vehicles: List<Vehicle>, val arrivals: List<Arrival>)
data class LiveProvider(val id: String, val city: String, val enabled: Boolean, val endpoint: String?)

object LiveCodec {
    private val modes = setOf("bus","tram","train","ferry","catamaran")
    fun providers(text: String): List<LiveProvider> {
        val root = JSONObject(text)
        require(root.getInt("schemaVersion") == 1)
        val array = root.getJSONArray("providers")
        return (0 until array.length()).map { n ->
            val x = array.getJSONObject(n)
            val url = x.optString("endpoint").takeIf { it.startsWith("https://") }
            LiveProvider(x.getString("id"),x.getString("city"),x.getBoolean("enabled") && url != null,url)
        }
    }
    fun parse(text: String, city: String, provider: String, now: Long): LiveData {
        require(text.length <= 524288)
        val root = JSONObject(text)
        require(root.getInt("schemaVersion") == 1 && root.getString("city") == city &&
            root.getString("provider") == provider)
        val at = root.getLong("generatedAt")
        require(at <= now + 15 && now - at in 0..90)
        val vs = root.getJSONArray("vehicles")
        val asJson = root.getJSONArray("arrivals")
        require(vs.length() <= 1500 && asJson.length() <= 6000)
        val vehicles = (0 until vs.length()).mapNotNull { n ->
            val x = vs.getJSONObject(n)
            val lat = x.getDouble("lat"); val lon = x.getDouble("lon")
            val seen = x.getLong("observedAt"); val mode = x.getString("mode")
            if (!lat.isFinite() || !lon.isFinite() || lat !in 40.0..47.0 ||
                lon !in 12.0..20.0 || mode !in modes || seen > now + 15 ||
                now - seen !in 0..90) null
            else Vehicle(x.getString("id"),x.getString("route"),mode,lat,lon,seen,provider)
        }.distinctBy { it.id }
        val arrivals = (0 until asJson.length()).mapNotNull { n ->
            val x = asJson.getJSONObject(n)
            val seen = x.getLong("updatedAt"); val mode = x.getString("mode")
            if (mode !in modes || seen > now + 15 || now - seen !in 0..90) null
            else {
                val scheduled = x.optLong("scheduledAt").takeIf { it > 0 }
                val predicted = x.optLong("predictedAt").takeIf { it > 0 }
                if (scheduled == null && predicted == null) null
                else Arrival(x.getString("id"),x.getString("stopId"),x.getString("route"),
                    mode,scheduled,predicted,seen,provider)
            }
        }.distinctBy { it.id }
        return LiveData(city,provider,at,vehicles,arrivals)
    }
    fun sources(context: Context): List<LiveProvider> = context.assets
        .open("live_sources.json").bufferedReader().use { providers(it.readText()) }
}

class LiveNetwork(private val sources: List<LiveProvider>) {
    suspend fun refresh(city: String, now: Long = Instant.now().epochSecond): List<LiveData> =
        withContext(Dispatchers.IO) {
            sources.filter { it.enabled && it.city == city }.mapNotNull { source ->
                try {
                    val u = URI(source.endpoint)
                    require(u.scheme == "https" && u.host != null && u.userInfo == null &&
                        u.port == -1 && u.rawQuery == null && u.fragment == null)
                    val connection = u.toURL().openConnection() as HttpURLConnection
                    try {
                        connection.instanceFollowRedirects = false
                        connection.connectTimeout = 5000; connection.readTimeout = 5000
                        connection.setRequestProperty("Accept","application/json")
                        require(connection.responseCode == 200)
                        val bytes = connection.inputStream.use { it.readNBytes(524289) }
                        require(bytes.size <= 524288)
                        LiveCodec.parse(bytes.toString(Charsets.UTF_8),city,source.id,now)
                    } finally { connection.disconnect() }
                } catch (_: Exception) { null }
            }
        }
}
