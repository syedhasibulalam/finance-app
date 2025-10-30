
package com.achievemeaalk.freedjf

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.achievemeaalk.freedjf.data.preferences.Theme
import com.achievemeaalk.freedjf.ui.settings.SettingsViewModel
import com.achievemeaalk.freedjf.ui.theme.MonefyTheme
import com.achievemeaalk.freedjf.data.security.viewmodel.SecurityViewModel
import com.achievemeaalk.freedjf.ui.passcode.PasscodeScreen
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.flow.first
import com.achievemeaalk.freedjf.ui.navigation.MainActivityViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TRANSACTION_TYPE = "EXTRA_TRANSACTION_TYPE"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val theme by settingsViewModel.theme.collectAsState()
            val language by settingsViewModel.language.collectAsState()
            val activity = (LocalContext.current as? Activity)
            val context = LocalContext.current



            LaunchedEffect(language) {
                val desiredLocale = LocaleListCompat.forLanguageTags(language)
                if (AppCompatDelegate.getApplicationLocales() != desiredLocale) {
                    Log.d("MainActivity", "Language changed to: $language, recreating activity.")
                    AppCompatDelegate.setApplicationLocales(desiredLocale)

                    // Log the app name in the newly set language to confirm the change
                    val newConfig = context.createConfigurationContext(context.resources.configuration)
                    val appName = newConfig.getString(R.string.app_name)
                    Log.d("MainActivity", "App name in ${language}: $appName")

                    activity?.recreate()
                }
            }

            val useDarkTheme = when (theme) {
                Theme.LIGHT -> false
                Theme.DARK -> true
                Theme.SYSTEM -> isSystemInDarkTheme()
            }

            MonefyTheme(useDarkTheme = useDarkTheme) {
                MonefyAppRoot()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Emit the navigation route to a shared ViewModel so composables can act on it
        val navVm: MainActivityViewModel by viewModels()
        handleIntent(intent) { route ->
            Log.d("MainActivity", "onNewIntent: Emitting route $route")
            navVm.postRoute(route)
        }
    }

    override fun onResume() {
        super.onResume()
        val securityViewModel: SecurityViewModel by viewModels()
        if (securityViewModel.isPasscodeEnabled.value) {
            securityViewModel.isLocked.value = true
        }
    }

    internal fun handleIntent(intent: Intent?, navigate: (String) -> Unit) {
        if (intent?.hasExtra(EXTRA_TRANSACTION_TYPE) == true) {
            val type = intent.getStringExtra(EXTRA_TRANSACTION_TYPE)
            if (!type.isNullOrBlank()) {
                val route = "addEditTransaction/0?type=$type"
                navigate(route)
            }
        }
        if (intent?.hasExtra("destination") == true) {
            val destination = intent.getStringExtra("destination")
            if (destination == "paywall") {
                navigate("paywall")
            }
        }
    }
}

@Composable
fun MonefyAppRoot() {
    val securityViewModel: SecurityViewModel = hiltViewModel()
    val isLocked by securityViewModel.isLocked.collectAsState()
    val isPasscodeEnabled by securityViewModel.isPasscodeEnabled.collectAsState()
    val navController = rememberNavController()
    val activity = (LocalContext.current as? MainActivity)
    var handledInitialIntent by rememberSaveable { mutableStateOf(false) }
    val navVm: MainActivityViewModel = hiltViewModel()

    LaunchedEffect(navController, isLocked, isPasscodeEnabled) {
        // Navigate only after the NavHost is set and the app is unlocked (if passcode enabled)
        val unlocked = !isPasscodeEnabled || !isLocked
        if (!handledInitialIntent && unlocked) {
            // Ensure NavHost has set the graph and a back stack entry exists
            navController.currentBackStackEntryFlow.first()
            activity?.handleIntent(activity.intent, navController::navigate)
            handledInitialIntent = true
        }
    }

    // Handle navigation events while the app is running
    LaunchedEffect(navController) {
        navVm.navigationEvents.collect { route ->
            val unlocked = !isPasscodeEnabled || !isLocked
            if (unlocked) {
                // Ensure a back stack entry exists before navigation
                navController.currentBackStackEntryFlow.first()
                navController.navigate(route)
            } else {
                // Defer until unlocked by re-emitting once unlocked
                // Using tryEmit to avoid suspension; events are small
                navVm.postRoute(route)
            }
        }
    }

    if (isPasscodeEnabled && isLocked) {
        PasscodeScreen(
            onUnlock = {
                securityViewModel.isLocked.value = false
            }
        )
    } else {
        MonefyApp(navController = navController)
    }
}