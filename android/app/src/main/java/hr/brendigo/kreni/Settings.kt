package hr.brendigo.kreni

import android.content.Context
import android.util.AtomicFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class Preferences(
    val city: String = "zagreb",
    val dark: Boolean = false,
    val favorites: Set<String> = emptySet()
)

class Settings(private val context: Context, private val catalog: Catalog) {
    private val file = AtomicFile(File(context.filesDir, "kreni_preferences.json"))

    fun read(): Preferences = try {
        val root = JSONObject(file.openRead().bufferedReader().use { it.readText() })
        val city = root.optString("city", "zagreb").takeIf { id ->
            catalog.cities.any { it.id == id }
        } ?: "zagreb"
        val arr = root.optJSONArray("favorites") ?: JSONArray()
        val known = catalog.stops.map { it.id }.toSet()
        Preferences(city, root.optBoolean("dark", false),
            (0 until arr.length()).mapNotNull { arr.optString(it) }.filter { it in known }.toSet())
    } catch (_: Exception) {
        Preferences()
    }

    fun save(state: Preferences) {
        val root = JSONObject().put("schemaVersion", 1).put("city", state.city)
            .put("dark", state.dark).put("favorites", JSONArray(state.favorites.sorted()))
        var stream: java.io.FileOutputStream? = null
        try {
            stream = file.startWrite()
            stream.write(root.toString().toByteArray(Charsets.UTF_8))
            file.finishWrite(stream)
        } catch (_: Exception) {
            if (stream != null) file.failWrite(stream)
        }
    }
}
