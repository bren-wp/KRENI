package hr.brendigo.kreni

import android.content.Context
import org.json.JSONObject
import kotlinx.coroutines.CancellationException
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
    fun fresh(now: Long): Boolean = updatedAt in (now - 90)..(now + 15)
    fun minutes(now: Long): Int? {
        if (!fresh(now)) return null
        val time = predictedAt ?: scheduledAt ?: return null
        return if (time < now - 30 || time > now + 7200) null
        else ceil((time - now).coerceAtLeast(0L) / 60.0).toInt()
    }
    fun estimated(now: Long) = predictedAt != null && fresh(now)
}
data class LiveData(val city: String, val provider: String, val generatedAt: Long,
    val vehicles: List<Vehicle>, val arrivals: List<Arrival>) {
    fun freshFor(requestedCity: String, now: Long): Boolean =
        city == requestedCity && generatedAt in (now - 90)..(now + 15)

    fun filtered(now: Long): LiveData = copy(
        vehicles = vehicles.filter { it.observedAt in (now - 90)..(now + 15) },
        arrivals = arrivals.filter { it.fresh(now) }
    )
}
data class LiveProvider(val id: String, val city: String, val enabled: Boolean, val endpoint: String?, val format: String = "json")

object LiveCodec {
    private val modes = setOf("bus","tram","train","ferry","catamaran")
    fun providers(text: String): List<LiveProvider> {
        val root = JSONObject(text)
        require(root.getInt("schemaVersion") == 1)
        val array = root.getJSONArray("providers")
        return (0 until array.length()).map { n ->
            val x = array.getJSONObject(n)
            val url = x.optString("endpoint").takeIf {
                try {
                    val uri = URI(it)
                    uri.scheme == "https" && !uri.host.isNullOrBlank() &&
                        uri.userInfo == null && uri.port == -1 &&
                        uri.rawQuery == null && uri.fragment == null
                } catch (_: Exception) { false }
            }
            val approved = x.optBoolean("approvedForProduction", false) &&
                x.optString("licenseUrl").startsWith("https://")
            val format = x.optString("format", "json")
            LiveProvider(x.getString("id"),x.getString("city"),
                x.getBoolean("enabled") && approved && url != null && format in setOf("json", "gtfsrt"),
                url, format)
        }
    }
    fun parse(text: String, city: String, provider: String, now: Long): LiveData {
        require(text.length <= 524288)
        val root = JSONObject(text)
        require(root.getInt("schemaVersion") == 1 && root.getString("city") == city &&
            root.getString("provider") == provider)
        val at = root.getLong("generatedAt")
        require(at in (now - 90)..(now + 15))
        val vs = root.getJSONArray("vehicles")
        val asJson = root.getJSONArray("arrivals")
        require(vs.length() <= 1500 && asJson.length() <= 6000)
        val vehicles = (0 until vs.length()).mapNotNull { n ->
            val x = vs.getJSONObject(n)
            val lat = x.getDouble("lat"); val lon = x.getDouble("lon")
            val seen = x.getLong("observedAt"); val mode = x.getString("mode")
            if (!lat.isFinite() || !lon.isFinite() || lat !in 40.0..47.0 ||
                lon !in 12.0..20.0 || mode !in modes || seen !in (now - 90)..(now + 15)) null
            else Vehicle(x.getString("id"),x.getString("route"),mode,lat,lon,seen,provider)
        }.distinctBy { it.id }
        val arrivals = (0 until asJson.length()).mapNotNull { n ->
            val x = asJson.getJSONObject(n)
            val seen = x.getLong("updatedAt"); val mode = x.getString("mode")
            if (mode !in modes || seen !in (now - 90)..(now + 15)) null
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
    fun activeCount(city: String): Int = sources.count { it.enabled && it.city == city }

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
                        connection.setRequestProperty("Accept", if (source.format == "gtfsrt") "application/x-protobuf" else "application/json")
                        require(connection.responseCode == 200)
                        val bytes = connection.inputStream.use { it.readNBytes(524289) }
                        require(bytes.size <= 524288)
                        if (source.format == "gtfsrt")
                            GtfsRealtimeCodec.parse(bytes, city, source.id, Instant.now().epochSecond)
                        else LiveCodec.parse(bytes.toString(Charsets.UTF_8),city,source.id,
                            Instant.now().epochSecond)
                    } finally { connection.disconnect() }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) { null }
            }
        }
}
