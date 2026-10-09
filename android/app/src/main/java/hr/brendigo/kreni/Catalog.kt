package hr.brendigo.kreni

import android.content.Context
import org.json.JSONObject

data class City(val id: String, val name: String, val lat: Double, val lon: Double, val trams: Boolean)
data class Stop(val id: String, val city: String, val name: String, val lat: Double, val lon: Double, val modes: List<String>)
data class Catalog(val cities: List<City>, val stops: List<Stop>) {
    fun stopsIn(cityId: String): List<Stop> = stops.filter { it.city == cityId }
}

object CatalogReader {
    fun parse(text: String): Catalog {
        val root = JSONObject(text)
        require(root.getInt("schemaVersion") == 1)
        val cities = root.getJSONArray("cities")
        val stops = root.getJSONArray("stops")
        val parsedCities = (0 until cities.length()).map { n ->
            val c = cities.getJSONObject(n)
            City(c.getString("id"), c.getString("name"), c.getDouble("lat"),
                c.getDouble("lon"), c.getBoolean("trams"))
        }
        val parsedStops = (0 until stops.length()).map { n ->
            val s = stops.getJSONObject(n)
            val modes = s.getJSONArray("modes")
            Stop(s.getString("id"), s.getString("city"), s.getString("name"),
                s.getDouble("lat"), s.getDouble("lon"),
                (0 until modes.length()).map { modes.getString(it) })
        }
        require(parsedCities.map { it.id }.toSet().size == parsedCities.size)
        require(parsedStops.map { it.id }.toSet().size == parsedStops.size)
        require(parsedStops.all { stop -> parsedCities.any { it.id == stop.city } })
        require(parsedStops.all { stop ->
            "tram" !in stop.modes || parsedCities.first { it.id == stop.city }.trams
        })
        return Catalog(parsedCities, parsedStops)
    }
    fun fromAssets(context: Context): Catalog =
        parse(context.assets.open("catalog.json").bufferedReader().use { it.readText() })
}
