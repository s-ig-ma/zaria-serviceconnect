package com.example.zariaserviceconnect.ui.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.zariaserviceconnect.viewmodel.MainViewModel
import com.example.zariaserviceconnect.viewmodel.UiState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(viewModel: MainViewModel) {
    val notificationsState by viewModel.notifications.collectAsState()
    val notificationAction by viewModel.notificationAction.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        while (true) {
            viewModel.loadNotifications()
            delay(15000)
        }
    }

    LaunchedEffect(notificationAction) {
        when (notificationAction) {
            is UiState.Success<*> -> {
                snackbarHostState.showSnackbar((notificationAction as UiState.Success<*>).data.toString())
                viewModel.resetNotificationAction()
                viewModel.loadNotifications()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((notificationAction as UiState.Error).message)
                viewModel.resetNotificationAction()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
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
                .padding(16.dp)
        ) {
            Button(
                onClick = { viewModel.markAllNotificationsRead() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mark All Read")
            }

            when (val state = notificationsState) {
                is UiState.Loading -> LoadingView("Loading notifications...")
                is UiState.Error -> ErrorView(state.message) { viewModel.loadNotifications() }
                is UiState.Success -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.data) { item ->
                            val cardColor = when (item.type) {
                                "booking" -> Color(0xFFE8FFF1)
                                "complaint", "complaint_action", "message" -> Color(0xFFEAF1FF)
                                else -> Color.White
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = cardColor)
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(item.title, fontWeight = FontWeight.Bold)
                                    Text(item.message)
                                    if (!item.isRead) {
                                        TextButton(onClick = { viewModel.markNotificationRead(item.id) }) {
                                            Text("Mark Read")
                                        }
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
