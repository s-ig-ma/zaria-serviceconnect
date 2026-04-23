package com.example.zariaserviceconnect.ui.resident

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zariaserviceconnect.models.BookingModel
import com.example.zariaserviceconnect.models.ComplaintModel
import com.example.zariaserviceconnect.models.MessageModel
import com.example.zariaserviceconnect.models.NotificationModel
import com.example.zariaserviceconnect.models.ProviderModel
import com.example.zariaserviceconnect.models.UserModel
import com.example.zariaserviceconnect.ui.shared.*
import com.example.zariaserviceconnect.viewmodel.MainViewModel
import com.example.zariaserviceconnect.viewmodel.UiState

// ── Categories Screen with Global Search ─────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel          : MainViewModel,
    onCategorySelected : (Int, String) -> Unit,
    onAllProviders     : () -> Unit,
    onBookingsClick    : () -> Unit,
    onLogout           : () -> Unit,
    onProviderClick    : (Int) -> Unit = {}
) {
    val categoriesState by viewModel.categories.collectAsState()
    val searchQuery     by viewModel.searchQuery.collectAsState()
    val searchResults   by viewModel.searchResults.collectAsState()
    val isSearching     by viewModel.isSearching.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadCategories() }

    val categoryIcons = mapOf(
        "Plumbing"         to Pair(Icons.Default.Plumbing,           Color(0xFF1565C0)),
        "Electrical"       to Pair(Icons.Default.ElectricalServices,  Color(0xFFF57C00)),
        "Cleaning"         to Pair(Icons.Default.CleaningServices,    Color(0xFF00897B)),
        "Appliance Repair" to Pair(Icons.Default.HomeRepairService,   Color(0xFF7B1FA2)),
        "Carpentry"        to Pair(Icons.Default.Carpenter,           Color(0xFFC62828))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zaria ServiceConnect",
                    maxLines = 1, overflow = TextOverflow.Ellipsis) },
                actions = {
                    IconButton(onClick = onBookingsClick) {
                        Icon(Icons.Default.CalendarToday, "Bookings", tint = Color.White)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = PrimaryBlue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ── Search Bar ────────────────────────────────────────────────────
            Surface(shadowElevation = 4.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    OutlinedTextField(
                        value         = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        modifier      = Modifier.fillMaxWidth(),
                        placeholder   = {
                            Text("Search by name, service, description...",
                                color = Color.Gray)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, null,
                                tint = if (isSearching) PrimaryBlue else Color.Gray)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearSearch() }) {
                                    Icon(Icons.Default.Close, "Clear search",
                                        tint = Color.Gray)
                                }
                            }
                        },
                        singleLine = true,
                        shape      = RoundedCornerShape(12.dp),
                        colors     = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = PrimaryBlue,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f)
                        )
                    )

                    // Search hint chips shown below the search bar
                    if (!isSearching) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Plumber", "Electrician", "Cleaning").forEach { hint ->
                                SuggestionChip(
                                    onClick = { viewModel.onSearchQueryChanged(hint) },
                                    label  = { Text(hint, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // ── Content: Search Results OR Category List ───────────────────────
            if (isSearching) {
                // ── Search Results ────────────────────────────────────────────
                SearchResultsContent(
                    searchQuery   = searchQuery,
                    searchResults = searchResults,
                    onProviderClick = onProviderClick,
                    onClearSearch = { viewModel.clearSearch() }
                )
            } else {
                // ── Normal Category List ──────────────────────────────────────
                when (val state = categoriesState) {
                    is UiState.Loading -> LoadingView("Loading services...")
                    is UiState.Error   -> ErrorView(state.message) {
                        viewModel.loadCategories()
                    }
                    is UiState.Success -> {
                        LazyColumn(
                            contentPadding      = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier            = Modifier.fillMaxSize()
                        ) {
                        item {
                            Card(
                                modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onAllProviders() },
                                    colors = CardDefaults.cardColors(
                                        containerColor = PrimaryBlue)
                                ) {
                                    Row(
                                        Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.People, null,
                                            tint     = Color.White,
                                            modifier = Modifier.size(32.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text("Browse All Providers",
                                            color      = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize   = 16.sp)
                                    }
                                }
                            }
                            items(state.data) { category ->
                                val (icon, color) = categoryIcons[category.name]
                                    ?: Pair(Icons.Default.MiscellaneousServices, Color.Gray)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onCategorySelected(category.id, category.name)
                                        }
                                ) {
                                    Row(
                                        Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .background(
                                                    color.copy(alpha = 0.1f),
                                                    RoundedCornerShape(12.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(icon, null,
                                                tint     = color,
                                                modifier = Modifier.size(30.dp))
                                        }
                                        Spacer(Modifier.width(14.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(category.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize   = 16.sp,
                                                maxLines   = 1,
                                                overflow   = TextOverflow.Ellipsis)
                                            if (category.description != null)
                                                Text(category.description,
                                                    color    = Color.Gray,
                                                    fontSize = 13.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis)
                                        }
                                        Icon(Icons.Default.ChevronRight, null,
                                            tint = Color.Gray)
                                    }
                                }
                            }
                            item { Spacer(Modifier.height(16.dp)) }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

// ── Search Results Content ────────────────────────────────────────────────────
@Composable
private fun SearchResultsContent(
    searchQuery     : String,
    searchResults   : UiState<List<ProviderModel>>,
    onProviderClick : (Int) -> Unit,
    onClearSearch   : () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {

        // Results header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Results for \"$searchQuery\"",
                fontWeight = FontWeight.Bold,
                fontSize   = 15.sp,
                modifier   = Modifier.weight(1f),
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            TextButton(onClick = onClearSearch) {
                Text("Clear", color = PrimaryBlue)
            }
        }

        when (searchResults) {
            is UiState.Loading -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PrimaryBlue)
                        Spacer(Modifier.height(12.dp))
                        Text("Searching...", color = Color.Gray)
                    }
                }
            }

            is UiState.Error -> {
                ErrorView(searchResults.message) {}
            }

            is UiState.Success -> {
                if (searchResults.data.isEmpty()) {
                    // No results state
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier            = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint     = Color.Gray
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No providers found",
                                fontSize   = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Color.Gray
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "No results for \"$searchQuery\".\nTry a different name or service.",
                                fontSize  = 14.sp,
                                color     = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(20.dp))
                            OutlinedButton(onClick = onClearSearch) {
                                Icon(Icons.Default.ArrowBack, null,
                                    modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Back to Categories")
                            }
                        }
                    }
                } else {
                    // Results list
                    LazyColumn(
                        modifier       = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item {
                            Text(
                                "${searchResults.data.size} provider(s) found",
                                color    = Color.Gray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(
                                    horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                        items(searchResults.data) { provider ->
                            SearchProviderCard(
                                provider = provider,
                                query    = searchQuery,
                                onClick  = { onProviderClick(provider.id) }
                            )
                        }
                    }
                }
            }

            is UiState.Idle -> {
                // Show nothing while user hasn't typed yet
            }
        }
    }
}

// ── Availability Badge (reusable) ────────────────────────────────────────────
@Composable
fun AvailabilityBadge(status: String) {
    val (color, bgColor, label) = when (status) {
        "busy"    -> Triple(Color(0xFFE65100), Color(0xFFFFF3E0), "Busy")
        "offline" -> Triple(Color(0xFF757575), Color(0xFFF5F5F5), "Offline")
        else      -> Triple(Color(0xFF2E7D32), Color(0xFFE8F5E9), "Available")
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, androidx.compose.foundation.shape.CircleShape)
        )
        Spacer(Modifier.width(4.dp))
        Text(label,
            color      = color,
            fontSize   = 11.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
    }
}

// ── Search Provider Card ──────────────────────────────────────────────────────
// Shows matched provider with category badge
@Composable
private fun SearchProviderCard(
    provider : ProviderModel,
    query    : String,
    onClick  : () -> Unit
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarCircle(name = provider.user.name, size = 52, imagePath = provider.user.profilePhoto)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        provider.user.name,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 15.sp,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(4.dp))
                    VerifiedBadge()
                }
                // Category badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = PrimaryBlue.copy(alpha = 0.1f)
                ) {
                    Text(
                        provider.displayServiceName,
                        color    = PrimaryBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(
                            horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                StarRatingDisplay(provider.averageRating, provider.totalReviews)
                Spacer(Modifier.height(3.dp))
                AvailabilityBadge(provider.availabilityStatus)
                if (provider.description != null) {
                    Text(
                        provider.description,
                        color    = Color.Gray,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (provider.location != null) {
                        Icon(Icons.Default.LocationOn, null,
                            modifier = Modifier.size(13.dp),
                            tint     = Color.Gray)
                        Spacer(Modifier.width(2.dp))
                        Text(provider.location,
                            color    = Color.Gray,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false))
                    }
                    if (provider.distanceKm != null) {
                        if (provider.location != null) {
                            Text(" • ", color = Color.Gray, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.NearMe, null,
                            modifier = Modifier.size(13.dp),
                            tint     = Color(0xFF1565C0))
                        Spacer(Modifier.width(2.dp))
                        val distText = if (provider.distanceKm < 1.0)
                            "${(provider.distanceKm * 1000).toInt()} m away"
                        else
                            "${"%.1f".format(provider.distanceKm)} km away"
                        Text(distText,
                            color      = Color(0xFF1565C0),
                            fontSize   = 12.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                    }
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}

// ── Providers List Screen ─────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvidersListScreen(
    categoryId      : Int?,
    categoryName    : String,
    viewModel       : MainViewModel,
    onProviderClick : (Int) -> Unit,
    onBack          : () -> Unit
) {
    val providersState by viewModel.providers.collectAsState()
    LaunchedEffect(categoryId) { viewModel.loadProviders(categoryId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName,
                    maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = PrimaryBlue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = providersState) {
                is UiState.Loading -> LoadingView("Finding providers...")
                is UiState.Error   -> ErrorView(state.message) {
                    viewModel.loadProviders(categoryId)
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(Icons.Default.PersonSearch, null,
                                    modifier = Modifier.size(64.dp), tint = Color.Gray)
                                Spacer(Modifier.height(12.dp))
                                Text("No providers found in this category yet.",
                                    color     = Color.Gray,
                                    textAlign = TextAlign.Center)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier       = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(state.data) { provider ->
                                ProviderCard(
                                    provider = provider,
                                    onClick  = { onProviderClick(provider.id) }
                                )
                            }
                            item { Spacer(Modifier.height(16.dp)) }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

// ── Provider Card ─────────────────────────────────────────────────────────────
@Composable
fun ProviderCard(provider: ProviderModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            AvatarCircle(name = provider.user.name, size = 52, imagePath = provider.user.profilePhoto)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(provider.user.name,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 15.sp,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f))
                    Spacer(Modifier.width(4.dp))
                    VerifiedBadge()
                }
                Text(provider.displayServiceName, color = Color.Gray, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                StarRatingDisplay(provider.averageRating, provider.totalReviews)
                Spacer(Modifier.height(3.dp))
                AvailabilityBadge(provider.availabilityStatus)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (provider.location != null) {
                        Icon(Icons.Default.LocationOn, null,
                            modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(Modifier.width(2.dp))
                        Text(provider.location,
                            color    = Color.Gray,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false))
                    }
                    if (provider.distanceKm != null) {
                        if (provider.location != null) {
                            Text(" • ", color = Color.Gray, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.NearMe, null,
                            modifier = Modifier.size(13.dp),
                            tint     = Color(0xFF1565C0))
                        Spacer(Modifier.width(2.dp))
                        val distText = if (provider.distanceKm < 1.0)
                            "${(provider.distanceKm * 1000).toInt()} m away"
                        else
                            "${"%.1f".format(provider.distanceKm)} km away"
                        Text(distText,
                            color      = Color(0xFF1565C0),
                            fontSize   = 12.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                    }
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}

// ── Provider Profile Screen ───────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderProfileScreen(
    providerId    : Int,
    viewModel     : MainViewModel,
    onBookService : (Int) -> Unit,
    onBack        : () -> Unit
) {
    val context       = LocalContext.current
    val providerState by viewModel.selectedProvider.collectAsState()
    val reviewsState  by viewModel.reviews.collectAsState()

    LaunchedEffect(providerId) {
        viewModel.loadProviderById(providerId)
        viewModel.loadProviderReviews(providerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Provider Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = PrimaryBlue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = providerState) {
                is UiState.Loading -> LoadingView()
                is UiState.Error   -> ErrorView(state.message)
                is UiState.Success -> {
                    val p = state.data
                    LazyColumn(
                        modifier       = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        item {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .background(PrimaryBlue.copy(alpha = 0.05f))
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AvatarCircle(name = p.user.name, size = 80, imagePath = p.user.profilePhoto)
                                Spacer(Modifier.height(10.dp))
                                Text(p.user.name,
                                    fontSize   = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines   = 1,
                                    overflow   = TextOverflow.Ellipsis)
                                Text(p.displayServiceName, color = Color.Gray, fontSize = 15.sp)
                                Spacer(Modifier.height(6.dp))
                                VerifiedBadge()
                                Spacer(Modifier.height(6.dp))
                                StarRatingDisplay(p.averageRating, p.totalReviews)
                            }
                        }
                        item {
                            Card(Modifier.fillMaxWidth().padding(16.dp)) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("Provider Details",
                                        fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                    InfoRow(Icons.Default.Work, "Experience",
                                        "${p.yearsOfExperience} years")
                                    if (p.location != null)
                                        InfoRow(Icons.Default.LocationOn, "Location", p.location)
                                    InfoRow(Icons.Default.Phone, "Phone", p.user.phone)
                                    if (p.description != null) {
                                        Spacer(Modifier.height(8.dp))
                                        Text("About", fontWeight = FontWeight.SemiBold)
                                        Text(p.description, color = Color.Gray, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                        item {
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick  = { onBookService(p.id) },
                                    modifier = Modifier.weight(2f).height(48.dp),
                                    colors   = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryBlue)
                                ) {
                                    Icon(Icons.Default.CalendarToday, null,
                                        modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Book Service", fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = {
                                        val intent = Intent(
                                            Intent.ACTION_DIAL,
                                            Uri.parse("tel:${p.user.phone}")
                                        )
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    colors   = ButtonDefaults.buttonColors(
                                        containerColor = AccentGreen)
                                ) {
                                    Icon(Icons.Default.Phone, null,
                                        modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Call")
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                        item { SectionTitle("Customer Reviews") }
                        when (val rv = reviewsState) {
                            is UiState.Success -> {
                                if (rv.data.isEmpty()) {
                                    item {
                                        Text("No reviews yet.", color = Color.Gray,
                                            modifier = Modifier.padding(16.dp))
                                    }
                                } else {
                                    items(rv.data) { review ->
                                        Card(
                                            Modifier.fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                        ) {
                                            Row(Modifier.padding(12.dp)) {
                                                AvatarCircle(review.resident.name, size = 40)
                                                Spacer(Modifier.width(10.dp))
                                                Column {
                                                    Text(review.resident.name,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines   = 1,
                                                        overflow   = TextOverflow.Ellipsis)
                                                    StarRatingDisplay(review.rating.toDouble())
                                                    if (review.comment != null)
                                                        Text(review.comment,
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
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text("$label: ", fontWeight = FontWeight.Medium)
        Text(value, color = Color.Gray, maxLines = 1,
            overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
    }
}

// ── Book Service Screen ───────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookServiceScreen(
    providerId : Int,
    viewModel  : MainViewModel,
    onSuccess  : () -> Unit,
    onBack     : () -> Unit
) {
    var description  by remember { mutableStateOf("") }
    var date         by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var serviceAddress by remember { mutableStateOf("") }
    var notes        by remember { mutableStateOf("") }
    val bookingAction  by viewModel.bookingAction.collectAsState()
    val providerState  by viewModel.selectedProvider.collectAsState()
    val profileState   by viewModel.myUserProfile.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val timeSlots = listOf(
        "8:00 AM","9:00 AM","10:00 AM","11:00 AM","12:00 PM",
        "1:00 PM","2:00 PM","3:00 PM","4:00 PM","5:00 PM"
    )

    LaunchedEffect(Unit) { viewModel.loadMyUserProfile() }

    LaunchedEffect(profileState) {
        if (profileState is UiState.Success<*>) {
            val profile = (profileState as UiState.Success<*>).data
            if (profile is UserModel && serviceAddress.isBlank()) {
                serviceAddress = profile.homeAddress ?: ""
            }
        }
    }

    LaunchedEffect(bookingAction) {
        when (bookingAction) {
            is UiState.Success<*> -> {
                snackbarHostState.showSnackbar(
                    (bookingAction as UiState.Success<*>).data.toString()
                )
                viewModel.resetBookingAction()
                onSuccess()
            }
            is UiState.Error      -> {
                snackbarHostState.showSnackbar(
                    (bookingAction as UiState.Error).message)
                viewModel.resetBookingAction()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Service") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (providerState is UiState.Success<*>) {
                val data = (providerState as UiState.Success<*>).data
                if (data is ProviderModel) {
                    Card(colors = CardDefaults.cardColors(
                        containerColor = PrimaryBlue.copy(alpha = 0.07f))) {
                        Row(Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            AvatarCircle(data.user.name, size = 44, color = PrimaryBlue, imagePath = data.user.profilePhoto)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(data.user.name, fontWeight = FontWeight.Bold,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(data.displayServiceName, color = Color.Gray, fontSize = 13.sp)
                                Text("Availability: ${data.availabilityStatus}",
                                    color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            Text("Describe the service needed", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = description, onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                placeholder = { Text("e.g. Fix leaking pipe under kitchen sink") },
                maxLines = 4
            )
            Spacer(Modifier.height(16.dp))

            Text("Preferred Date (DD/MM/YYYY)", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = date, onValueChange = { date = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. 25/06/2025") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))

            Text("Select Time", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            timeSlots.chunked(3).forEach { rowSlots ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()) {
                    rowSlots.forEach { time ->
                        FilterChip(
                            selected = selectedTime == time,
                            onClick  = { selectedTime = time },
                            label    = { Text(time, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - rowSlots.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(6.dp))
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Additional Notes (optional)") },
                placeholder = { Text("e.g. Gate code is 1234") },
                maxLines = 2
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = serviceAddress, onValueChange = { serviceAddress = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Home Address for This Booking") },
                placeholder = { Text("This auto-fills from your profile, but you can change it") },
                maxLines = 3
            )
            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    viewModel.createBooking(
                        providerId, description.trim(), date.trim(),
                        selectedTime, serviceAddress.trim(), notes.trim().ifEmpty { null }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled  = bookingAction !is UiState.Loading
                        && description.isNotBlank()
                        && date.isNotBlank()
                        && selectedTime.isNotEmpty()
                        && serviceAddress.isNotBlank(),
                colors   = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (bookingAction is UiState.Loading)
                    CircularProgressIndicator(color = Color.White,
                        modifier = Modifier.size(22.dp))
                else {
                    Icon(Icons.Default.Send, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Send Booking Request",
                        fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Resident Bookings Screen ──────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentBookingsScreen(
    viewModel         : MainViewModel,
    onLeaveReview     : (Int, String) -> Unit,
    onSubmitComplaint : (Int, String) -> Unit,
    onBack            : () -> Unit
) {
    val bookingsState     by viewModel.bookings.collectAsState()
    val bookingAction     by viewModel.bookingAction.collectAsState()
    val snackbarHostState  = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadResidentBookings() }
    LaunchedEffect(bookingAction) {
        if (bookingAction is UiState.Success<*>) {
            snackbarHostState.showSnackbar((bookingAction as UiState.Success<*>).data.toString())
            viewModel.resetBookingAction()
            viewModel.loadResidentBookings()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bookings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = bookingsState) {
                is UiState.Loading -> LoadingView()
                is UiState.Error   -> ErrorView(state.message) {
                    viewModel.loadResidentBookings()
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)) {
                                Icon(Icons.Default.CalendarToday, null,
                                    modifier = Modifier.size(64.dp), tint = Color.Gray)
                                Spacer(Modifier.height(12.dp))
                                Text("No bookings yet.", color = Color.Gray)
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)) {
                            items(state.data) { booking ->
                                val providerName =
                                    booking.provider?.user?.name ?: "Provider"
                                ResidentBookingCard(
                                    booking     = booking,
                                    onCancel    = {
                                        viewModel.updateBookingStatus(
                                            booking.id, "cancelled")
                                    },
                                    onConfirmCompletion = {
                                        viewModel.updateBookingStatus(
                                            booking.id, "completed")
                                    },
                                    onReview    = {
                                        onLeaveReview(booking.id, providerName)
                                    },
                                    onComplaint = {
                                        onSubmitComplaint(booking.id, providerName)
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
fun ResidentBookingCard(
    booking     : BookingModel,
    onCancel    : () -> Unit,
    onConfirmCompletion : () -> Unit,
    onReview    : () -> Unit,
    onComplaint : () -> Unit
) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(booking.provider?.user?.name ?: "Provider",
                    fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                StatusChip(booking.status)
            }
            Text(booking.provider?.displayServiceName ?: "", color = Color.Gray, fontSize = 13.sp)
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text(booking.serviceDescription, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null,
                    modifier = Modifier.size(14.dp), tint = Color.Gray)
                Spacer(Modifier.width(4.dp))
                Text("${booking.scheduledDate} at ${booking.scheduledTime}",
                    color = Color.Gray, fontSize = 13.sp, maxLines = 1)
            }
            if (booking.providerNotes != null) {
                Spacer(Modifier.height(4.dp))
                Text("Provider note: ${booking.providerNotes}",
                    fontSize = 13.sp, color = Color(0xFF5D4037), maxLines = 2)
            }
            if (booking.serviceAddress != null) {
                Spacer(Modifier.height(4.dp))
                Text("Address: ${booking.serviceAddress}",
                    fontSize = 13.sp, color = Color.Gray, maxLines = 3)
            }
            when (booking.status) {
                "pending" -> {
                    Spacer(Modifier.height(10.dp))
                    TextButton(onClick = onCancel,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
                        Icon(Icons.Default.Cancel, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Cancel Booking")
                    }
                }
                "completion_requested" -> {
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = onConfirmCompletion, modifier = Modifier.fillMaxWidth()) {
                        Text("Confirm Satisfaction")
                    }
                }
                "completed" -> {
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onReview, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Star, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Review", fontSize = 13.sp)
                        }
                        OutlinedButton(onClick = onComplaint, modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Red)) {
                            Icon(Icons.Default.Report, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Complaint", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentProfileScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    val profileState by viewModel.myUserProfile.collectAsState()
    val profileAction by viewModel.profileAction.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var homeAddress by remember { mutableStateOf("") }
    var profilePhotoUri by remember { mutableStateOf<Uri?>(null) }
    var formInitialized by remember { mutableStateOf(false) }
    val profilePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        profilePhotoUri = it
    }

    LaunchedEffect(Unit) { viewModel.loadMyUserProfile() }

    LaunchedEffect(profileAction) {
        when (profileAction) {
            is UiState.Success<*> -> {
                snackbarHostState.showSnackbar((profileAction as UiState.Success<*>).data.toString())
                viewModel.resetProfileAction()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((profileAction as UiState.Error).message)
                viewModel.resetProfileAction()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val state = profileState) {
                is UiState.Loading -> LoadingView()
                is UiState.Error -> ErrorView(state.message) { viewModel.loadMyUserProfile() }
                is UiState.Success -> {
                    val profile = state.data
                    if (!formInitialized) {
                        name = profile.name
                        phone = profile.phone
                        location = profile.location ?: ""
                        homeAddress = profile.homeAddress ?: ""
                        formInitialized = true
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()) {
                                AvatarCircle(profile.name, size = 84, imagePath = profile.profilePhoto)
                                Spacer(Modifier.height(10.dp))
                                Text(profile.name, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                                Text(profile.email, color = Color.Gray, fontSize = 13.sp)
                            }
                        }
                        item {
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(value = name, onValueChange = { name = it },
                                        modifier = Modifier.fillMaxWidth(), label = { Text("Full Name") })
                                    OutlinedTextField(value = phone, onValueChange = { phone = it },
                                        modifier = Modifier.fillMaxWidth(), label = { Text("Phone Number") })
                                    OutlinedTextField(value = location, onValueChange = { location = it },
                                        modifier = Modifier.fillMaxWidth(), label = { Text("Area / Location") })
                                    OutlinedTextField(value = homeAddress, onValueChange = { homeAddress = it },
                                        modifier = Modifier.fillMaxWidth(), label = { Text("Home Address in Zaria") }, maxLines = 3)
                                    OutlinedButton(onClick = { profilePicker.launch(arrayOf("image/*")) },
                                        modifier = Modifier.fillMaxWidth()) {
                                        Text("Change Profile Photo")
                                    }
                                    Button(onClick = {
                                        viewModel.updateMyUserProfile(
                                            name.trim(),
                                            phone.trim(),
                                            location.trim(),
                                            homeAddress.trim(),
                                            profilePhotoUri
                                        )
                                    }, modifier = Modifier.fillMaxWidth()) {
                                        Text("Save Profile Changes")
                                    }
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        "Logout",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFFB71C1C)
                                    )
                                    Text(
                                        "Sign out here if you want to switch to another account.",
                                        color = Color(0xFF7F0000),
                                        fontSize = 13.sp
                                    )
                                    Button(
                                        onClick = onLogout,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
                                    ) {
                                        Icon(Icons.Default.Logout, null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Logout")
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

// ── Leave Review Screen ───────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveReviewScreen(
    bookingId    : Int,
    providerName : String,
    viewModel    : MainViewModel,
    onSuccess    : () -> Unit,
    onBack       : () -> Unit
) {
    var rating  by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    val reviewAction      by viewModel.reviewAction.collectAsState()
    val snackbarHostState  = remember { SnackbarHostState() }

    LaunchedEffect(reviewAction) {
        when (reviewAction) {
            is UiState.Success<*> -> { viewModel.resetReviewAction(); onSuccess() }
            is UiState.Error      -> {
                snackbarHostState.showSnackbar(
                    (reviewAction as UiState.Error).message)
                viewModel.resetReviewAction()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leave a Review") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
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
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Text("How was the service?", color = Color.Gray)
            Text(providerName, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(32.dp))
            Text("Tap to rate:", fontSize = 15.sp)
            Spacer(Modifier.height(10.dp))
            Row {
                repeat(5) { i ->
                    IconButton(onClick = { rating = i + 1 }) {
                        Icon(
                            if (i < rating) Icons.Default.Star
                            else Icons.Default.StarBorder,
                            null, tint = Color(0xFFFFC107),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
            Text("$rating out of 5 stars", color = Color.Gray)
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = comment, onValueChange = { comment = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                label = { Text("Write your review (optional)") },
                placeholder = { Text("Share your experience...") },
                maxLines = 5
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = {
                    viewModel.submitReview(bookingId, rating,
                        comment.trim().ifEmpty { null })
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled  = reviewAction !is UiState.Loading,
                colors   = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (reviewAction is UiState.Loading)
                    CircularProgressIndicator(color = Color.White,
                        modifier = Modifier.size(22.dp))
                else {
                    Icon(Icons.Default.Send, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Submit Review", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Submit Complaint Screen ───────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitComplaintScreen(
    bookingId    : Int,
    providerName : String,
    viewModel    : MainViewModel,
    onSuccess    : () -> Unit,
    onBack       : () -> Unit
) {
    var message by remember { mutableStateOf("") }
    val submitState       by viewModel.complaintSubmit.collectAsState()
    val snackbarHostState  = remember { SnackbarHostState() }

    LaunchedEffect(submitState) {
        when (submitState) {
            is UiState.Success<*> -> {
                snackbarHostState.showSnackbar(
                    (submitState as UiState.Success<*>).data.toString())
                viewModel.resetComplaintSubmit()
                onSuccess()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(
                    (submitState as UiState.Error).message)
                viewModel.resetComplaintSubmit()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report an Issue") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = Color(0xFFB71C1C),
                    titleContentColor          = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Info, null, tint = Color(0xFFB71C1C),
                        modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Reporting issue with: $providerName",
                            fontWeight = FontWeight.Bold, color = Color(0xFFB71C1C),
                            fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(4.dp))
                        Text("Our admin team will review your complaint.",
                            fontSize = 13.sp, color = Color(0xFF7F0000))
                    }
                }
            }
            Text("Describe the Problem", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            OutlinedTextField(
                value = message, onValueChange = { message = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp),
                placeholder = { Text("Describe what happened in detail...", color = Color.Gray) },
                maxLines = 10, shape = RoundedCornerShape(10.dp)
            )
            Text("${message.length} characters (minimum 10)", fontSize = 12.sp,
                color = if (message.length < 10) Color.Gray else Color(0xFF2E7D32))
            Button(
                onClick = {
                    if (message.trim().length >= 10) {
                        viewModel.submitComplaint(bookingId, message.trim())
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled  = message.trim().length >= 10 && submitState !is UiState.Loading,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB71C1C),
                    disabledContainerColor = Color.Gray),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (submitState is UiState.Loading)
                    CircularProgressIndicator(color = Color.White,
                        modifier = Modifier.size(22.dp))
                else {
                    Icon(Icons.Default.Send, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Submit Complaint", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", color = Color.Gray)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── My Complaints Screen ──────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyComplaintsScreen(
    viewModel : MainViewModel,
    onBack    : () -> Unit,
    viewerRole: String = "resident",
    onComplaintOpen: (ComplaintModel) -> Unit = {}
) {
    val complaintsState by viewModel.myComplaints.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadMyComplaints() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Complaints") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = Color(0xFFB71C1C),
                    titleContentColor          = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = complaintsState) {
                is UiState.Loading -> LoadingView("Loading complaints...")
                is UiState.Error   -> ErrorView(state.message) {
                    viewModel.loadMyComplaints()
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)) {
                                Icon(Icons.Default.CheckCircle, null,
                                    modifier = Modifier.size(72.dp), tint = Color(0xFF4CAF50))
                                Spacer(Modifier.height(16.dp))
                                Text("No complaints submitted",
                                    fontSize = 18.sp, fontWeight = FontWeight.Bold,
                                    color = Color.Gray)
                                Spacer(Modifier.height(8.dp))
                                Text("All your experiences have been good!",
                                    fontSize = 14.sp, color = Color.Gray)
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            item {
                                Text("${state.data.size} complaint(s)",
                                    color = Color.Gray, fontSize = 13.sp)
                            }
                            items(state.data) { complaint ->
                                ComplaintCard(
                                    complaint = complaint,
                                    viewerRole = viewerRole,
                                    onClick = { onComplaintOpen(complaint) }
                                )
                            }
                            item { Spacer(Modifier.height(16.dp)) }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

// ── Complaint Card ────────────────────────────────────────────────────────────
@Composable
fun ComplaintCard(
    complaint: ComplaintModel,
    viewerRole: String = "resident",
    onClick: () -> Unit = {}
) {
    val (statusColor, statusBg, statusLabel) = when (complaint.status) {
        "open"      -> Triple(Color(0xFFE65100), Color(0xFFFFF3E0), "OPEN")
        "in_review" -> Triple(Color(0xFF1565C0), Color(0xFFE3F2FD), "IN REVIEW")
        "resolved"  -> Triple(Color(0xFF2E7D32), Color(0xFFE8F5E9), "RESOLVED")
        else        -> Triple(Color.Gray, Color(0xFFF5F5F5), complaint.status.uppercase())
    }
    val counterpartLabel = if (viewerRole == "provider") {
        "Resident: ${complaint.user.name}"
    } else {
        "Against: ${complaint.provider.user.name}"
    }

    Card(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Report, null, tint = Color(0xFFB71C1C),
                    modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(counterpartLabel,
                    fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(20.dp), color = statusBg) {
                    Text(statusLabel, color = statusColor, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Tap to open admin chat",
                    fontSize = 12.sp,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Default.ChevronRight,
                    null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text("Your complaint:", fontSize = 12.sp, color = Color.Gray,
                fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(complaint.message, fontSize = 14.sp, color = Color(0xFF212121))
            if (!complaint.resolutionNote.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFF1565C0).copy(alpha = 0.2f))
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AdminPanelSettings, null,
                        tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Admin Response:", fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp, color = PrimaryBlue)
                }
                Spacer(Modifier.height(4.dp))
                Text(complaint.resolutionNote, fontSize = 13.sp, color = Color(0xFF1A237E))
            }
            Spacer(Modifier.height(10.dp))
            Text("Submitted: ${complaint.createdAt.take(10)}",
                fontSize = 11.sp, color = Color.Gray)
        }
    }
}
