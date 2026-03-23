package com.example.zariaserviceconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.zariaserviceconnect.ui.auth.*
import com.example.zariaserviceconnect.ui.provider.*
import com.example.zariaserviceconnect.ui.resident.*
import com.example.zariaserviceconnect.ui.shared.*
import com.example.zariaserviceconnect.viewmodel.MainViewModel
import kotlinx.coroutines.delay

// ── All navigation route strings defined here ─────────────────────────────────
object Routes {
    const val SPLASH            = "splash"
    const val WELCOME           = "welcome"
    const val LOGIN             = "login"
    const val REGISTER          = "register"
    const val RESIDENT_HOME     = "resident_home"
    const val PROVIDER_HOME     = "provider_home"
    const val PROVIDERS_LIST    = "providers_list/{categoryId}/{categoryName}"
    const val RESIDENT_BOOKINGS = "resident_bookings"
    const val MY_COMPLAINTS     = "my_complaints"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ZariaApp()
                }
            }
        }
    }
}

@Composable
fun ZariaApp() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        // ── Splash ────────────────────────────────────────────────────────────
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigate = { role ->
                    val dest = when (role) {
                        "resident" -> Routes.RESIDENT_HOME
                        "provider" -> Routes.PROVIDER_HOME
                        else       -> Routes.WELCOME
                    }
                    navController.navigate(dest) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // ── Welcome ───────────────────────────────────────────────────────────
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onResidentLogin = { navController.navigate("${Routes.LOGIN}/resident") },
                onProviderLogin = { navController.navigate("${Routes.LOGIN}/provider") },
                onRegister      = { navController.navigate(Routes.REGISTER) }
            )
        }

        // ── Login ─────────────────────────────────────────────────────────────
        composable(
            "${Routes.LOGIN}/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { back ->
            val role = back.arguments?.getString("role") ?: "resident"
            LoginScreen(
                role           = role,
                viewModel      = viewModel,
                onLoginSuccess = { r ->
                    val dest = if (r == "provider") Routes.PROVIDER_HOME
                               else Routes.RESIDENT_HOME
                    navController.navigate(dest) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Register ──────────────────────────────────────────────────────────
        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel = viewModel,
                onSuccess = {
                    navController.navigate("${Routes.LOGIN}/resident") {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Resident Home (bottom nav) ─────────────────────────────────────────
        composable(Routes.RESIDENT_HOME) {
            ResidentHomeScreen(
                viewModel  = viewModel,
                onLogout   = {
                    viewModel.logout()
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        // ── Providers List ────────────────────────────────────────────────────
        composable(
            Routes.PROVIDERS_LIST,
            arguments = listOf(
                navArgument("categoryId")   { type = NavType.IntType    },
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) { back ->
            val catId   = back.arguments?.getInt("categoryId")
            val catName = back.arguments?.getString("categoryName") ?: "Providers"
            ProvidersListScreen(
                categoryId      = catId,
                categoryName    = catName,
                viewModel       = viewModel,
                onProviderClick = { id -> navController.navigate("provider_profile/$id") },
                onBack          = { navController.popBackStack() }
            )
        }

        // ── Provider Profile ──────────────────────────────────────────────────
        composable(
            "provider_profile/{providerId}",
            arguments = listOf(navArgument("providerId") { type = NavType.IntType })
        ) { back ->
            val pid = back.arguments?.getInt("providerId") ?: 0
            ProviderProfileScreen(
                providerId    = pid,
                viewModel     = viewModel,
                onBookService = { id -> navController.navigate("book_service/$id") },
                onBack        = { navController.popBackStack() }
            )
        }

        // ── Book Service ──────────────────────────────────────────────────────
        composable(
            "book_service/{providerId}",
            arguments = listOf(navArgument("providerId") { type = NavType.IntType })
        ) { back ->
            val pid = back.arguments?.getInt("providerId") ?: 0
            BookServiceScreen(
                providerId = pid,
                viewModel  = viewModel,
                onSuccess  = {
                    navController.navigate(Routes.RESIDENT_HOME) {
                        popUpTo(Routes.RESIDENT_HOME) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Resident Bookings ─────────────────────────────────────────────────
        composable(Routes.RESIDENT_BOOKINGS) {
            ResidentBookingsScreen(
                viewModel         = viewModel,
                onLeaveReview     = { bookingId, providerName ->
                    navController.navigate("leave_review/$bookingId/$providerName")
                },
                onSubmitComplaint = { bookingId, providerName ->
                    navController.navigate("submit_complaint/$bookingId/$providerName")
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Leave Review ──────────────────────────────────────────────────────
        composable(
            "leave_review/{bookingId}/{providerName}",
            arguments = listOf(
                navArgument("bookingId")    { type = NavType.IntType    },
                navArgument("providerName") { type = NavType.StringType }
            )
        ) { back ->
            val bookingId    = back.arguments?.getInt("bookingId")       ?: 0
            val providerName = back.arguments?.getString("providerName") ?: ""
            LeaveReviewScreen(
                bookingId    = bookingId,
                providerName = providerName,
                viewModel    = viewModel,
                onSuccess    = { navController.popBackStack() },
                onBack       = { navController.popBackStack() }
            )
        }

        // ── Submit Complaint ──────────────────────────────────────────────────
        composable(
            "submit_complaint/{bookingId}/{providerName}",
            arguments = listOf(
                navArgument("bookingId")    { type = NavType.IntType    },
                navArgument("providerName") { type = NavType.StringType }
            )
        ) { back ->
            val bookingId    = back.arguments?.getInt("bookingId")       ?: 0
            val providerName = back.arguments?.getString("providerName") ?: "Provider"
            SubmitComplaintScreen(
                bookingId    = bookingId,
                providerName = providerName,
                viewModel    = viewModel,
                onSuccess    = { navController.popBackStack() },
                onBack       = { navController.popBackStack() }
            )
        }

        // ── My Complaints ─────────────────────────────────────────────────────
        composable(Routes.MY_COMPLAINTS) {
            MyComplaintsScreen(
                viewModel = viewModel,
                onBack    = { navController.popBackStack() }
            )
        }

        // ── Provider Home (bottom nav) ────────────────────────────────────────
        composable(Routes.PROVIDER_HOME) {
            ProviderHomeScreen(
                viewModel = viewModel,
                onLogout  = {
                    viewModel.logout()
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

// ── Splash Screen ─────────────────────────────────────────────────────────────
@Composable
fun SplashScreen(onNavigate: (String?) -> Unit) {
    val viewModel: MainViewModel = viewModel()
    val role by viewModel.userRole.collectAsState()

    LaunchedEffect(Unit) {
        delay(1500)
        onNavigate(role)
    }

    WelcomeScreen(
        onResidentLogin = {},
        onProviderLogin = {},
        onRegister      = {}
    )
}

// ── Resident Home with Bottom Navigation ─────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentHomeScreen(
    viewModel  : MainViewModel,
    onLogout   : () -> Unit,
    onNavigate : (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick  = { selectedTab = 0 },
                    icon     = { Icon(Icons.Default.Home, null) },
                    label    = { Text("Services") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick  = { selectedTab = 1 },
                    icon     = { Icon(Icons.Default.CalendarToday, null) },
                    label    = { Text("My Bookings") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick  = { selectedTab = 2 },
                    icon     = { Icon(Icons.Default.Report, null) },
                    label    = { Text("Complaints") }
                )
            }
        }
    ) { padding ->
        // padding contains the bottom nav bar height — must be applied
        // so content is never hidden behind the navigation bar
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> CategoriesScreen(
                    viewModel          = viewModel,
                    onCategorySelected = { id, name ->
                        onNavigate("providers_list/$id/$name")
                    },
                    onAllProviders     = { onNavigate("providers_list/0/All Providers") },
                    onBookingsClick    = { selectedTab = 1 },
                    onLogout           = onLogout,
                    onProviderClick    = { id -> onNavigate("provider_profile/$id") }
                )
                1 -> ResidentBookingsScreen(
                    viewModel         = viewModel,
                    onLeaveReview     = { bookingId, providerName ->
                        onNavigate("leave_review/$bookingId/$providerName")
                    },
                    onSubmitComplaint = { bookingId, providerName ->
                        onNavigate("submit_complaint/$bookingId/$providerName")
                    },
                    onBack = { selectedTab = 0 }
                )
                2 -> MyComplaintsScreen(
                    viewModel = viewModel,
                    onBack    = { selectedTab = 0 }
                )
            }
        }
    }
}

// ── Provider Home with Bottom Navigation ─────────────────────────────────────
@Composable
fun ProviderHomeScreen(viewModel: MainViewModel, onLogout: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick  = { selectedTab = 0 },
                    icon     = { Icon(Icons.Default.Work, null) },
                    label    = { Text("Job Requests") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick  = { selectedTab = 1 },
                    icon     = { Icon(Icons.Default.Person, null) },
                    label    = { Text("My Profile") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> ProviderJobsScreen(viewModel = viewModel, onLogout = onLogout)
                1 -> ProviderProfileScreen(viewModel = viewModel)
            }
        }
    }
}
