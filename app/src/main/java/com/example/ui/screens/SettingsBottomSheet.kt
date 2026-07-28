package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.UserProfile
import com.example.ui.components.NeonCapsule
import com.example.ui.theme.EmeraldNeon
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    onDismiss: () -> Unit,
    userProfile: UserProfile?,
    onSaveProfile: (String, String?) -> Unit,
    onSaveAiSettings: (String, String) -> Unit,
    onVerifyAiKey: suspend (String, String) -> Pair<Boolean, String>
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    
    var nameInput by remember { mutableStateOf(userProfile?.name ?: "") }
    // User profile photo state
    var photoUriInput by remember { mutableStateOf<Uri?>(userProfile?.photoUri?.let { Uri.parse(it) }) }
    
    var aiProvider by remember { mutableStateOf(userProfile?.aiProvider ?: "Gemini") }
    var apiKeyInput by remember { mutableStateOf(userProfile?.aiApiKey ?: "") }
    
    var isVerifying by remember { mutableStateOf(false) }
    var verificationMessage by remember { mutableStateOf<String?>(null) }
    var isVerificationSuccess by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Ignore if not permitted
            }
            photoUriInput = it
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        val scrollState = rememberScrollState()
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(scrollState)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pengaturan",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            NeonCapsule(padding = 16.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Profil Pengguna", color = EmeraldNeon, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Start))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.BottomEnd) {
                        Image(
                            painter = if (photoUriInput != null) rememberAsyncImagePainter(photoUriInput) 
                                      else painterResource(id = R.drawable.img_avatar),
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(CircleShape)
                                .border(2.dp, EmeraldNeon, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(EmeraldNeon)
                                .clickable { launcher.launch(arrayOf("image/*")) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(16.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Nama") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldNeon,
                            focusedLabelColor = EmeraldNeon,
                            cursorColor = EmeraldNeon
                        )
                    )
                }
            }
            
            NeonCapsule(padding = 16.dp) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Pengaturan AI", color = EmeraldNeon, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = aiProvider == "Gemini",
                            onClick = { aiProvider = "Gemini" },
                            colors = RadioButtonDefaults.colors(selectedColor = EmeraldNeon)
                        )
                        Text("Gemini", color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(
                            selected = aiProvider == "Groq",
                            onClick = { aiProvider = "Groq" },
                            colors = RadioButtonDefaults.colors(selectedColor = EmeraldNeon)
                        )
                        Text("Groq", color = MaterialTheme.colorScheme.onSurface)
                    }
                    
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it; verificationMessage = null },
                        label = { Text("API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldNeon,
                            focusedLabelColor = EmeraldNeon,
                            cursorColor = EmeraldNeon
                        )
                    )
                    
                    if (verificationMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = verificationMessage ?: "",
                            color = if (isVerificationSuccess) EmeraldNeon else MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            NeonCapsule(padding = 16.dp) {
                Column {
                    Text(
                        text = "Data",
                        color = EmeraldNeon,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { /* TODO: Backup logic */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = EmeraldNeon
                            )
                        ) {
                            Text("Backup")
                        }
                        
                        Button(
                            onClick = { /* TODO: Restore logic */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = EmeraldNeon
                            )
                        ) {
                            Text("Restore")
                        }
                    }
                }
            }

            NeonCapsule(padding = 16.dp) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Info Aplikasi",
                        color = EmeraldNeon,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "TabungKu v1.0",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Aplikasi tabungan pribadi modern dengan nuansa futuristik neon emerald.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            Button(
                onClick = {
                    onSaveProfile(nameInput, photoUriInput?.toString())
                    isVerifying = true
                    verificationMessage = null
                    scope.launch {
                        val (success, message) = onVerifyAiKey(aiProvider, apiKeyInput)
                        isVerifying = false
                        isVerificationSuccess = success
                        verificationMessage = message
                        if (success) {
                            onSaveAiSettings(aiProvider, apiKeyInput)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isVerifying,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldNeon,
                    contentColor = MaterialTheme.colorScheme.background,
                    disabledContainerColor = EmeraldNeon.copy(alpha = 0.5f)
                )
            ) {
                Text(if (isVerifying) "Memverifikasi..." else "Verifikasi & Simpan")
            }
        }
    }
}
