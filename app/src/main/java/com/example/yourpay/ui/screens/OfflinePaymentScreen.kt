package com.example.yourpay.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yourpay.data.entity.TransactionInfo
import com.example.yourpay.utils.AIScamEngine
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflinePaymentScreen(
    currentBalance: Double,
    transactions: List<TransactionInfo>,
    onBack: () -> Unit,
    onProceedToTokenGen: (String, Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var receiverId by remember { mutableStateOf("") }
    var showWarningDialog by remember { mutableStateOf(false) }
    var warningMessage by remember { mutableStateOf("") }
    
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offline Payment") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Available Offline Balance: ₹${"%.2f".format(currentBalance)}",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = receiverId,
                onValueChange = { receiverId = it },
                label = { Text("Receiver Device ID / UPI") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull()
                    if (receiverId.isBlank()) {
                        Toast.makeText(context, "Enter a valid receiver ID", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (parsedAmount == null || parsedAmount <= 0) {
                        Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (parsedAmount > currentBalance) {
                        Toast.makeText(context, "Insufficient Offline Balance", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    // AI Scam Detection Flow
                    val scamResult = AIScamEngine.evaluateTransaction(receiverId, amount, transactions)
                    if (scamResult.isSuspicious) {
                        warningMessage = scamResult.reason
                        showWarningDialog = true
                    } else {
                        onProceedToTokenGen(receiverId, parsedAmount)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Generate Offline Token", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showWarningDialog) {
            AlertDialog(
                onDismissRequest = { showWarningDialog = false },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red) },
                title = { Text("Suspicious Transaction Detected") },
                text = { Text(warningMessage) },
                confirmButton = {
                    TextButton(onClick = { 
                        showWarningDialog = false 
                        onProceedToTokenGen(receiverId, amount.toDoubleOrNull() ?: 0.0)
                    }) {
                        Text("Proceed Anyway", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWarningDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
