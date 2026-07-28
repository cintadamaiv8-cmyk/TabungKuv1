package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.MainSavings
import com.example.data.SavingsTarget
import com.example.data.UserProfile
import com.example.ui.TabungKuViewModel
import com.example.ui.components.BannerCarousel
import com.example.ui.components.NeonCapsule
import com.example.ui.components.NeonProgressBar
import com.example.ui.theme.EmeraldNeon
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: TabungKuViewModel) {
    val scrollState = rememberScrollState()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val mainSavings by viewModel.mainSavings.collectAsStateWithLifecycle()
    val allTargets by viewModel.allTargets.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle(emptyList())

    var showSettings by remember { mutableStateOf(false) }
    var showFinancialSummary by remember { mutableStateOf(false) }
    var showSavingsMenu by remember { mutableStateOf(false) }
    var showTransactionSheet by remember { mutableStateOf(false) }
    var isDepositTransaction by remember { mutableStateOf(true) }
    var showHistory by remember { mutableStateOf(false) }
    var showAddTarget by remember { mutableStateOf(false) }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            
            // 1. Banner Carousel
            BannerCarousel()

            // 2. Tabungan Utama
            MainSavingsCard(mainSavings)

            // 3. Profil Pengguna
            UserProfileCard(
                userProfile = userProfile,
                onOpenSettings = { showSettings = true }
            )

            // 4. Daftar Fitur Aplikasi
            FeatureList(
                onOpenStatistics = { showFinancialSummary = true },
                onOpenDeposit = { isDepositTransaction = true; showTransactionSheet = true },
                onOpenWithdraw = { isDepositTransaction = false; showTransactionSheet = true },
                onOpenTarget = { showAddTarget = true }
            )

            // 5. Progress Target Tabungan
            if (allTargets.isNotEmpty()) {
                TargetProgressSection(allTargets)
            }
        }
        
        if (showSettings) {
            SettingsBottomSheet(
                onDismiss = { showSettings = false },
                userProfile = userProfile,
                onSaveProfile = { name, uri -> viewModel.updateProfile(name, uri) },
                onSaveAiSettings = { provider, key -> viewModel.updateAiSettings(provider, key) },
                onVerifyAiKey = { provider, key -> viewModel.verifyAiKey(provider, key) }
            )
        }
        
        if (showFinancialSummary) {
            FinancialSummarySheet(
                onDismiss = { showFinancialSummary = false },
                viewModel = viewModel
            )
        }
        
        if (showSavingsMenu) {
            SavingsMenuSheet(
                onDismiss = { showSavingsMenu = false },
                onDeposit = { isDepositTransaction = true; showTransactionSheet = true },
                onWithdraw = { isDepositTransaction = false; showTransactionSheet = true },
                onHistory = { showHistory = true }
            )
        }

        if (showTransactionSheet) {
            TransactionSheet(
                onDismiss = { showTransactionSheet = false },
                isDeposit = isDepositTransaction,
                onConfirm = { amount -> 
                    if (isDepositTransaction) viewModel.depositMain(amount) 
                    else viewModel.withdrawMain(amount) 
                }
            )
        }

        if (showHistory) {
            HistorySheet(
                onDismiss = { showHistory = false },
                transactions = allTransactions
            )
        }

        if (showAddTarget) {
            AddTargetSheet(
                onDismiss = { showAddTarget = false },
                onConfirm = { name, amount -> viewModel.addTarget(name, amount) }
            )
        }
    }
}

@Composable
fun MainSavingsCard(mainSavings: MainSavings?) {
    val balance = mainSavings?.balance ?: 0.0
    val formattedBalance = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(balance)
    
    NeonCapsule(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        padding = 24.dp,
        cornerRadius = 32.dp,
        borderWidth = 2.dp,
        glowRadius = 15.dp,
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TABUNGAN UTAMA",
                color = EmeraldNeon,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formattedBalance,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(EmeraldNeon.copy(alpha = 0.2f))
                    .border(1.dp, EmeraldNeon.copy(alpha = 0.3f), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Ayo menabung hari ini!",
                    color = EmeraldNeon.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun UserProfileCard(
    userProfile: UserProfile?,
    onOpenSettings: () -> Unit
) {
    val name = userProfile?.name ?: "Pengguna"
    
    NeonCapsule(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        padding = 12.dp,
        cornerRadius = 50.dp,
        borderWidth = 2.dp,
        borderColor = EmeraldNeon.copy(alpha = 0.4f),
        glowRadius = 10.dp,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = if (userProfile?.photoUri != null) coil.compose.rememberAsyncImagePainter(android.net.Uri.parse(userProfile.photoUri)) 
                          else painterResource(id = R.drawable.img_avatar),
                contentDescription = "Profile Photo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Halo, $name 👋",
                    color = EmeraldNeon,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Kelola tabunganmu hari ini",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(EmeraldNeon.copy(alpha = 0.1f))
                    .clickable { onOpenSettings() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = EmeraldNeon,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun FeatureList(
    onOpenStatistics: () -> Unit,
    onOpenDeposit: () -> Unit,
    onOpenWithdraw: () -> Unit,
    onOpenTarget: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureItem(
                modifier = Modifier.weight(1f),
                title = "Tambah\nTabungan",
                icon = Icons.Default.Add,
                onClick = onOpenDeposit
            )
            FeatureItem(
                modifier = Modifier.weight(1f),
                title = "Target\nMenabung",
                icon = Icons.Default.Add, // Or another target icon
                onClick = onOpenTarget
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureItem(
                modifier = Modifier.weight(1f),
                title = "Statistik\nKeuangan",
                icon = Icons.Default.TrendingUp,
                onClick = onOpenStatistics
            )
            FeatureItem(
                modifier = Modifier.weight(1f),
                title = "Tarik\nSaldo",
                icon = Icons.Default.Money, // Or withdraw icon
                onClick = onOpenWithdraw
            )
        }
    }
}

@Composable
fun FeatureItem(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    NeonCapsule(
        modifier = modifier.clickable { onClick() },
        padding = 12.dp,
        cornerRadius = 16.dp,
        borderWidth = 1.dp,
        borderColor = EmeraldNeon.copy(alpha = 0.4f),
        glowRadius = 8.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title.replace("\n", " "),
                tint = EmeraldNeon,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun TargetProgressSection(targets: List<SavingsTarget>) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "TARGET AKTIF",
            color = EmeraldNeon,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
        
        targets.take(5).forEach { target ->
            TargetItem(
                target = target,
                onClick = { /* Could expand target logic here, or pass deposit action directly */ }
            )
        }
    }
}

@Composable
fun TargetItem(
    target: SavingsTarget,
    onClick: () -> Unit = {}
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    val currentStr = formatter.format(target.currentAmount)
    val targetStr = formatter.format(target.targetAmount)
    val progress = if (target.targetAmount > 0) {
        (target.currentAmount / target.targetAmount).toFloat().coerceIn(0f, 1f)
    } else 0f
    
    val percentStr = "${(progress * 100).toInt()}%"

    NeonCapsule(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        padding = 12.dp,
        cornerRadius = 16.dp,
        borderWidth = 1.dp,
        borderColor = EmeraldNeon.copy(alpha = 0.5f),
        glowRadius = 10.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = target.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$currentStr / $targetStr",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = percentStr,
                    color = EmeraldNeon,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            NeonProgressBar(progress = progress)
        }
    }
}
