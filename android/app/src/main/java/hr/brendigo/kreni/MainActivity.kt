package hr.brendigo.kreni

import android.os.Bundle
import java.time.Instant
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val catalog = CatalogReader.fromAssets(this)
        val store = Settings(this, catalog)
        val network = LiveNetwork(LiveCodec.sources(this))
        val captureTab = intent?.getIntExtra("kreni_capture_tab", -1) ?: -1
        setContent {
            var prefs by remember { mutableStateOf(store.read()) }
            var tab by remember { mutableIntStateOf(captureTab.coerceIn(0, 4)) }
            var selectedStop by remember { mutableStateOf<Stop?>(null) }
            var live by remember { mutableStateOf(emptyList<LiveData>()) }
            var now by remember { mutableLongStateOf(Instant.now().epochSecond) }
            val lifecycle = LocalLifecycleOwner.current.lifecycle
            val lifecycleState by lifecycle.currentStateAsState()
            val isStarted = lifecycleState.isAtLeast(Lifecycle.State.STARTED)
            LaunchedEffect(isStarted) {
                if (!isStarted) return@LaunchedEffect
                now = Instant.now().epochSecond
                while (true) {
                    delay(10_000)
                    now = Instant.now().epochSecond
                }
            }
            LaunchedEffect(prefs.city, isStarted) {
                live = emptyList()
                if (!isStarted || network.activeCount(prefs.city) == 0) return@LaunchedEffect
                val requestedCity = prefs.city
                while (true) {
                    live = network.refresh(requestedCity)
                    delay(30_000)
                }
            }
            val city = catalog.cities.firstOrNull { it.id == prefs.city } ?: catalog.cities.first()
            val stops = catalog.stopsIn(city.id)
            val currentLive = live.filter { it.freshFor(city.id, now) }.map { it.filtered(now) }
            val activeProviders = network.activeCount(city.id)
            fun update(value: Preferences) { prefs = value; store.save(value) }
            fun favorite(id: String) {
                update(prefs.copy(favorites = if (id in prefs.favorites) prefs.favorites - id
                    else prefs.favorites + id))
            }
            val dark = prefs.dark
            MaterialTheme(colorScheme = if (dark) darkColorScheme(
                primary = BrandBlue, background = BrandNavy, surface = BrandSurfaceDark,
                onBackground = Color.White, onSurface = Color.White
            ) else lightColorScheme(
                primary = BrandBlue, background = BrandBackground, surface = Color.White,
                onBackground = BrandNavy, onSurface = BrandNavy
            )) {
                if (!prefs.welcomeSeen && captureTab < 0) {
                    WelcomeScreen(onStart = { update(prefs.copy(welcomeSeen = true)) })
                } else {
                    Scaffold(
                        containerColor = if (dark) BrandNavy else BrandBackground,
                        bottomBar = {
                            NavigationBar(
                                containerColor = if (dark) BrandSurfaceDark else Color.White,
                                tonalElevation = 0.dp
                            ) {
                                val items = listOf(
                                    Triple("Početna", Icons.Default.Home, 0),
                                    Triple("Karta", Icons.Default.Map, 1),
                                    Triple("Polasci", Icons.Default.DirectionsBus, 2),
                                    Triple("Planer", Icons.AutoMirrored.Filled.AltRoute, 3),
                                    Triple("Više", Icons.Default.Menu, 4)
                                )
                                items.forEach { (name, icon, id) ->
                                    NavigationBarItem(
                                        selected = tab == id,
                                        onClick = { tab = id },
                                        icon = { Icon(icon, contentDescription = name) },
                                        label = { Text(name, maxLines = 1) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = BrandBlue,
                                            selectedTextColor = BrandBlue,
                                            indicatorColor = if (dark) BrandNavy else BrandPaleBlue
                                        )
                                    )
                                }
                            }
                        }
                    ) { padding ->
                        Box(Modifier.fillMaxSize().padding(padding)) {
                            when (tab) {
                                0 -> HomeScreen(
                                    city = city, stops = stops, favorites = prefs.favorites,
                                    live = currentLive, activeProviders = activeProviders,
                                    onOpenStop = { selectedStop = it; tab = 1 },
                                    onToggleFavorite = ::favorite,
                                    onShowMap = { tab = 1 }, onPlanner = { tab = 3 },
                                    onFavorites = { tab = 4 },
                                    onCitySelected = { update(prefs.copy(city = it)); selectedStop = null },
                                    cities = catalog.cities
                                )
                                1 -> LiveMapScreen(
                                    city = city, stops = stops, selectedStop = selectedStop,
                                    live = currentLive, now = now, activeProviders = activeProviders,
                                    favorites = prefs.favorites, onToggleFavorite = ::favorite,
                                    onSelectedStop = { selectedStop = it }
                                )
                                2 -> DeparturesScreen(
                                    city = city, stops = stops, live = currentLive,
                                    now = now, activeProviders = activeProviders,
                                    favorites = prefs.favorites, onToggleFavorite = ::favorite
                                )
                                3 -> PlannerScreen(stops = stops, city = city, onMap = {
                                    selectedStop = it; tab = 1
                                })
                                else -> MoreScreen(
                                    prefs = prefs, cities = catalog.cities,
                                    favoriteStops = catalog.stops.filter { it.id in prefs.favorites },
                                    onUpdate = ::update,
                                    onOpenStop = { selectedStop = it; tab = 1 },
                                    onToggleFavorite = ::favorite
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
