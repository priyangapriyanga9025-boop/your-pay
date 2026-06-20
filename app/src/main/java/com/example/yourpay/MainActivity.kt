package com.example.yourpay

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.yourpay.data.AppDatabase
import com.example.yourpay.data.entity.TransactionInfo
import com.example.yourpay.data.entity.WalletInfo
import com.example.yourpay.ui.screens.*
import com.example.yourpay.ui.theme.YourPayTheme
import com.example.yourpay.utils.BiometricHelper
import com.example.yourpay.utils.OfflineTransactionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(this)
        val transactionDao = database.transactionDao()
        val walletDao = database.walletDao()
        val offlineTxnManager = OfflineTransactionManager(walletDao)
        
        // Initialize Wallet if it doesn't exist
        lifecycleScope.launch(Dispatchers.IO) {
            val wallet = walletDao.getWalletNow()
            if (wallet == null) {
                walletDao.setWallet(WalletInfo(id = 1, balance = 0.0))
            }
        }
        
        setContent {
            YourPayTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val transactions by transactionDao.getAllTransactions().collectAsState(initial = emptyList())
                    val walletInfo by walletDao.getWalletFlow().collectAsState(initial = null)
                    
                    var lastAmount by remember { mutableStateOf("") }
                    var lastUpiId by remember { mutableStateOf("") }
                    var offlineTokenToDisplay by remember { mutableStateOf("") }
                    
                    NavHost(navController = navController, startDestination = "auth") {
                        composable("auth") {
                            AuthScreen(
                                onAuthSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                },
                                triggerAuth = {
                                    BiometricHelper.authenticate(
                                        activity = this@MainActivity,
                                        onSuccess = {
                                            navController.navigate("home") {
                                                popUpTo("auth") { inclusive = true }
                                            }
                                        },
                                        onError = { errorMsg ->
                                            Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            )
                        }
                        
                        composable("home") {
                            HomeScreen(
                                walletBalance = walletInfo?.balance ?: 0.0,
                                onScanClick = { navController.navigate("scan") },
                                onPayUpiClick = { navController.navigate("payment/") },
                                onHistoryClick = { navController.navigate("history") },
                                onAddMoneyClick = { navController.navigate("add_money") },
                                onOfflineTransferClick = { navController.navigate("offline_payment") },
                                onServiceClick = { service -> 
                                    val formattedUpi = service.replace(" ", "").lowercase() + "@yourpay"
                                    navController.navigate("payment/${Uri.encode(formattedUpi)}")
                                }
                            )
                        }
                        
                        composable("add_money") {
                            AddMoneyScreen(
                                onBack = { navController.popBackStack() },
                                onAddSuccess = { addedAmount ->
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        walletDao.addBalance(addedAmount)
                                        withContext(Dispatchers.Main) {
                                            navController.popBackStack()
                                        }
                                    }
                                }
                            )
                        }
                        
                        composable("offline_payment") {
                            OfflinePaymentScreen(
                                currentBalance = walletInfo?.balance ?: 0.0,
                                transactions = transactions,
                                onBack = { navController.popBackStack() },
                                onProceedToTokenGen = { receiverId, amount ->
                                    BiometricHelper.authenticate(
                                        activity = this@MainActivity,
                                        onSuccess = {
                                            lifecycleScope.launch(Dispatchers.IO) {
                                                try {
                                                    val token = offlineTxnManager.processOfflinePayment("self_uuid", receiverId, amount)
                                                    
                                                    // Log it
                                                    val txn = TransactionInfo(
                                                        upiId = receiverId,
                                                        amount = amount.toString(),
                                                        timestamp = System.currentTimeMillis(),
                                                        status = "OFFLINE"
                                                    )
                                                    transactionDao.insertTransaction(txn)
                                                    
                                                    withContext(Dispatchers.Main) {
                                                        offlineTokenToDisplay = token
                                                        navController.navigate("token_screen") {
                                                            popUpTo("home")
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(this@MainActivity, e.message ?: "Failed", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            }
                                        },
                                        onError = { errorMsg ->
                                            Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            )
                        }
                        
                        composable("token_screen") {
                            TokenScreen(
                                token = offlineTokenToDisplay,
                                onDone = {
                                    Toast.makeText(this@MainActivity, "Payment Successful (Offline Mode)", Toast.LENGTH_LONG).show()
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }
                        
                        composable("scan") {
                            ScanScreen(
                                onBack = { navController.popBackStack() },
                                onQrScanned = { result ->
                                    if (result.isEmpty()) {
                                        navController.navigate("payment/") {
                                            popUpTo("home")
                                        }
                                    } else {
                                        val upiId = extractUpiId(result)
                                        val safeUpiId = if (upiId.isNotBlank()) Uri.encode(upiId) else "unknown"
                                        navController.navigate("payment/$safeUpiId") {
                                            popUpTo("home")
                                        }
                                    }
                                }
                            )
                        }
                        
                        composable("payment/") {
                            PaymentScreen(
                                initialUpi = "",
                                transactions = transactions,
                                onBack = { navController.popBackStack() },
                                onPay = { upi, amount ->
                                    BiometricHelper.authenticate(
                                        activity = this@MainActivity,
                                        onSuccess = {
                                            navController.navigate("processing/${Uri.encode(upi)}/${Uri.encode(amount)}")
                                        },
                                        onError = { errorMsg ->
                                            Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            )
                        }
                        
                        composable("payment/{upiId}") { backStackEntry ->
                            val upiId = backStackEntry.arguments?.getString("upiId") ?: ""
                            PaymentScreen(
                                initialUpi = upiId,
                                transactions = transactions,
                                onBack = { navController.popBackStack() },
                                onPay = { upi, amount ->
                                    BiometricHelper.authenticate(
                                        activity = this@MainActivity,
                                        onSuccess = {
                                            navController.navigate("processing/${Uri.encode(upi)}/${Uri.encode(amount)}")
                                        },
                                        onError = { errorMsg ->
                                            Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            )
                        }
                        
                        composable("processing/{upiId}/{amount}") { backStackEntry ->
                            val upiId = backStackEntry.arguments?.getString("upiId") ?: ""
                            val amount = backStackEntry.arguments?.getString("amount") ?: ""
                            
                            ProcessingScreen(
                                upiId = Uri.decode(upiId),
                                amount = Uri.decode(amount),
                                onCancel = {
                                    Toast.makeText(this@MainActivity, "Payment Cancelled", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack(route = "home", inclusive = false)
                                },
                                onPaymentComplete = {
                                    val decodedUpi = Uri.decode(upiId)
                                    val decodedAmount = Uri.decode(amount)
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        // Attempt deduct from wallet before confirming standard payments too
                                        val amt = decodedAmount.toDoubleOrNull() ?: 0.0
                                        val isDeducted = walletDao.deductBalance(amt)
                                        if (isDeducted > 0) {
                                            val txn = TransactionInfo(
                                                upiId = decodedUpi,
                                                amount = decodedAmount,
                                                timestamp = System.currentTimeMillis(),
                                                status = "SUCCESS"
                                            )
                                            transactionDao.insertTransaction(txn)
                                            withContext(Dispatchers.Main) {
                                                lastAmount = decodedAmount
                                                lastUpiId = decodedUpi
                                                navController.navigate("success") {
                                                    popUpTo("home")
                                                }
                                            }
                                        } else {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(this@MainActivity, "Insufficient Wallet Balance!", Toast.LENGTH_LONG).show()
                                                navController.popBackStack(route = "home", inclusive = false)
                                            }
                                        }
                                    }
                                }
                            )
                        }
                        
                        composable("success") {
                            SuccessScreen(
                                amount = lastAmount,
                                upiId = lastUpiId,
                                onDone = {
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                },
                                onRefund = {
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        // Find transaction
                                        val txn = transactions.firstOrNull { it.status == "SUCCESS" && it.amount == lastAmount && it.upiId == lastUpiId }
                                        if (txn != null) {
                                            val timeDiff = System.currentTimeMillis() - txn.timestamp
                                            if (timeDiff < 30 * 60 * 1000) { // < 30 mins
                                                transactionDao.updateTransactionStatus(txn.id, "REFUNDED")
                                                walletDao.addBalance(txn.amount.toDoubleOrNull() ?: 0.0) // Return the money back
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(this@MainActivity, "Refunded successfully! Money added back to Wallet.", Toast.LENGTH_SHORT).show()
                                                    navController.navigate("home") {
                                                        popUpTo("home") { inclusive = true }
                                                    }
                                                }
                                            } else {
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(this@MainActivity, "Refund period (30 mins) has expired.", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        } else {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(this@MainActivity, "Could not find transaction to refund", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            )
                        }
                        
                        composable("history") {
                            HistoryScreen(
                                transactions = transactions,
                                onRefundClick = { txnId ->
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        val txn = transactions.find { it.id == txnId }
                                        if (txn != null && (txn.status == "SUCCESS" || txn.status == "OFFLINE")) {
                                            val timeDiff = System.currentTimeMillis() - txn.timestamp
                                            if (timeDiff < 30 * 60 * 1000) {
                                                transactionDao.updateTransactionStatus(txnId, "REFUNDED")
                                                walletDao.addBalance(txn.amount.toDoubleOrNull() ?: 0.0)
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(this@MainActivity, "Refund successful.", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                 withContext(Dispatchers.Main) {
                                                    Toast.makeText(this@MainActivity, "Refund period (30 mins) expired.", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
    
    private fun extractUpiId(data: String): String {
        return if (data.startsWith("upi://pay")) {
            try {
                val uri = Uri.parse(data)
                uri.getQueryParameter("pa") ?: data
            } catch (e: Exception) {
                data
            }
        } else {
            data
        }
    }
}
