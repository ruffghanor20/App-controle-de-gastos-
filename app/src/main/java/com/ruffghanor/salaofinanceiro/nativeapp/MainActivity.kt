package com.ruffghanor.salaofinanceiro.nativeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ruffghanor.salaofinanceiro.nativeapp.data.AppDatabase
import com.ruffghanor.salaofinanceiro.nativeapp.data.SalonRepository
import com.ruffghanor.salaofinanceiro.nativeapp.ui.SalonFinanceiroNativeApp
import com.ruffghanor.salaofinanceiro.nativeapp.ui.SalonViewModel
import com.ruffghanor.salaofinanceiro.nativeapp.ui.SalonViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: SalonViewModel by viewModels {
        SalonViewModelFactory(SalonRepository(AppDatabase.get(applicationContext)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SalonFinanceiroNativeApp(viewModel)
        }
    }
}
