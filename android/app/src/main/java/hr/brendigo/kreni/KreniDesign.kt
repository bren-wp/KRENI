package hr.brendigo.kreni

import android.graphics.BitmapFactory
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val BrandNavy = Color(0xFF0B1F3B)
val BrandBlue = Color(0xFF2563FF)
val BrandAqua = Color(0xFF22D3EE)
val BrandGreen = Color(0xFF10B981)
val BrandOrange = Color(0xFFF59E0B)
val BrandRed = Color(0xFFEF4444)
val BrandBackground = Color(0xFFF5F9FD)
val BrandPaleBlue = Color(0xFFEAF4FF)
val BrandSurfaceDark = Color(0xFF142C4B)
val BrandMuted = Color(0xFF647899)
private val brandShape = RoundedCornerShape(21.dp)

fun transportIcon(mode: String) = when (mode) {
    "tram" -> Icons.Default.Tram
    "train" -> Icons.Default.Train
    "ferry", "catamaran" -> Icons.Default.DirectionsBoat
    else -> Icons.Default.DirectionsBus
}

fun transportName(mode: String) = when (mode) {
    "bus" -> "Autobus"
    "tram" -> "Tramvaj"
    "train" -> "Vlak"
    "ferry" -> "Trajekt"
    "catamaran" -> "Katamaran"
    else -> "Vozilo"
}

@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = brandShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        content = content
    )
}

@Composable
fun ScreenHeading(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onBackground, letterSpacing = (-0.7).sp)
}

@Composable
fun SectionHeading(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
}

@Composable
fun BrandHeader(kind: String, modifier: Modifier = Modifier, height: Int = 178) {
    val context = LocalContext.current
    val filename = when (kind) {
        "more" -> "brand/more_banner.jpg"
        "welcome" -> "brand/welcome_city.jpg"
        else -> "brand/home_banner.jpg"
    }
    val picture = remember(filename) {
        runCatching {
            context.assets.open(filename).use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
        }.getOrNull()
    }
    Box(
        modifier.fillMaxWidth().height(height.dp)
            .background(Brush.horizontalGradient(listOf(BrandNavy, Color(0xFF12407D), BrandNavy)))
    ) {
        if (picture != null) {
            Image(bitmap = picture, contentDescription = "KRENI — grafički motiv hrvatskog javnog prijevoza",
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Column(Modifier.align(Alignment.CenterStart).padding(22.dp)) {
                Text(if (kind == "more") "Više" else "KRENI",
                    fontSize = if (kind == "more") 48.sp else 40.sp,
                    fontWeight = FontWeight.Black, color = Color.White)
                Text(if (kind == "more") "POSTAVKE I OMILJENO"
                else "JAVNI PRIJEVOZ UVIJEK S TOBOM",
                    color = BrandAqua, fontSize = 11.sp, letterSpacing = 2.sp)
            }
        }
    }
}

@Composable
fun FeatureTile(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
                tint: Color, background: Color, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(background)
            .clickable(onClick = onClick).padding(vertical = 20.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(Modifier.size(47.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.65f)),
            contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(27.dp))
        }
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandNavy,
            maxLines = 1)
    }
}

@Composable
fun StopListCard(
    stop: Stop,
    favorite: Boolean,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumCard(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 79.dp).padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(51.dp).clip(RoundedCornerShape(16.dp))
                .background(BrandPaleBlue), contentAlignment = Alignment.Center) {
                Icon(transportIcon(stop.modes.firstOrNull() ?: "bus"), null,
                    tint = BrandBlue, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f).clickable(onClick = onOpen)) {
                Text(stop.name, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    stop.modes.take(3).forEach { mode ->
                        Text(transportName(mode), fontSize = 11.sp,
                            color = BrandBlue, modifier = Modifier
                                .background(BrandPaleBlue, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp))
                    }
                }
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (favorite) "Ukloni iz omiljenih" else "Spremi omiljeno",
                    tint = if (favorite) BrandBlue else BrandMuted)
            }
            IconButton(onClick = onOpen, modifier = Modifier.size(33.dp)) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Otvori", tint = BrandMuted)
            }
        }
    }
}

@Composable
fun StatusCard(title: String, details: String, icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.InfoOutline) {
    PremiumCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun PrimaryAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick, enabled = enabled, modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
    ) {
        Text(text, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(12.dp))
        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(19.dp))
    }
}

@Composable
fun TransportPill(text: String, color: Color = BrandBlue) {
    Box(Modifier.background(color, RoundedCornerShape(8.dp)).padding(horizontal = 9.dp, vertical = 6.dp)) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
