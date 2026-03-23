package com.example.zariaserviceconnect.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val PrimaryBlue  = Color(0xFF1565C0)
val AccentGreen  = Color(0xFF00897B)
val WarningOrange = Color(0xFFF57C00)

// ── Loading Spinner ───────────────────────────────────────────────────────────
@Composable
fun LoadingView(message: String = "Loading...") {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = PrimaryBlue)
            Spacer(Modifier.height(12.dp))
            Text(message, color = Color.Gray)
        }
    }
}

// ── Error View ────────────────────────────────────────────────────────────────
@Composable
fun ErrorView(message: String, onRetry: (() -> Unit)? = null) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(12.dp))
            Text(message, color = Color.Gray, fontSize = 15.sp)
            if (onRetry != null) {
                Spacer(Modifier.height(16.dp))
                Button(onClick = onRetry) { Text("Try Again") }
            }
        }
    }
}

// ── Avatar circle with initials ───────────────────────────────────────────────
@Composable
fun AvatarCircle(name: String, size: Int = 48, color: Color = PrimaryBlue) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(color.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
            fontSize = (size / 2).sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// ── Verified Badge ────────────────────────────────────────────────────────────
@Composable
fun VerifiedBadge() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text("Verified", color = Color(0xFF1976D2), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

// ── Booking status colored chip ───────────────────────────────────────────────
@Composable
fun StatusChip(status: String) {
    val (bg, fg) = when (status.lowercase()) {
        "pending"   -> Color(0xFFFFF3E0) to Color(0xFFF57C00)
        "accepted"  -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        "completed" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "cancelled" -> Color(0xFFF5F5F5) to Color(0xFF757575)
        "declined"  -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        else        -> Color(0xFFF5F5F5) to Color(0xFF757575)
    }
    Surface(shape = RoundedCornerShape(20.dp), color = bg) {
        Text(
            status.uppercase(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = fg, fontSize = 11.sp, fontWeight = FontWeight.Bold
        )
    }
}

// ── Star Rating Display ───────────────────────────────────────────────────────
@Composable
fun StarRatingDisplay(rating: Double, totalReviews: Int = 0) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { i ->
            Icon(
                imageVector = when {
                    i < rating.toInt() -> Icons.Default.Star
                    i < rating         -> Icons.Default.StarHalf
                    else               -> Icons.Default.StarBorder
                },
                contentDescription = null,
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(
            if (rating > 0) "${rating} ($totalReviews reviews)" else "No reviews yet",
            color = Color.Gray, fontSize = 13.sp
        )
    }
}

// ── Section Title ─────────────────────────────────────────────────────────────
@Composable
fun SectionTitle(text: String) {
    Text(
        text, fontSize = 17.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
