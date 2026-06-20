package com.example.yourpay.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMoneyScreen(
    onBack: () -> Unit,
    onAddSuccess: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Money to Wallet") },
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
            Spacer(modifier = Modifier.height(24.dp))
            Text("Enter Amount via UPI", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = "₹ $amount",
                onValueChange = { 
                    val cleanString = it.replace("₹ ", "").replace(",", "")
                    if (cleanString.all { char -> char.isDigit() }) {
                        amount = cleanString
                    }
                },
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 32.sp, 
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quick amounts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickAmountButton("₹ 500") { amount = "500" }
                QuickAmountButton("₹ 1000") { amount = "1000" }
                QuickAmountButton("₹ 500000") { amount = "500000" }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isProcessing) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Connecting to UPI Gateway...")
            } else {
                Button(
                    onClick = {
                        val parsedAmount = amount.toDoubleOrNull()
                        if (parsedAmount != null && parsedAmount > 0) {
                            coroutineScope.launch {
                                isProcessing = true
                                // Simulate UPI Delay
                                delay(2000)
                                isProcessing = false
                                Toast.makeText(context, "Added ₹$parsedAmount via UPI", Toast.LENGTH_SHORT).show()
                                onAddSuccess(parsedAmount)
                            }
                        } else {
                            Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Add to Wallet via UPI", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun QuickAmountButton(label: String, onClick: () -> Unit) {
    ElevatedButton(onClick = onClick) {
        Text(label)
    }
}
