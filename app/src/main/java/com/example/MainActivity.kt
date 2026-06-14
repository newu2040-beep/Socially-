package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.SociallyDatabase
import com.example.data.SociallyRepository
import com.example.ui.SociallyAppContent
import com.example.ui.SociallyViewModel
import com.example.ui.SociallyViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize SQLite Room database & Repository
        val database = SociallyDatabase.getDatabase(this)
        val repository = SociallyRepository(database)
        
        // Instantiate ViewModel
        val factory = SociallyViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[SociallyViewModel::class.java]

        setContent {
            MyApplicationTheme {
                SociallyAppContent(viewModel = viewModel)
            }
        }
    }
}
