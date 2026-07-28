package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.TabungKuViewModel
import com.example.ui.TabungKuViewModelFactory
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.TabungKuTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val app = application as TabungKuApp
    val viewModel: TabungKuViewModel by viewModels {
        TabungKuViewModelFactory(app.repository)
    }
    
    setContent {
      TabungKuTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            HomeScreen(viewModel = viewModel)
        }
      }
    }
  }
}

