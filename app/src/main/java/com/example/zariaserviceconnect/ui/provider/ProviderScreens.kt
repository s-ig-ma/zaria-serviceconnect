package com.example.zariaserviceconnect.ui.provider

import androidx.compose.foundation.background
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zariaserviceconnect.models.BookingModel
import com.example.zariaserviceconnect.ui.shared.*
import com.example.zariaserviceconnect.viewmodel.MainViewModel
import com.example.zariaserviceconnect.viewmodel.UiState

// ── Provider Jobs Screen ──────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderJobsScreen(
    viewModel : MainViewModel,
    onLogout  : () -> Unit
) {
    val bookingsState     by viewModel.bookings.collectAsState()
    val bookingAction     by viewModel.bookingAction.collectAsState()
    val snackbarHostState  = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadProviderBookings() }

    LaunchedEffect(bookingAction) {
        if (bookingAction is UiState.Success<*> || bookingAction is UiState.Error) {
            val msg = when (bookingAction) {
                is UiState.Success<*> -> (bookingAction as UiState.Success<*>).data.toString()
                is UiState.Error      -> (bookingAction as UiState.Error).message
                else                  -> ""
            }
            if (msg.isNotEmpty()) snackbarHostState.showSnackbar(msg)
            viewModel.resetBookingAction()
            viewModel.loadProviderBookings()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job Requests") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = PrimaryBlue,
                    titleContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)   // ← SafeArea
        ) {
            when (val state = bookingsState) {
                is UiState.Loading -> LoadingView()
                is UiState.Error   -> ErrorView(state.message) {
                    viewModel.loadProviderBookings()
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(Icons.Default.WorkOff, null,
                                    modifier = Modifier.size(64.dp),
                                    tint     = Color.Gray)
                                Spacer(Modifier.height(12.dp))
                                Text("No booking requests yet.",
                                    color = Color.Gray)
                                Spacer(Modifier.height(8.dp))
                                Text("New requests will appear here.",
                                    color    = Color.Gray,
                                    fontSize = 13.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier       = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top    = 8.dp,
                                bottom = 24.dp
                            )
                        ) {
                            items(state.data) { booking ->
                                ProviderBookingCard(
                                    booking    = booking,
                                    onAccept   = {
                                        viewModel.updateBookingStatus(
                                            booking.id, "accepted")
                                    },
                                    onDecline  = {
                                        viewModel.updateBookingStatus(
                                            booking.id, "declined")
                                    },
                                    onComplete = {
                                        viewModel.updateBookingStatus(
                                            booking.id, "completed")
                                    }
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ProviderBookingCard(
    booking    : BookingModel,
    onAccept   : () -> Unit,
    onDecline  : () -> Unit,
    onComplete : () -> Unit
) {
    val context       = LocalContext.current
    val residentName  = booking.resident?.name  ?: "Resident"
    val residentPhone = booking.resident?.phone ?: ""

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarCircle(residentName, size = 44, color = PrimaryBlue)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(residentName,
                        fontWeight = FontWeight.Bold,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis)
                    if (residentPhone.isNotEmpty())
                        Text(residentPhone,
                            color    = Color.Gray,
                            fontSize = 13.sp)
                }
                StatusChip(booking.status)
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text("Service:", color = Color.Gray, fontSize = 12.sp)
            Text(booking.serviceDescription,
                fontWeight = FontWeight.Medium,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null,
                    modifier = Modifier.size(14.dp),
                    tint     = Color.Gray)
                Spacer(Modifier.width(4.dp))
                Text(
                    "${booking.scheduledDate} at ${booking.scheduledTime}",
                    color    = Color.Gray,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }
            if (booking.notes != null) {
                Spacer(Modifier.height(4.dp))
                Text("Note: ${booking.notes}",
                    color    = Color.Gray,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis)
            }

            Spacer(Modifier.height(12.dp))

            when (booking.status) {
                "pending" -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick  = onAccept,
                            modifier = Modifier.weight(1f),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32))
                        ) {
                            Icon(Icons.Default.Check, null,
                                modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Accept")
                        }
                        OutlinedButton(
                            onClick  = onDecline,
                            modifier = Modifier.weight(1f),
                            colors   = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Red)
                        ) {
                            Icon(Icons.Default.Close, null,
                                modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Decline")
                        }
                    }
                }
                "accepted" -> {
                    Button(
                        onClick  = onComplete,
                        modifier = Modifier.fillMaxWidth(),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.DoneAll, null,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Mark as Completed")
                    }
                }
            }

            if (residentPhone.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                TextButton(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_DIAL,
                            Uri.parse("tel:$residentPhone")
                        )
                        context.startActivity(intent)
                    }
                ) {
                    Icon(Icons.Default.Phone, null,
                        modifier = Modifier.size(16.dp),
                        tint     = AccentGreen)
                    Spacer(Modifier.width(4.dp))
                    Text("Call Resident", color = AccentGreen)
                }
            }
        }
    }
}

// ── Provider Profile Screen ───────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderProfileScreen(viewModel: MainViewModel) {
    val profileState        by viewModel.myProviderProfile.collectAsState()
    val availabilityAction  by viewModel.availabilityAction.collectAsState()
    val snackbarHostState    = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadMyProviderProfile() }

    LaunchedEffect(availabilityAction) {
        when (availabilityAction) {
            is UiState.Success<*> -> {
                snackbarHostState.showSnackbar(
                    (availabilityAction as UiState.Success<*>).data.toString())
                viewModel.resetAvailabilityAction()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(
                    (availabilityAction as UiState.Error).message)
                viewModel.resetAvailabilityAction()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = PrimaryBlue,
                    titleContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)   // ← SafeArea
        ) {
            when (val state = profileState) {
                is UiState.Loading -> LoadingView()
                is UiState.Error   -> ErrorView(state.message) {
                    viewModel.loadMyProviderProfile()
                }
                is UiState.Success -> {
                    val p = state.data

                    // LazyColumn scrolls — no overflow possible
                    LazyColumn(
                        modifier       = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        // Status warnings
                        if (p.status == "pending") {
                            item {
                                Card(
                                    colors   = CardDefaults.cardColors(
                                        containerColor = Color(0xFFFFF3E0)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(Modifier.padding(12.dp)) {
                                        Icon(Icons.Default.HourglassEmpty, null,
                                            tint = WarningOrange)
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Your account is pending admin approval. " +
                                            "You won't appear in search results until approved.",
                                            color    = Color(0xFF5D4037),
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }

                        if (p.status == "suspended") {
                            item {
                                Card(
                                    colors   = CardDefaults.cardColors(
                                        containerColor = Color(0xFFFFEBEE)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(Modifier.padding(12.dp)) {
                                        Icon(Icons.Default.Block, null,
                                            tint = Color.Red)
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Your account has been suspended. " +
                                            "Please contact admin.",
                                            color    = Color.Red,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Profile header
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AvatarCircle(p.user.name, size = 80)
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    p.user.name,
                                    fontSize   = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines   = 1,
                                    overflow   = TextOverflow.Ellipsis
                                )
                                Text(p.category.name, color = Color.Gray)
                                Spacer(Modifier.height(6.dp))
                                if (p.status == "approved") VerifiedBadge()
                                Spacer(Modifier.height(6.dp))
                                StarRatingDisplay(p.averageRating, p.totalReviews)
                            }
                        }

                        // Availability toggle card
                        item {
                            val p = (profileState as UiState.Success<*>).data
                            if (p is com.example.zariaserviceconnect.models.ProviderModel
                                && p.status == "approved") {
                                AvailabilityToggleCard(
                                    currentStatus = p.availabilityStatus,
                                    isLoading     = availabilityAction is UiState.Loading,
                                    onStatusChange = { newStatus ->
                                        viewModel.setAvailability(newStatus)
                                    }
                                )
                            }
                        }

                        // Details card
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("My Details",
                                        fontWeight = FontWeight.Bold,
                                        fontSize   = 16.sp)
                                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                    ProfileDetailRow(Icons.Default.Email,
                                        "Email", p.user.email)
                                    ProfileDetailRow(Icons.Default.Phone,
                                        "Phone", p.user.phone)
                                    ProfileDetailRow(Icons.Default.Work,
                                        "Experience",
                                        "${p.yearsOfExperience} years")
                                    if (p.location != null)
                                        ProfileDetailRow(Icons.Default.LocationOn,
                                            "Location", p.location)
                                    ProfileDetailRow(Icons.Default.VerifiedUser,
                                        "Status", p.status.uppercase())
                                    if (p.description != null) {
                                        Spacer(Modifier.height(8.dp))
                                        Text("About Me",
                                            fontWeight = FontWeight.SemiBold)
                                        Text(p.description,
                                            color    = Color.Gray,
                                            fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun ProfileDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        Modifier.padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text("$label: ", fontWeight = FontWeight.Medium)
        Text(
            value,
            color    = Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}


// ── Availability Toggle Card ──────────────────────────────────────────────────
@Composable
fun AvailabilityToggleCard(
    currentStatus : String,
    isLoading     : Boolean,
    onStatusChange : (String) -> Unit
) {
    val (statusColor, statusBg, statusLabel) = when (currentStatus) {
        "busy"    -> Triple(Color(0xFFE65100), Color(0xFFFFF3E0), "Busy")
        "offline" -> Triple(Color(0xFF757575), Color(0xFFF5F5F5), "Offline")
        else      -> Triple(Color(0xFF2E7D32), Color(0xFFE8F5E9), "Available")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = statusBg)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(12.dp)
                        .background(statusColor,
                            androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Status: $statusLabel",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp,
                    color      = statusColor
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                when (currentStatus) {
                    "busy"    -> "You are currently busy with a booking."
                    "offline" -> "You are offline. Residents cannot see you."
                    else      -> "You are available and visible to residents."
                },
                fontSize = 13.sp,
                color    = Color.Gray
            )
            Spacer(Modifier.height(12.dp))

            // Three buttons for the three statuses
            Text("Set your status:",
                fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Available button
                OutlinedButton(
                    onClick  = { if (currentStatus != "available") onStatusChange("available") },
                    modifier = Modifier.weight(1f),
                    enabled  = !isLoading && currentStatus != "available",
                    colors   = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (currentStatus == "available")
                            Color(0xFF2E7D32) else Color.Transparent,
                        contentColor   = if (currentStatus == "available")
                            Color.White else Color(0xFF2E7D32)
                    )
                ) {
                    Text("Available", fontSize = 12.sp)
                }

                // Busy button
                OutlinedButton(
                    onClick  = { if (currentStatus != "busy") onStatusChange("busy") },
                    modifier = Modifier.weight(1f),
                    enabled  = !isLoading && currentStatus != "busy",
                    colors   = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (currentStatus == "busy")
                            Color(0xFFE65100) else Color.Transparent,
                        contentColor   = if (currentStatus == "busy")
                            Color.White else Color(0xFFE65100)
                    )
                ) {
                    Text("Busy", fontSize = 12.sp)
                }

                // Offline button
                OutlinedButton(
                    onClick  = { if (currentStatus != "offline") onStatusChange("offline") },
                    modifier = Modifier.weight(1f),
                    enabled  = !isLoading && currentStatus != "offline",
                    colors   = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (currentStatus == "offline")
                            Color(0xFF757575) else Color.Transparent,
                        contentColor   = if (currentStatus == "offline")
                            Color.White else Color(0xFF757575)
                    )
                ) {
                    Text("Offline", fontSize = 12.sp)
                }
            }

            if (isLoading) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color    = statusColor
                )
            }
        }
    }
}
