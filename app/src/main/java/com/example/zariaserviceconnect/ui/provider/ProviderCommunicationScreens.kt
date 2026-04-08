package com.example.zariaserviceconnect.ui.provider

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zariaserviceconnect.models.ComplaintModel
import com.example.zariaserviceconnect.ui.shared.ErrorView
import com.example.zariaserviceconnect.ui.shared.LoadingView
import com.example.zariaserviceconnect.ui.shared.PrimaryBlue
import com.example.zariaserviceconnect.viewmodel.MainViewModel
import com.example.zariaserviceconnect.viewmodel.UiState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderMessagesScreen(
    viewModel: MainViewModel,
    complaint: ComplaintModel,
    onBack: () -> Unit
) {
    val messagesState by viewModel.complaintMessages.collectAsState()
    val messageAction by viewModel.messageAction.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var replyText by remember { mutableStateOf("") }

    LaunchedEffect(complaint.id) {
        while (true) {
            viewModel.loadComplaintMessages(complaint.id)
            delay(15000)
        }
    }

    LaunchedEffect(messageAction) {
        when (messageAction) {
            is UiState.Success<*> -> {
                snackbarHostState.showSnackbar((messageAction as UiState.Success<*>).data.toString())
                viewModel.resetMessageAction()
                viewModel.loadComplaintMessages(complaint.id)
                replyText = ""
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((messageAction as UiState.Error).message)
                viewModel.resetMessageAction()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complaint Chat") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8FFF1))
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("Complaint #${complaint.id}", fontWeight = FontWeight.Bold)
                    Text("Status: ${complaint.status}", color = Color.Gray, fontSize = 13.sp)
                    Text(
                        "Admin chat about ${complaint.user.name}",
                        color = Color(0xFF1A7F4B),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Admin Support Chat", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(
                "This complaint thread is only between you and admin.",
                color = Color.Gray,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(8.dp))

            when (val messageState = messagesState) {
                is UiState.Loading -> LoadingView("Loading messages...")
                is UiState.Error -> ErrorView(messageState.message) {
                    viewModel.loadComplaintMessages(complaint.id)
                }
                is UiState.Success -> {
                    val lastAdminMessage = messageState.data.lastOrNull { it.sender.role == "admin" }
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messageState.data) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = when (item.sender.role) {
                                        "admin" -> Color(0xFFE6F0FF)
                                        "provider" -> Color(0xFFE8FFF1)
                                        else -> Color(0xFFFFF4DE)
                                    }
                                )
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(
                                        if (item.sender.role == "admin") item.sender.name else "You",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(item.content)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Reply to Admin") },
                        placeholder = {
                            Text(
                                if (lastAdminMessage != null) {
                                    "Write your reply here..."
                                } else {
                                    "Write your message to admin here..."
                                }
                            )
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.sendComplaintMessage(
                                lastAdminMessage?.sender?.id,
                                complaint.id,
                                replyText.trim()
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = replyText.isNotBlank()
                    ) {
                        Text("Send to Admin")
                    }
                }
                else -> Text("Loading conversation...", color = Color.Gray)
            }
        }
    }
}
