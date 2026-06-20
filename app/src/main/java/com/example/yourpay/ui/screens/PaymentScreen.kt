package com.example.yourpay.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

import com.example.yourpay.data.entity.TransactionInfo
import com.example.yourpay.utils.AIScamEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    initialUpi: String = "",
    transactions: List<TransactionInfo>,
    onBack: () -> Unit,
    onPay: (String, String) -> Unit
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var upiId by rememberSaveable { mutableStateOf(initialUpi) }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var showScamWarning by remember { mutableStateOf(false) }
    var scamReason by remember { mutableStateOf("") }
    
    val isFormValid = if (selectedTabIndex == 0) {
        upiId.isNotBlank() && amount.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0
    } else {
        phoneNumber.isNotBlank() && phoneNumber.length >= 10 && amount.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfer Money") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TabRow(selectedTabIndex = selectedTabIndex, modifier = Modifier.fillMaxWidth()) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("UPI ID") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Phone Number") }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            if (selectedTabIndex == 0) {
                OutlinedTextField(
                    value = upiId,
                    onValueChange = { upiId = it },
                    label = { Text("Enter Receiver UPI ID") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { if (it.length <= 15) phoneNumber = it },
                    label = { Text("Enter Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Amount", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                textStyle = LocalTextStyle.current.copy(fontSize = 32.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("₹ 0", fontSize = 32.sp, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
            )

            Spacer(modifier = Modifier.weight(1f))

            val targetId = if (selectedTabIndex == 0) upiId else "$phoneNumber@ybl" // Mocked Virtual Payment Address for Phone Numbers

            Button(
                onClick = {
                    if (isFormValid) {
                        val targetId = if (selectedTabIndex == 0) upiId else "$phoneNumber@ybl"
                        val scamResult = AIScamEngine.evaluateTransaction(targetId, amount, transactions)
                        
                        if (scamResult.isSuspicious) {
                            scamReason = scamResult.reason
                            showScamWarning = true
                        } else {
                            onPay(targetId, amount)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 16.dp),
                enabled = isFormValid,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
            ) {
                Text("Proceed to Pay", fontSize = 18.sp)
            }
        }

        if (showScamWarning) {
            val targetId = if (selectedTabIndex == 0) upiId else "$phoneNumber@ybl"
            AlertDialog(
                onDismissRequest = { showScamWarning = false },
                title = { Text("⚠️ Security Warning") },
                text = { Text("This payment has been flagged as RISKY by our AI detection engine.\n\nReason: $scamReason\n\nAre you sure you want to proceed?") },
                confirmButton = {
                    TextButton(onClick = {
                        showScamWarning = false
                        onPay(targetId, amount)
                    }) {
                        Text("Proceed Anyway", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showScamWarning = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
