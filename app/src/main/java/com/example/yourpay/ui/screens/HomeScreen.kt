package com.example.yourpay.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    walletBalance: Double,
    onScanClick: () -> Unit,
    onPayUpiClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onAddMoneyClick: () -> Unit,
    onOfflineTransferClick: () -> Unit,
    onServiceClick: (String) -> Unit
) {
    var selectedBottomTab by remember { mutableStateOf(0) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .clickable {
                                Toast.makeText(context, "Search coming soon", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Search for users, bills & more", color = Color.Gray, fontSize = 14.sp)
                    }
                },
                navigationIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            Toast.makeText(context, "Profile clicked", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        var isOnline by remember { mutableStateOf(true) }
                        Box(
                            modifier = Modifier
                                .clickable { isOnline = !isOnline }
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isOnline) Color(0xFFE8F5E9) else Color(0xFFFFF3E0))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isOnline) Color(0xFF4CAF50) else Color(0xFFFF9800))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isOnline) "Online" else "Offline",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOnline) Color(0xFF2E7D32) else Color(0xFFE65100)
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onScanClick) {
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "Scan")
                    }
                    IconButton(onClick = {
                        Toast.makeText(context, "Notifications feature coming soon", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = selectedBottomTab == 0,
                    onClick = { selectedBottomTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan") },
                    label = { Text("ScanAnyQR") },
                    selected = selectedBottomTab == 1,
                    onClick = { 
                        selectedBottomTab = 1
                        onScanClick()
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") },
                    selected = selectedBottomTab == 2,
                    onClick = {
                        selectedBottomTab = 2
                        onHistoryClick()
                    }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Image/Promo section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Offline Wallet Balance", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("₹ ${"%.2f".format(walletBalance)}", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onAddMoneyClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                        ) {
                            Text("Add Money")
                        }
                    }
                }
            }

            // Transfer Money Section
            item {
                SectionTitle("Transfer Money")
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ActionItem(icon = Icons.Default.QrCodeScanner, label = "Scan QR", onClick = onScanClick)
                            ActionItem(icon = Icons.Default.WifiTethering, label = "Offline Transfer", onClick = onOfflineTransferClick)
                            ActionItem(icon = Icons.Default.Contacts, label = "Pay Contacts", onClick = onPayUpiClick)
                            ActionItem(icon = Icons.Default.AccountBalance, label = "Bank Transfer", onClick = onPayUpiClick)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ActionItem(icon = Icons.Default.Email, label = "Pay UPI ID", onClick = onPayUpiClick)
                            ActionItem(icon = Icons.Default.Person, label = "Self Transfer", onClick = { onServiceClick("self") })
                            ActionItem(icon = Icons.Default.Receipt, label = "Pay Bills", onClick = { onServiceClick("Pay Bills") })
                            ActionItem(icon = Icons.Default.MobileFriendly, label = "Mobile Recharge", onClick = { onServiceClick("Mobile Recharge") })
                        }
                    }
                }
            }

            // Recharge & Pay Bills Section
            item {
                SectionTitle("Recharge & Pay Bills")
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ActionItem(icon = Icons.Default.PhoneIphone, label = "Mobile", onClick = { onServiceClick("Mobile Recharge") })
                            ActionItem(icon = Icons.Default.Tv, label = "DTH", onClick = { onServiceClick("DTH Recharge") })
                            ActionItem(icon = Icons.Default.Lightbulb, label = "Electricity", onClick = { onServiceClick("Electricity Bill") })
                            ActionItem(icon = Icons.Default.CreditCard, label = "Credit Card", onClick = { onServiceClick("Credit Card Payment") })
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ActionItem(icon = Icons.Default.Home, label = "Rent", onClick = { onServiceClick("House Rent") })
                            ActionItem(icon = Icons.Default.AccountBalanceWallet, label = "Loan EMI", onClick = { onServiceClick("Loan Repayment") })
                            ActionItem(icon = Icons.Default.LocalGasStation, label = "Gas Cylinder", onClick = { onServiceClick("Gas Booking") })
                            ActionItem(icon = Icons.Default.MoreHoriz, label = "More", onClick = { onServiceClick("Other Utilities") })
                        }
                    }
                }
            }
            
            // Banking & Accounts
            item {
                SectionTitle("Banking & Offers")
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        ListItem(
                            headlineContent = { Text("Transaction History") },
                            leadingContent = { Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            modifier = Modifier.clickable { onHistoryClick() }
                        )
                        Divider()
                        ListItem(
                            headlineContent = { Text("Rewards & Offers") },
                            leadingContent = { Icon(Icons.Default.LocalOffer, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                            modifier = Modifier.clickable { Toast.makeText(context, "No offers available right now", Toast.LENGTH_SHORT).show() }
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun ActionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .width(76.dp)
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label, 
            fontSize = 12.sp, 
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}
