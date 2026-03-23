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
    // verticalScroll prevents overflow on very small screens
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(32.dp))

        // Logo / Icon
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

        // Resident login button
        Button(
            onClick  = onResidentLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape  = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Person, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text("I am a Resident", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(14.dp))

        // Provider login button
        OutlinedButton(
            onClick  = onProviderLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
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
    role          : String,
    viewModel     : MainViewModel,
    onLoginSuccess : (String) -> Unit,
    onBack        : () -> Unit
) {
    var email          by remember { mutableStateOf("") }
    var password       by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val loginState     by viewModel.loginState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val title = if (role == "provider") "Provider Login" else "Resident Login"

    LaunchedEffect(loginState) {
        when (loginState) {
            is UiState.Success -> {
                val r = (loginState as UiState.Success).data.role
                onLoginSuccess(r)
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(
                    (loginState as UiState.Error).message)
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
        // verticalScroll = SingleChildScrollView
        // SafeArea = padding(padding) from Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)               // ← SafeArea
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
            Text(title,
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value         = email,
                onValueChange = { email = it },
                label         = { Text("Email Address") },
                leadingIcon   = { Icon(Icons.Default.Email, null) },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email)
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value         = password,
                onValueChange = { password = it },
                label         = { Text("Password") },
                leadingIcon   = { Icon(Icons.Default.Lock, null) },
                trailingIcon  = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            null
                        )
                    }
                },
                modifier             = Modifier.fillMaxWidth(),
                singleLine           = true,
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password)
            )
            Spacer(Modifier.height(32.dp))

            Button(
                onClick  = { viewModel.login(email.trim(), password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled  = loginState !is UiState.Loading
                        && email.isNotBlank()
                        && password.isNotBlank(),
                colors   = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (loginState is UiState.Loading) {
                    CircularProgressIndicator(color = Color.White,
                        modifier = Modifier.size(22.dp))
                } else {
                    Text("Login",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold)
                }
            }

            if (loginState is UiState.Error) {
                Spacer(Modifier.height(12.dp))
                Text(
                    (loginState as UiState.Error).message,
                    color    = Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Register Screen ───────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel : MainViewModel,
    onSuccess : () -> Unit,
    onBack    : () -> Unit
) {
    var name           by remember { mutableStateOf("") }
    var email          by remember { mutableStateOf("") }
    var phone          by remember { mutableStateOf("") }
    var location       by remember { mutableStateOf("") }
    var password       by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val registerState  by viewModel.registerState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(registerState) {
        when (registerState) {
            is UiState.Success<*> -> {
                snackbarHostState.showSnackbar("Account created! Please login.")
                viewModel.resetRegisterState()
                onSuccess()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(
                    (registerState as UiState.Error).message)
                viewModel.resetRegisterState()
            }
            else -> {}
        }
    }

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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        // verticalScroll handles overflow on any screen size
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)               // ← SafeArea
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text("Resident Registration",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold)
            Text("Fill in your details to create an account.",
                color    = Color.Gray,
                fontSize = 14.sp)

            OutlinedTextField(
                value         = name,
                onValueChange = { name = it },
                label         = { Text("Full Name") },
                leadingIcon   = { Icon(Icons.Default.Person, null) },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true
            )

            OutlinedTextField(
                value         = email,
                onValueChange = { email = it },
                label         = { Text("Email Address") },
                leadingIcon   = { Icon(Icons.Default.Email, null) },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email)
            )

            OutlinedTextField(
                value         = phone,
                onValueChange = { phone = it },
                label         = { Text("Phone Number") },
                leadingIcon   = { Icon(Icons.Default.Phone, null) },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone)
            )

            OutlinedTextField(
                value         = location,
                onValueChange = { location = it },
                label         = { Text("Your Area / Location") },
                leadingIcon   = { Icon(Icons.Default.LocationOn, null) },
                placeholder   = { Text("e.g. Sabon Gari, Zaria") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true
            )

            OutlinedTextField(
                value         = password,
                onValueChange = { password = it },
                label         = { Text("Password (min 6 characters)") },
                leadingIcon   = { Icon(Icons.Default.Lock, null) },
                trailingIcon  = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            null
                        )
                    }
                },
                modifier             = Modifier.fillMaxWidth(),
                singleLine           = true,
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password)
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick  = {
                    viewModel.registerResident(
                        name.trim(),
                        email.trim(),
                        phone.trim(),
                        password,
                        location.trim()
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled  = registerState !is UiState.Loading
                        && name.isNotBlank()
                        && email.isNotBlank()
                        && phone.isNotBlank()
                        && password.length >= 6,
                colors   = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (registerState is UiState.Loading) {
                    CircularProgressIndicator(color = Color.White,
                        modifier = Modifier.size(22.dp))
                } else {
                    Text("Create Account",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold)
                }
            }

            if (registerState is UiState.Error) {
                Text(
                    (registerState as UiState.Error).message,
                    color    = Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            TextButton(
                onClick  = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Already have an account? Login", color = PrimaryBlue)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
