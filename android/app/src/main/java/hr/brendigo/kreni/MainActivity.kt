package hr.brendigo.kreni

import android.os.Bundle
import java.time.Instant
import kotlinx.coroutines.delay
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

private val Navy = Color(0xFF071936)
private val Electric = Color(0xFF146BFC)
private val Aqua = Color(0xFF02CDBD)

private fun symbol(mode: String) = when (mode) {
    "bus" -> Icons.Default.DirectionsBus
    "tram" -> Icons.Default.Tram
    "train" -> Icons.Default.Train
    "ferry" -> Icons.Default.DirectionsBoat
    else -> Icons.Default.Sailing
}
private fun modeName(mode: String) = when (mode) {
    "bus" -> "Autobusi"
    "tram" -> "Tramvaji"
    "train" -> "Vlakovi"
    "ferry" -> "Trajekti"
    else -> "Katamarani"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val catalog = CatalogReader.fromAssets(this)
        val settings = Settings(this, catalog)
        val liveNetwork = LiveNetwork(LiveCodec.sources(this))
        setContent {
            var prefs by remember { mutableStateOf(settings.read()) }
            var selectedTab by remember { mutableIntStateOf(intent?.getIntExtra("kreni_capture_tab", 0)?.coerceIn(0, 3) ?: 0) }
            var chosenStop by remember { mutableStateOf<Stop?>(null) }
            var live by remember { mutableStateOf(emptyList<LiveData>()) }
            var clock by remember { mutableLongStateOf(Instant.now().epochSecond) }
            LaunchedEffect(Unit) {
                while (true) {
                    delay(10_000)
                    clock = Instant.now().epochSecond
                }
            }
            LaunchedEffect(prefs.city) {
                live = emptyList()
                val requestedCity = prefs.city
                while (true) {
                    live = liveNetwork.refresh(requestedCity)
                    delay(30_000)
                }
            }
            val city = catalog.cities.firstOrNull { it.id == prefs.city } ?: catalog.cities.first()
            val currentLive = live.filter { it.freshFor(city.id, clock) }
                .map { it.filtered(clock) }
            val surface = if (prefs.dark) Navy else Color(0xFFF4F7FC)
            val foreground = if (prefs.dark) Color.White else Navy

            fun apply(newState: Preferences) {
                prefs = newState
                settings.save(newState)
            }

            MaterialTheme(
                colorScheme = if (prefs.dark) darkColorScheme(primary = Aqua, surface = Navy)
                    else lightColorScheme(primary = Electric, surface = Color.White),
            ) {
                Scaffold(
                    containerColor = surface,
                    bottomBar = {
                        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                            listOf(
                                Triple("Početna", Icons.Default.Home, 0),
                                Triple("Karta", Icons.Default.Map, 1),
                                Triple("Polasci", Icons.Default.Schedule, 2),
                                Triple("Više", Icons.Default.Person, 3)
                            ).forEach { (label, icon, tab) ->
                                NavigationBarItem(
                                    selected = selectedTab == tab,
                                    onClick = { selectedTab = tab },
                                    icon = { Icon(icon, contentDescription = null) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                ) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("KRENI", fontSize = 29.sp, color = foreground, fontWeight = FontWeight.Black)
                            var expanded by remember { mutableStateOf(false) }
                            Box {
                                TextButton(onClick = { expanded = true }) {
                                    Text(city.name, maxLines = 1)
                                    Icon(Icons.Default.KeyboardArrowDown, null)
                                }
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    catalog.cities.forEach { choice ->
                                        DropdownMenuItem(text = { Text(choice.name) }, onClick = {
                                            expanded = false
                                            chosenStop = null
                                            apply(prefs.copy(city = choice.id))
                                        })
                                    }
                                }
                            }
                        }
                        when (selectedTab) {
                            0 -> HomeTab(catalog.stopsIn(city.id), city, prefs.favorites,
                                onFavorite = { id ->
                                    apply(prefs.copy(favorites =
                                        if (id in prefs.favorites) prefs.favorites - id else prefs.favorites + id))
                                }, onMap = { stop ->
                                    chosenStop = stop
                                    selectedTab = 1
                                })
                            1 -> MapTab(
                                city,
                                catalog.stopsIn(city.id),
                                chosenStop,
                                prefs.favorites,
                                currentLive,
                                clock,
                                onFavorite = { id ->
                                    apply(prefs.copy(favorites =
                                        if (id in prefs.favorites) prefs.favorites - id else prefs.favorites + id))
                                })
                            2 -> DeparturesTab(city, catalog.stopsIn(city.id), currentLive, clock)
                            else -> MoreTab(
                                prefs, catalog.stops.filter { it.id in prefs.favorites },
                                onDark = { apply(prefs.copy(dark = it)) },
                                onMap = { stop ->
                                    apply(prefs.copy(city = stop.city))
                                    chosenStop = stop
                                    selectedTab = 1
                                },
                                onRemove = { id -> apply(prefs.copy(favorites = prefs.favorites - id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Hero(onPlan: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Navy),
        shape = RoundedCornerShape(25.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().background(
                Brush.linearGradient(listOf(Navy, Color(0xFF1656B8)))
            ).padding(22.dp)
        ) {
            Text("Krenimo zajedno.", fontSize = 27.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("Brže. Jednostavnije. Cijelom Hrvatskom.", color = Color(0xFFDBEAFF))
            Spacer(Modifier.height(18.dp))
            Button(onClick = onPlan, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Aqua, contentColor = Navy)) {
                Icon(Icons.AutoMirrored.Filled.AltRoute, null)
                Spacer(Modifier.width(8.dp))
                Text("Planiraj put")
            }
        }
    }
}

@Composable
private fun HomeTab(
    stops: List<Stop>, city: City, favorites: Set<String>,
    onFavorite: (String) -> Unit, onMap: (Stop) -> Unit
) {
    var query by remember(city.id) { mutableStateOf("") }
    var planning by remember { mutableStateOf(false) }
    var origin by remember { mutableStateOf<Stop?>(null) }
    var destination by remember { mutableStateOf<Stop?>(null) }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Hero { planning = !planning } }
        if (planning) {
            item {
                Card {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Planer putovanja", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        StopSelector("Odakle?", stops, origin) { origin = it }
                        StopSelector("Kamo?", stops, destination) { destination = it }
                        if (origin != null && destination != null && origin != destination) {
                            Text("Za provjereno putovanje odaberi polazak iz dostupnog voznog reda prijevoznika.",
                                style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("bus", "tram", "train", "ferry", "catamaran")
                    .filter { it != "tram" || city.trams }.forEach { mode ->
                        Icon(symbol(mode), contentDescription = modeName(mode),
                            tint = Electric, modifier = Modifier.size(30.dp))
                    }
            }
        }
        item {
            OutlinedTextField(value = query, onValueChange = { query = it },
                label = { Text("Pretraži stanice i luke") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
        }
        item { Text("Stanice i terminali", fontSize = 21.sp, fontWeight = FontWeight.Bold) }
        items(stops.filter { it.name.contains(query, ignoreCase = true) }, key = { it.id }) { stop ->
            StopCard(stop, stop.id in favorites, { onFavorite(stop.id) }, { onMap(stop) })
        }
    }
}

@Composable
private fun StopSelector(label: String, stops: List<Stop>, value: Stop?, change: (Stop) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth()) {
            Text(value?.name ?: label, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.KeyboardArrowDown, null)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            stops.forEach { stop ->
                DropdownMenuItem(text = { Text(stop.name) }, onClick = {
                    change(stop); open = false
                })
            }
        }
    }
}

@Composable
private fun StopCard(stop: Stop, favorite: Boolean, onFavorite: () -> Unit, onOpen: () -> Unit) {
    Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(symbol(stop.modes.first()), null, tint = Electric, modifier = Modifier.size(35.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f).clickable(onClick = onOpen)) {
                Text(stop.name, fontWeight = FontWeight.SemiBold)
                Text(stop.modes.joinToString(" · ") { modeName(it) },
                    style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onFavorite) {
                Icon(if (favorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (favorite) "Ukloni omiljeno" else "Spremi omiljeno",
                    tint = Electric)
            }
            IconButton(onClick = onOpen) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Otvori na karti")
            }
        }
    }
}

@Composable
private fun MapTab(
    city: City, stops: List<Stop>, focus: Stop?, favorites: Set<String>,
    live: List<LiveData>, clock: Long, onFavorite: (String) -> Unit
) {
    var filter by remember(city.id) { mutableStateOf("all") }
    var selected by remember(city.id, focus?.id) { mutableStateOf(focus) }
    val visible = stops.filter { filter == "all" || filter in it.modes }
    val vehicles = live.flatMap { it.vehicles }.filter { filter == "all" || it.mode == filter }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp)) {
            (listOf("all", "bus", "tram", "train", "ferry", "catamaran")
                .filter { it != "tram" || city.trams }).forEach { mode ->
                FilterChip(
                    selected = filter == mode,
                    onClick = { filter = mode; selected = null },
                    label = { Text(if (mode == "all") "Sve" else modeName(mode)) },
                    modifier = Modifier.padding(end = 7.dp)
                )
            }
        }
        val updated = live.maxOfOrNull { it.generatedAt }
        Text(if (vehicles.isEmpty()) "Praćenje vozila trenutačno nije dostupno"
             else "GPS vozila: " + vehicles.size +
                 " · osvježeno prije " + (clock - (updated ?: clock)).coerceAtLeast(0) + " s",
             modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
             style = MaterialTheme.typography.bodySmall)
        Box(Modifier.weight(1f)) {
            key(city.id, filter, focus?.id) {
                MapLibreContent(city, visible, vehicles, focus, Modifier.fillMaxSize())
            }
            Surface(
                modifier = Modifier.align(Alignment.TopCenter).padding(12.dp),
                shape = RoundedCornerShape(14.dp),
                shadowElevation = 3.dp
            ) {
                Text("© OpenStreetMap contributors · odabrane stanice",
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.labelSmall)
            }
        }
        if (selected != null) {
            val stop = selected!!
            StopCard(stop, stop.id in favorites, { onFavorite(stop.id) }) { selected = null }
        }
        LazyColumn(modifier = Modifier.heightIn(max = 185.dp),
            contentPadding = PaddingValues(8.dp)) {
            items(visible, key = { it.id }) { stop ->
                ListItem(
                    headlineContent = { Text(stop.name) },
                    supportingContent = { Text(stop.modes.joinToString { modeName(it) }) },
                    modifier = Modifier.clickable { selected = stop })
            }
        }
    }
}

@Composable
private fun MapLibreContent(city: City, stops: List<Stop>, vehicles: List<Vehicle>,
    focus: Stop?, modifier: Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val view = remember(city.id, focus?.id) {
        MapLibre.getInstance(context)
        MapView(context).apply {
            onCreate(null)
            getMapAsync { map ->
                val json = """{"version":8,"sources":{"osm":{"type":"raster","tiles":["https://tile.openstreetmap.org/{z}/{x}/{y}.png"],"tileSize":256,"attribution":"© OpenStreetMap contributors"}},"layers":[{"id":"osm-layer","type":"raster","source":"osm"}]}"""
                map.setStyle(Style.Builder().fromJson(json)) {
                    map.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(focus?.lat ?: city.lat, focus?.lon ?: city.lon))
                        .zoom(if (focus == null) 12.0 else 15.0).build()
                    stops.forEach { stop ->
                        map.addMarker(MarkerOptions()
                            .position(LatLng(stop.lat, stop.lon)).title(stop.name))
                    }
                    vehicles.forEach { v ->
                        map.addMarker(MarkerOptions()
                            .position(LatLng(v.lat, v.lon))
                            .title(modeName(v.mode) + " " + v.route + " · GPS"))
                    }
                }
            }
        }
    }
    DisposableEffect(view, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> view.onStart()
                Lifecycle.Event.ON_RESUME -> view.onResume()
                Lifecycle.Event.ON_PAUSE -> view.onPause()
                Lifecycle.Event.ON_STOP -> view.onStop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            view.onPause()
            view.onStop()
            view.onDestroy()
        }
    }
    LaunchedEffect(view, stops, vehicles) {
        view.getMapAsync { map ->
            if (map.style != null) {
                map.clear()
                stops.forEach { stop -> map.addMarker(MarkerOptions()
                    .position(LatLng(stop.lat, stop.lon)).title(stop.name)) }
                vehicles.forEach { v -> map.addMarker(MarkerOptions()
                    .position(LatLng(v.lat, v.lon))
                    .title(modeName(v.mode) + " " + v.route + " · GPS")) }
            }
        }
    }
    AndroidView(factory = { view }, modifier = modifier)
}

@Composable
private fun DeparturesTab(city: City, stops: List<Stop>, live: List<LiveData>,
    now: Long) {
    val arrivals = live.flatMap { it.arrivals }.filter { arrival ->
        stops.any { it.id == arrival.stopId } && arrival.minutes(now) != null
    }.sortedBy { it.predictedAt ?: it.scheduledAt ?: Long.MAX_VALUE }
    LazyColumn(contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Polasci · " + city.name, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Text("Samo provjerene informacije prijevoznika.",
                style = MaterialTheme.typography.bodySmall)
        }
        if (arrivals.isEmpty()) item {
            Card(shape = RoundedCornerShape(20.dp)) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, tint = Electric,
                        modifier = Modifier.size(38.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Nema potvrđenih dolazaka", fontWeight = FontWeight.SemiBold)
                        Text("Prije putovanja provjeri službeni vozni red.",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        items(arrivals, key = { it.provider + ":" + it.id }) { arrival ->
            Card(shape = RoundedCornerShape(17.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(symbol(arrival.mode), null, tint = Electric)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Linija " + arrival.route, fontWeight = FontWeight.Bold)
                        Text(stops.first { it.id == arrival.stopId }.name,
                            style = MaterialTheme.typography.bodySmall)
                        Text(if (arrival.estimated(now)) "Procjena prijevoznika"
                             else "Planirani polazak",
                            style = MaterialTheme.typography.labelSmall)
                        Text("Ažurirano prije " + (now - arrival.updatedAt).coerceAtLeast(0) + " s",
                            style = MaterialTheme.typography.labelSmall)
                    }
                    Text(arrival.minutes(now).toString() + " min",
                        fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Electric)
                }
            }
        }
    }
}

@Composable
private fun MoreTab(
    prefs: Preferences, saved: List<Stop>, onDark: (Boolean) -> Unit,
    onMap: (Stop) -> Unit, onRemove: (String) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Omiljeno", fontSize = 29.sp, fontWeight = FontWeight.Bold) }
        if (saved.isEmpty()) item { Text("Spremi svoje stanice dodirom na zvjezdicu.") }
        items(saved, key = { it.id }) { stop ->
            StopCard(stop, true, { onRemove(stop.id) }) { onMap(stop) }
        }
        item {
            Spacer(Modifier.height(14.dp))
            Text("Postavke", fontSize = 25.sp, fontWeight = FontWeight.Bold)
        }
        item {
            Card {
                Column {
                    Row(Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Tamna tema", modifier = Modifier.weight(1f))
                        Switch(checked = prefs.dark, onCheckedChange = onDark)
                    }
                    HorizontalDivider()
                    ListItem(headlineContent = { Text("Jezik") },
                        supportingContent = { Text("Hrvatski") },
                        leadingContent = { Icon(Icons.Default.Language, null) })
                    HorizontalDivider()
                    ListItem(headlineContent = { Text("Privatnost") },
                        supportingContent = { Text("Favoriti i postavke ostaju na uređaju.") },
                        leadingContent = { Icon(Icons.Default.Lock, null) })
                }
            }
        }
    }
}
