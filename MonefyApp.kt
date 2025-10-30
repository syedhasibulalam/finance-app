package com.achievemeaalk.freedjf

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.achievemeaalk.freedjf.ui.Navigation
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.achievemeaalk.freedjf.ui.onboarding.firstAccountPromptRoute
import com.achievemeaalk.freedjf.ui.onboarding.onboardingRoute
import com.achievemeaalk.freedjf.ui.accounts.AccountsViewModel
import com.achievemeaalk.freedjf.ui.components.AddRecordBottomSheet
import com.achievemeaalk.freedjf.ui.components.BottomNavItem
import com.achievemeaalk.freedjf.ui.components.CustomBottomNavigationBar
import com.achievemeaalk.freedjf.ui.dashboard.DashboardViewModel
import com.achievemeaalk.freedjf.ui.settings.SettingsViewModel
import com.canopas.lib.showcase.IntroShowcase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonefyApp(
    navController: NavHostController = rememberNavController()
) {
    val sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true)
    val scope=rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    // ViewModels
    val dashboardViewModel: DashboardViewModel=hiltViewModel()
    val accountsViewModel: AccountsViewModel=hiltViewModel()
    val settingsViewModel: SettingsViewModel=hiltViewModel()

    val isLoading by settingsViewModel.isLoading.collectAsState()
    val dashboardState by dashboardViewModel.dashboardState.collectAsState()
    val accountsState by accountsViewModel.accounts.collectAsState()
    val currency by settingsViewModel.currency.collectAsState()
    val completedShowcaseRoutes by settingsViewModel.completedShowcaseRoutes.collectAsState()
    val language by settingsViewModel.language.collectAsState()
    val onboardingCompleted by settingsViewModel.onboardingCompleted.collectAsState()
    val getStartedCompleted by settingsViewModel.getStartedCompleted.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute=navBackStackEntry?.destination?.route

    // This now determines the start destination ONLY ONCE and doesn't react to changes.
    val startDestination=remember {
        when {
            onboardingCompleted -> "dashboard"
            getStartedCompleted -> onboardingRoute
            else -> "get_started"
        }
    }

    if(isLoading) {
        Box(
            modifier=Modifier.fillMaxSize(),
            contentAlignment=Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
    else {
        val shouldShowMainAppUi=
            startDestination == "dashboard" || currentRoute?.let { it != "get_started" && it != onboardingRoute } == true


        // Showcase state
        val showcaseRoutes=remember { setOf("dashboard", "accounts", "budgets", "categories") }

        // --- START: MODIFIED SHOWCASE LOGIC ---
        var showShowcase by remember { mutableStateOf(false) }
        var showcaseKey by remember { mutableStateOf("") }

        LaunchedEffect(currentRoute, completedShowcaseRoutes) {
            val baseRoute=showcaseRoutes.find { currentRoute?.startsWith(it) == true }
            if(baseRoute != null && !completedShowcaseRoutes.contains(baseRoute)) {
                // Lock in the key for the showcase we intend to show
                showcaseKey=baseRoute
                showShowcase=true
            }
            else {
                showShowcase=false
            }
        }
        IntroShowcase(
            showIntroShowCase=showShowcase,
            onShowCaseCompleted={
                // Use the locked-in key to ensure we save the correct route
                if(showcaseKey.isNotBlank()) {
                    settingsViewModel.addCompletedShowcaseRoute(showcaseKey)
                    showcaseKey="" // Reset the key
                }
                showShowcase=false // Explicitly hide after completion
            }
        )
        // --- END: MODIFIED SHOWCASE LOGIC ---
        {
            if(shouldShowMainAppUi) {
                Scaffold(
                    bottomBar={
                                                val shouldShowBottomBar = when (currentRoute) {
                            "get_started",
                            onboardingRoute,
                            firstAccountPromptRoute,
                            "first_account_success",
                            "paywall",
                            "passcode",
                            "setPasscode",
                            "receiptScanner",
                            "settings",
                            "securitySettings",
                            "homeScreenSettings",
                            "recurringBills"
                                -> false

                            else -> currentRoute?.startsWith("addEditTransaction") != true
                        }

                        if(shouldShowBottomBar) {
                            CustomBottomNavigationBar(
                                navController=navController,
                                items=listOf(
                                    BottomNavItem.Dashboard,
                                    BottomNavItem.Accounts,
                                    BottomNavItem.Budgets,
                                    BottomNavItem.Categories
                                ),
                                onFabClick={ showBottomSheet=true },
                                completedShowcaseRoutes=completedShowcaseRoutes
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier=Modifier.padding(innerPadding)) {
                        Navigation(
                            navController=navController,
                            dashboardViewModel=dashboardViewModel,
                            language=language,
                            startDestination=startDestination
                        )
                        if(showBottomSheet) {
                            ModalBottomSheet(
                                onDismissRequest={ showBottomSheet=false },
                                sheetState=sheetState,
                                dragHandle=null
                            ) {
                                AddRecordBottomSheet(
                                    onDismissRequest={
                                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                                            if(!sheetState.isVisible) {
                                                showBottomSheet=false
                                            }
                                        }
                                    },
                                    onActionSelected={ transactionType ->
                                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                                            if(!sheetState.isVisible) {
                                                showBottomSheet=false
                                            }
                                            navController.navigate("addEditTransaction/0?type=${transactionType.name}")
                                        }
                                    },
                                    totalBalance=dashboardState.totalBalance,
                                    spentAmount=dashboardState.totalSpent,
                                    timeFilter=dashboardViewModel.selectedTimeFilter.collectAsState().value,
                                    accountsCount=accountsState.size,
                                    currencyCode=currency
                                )
                            }
                        }
                    }
                }
            }
            else {
                Navigation(
                    navController=navController,
                    dashboardViewModel=dashboardViewModel,
                    language=language,
                    startDestination=startDestination
                )
            }
        }
    }
}

