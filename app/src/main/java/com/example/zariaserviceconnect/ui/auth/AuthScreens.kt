package com.example.zariaserviceconnect.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zariaserviceconnect.models.CategoryModel
import com.example.zariaserviceconnect.ui.shared.PrimaryBlue
import com.example.zariaserviceconnect.viewmodel.MainViewModel
import com.example.zariaserviceconnect.viewmodel.UiState

// ── Welcome Screen ────────────────────────────────────────────────────────────
@Composable
fun WelcomeScreen(
    onResidentLogin : () -> Unit,
    onProviderLogin : () -> Unit,
    onRegister      : () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(32.dp))
        Icon(
            Icons.Default.HomeRepairService,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint     = PrimaryBlue
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Zaria ServiceConnect",
            fontSize   = 26.sp,
            fontWeight = FontWeight.Bold,
            color      = PrimaryBlue,
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Find trusted home service providers in Zaria",
            fontSize  = 15.sp,
            color     = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(48.dp))

        Button(
            onClick  = onResidentLogin,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape    = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Person, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text("I am a Resident", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(14.dp))

        OutlinedButton(
            onClick  = onProviderLogin,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Engineering, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text("I am a Provider", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(24.dp))

        TextButton(onClick = onRegister) {
            Text("New here? Create an account", color = PrimaryBlue)
        }
        Spacer(Modifier.height(32.dp))
    }
}

// ── Login Screen ──────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    role           : String,
    viewModel      : MainViewModel,
    onLoginSuccess : (String) -> Unit,
    onBack         : () -> Unit
) {
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val loginState      by viewModel.loginState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val title = if (role == "provider") "Provider Login" else "Resident Login"

    LaunchedEffect(loginState) {
        when (loginState) {
            is UiState.Success<*> -> {
                val r = (loginState as UiState.Success<*>).data
                if (r is com.example.zariaserviceconnect.models.LoginResponse) {
                    onLoginSuccess(r.role)
                }
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((loginState as UiState.Error).message)
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Icon(
                if (role == "provider") Icons.Default.Engineering
                else Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint     = PrimaryBlue
            )
            Spacer(Modifier.height(16.dp))
            Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value           = email,
                onValueChange   = { email = it },
                label           = { Text("Email Address") },
                leadingIcon     = { Icon(Icons.Default.Email, null) },
                modifier        = Modifier.fillMaxWidth(),
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value                = password,
                onValueChange        = { password = it },
                label                = { Text("Password") },
                leadingIcon          = { Icon(Icons.Default.Lock, null) },
                trailingIcon         = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility, null
                        )
                    }
                },
                modifier             = Modifier.fillMaxWidth(),
                singleLine           = true,
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(Modifier.height(32.dp))

            Button(
                onClick  = { viewModel.login(email.trim(), password) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled  = loginState !is UiState.Loading
                        && email.isNotBlank() && password.isNotBlank(),
                colors   = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (loginState is UiState.Loading)
                    CircularProgressIndicator(color = Color.White,
                        modifier = Modifier.size(22.dp))
                else
                    Text("Login", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            if (loginState is UiState.Error) {
                Spacer(Modifier.height(12.dp))
                Text(
                    (loginState as UiState.Error).message,
                    color     = Color.Red,
                    fontSize  = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Register Screen — Choose Registration Type ────────────────────────────────
// Shows two options: Register as Resident OR Register as Provider
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel : MainViewModel,
    onSuccess : () -> Unit,
    onBack    : () -> Unit
) {
    // Track which form to show: null = choose, "resident" or "provider"
    var registrationType by remember { mutableStateOf<String?>(null) }

    when (registrationType) {
        "resident" -> ResidentRegisterForm(
            viewModel = viewModel,
            onSuccess = onSuccess,
            onBack    = { registrationType = null }
        )
        "provider" -> ProviderRegisterForm(
            viewModel = viewModel,
            onSuccess = onSuccess,
            onBack    = { registrationType = null }
        )
        else -> {
            // Show the choice screen
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Create Account") },
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.AppRegistration, null,
                        modifier = Modifier.size(64.dp), tint = PrimaryBlue)
                    Spacer(Modifier.height(16.dp))
                    Text("Who are you registering as?",
                        fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Text("Choose your account type below.",
                        color = Color.Gray, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(40.dp))

                    // Resident option
                    Card(
                        onClick  = { registrationType = "resident" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, null,
                                tint = PrimaryBlue, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Register as Resident",
                                    fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("I want to find and hire service providers",
                                    color = Color.Gray, fontSize = 13.sp)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    // Provider option
                    Card(
                        onClick  = { registrationType = "provider" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Engineering, null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(40.dp))
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Register as Provider",
                                    fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("I want to offer my services to residents",
                                    color = Color.Gray, fontSize = 13.sp)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// ── Resident Registration Form ────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResidentRegisterForm(
    viewModel : MainViewModel,
    onSuccess : () -> Unit,
    onBack    : () -> Unit
) {
    var name            by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var phone           by remember { mutableStateOf("") }
    var location        by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val registerState   by viewModel.registerState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(registerState) {
        when (registerState) {
            is UiState.Success<*> -> {
                snackbarHostState.showSnackbar("Account created! Please login.")
                viewModel.resetRegisterState()
                onSuccess()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((registerState as UiState.Error).message)
                viewModel.resetRegisterState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resident Registration") },
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text("Create Resident Account",
                fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Fill in your details below.",
                color = Color.Gray, fontSize = 14.sp)

            OutlinedTextField(value = name, onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true)

            OutlinedTextField(value = email, onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))

            OutlinedTextField(value = phone, onValueChange = { phone = it },
                label = { Text("Phone Number") },
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))

            OutlinedTextField(value = location, onValueChange = { location = it },
                label = { Text("Your Area / Location") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                placeholder = { Text("e.g. Sabon Gari, Zaria") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)

            OutlinedTextField(
                value                = password,
                onValueChange        = { password = it },
                label                = { Text("Password (min 6 characters)") },
                leadingIcon          = { Icon(Icons.Default.Lock, null) },
                trailingIcon         = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.VisibilityOff
                             else Icons.Default.Visibility, null)
                    }
                },
                modifier             = Modifier.fillMaxWidth(),
                singleLine           = true,
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(Modifier.height(8.dp))
            Button(
                onClick  = {
                    viewModel.registerResident(
                        name.trim(), email.trim(), phone.trim(),
                        password, location.trim())
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled  = registerState !is UiState.Loading
                        && name.isNotBlank() && email.isNotBlank()
                        && phone.isNotBlank() && password.length >= 6,
                colors   = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (registerState is UiState.Loading)
                    CircularProgressIndicator(color = Color.White,
                        modifier = Modifier.size(22.dp))
                else
                    Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            if (registerState is UiState.Error) {
                Text((registerState as UiState.Error).message,
                    color = Color.Red, fontSize = 14.sp,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }

            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Already have an account? Login", color = PrimaryBlue)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Provider Registration Form ────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderRegisterForm(
    viewModel : MainViewModel,
    onSuccess : () -> Unit,
    onBack    : () -> Unit
) {
    var name                by remember { mutableStateOf("") }
    var email               by remember { mutableStateOf("") }
    var phone               by remember { mutableStateOf("") }
    var location            by remember { mutableStateOf("") }
    var password            by remember { mutableStateOf("") }
    var passwordVisible     by remember { mutableStateOf(false) }
    var description         by remember { mutableStateOf("") }
    var yearsOfExperience   by remember { mutableStateOf("0") }
    var selectedCategoryId  by remember { mutableStateOf<Int?>(null) }
    var categoryExpanded    by remember { mutableStateOf(false) }
    val categoriesState     by viewModel.categories.collectAsState()
    val registerState       by viewModel.registerState.collectAsState()
    val snackbarHostState    = remember { SnackbarHostState() }

    // Load categories when screen opens
    LaunchedEffect(Unit) { viewModel.loadCategories() }

    LaunchedEffect(registerState) {
        when (registerState) {
            is UiState.Success<*> -> {
                snackbarHostState.showSnackbar(
                    "Provider account created! Please wait for admin approval.")
                viewModel.resetRegisterState()
                onSuccess()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((registerState as UiState.Error).message)
                viewModel.resetRegisterState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Provider Registration") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = Color(0xFF2E7D32),
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Info card
            Card(colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E9))) {
                Row(Modifier.padding(12.dp)) {
                    Icon(Icons.Default.Info, null,
                        tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "After registering, your account will be reviewed by admin " +
                        "before you can start receiving bookings.",
                        fontSize = 13.sp, color = Color(0xFF1B5E20)
                    )
                }
            }

            Text("Create Provider Account",
                fontSize = 20.sp, fontWeight = FontWeight.Bold)

            OutlinedTextField(value = name, onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true)

            OutlinedTextField(value = email, onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))

            OutlinedTextField(value = phone, onValueChange = { phone = it },
                label = { Text("Phone Number") },
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))

            OutlinedTextField(value = location, onValueChange = { location = it },
                label = { Text("Your Area / Location") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                placeholder = { Text("e.g. Sabon Gari, Zaria") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)

            // Service Category dropdown
            val categories = when (val s = categoriesState) {
                is UiState.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    s.data as? List<CategoryModel> ?: emptyList()
                }
                else -> emptyList()
            }
            val selectedCategoryName = categories.find {
                it.id == selectedCategoryId
            }?.name ?: "Select Service Category"

            ExposedDropdownMenuBox(
                expanded        = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value         = selectedCategoryName,
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Service Category") },
                    leadingIcon   = { Icon(Icons.Default.Category, null) },
                    trailingIcon  = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                    },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded        = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text    = { Text(category.name) },
                            onClick = {
                                selectedCategoryId = category.id
                                categoryExpanded   = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value         = yearsOfExperience,
                onValueChange = { yearsOfExperience = it.filter { c -> c.isDigit() } },
                label         = { Text("Years of Experience") },
                leadingIcon   = { Icon(Icons.Default.Work, null) },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value         = description,
                onValueChange = { description = it },
                label         = { Text("About Your Service") },
                placeholder   = { Text("Describe your skills and experience...") },
                modifier      = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                maxLines      = 4
            )

            OutlinedTextField(
                value                = password,
                onValueChange        = { password = it },
                label                = { Text("Password (min 6 characters)") },
                leadingIcon          = { Icon(Icons.Default.Lock, null) },
                trailingIcon         = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.VisibilityOff
                             else Icons.Default.Visibility, null)
                    }
                },
                modifier             = Modifier.fillMaxWidth(),
                singleLine           = true,
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick  = {
                    val catId = selectedCategoryId
                    if (catId != null) {
                        viewModel.registerProvider(
                            name                = name.trim(),
                            email               = email.trim(),
                            phone               = phone.trim(),
                            password            = password,
                            location            = location.trim(),
                            categoryId          = catId,
                            yearsOfExperience   = yearsOfExperience.toIntOrNull() ?: 0,
                            description         = description.trim()
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled  = registerState !is UiState.Loading
                        && name.isNotBlank() && email.isNotBlank()
                        && phone.isNotBlank() && password.length >= 6
                        && selectedCategoryId != null,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32))
            ) {
                if (registerState is UiState.Loading)
                    CircularProgressIndicator(color = Color.White,
                        modifier = Modifier.size(22.dp))
                else
                    Text("Submit Registration",
                        fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            if (registerState is UiState.Error) {
                Text((registerState as UiState.Error).message,
                    color = Color.Red, fontSize = 14.sp,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }

            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Already have an account? Login", color = PrimaryBlue)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
