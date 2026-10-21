package hr.brendigo.kreni

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

@Composable
fun LiveMapScreen(
    city: City, stops: List<Stop>, selectedStop: Stop?,
    live: List<LiveData>, now: Long, activeProviders: Int,
    favorites: Set<String>, onToggleFavorite: (String) -> Unit,
    onSelectedStop: (Stop) -> Unit
) {
    var filter by remember(city.id) { mutableStateOf("all") }
    var search by remember(city.id) { mutableStateOf("") }
    var vehicle by remember(city.id) { mutableStateOf<Vehicle?>(null) }
    var layersOpen by remember { mutableStateOf(false) }
    val vehicles = live.flatMap { it.vehicles }.filter { filter == "all" || it.mode == filter }
    val filteredStops = stops.filter { s ->
        (filter == "all" || filter in s.modes) &&
            s.name.contains(search, ignoreCase = true)
    }
    Box(Modifier.fillMaxSize()) {
        MapLibreSurface(city, filteredStops, vehicles, selectedStop, vehicle,
            onSelectedStop, Modifier.fillMaxSize())
        Column(Modifier.align(Alignment.TopCenter)
            .padding(start = 13.dp, end = 13.dp, top = 9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Karta", color = BrandNavy, fontWeight = FontWeight.ExtraBold,
                    fontSize = 31.sp)
                OutlinedTextField(
                    value = search, onValueChange = { search = it },
                    modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall,
                    placeholder = { Text("Traži stajalište") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
            Spacer(Modifier.height(7.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("all", "bus", "tram", "train", "ferry", "catamaran")
                    .filter { it != "tram" || city.trams }
                    .forEach { mode ->
                        FilterChip(
                            selected = mode == filter, onClick = { filter = mode; vehicle = null },
                            label = { Text(if (mode == "all") "Sve" else transportName(mode)) }
                        )
                    }
            }
            Surface(shape = RoundedCornerShape(13.dp),
                color = Color.White.copy(alpha = .94f),
                modifier = Modifier.padding(top = 5.dp)) {
                Row(Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (activeProviders == 0) Icons.Default.WifiOff else Icons.Default.Wifi,
                        contentDescription = null,
                        tint = if (vehicles.isNotEmpty()) BrandGreen else BrandMuted,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        if (activeProviders == 0) "Uživo nije dostupno — nema odobrenog izvora"
                        else if (vehicles.isEmpty()) "Nema svježih GPS položaja"
                        else "Uživo · " + vehicles.size + " vozila s GPS položajem",
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandNavy
                    )
                }
            }
        }
        Box(Modifier.align(Alignment.CenterEnd).padding(end = 14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FloatingActionButton(
                    onClick = { layersOpen = !layersOpen }, containerColor = Color.White,
                    contentColor = BrandNavy, modifier = Modifier.size(48.dp)
                ) { Icon(Icons.Default.Layers, contentDescription = "Slojevi karte") }
                FloatingActionButton(
                    onClick = { vehicle = null; if (stops.isNotEmpty()) onSelectedStop(stops.first()) },
                    containerColor = Color.White, contentColor = BrandNavy,
                    modifier = Modifier.size(48.dp)
                ) { Icon(Icons.Default.CenterFocusStrong, contentDescription = "Prikaži stajalište") }
            }
            DropdownMenu(expanded = layersOpen, onDismissRequest = { layersOpen = false }) {
                DropdownMenuItem(text = { Text("Sve vrste prijevoza") }, onClick = {
                    filter = "all"; layersOpen = false
                })
                DropdownMenuItem(text = { Text("Samo autobusi") }, onClick = {
                    filter = "bus"; layersOpen = false
                })
            }
        }
        PremiumCard(Modifier.align(Alignment.BottomCenter)
            .fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp)) {
            Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(49.dp).background(BrandBlue, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center) {
                        Icon(transportIcon(vehicle?.mode ?: selectedStop?.modes?.firstOrNull() ?: "bus"),
                            null, tint = Color.White, modifier = Modifier.size(27.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            vehicle?.let { transportName(it.mode) + " " + it.route }
                                ?: selectedStop?.name ?: "Pronađi prijevoz u " + city.name,
                            fontSize = 17.sp, fontWeight = FontWeight.Bold,
                            maxLines = 2, overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            if (vehicle != null) "GPS ažuriran prije " +
                                (now - vehicle!!.observedAt).coerceAtLeast(0) + " s"
                            else if (selectedStop != null) "Stajalište · " + city.name
                            else "Odaberi stajalište ili dostupno vozilo",
                            fontSize = 12.sp, color = BrandMuted
                        )
                    }
                    if (selectedStop != null && vehicle == null) {
                        IconButton(onClick = { onToggleFavorite(selectedStop.id) }) {
                            Icon(if (selectedStop.id in favorites) Icons.Default.Favorite
                                else Icons.Default.FavoriteBorder, "Omiljeno", tint = BrandBlue)
                        }
                    }
                }
                if (vehicles.isNotEmpty()) {
                    val nearest = vehicles.sortedWith(compareBy({ it.mode }, { it.route })).take(5)
                    Row(Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        nearest.forEach { v ->
                            AssistChip(onClick = { vehicle = v },
                                label = { Text(v.route + " · " + transportName(v.mode)) },
                                leadingIcon = { Icon(transportIcon(v.mode), null,
                                    modifier = Modifier.size(17.dp)) })
                        }
                    }
                } else {
                    Text(if (activeProviders == 0)
                        "Nema aktivnog službenog GPS izvora. Stajališta ostaju dostupna."
                    else "Čeka se novo provjereno ažuriranje prijevoznika.",
                        style = MaterialTheme.typography.bodySmall, color = BrandMuted)
                }
                if (filteredStops.isNotEmpty()) {
                    val displayed = if (search.isNotBlank()) filteredStops.take(3)
                    else selectedStop?.let { listOf(it) } ?: filteredStops.take(2)
                    displayed.forEach { stop ->
                        Row(Modifier.fillMaxWidth().clickable {
                            vehicle = null; onSelectedStop(stop)
                        }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, null, tint = BrandBlue)
                            Spacer(Modifier.width(8.dp))
                            Text(stop.name, modifier = Modifier.weight(1f), maxLines = 1,
                                fontSize = 13.sp)
                            Icon(Icons.Default.ChevronRight, null, tint = BrandMuted)
                        }
                    }
                }
                Text("© OpenStreetMap contributors", fontSize = 10.sp, color = BrandMuted)
            }
        }
    }
}

@Composable
private fun MapLibreSurface(
    city: City, stops: List<Stop>, vehicles: List<Vehicle>,
    selectedStop: Stop?, selectedVehicle: Vehicle?,
    onStopClick: (Stop) -> Unit, modifier: Modifier
) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val view = remember(city.id) {
        MapLibre.getInstance(context)
        MapView(context).apply {
            onCreate(null)
            getMapAsync { map ->
                val style = """{"version":8,"sources":{"osm":{"type":"raster","tiles":["https://tile.openstreetmap.org/{z}/{x}/{y}.png"],"tileSize":256,"attribution":"© OpenStreetMap contributors"}},"layers":[{"id":"osm-layer","type":"raster","source":"osm"}]}"""
                map.setStyle(Style.Builder().fromJson(style)) {
                    map.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(city.lat, city.lon)).zoom(12.0).build()
                }
            }
        }
    }
    DisposableEffect(view, owner) {
        val obs = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> view.onStart()
                Lifecycle.Event.ON_RESUME -> view.onResume()
                Lifecycle.Event.ON_PAUSE -> view.onPause()
                Lifecycle.Event.ON_STOP -> view.onStop()
                else -> Unit
            }
        }
        owner.lifecycle.addObserver(obs)
        onDispose {
            owner.lifecycle.removeObserver(obs)
            view.onPause()
            view.onStop()
            view.onDestroy()
        }
    }
    LaunchedEffect(view, stops, vehicles) {
        view.getMapAsync { map ->
            if (map.style != null) {
                map.clear()
                stops.forEach { stop ->
                    map.addMarker(MarkerOptions().position(LatLng(stop.lat, stop.lon))
                        .title(stop.name))
                }
                vehicles.forEach { v ->
                    map.addMarker(MarkerOptions().position(LatLng(v.lat, v.lon))
                        .title(transportName(v.mode) + " " + v.route + " · GPS"))
                }
                map.setOnMarkerClickListener { marker ->
                    val stop = stops.firstOrNull { it.name == marker.title }
                    if (stop != null) { onStopClick(stop); true } else false
                }
            }
        }
    }
    LaunchedEffect(view, selectedStop?.id, selectedVehicle?.id, selectedVehicle?.observedAt) {
        val lat = selectedVehicle?.lat ?: selectedStop?.lat
        val lon = selectedVehicle?.lon ?: selectedStop?.lon
        if (lat != null && lon != null) view.getMapAsync { map ->
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 14.5))
        }
    }
    AndroidView(factory = { view }, modifier = modifier)
}

@Composable
fun DeparturesScreen(
    city: City, stops: List<Stop>, live: List<LiveData>, now: Long, activeProviders: Int,
    favorites: Set<String>, onToggleFavorite: (String) -> Unit
) {
    var favoritesOnly by remember { mutableStateOf(false) }
    var selectedStop by remember(city.id) { mutableStateOf<Stop?>(null) }
    val arrivals = live.flatMap { it.arrivals }
        .filter { arrival -> stops.any { it.id == arrival.stopId } &&
            (!favoritesOnly || arrival.stopId in favorites) &&
            (selectedStop == null || selectedStop!!.id == arrival.stopId) &&
            arrival.minutes(now) != null
        }.sortedBy { it.predictedAt ?: it.scheduledAt ?: Long.MAX_VALUE }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenHeading("Polasci") }
        item {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(selected = !favoritesOnly,
                    onClick = { favoritesOnly = false }, shape = SegmentedButtonDefaults.itemShape(0, 2),
                    icon = { Icon(Icons.Default.Wifi, null, modifier = Modifier.size(17.dp)) }
                ) { Text("Uživo") }
                SegmentedButton(selected = favoritesOnly,
                    onClick = { favoritesOnly = true }, shape = SegmentedButtonDefaults.itemShape(1, 2),
                    icon = { Icon(Icons.Default.FavoriteBorder, null, modifier = Modifier.size(17.dp)) }
                ) { Text("Omiljeno") }
            }
        }
        item {
            PremiumCard {
                var expanded by remember { mutableStateOf(false) }
                Box {
                    Row(Modifier.fillMaxWidth().clickable { expanded = true }.padding(17.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tram, null, tint = BrandBlue,
                            modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(selectedStop?.name ?: "Sva stajališta · " + city.name,
                                fontWeight = FontWeight.Bold)
                            Text("Odaberi stajalište za prikaz polazaka",
                                style = MaterialTheme.typography.bodySmall, color = BrandMuted)
                        }
                        Icon(Icons.Default.ExpandMore, contentDescription = "Odaberi stajalište")
                    }
                    DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("Sva stajališta") }, onClick = {
                            selectedStop = null; expanded = false
                        })
                        stops.forEach { s ->
                            DropdownMenuItem(text = { Text(s.name) }, onClick = {
                                selectedStop = s; expanded = false
                            })
                        }
                    }
                }
            }
        }
        if (arrivals.isEmpty()) item {
            StatusCard(
                if (activeProviders == 0) "Nema potvrđenog live izvora"
                else if (favoritesOnly) "Nema potvrđenih omiljenih polazaka"
                else "Nema svježih polazaka",
                "Nisu prikazane izmišljene minute dolaska. " +
                    "Prije putovanja provjeri službeni vozni red.",
                Icons.Default.Schedule
            )
        }
        items(arrivals, key = { it.provider + ":" + it.id }) { arrival ->
            val stop = stops.first { it.id == arrival.stopId }
            PremiumCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(13.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    TransportPill(arrival.route)
                    Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stop.name, fontWeight = FontWeight.Bold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(transportName(arrival.mode), color = BrandMuted, fontSize = 12.sp)
                        Text(if (arrival.estimated(now)) "Uživo · procjena prijevoznika"
                        else "Po redu vožnje",
                            color = if (arrival.estimated(now)) BrandGreen else BrandMuted,
                            fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text((arrival.minutes(now) ?: 0).toString() + " min",
                            color = if (arrival.estimated(now)) BrandGreen else BrandNavy,
                            fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Prije " + (now - arrival.updatedAt).coerceAtLeast(0) + " s",
                            fontSize = 10.sp, color = BrandMuted)
                    }
                    IconButton(onClick = { onToggleFavorite(stop.id) },
                        modifier = Modifier.size(35.dp)) {
                        Icon(if (stop.id in favorites) Icons.Default.Favorite
                        else Icons.Default.FavoriteBorder, "Omiljeno", tint = BrandBlue)
                    }
                }
            }
        }
    }
}
