package hr.brendigo.kreni

import android.graphics.BitmapFactory
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(onStart: () -> Unit) {
    val context = LocalContext.current
    val image = remember {
        runCatching { context.assets.open("brand/welcome_city.jpg").use {
            BitmapFactory.decodeStream(it)?.asImageBitmap()
        } }.getOrNull()
    }
    val brandIcon = remember {
        runCatching { context.assets.open("brand/brand_icon.jpg").use {
            BitmapFactory.decodeStream(it)?.asImageBitmap()
        } }.getOrNull()
    }
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(BrandNavy, Color(0xFF143B75), BrandNavy))
        )
    ) {
        if (image != null) Image(bitmap = image,
            contentDescription = "Zagreb i gradski javni prijevoz",
            modifier = Modifier.fillMaxWidth().fillMaxHeight(.82f).align(Alignment.BottomCenter),
            contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(BrandNavy.copy(alpha = .30f),
                Color.Transparent, BrandNavy.copy(alpha = .95f)))
        ))
        Column(
            Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(.6f))
            if (brandIcon != null) {
                Image(bitmap = brandIcon, contentDescription = "KRENI logotip",
                    modifier = Modifier.size(120.dp).clip(RoundedCornerShape(28.dp)),
                    contentScale = ContentScale.Crop)
            } else {
                Icon(Icons.Default.AltRoute, null, tint = BrandAqua,
                    modifier = Modifier.size(96.dp))
            }
            Spacer(Modifier.height(15.dp))
            Text("KRENI", color = Color.White, fontSize = 48.sp,
                fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Text("Javni prijevoz uvijek s tobom.", color = Color.White,
                style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(2.5f))
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically) {
                listOf(
                    Icons.Default.DirectionsBus to "Prati uživo",
                    Icons.Default.LocationOn to "Planiraj",
                    Icons.Default.FavoriteBorder to "Omiljeno"
                ).forEach { (icon, title) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(icon, null, tint = BrandAqua, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.height(6.dp))
                        Text(title, color = Color.White, fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.height(26.dp))
            PrimaryAction("Krenimo", onStart)
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MoreSectionHeader(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = BrandBlue)
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold, letterSpacing = .5.sp)
    }
}

@Composable
fun MoreScreen(
    prefs: Preferences, cities: List<City>, favoriteStops: List<Stop>,
    onUpdate: (Preferences) -> Unit, onOpenStop: (Stop) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    var cityExpanded by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf<String?>(null) }
    LazyColumn(Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)) {
        item { BrandHeader("more", height = 175) }
        item {
            PremiumCard(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                MoreSectionHeader("OMILJENA MJESTA I STAJALIŠTA", Icons.Default.FavoriteBorder)
                if (favoriteStops.isEmpty()) {
                    Text("Spremi stajališta dodirom na srce uz njihovo ime.",
                        modifier = Modifier.padding(start = 18.dp, end = 18.dp, bottom = 19.dp),
                        color = BrandMuted, style = MaterialTheme.typography.bodySmall)
                }
                favoriteStops.forEach { stop ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .clickable { onOpenStop(stop) }.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(42.dp).background(BrandPaleBlue,
                            RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                            Icon(transportIcon(stop.modes.firstOrNull() ?: "bus"), null,
                                tint = BrandBlue)
                        }
                        Spacer(Modifier.width(11.dp))
                        Column(Modifier.weight(1f)) {
                            Text(stop.name, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Text(cities.firstOrNull { it.id == stop.city }?.name ?: "",
                                color = BrandMuted, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { onToggleFavorite(stop.id) }) {
                            Icon(Icons.Default.Favorite, "Ukloni omiljeno", tint = BrandBlue)
                        }
                    }
                }
                Spacer(Modifier.height(7.dp))
            }
        }
        item {
            PremiumCard(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                MoreSectionHeader("GLAVNE POSTAVKE", Icons.Default.Settings)
                ListItem(
                    headlineContent = { Text("Tamni način rada", fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("Promijeni izgled aplikacije") },
                    leadingContent = { Icon(Icons.Default.DarkMode, null, tint = BrandNavy) },
                    trailingContent = { Switch(
                        checked = prefs.dark,
                        onCheckedChange = { onUpdate(prefs.copy(dark = it)) }
                    ) }
                )
                HorizontalDivider(Modifier.padding(horizontal = 15.dp))
                ListItem(
                    headlineContent = { Text("Odabrani grad", fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("Prikaz relevantnih stajališta i izvora") },
                    leadingContent = { Icon(Icons.Default.LocationOn, null, tint = BrandBlue) },
                    trailingContent = {
                        Box {
                            TextButton(onClick = { cityExpanded = true }) {
                                Text(cities.firstOrNull { it.id == prefs.city }?.name ?: "Grad")
                                Icon(Icons.Default.ExpandMore, null)
                            }
                            DropdownMenu(cityExpanded, onDismissRequest = {
                                cityExpanded = false
                            }) {
                                cities.forEach { city ->
                                    DropdownMenuItem(text = { Text(city.name) }, onClick = {
                                        cityExpanded = false
                                        onUpdate(prefs.copy(city = city.id))
                                    })
                                }
                            }
                        }
                    }
                )
                Spacer(Modifier.height(6.dp))
            }
        }
        item {
            PremiumCard(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                MoreSectionHeader("OBAVIJESTI", Icons.Default.NotificationsNone)
                ListItem(
                    headlineContent = { Text("Poremećaji u prometu", fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("Dostupno nakon provjere službenog izvora upozorenja") },
                    leadingContent = { Icon(Icons.Default.WarningAmber, null, tint = BrandOrange) },
                    trailingContent = {
                        Switch(checked = prefs.alertPreferences, onCheckedChange = null,
                            enabled = false)
                    }
                )
                HorizontalDivider(Modifier.padding(horizontal = 15.dp))
                ListItem(
                    headlineContent = { Text("Omiljene linije", fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("Obavijesti još nisu aktivne") },
                    leadingContent = { Icon(Icons.Default.DirectionsBus, null, tint = BrandBlue) },
                    trailingContent = {
                        Switch(checked = prefs.favoriteNotifications, onCheckedChange = null,
                            enabled = false)
                    }
                )
                Spacer(Modifier.height(6.dp))
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf(
                    Triple("Pomoć", Icons.Default.HelpOutline, "help"),
                    Triple("Privatnost", Icons.Default.Shield, "privacy"),
                    Triple("O aplikaciji", Icons.Default.InfoOutline, "about")
                ).forEach { (name, icon, value) ->
                    Card(
                        onClick = { dialog = value }, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(17.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(Modifier.padding(11.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(icon, contentDescription = null, tint = BrandBlue)
                            Spacer(Modifier.height(8.dp))
                            Text(name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                maxLines = 1)
                        }
                    }
                }
            }
        }
    }
    if (dialog != null) {
        AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text(when (dialog) {
                "help" -> "Pomoć"
                "privacy" -> "Privatnost"
                else -> "O aplikaciji KRENI"
            }) },
            text = { Text(when (dialog) {
                "help" -> "Odaberi grad, pronađi stajalište i spremi ga u omiljene. Podaci uživo prikazuju se samo kada su dostupni od ovlaštenog prijevoznika."
                "privacy" -> "KRENI ne zahtijeva korisnički račun. Favoriti i odabrani grad ostaju na uređaju. Kartografski prikaz preuzima podatke pružatelja karte."
                else -> "KRENI 0.0.7 · Brendigo\nNativna Android aplikacija za javni prijevoz Hrvatske."
            }) },
            confirmButton = {
                TextButton(onClick = { dialog = null }) { Text("Zatvori") }
            }
        )
    }
}
