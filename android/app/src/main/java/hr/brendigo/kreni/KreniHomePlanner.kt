package hr.brendigo.kreni

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    city: City, stops: List<Stop>, favorites: Set<String>,
    live: List<LiveData>, activeProviders: Int,
    onOpenStop: (Stop) -> Unit, onToggleFavorite: (String) -> Unit,
    onShowMap: () -> Unit, onPlanner: () -> Unit, onFavorites: () -> Unit,
    onCitySelected: (String) -> Unit, cities: List<City>
) {
    var search by remember(city.id) { mutableStateOf("") }
    var cityPicker by remember { mutableStateOf(false) }
    val filtered = remember(stops, search) { stops.filter {
        it.name.contains(search, ignoreCase = true) || it.modes.any { m ->
            transportName(m).contains(search, ignoreCase = true)
        }
    } }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 23.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { BrandHeader("home", height = 171) }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 17.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                ScreenHeading("Početna")
                Box {
                    TextButton(onClick = { cityPicker = true }) {
                        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(17.dp))
                        Text(city.name, maxLines = 1)
                        Icon(Icons.Default.ExpandMore, null)
                    }
                    DropdownMenu(expanded = cityPicker, onDismissRequest = { cityPicker = false }) {
                        cities.forEach {
                            DropdownMenuItem(text = { Text(it.name) }, onClick = {
                                cityPicker = false; onCitySelected(it.id)
                            })
                        }
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                value = search, onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 17.dp),
                singleLine = true, shape = RoundedCornerShape(22.dp),
                placeholder = { Text("Kamo putujete?") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (search.isNotEmpty()) IconButton(onClick = { search = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Očisti pretragu")
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 17.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f)) {
                    FeatureTile("Uživo", Icons.Default.Wifi, BrandGreen,
                        Color(0xFFDFFAF1), onShowMap)
                }
                Box(Modifier.weight(1f)) {
                    FeatureTile("Planer", Icons.Default.CalendarMonth, BrandBlue,
                        Color(0xFFE7F2FF), onPlanner)
                }
                Box(Modifier.weight(1f)) {
                    FeatureTile("Omiljeno", Icons.Default.FavoriteBorder, BrandRed,
                        Color(0xFFFFEFF4), onFavorites)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 19.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                SectionHeading("U gradu")
                TextButton(onClick = onShowMap) {
                    Text("Prikaži sve", color = BrandBlue)
                    Icon(Icons.Default.ChevronRight, null, tint = BrandBlue)
                }
            }
        }
        if (activeProviders == 0) item {
            Text("Praćenje uživo nije dostupno za " + city.name + " — izvori još nisu potvrđeni.",
                modifier = Modifier.padding(horizontal = 21.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .65f))
        } else if (live.flatMap { it.vehicles }.isNotEmpty()) item {
            Text("Dostupna su svježa GPS ažuriranja prijevoznika.",
                modifier = Modifier.padding(horizontal = 21.dp), color = BrandGreen,
                style = MaterialTheme.typography.labelMedium)
        }
        if (filtered.isEmpty()) item {
            Box(Modifier.padding(horizontal = 17.dp)) {
                StatusCard("Nema pronađenih stajališta", "Pokušaj drugačiji naziv ili odaberi drugi grad.")
            }
        }
        items(filtered, key = { it.id }) { stop ->
            StopListCard(stop, stop.id in favorites, { onOpenStop(stop) },
                { onToggleFavorite(stop.id) },
                modifier = Modifier.padding(horizontal = 17.dp))
        }
    }
}

@Composable
private fun StopDropdown(
    label: String, value: Stop?, stops: List<Stop>, onSelect: (Stop) -> Unit
) {
    var opened by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = BrandMuted)
        Box {
            OutlinedButton(onClick = { opened = true }, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandBlue)
                Spacer(Modifier.width(8.dp))
                Text(value?.name ?: "Odaberi stajalište", modifier = Modifier.weight(1f),
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface)
                Icon(Icons.Default.ExpandMore, contentDescription = null)
            }
            DropdownMenu(opened, onDismissRequest = { opened = false }) {
                stops.forEach { stop ->
                    DropdownMenuItem(text = { Text(stop.name, maxLines = 1) },
                        onClick = { opened = false; onSelect(stop) })
                }
            }
        }
    }
}

@Composable
fun PlannerScreen(stops: List<Stop>, city: City, onMap: (Stop) -> Unit) {
    var origin by remember(city.id) { mutableStateOf<Stop?>(null) }
    var destination by remember(city.id) { mutableStateOf<Stop?>(null) }
    var options by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var onlyDirect by remember { mutableStateOf(false) }
    var selectedMode by remember { mutableStateOf("Sve") }
    val modes = listOf("Sve", "Autobus", "Tramvaj", "Vlak", "Brod")
    LazyColumn(
        Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp)
    ) {
        item { ScreenHeading("Planer") }
        item {
            PremiumCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StopDropdown("Polazište", origin, stops) { origin = it }
                    Row(Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End) {
                        IconButton(onClick = {
                            val old = origin; origin = destination; destination = old
                        }) {
                            Icon(Icons.Default.SwapVert, "Zamijeni polazište i odredište",
                                tint = BrandBlue)
                        }
                    }
                    StopDropdown("Odredište", destination, stops) { destination = it }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Polazak: Sada", fontWeight = FontWeight.SemiBold)
                    }
                }
                OutlinedButton(onClick = { options = !options },
                    shape = RoundedCornerShape(16.dp)) {
                    Icon(Icons.Default.Tune, contentDescription = null)
                    Spacer(Modifier.width(5.dp))
                    Text("Opcije")
                }
            }
        }
        if (options) item {
            PremiumCard {
                Column(Modifier.padding(14.dp)) {
                    Text("Vrsta prijevoza", fontWeight = FontWeight.Bold)
                    Row(Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        modes.forEach { mode ->
                            FilterChip(selected = selectedMode == mode,
                                onClick = { selectedMode = mode }, label = { Text(mode) })
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Bez presjedanja", modifier = Modifier.weight(1f))
                        Switch(checked = onlyDirect, onCheckedChange = { onlyDirect = it })
                    }
                }
            }
        }
        item {
            PrimaryAction("Prikaži rute", onClick = { showResult = true },
                enabled = origin != null && destination != null && origin!!.id != destination!!.id)
        }
        item {
            SectionHeading("Preporučene rute")
            Spacer(Modifier.height(8.dp))
            StatusCard(
                "Rute čekaju provjereni vozni red",
                "Za planiranje stvarnih putovanja potrebna je službena baza polazaka i presjedanja.",
                Icons.AutoMirrored.Filled.AltRoute
            )
        }
        if (destination != null) item {
            TextButton(onClick = { destination?.let(onMap) }) {
                Icon(Icons.Default.Map, null)
                Spacer(Modifier.width(8.dp))
                Text("Prikaži odredište na karti")
            }
        }
    }
    if (showResult) {
        AlertDialog(
            onDismissRequest = { showResult = false },
            title = { Text("Podaci o rutama nisu dostupni") },
            text = { Text("KRENI ne prikazuje izmišljene polaske ni procjene trajanja. Za " + city.name + " trenutačno nije povezan provjereni izvor za izračun putovanja.") },
            confirmButton = { TextButton(onClick = { showResult = false }) { Text("Razumijem") } }
        )
    }
}
