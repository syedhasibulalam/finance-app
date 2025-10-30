
package com.achievemeaalk.freedjf.ui

import androidx.camera.core.ExperimentalGetImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.achievemeaalk.freedjf.ui.accounts.AccountsScreen
import com.achievemeaalk.freedjf.ui.accounts.AccountsViewModel
import com.achievemeaalk.freedjf.ui.accounts.AddEditAccountBottomSheet
import com.achievemeaalk.freedjf.ui.budgets.BudgetsScreen
import com.achievemeaalk.freedjf.ui.categories.CategoriesScreen
import com.achievemeaalk.freedjf.ui.dashboard.DashboardScreen
import com.achievemeaalk.freedjf.ui.dashboard.DashboardViewModel
import com.achievemeaalk.freedjf.ui.onboarding.FirstAccountPromptScreen
import com.achievemeaalk.freedjf.ui.onboarding.FirstAccountSuccessScreen
import com.achievemeaalk.freedjf.ui.onboarding.GetStartedScreen
import com.achievemeaalk.freedjf.ui.onboarding.OnboardingScreen
import com.achievemeaalk.freedjf.ui.onboarding.firstAccountPromptRoute
import com.achievemeaalk.freedjf.ui.onboarding.firstAccountSuccessRoute
import com.achievemeaalk.freedjf.ui.onboarding.onboardingRoute
import com.achievemeaalk.freedjf.ui.passcode.SetPasscodeScreen
import com.achievemeaalk.freedjf.ui.recurring.RecurringTransactionsScreen
import com.achievemeaalk.freedjf.ui.settings.SettingsScreen
import com.achievemeaalk.freedjf.ui.settings.SettingsViewModel
import com.achievemeaalk.freedjf.ui.settings.homescreen.HomeScreenSettingsScreen
import com.achievemeaalk.freedjf.ui.settings.security.SecuritySettingsScreen
import com.achievemeaalk.freedjf.ui.transactions.AddEditTransactionScreen
import com.achievemeaalk.freedjf.ui.transactions.AllTransactionsScreen
import com.achievemeaalk.freedjf.ui.transactions.ReceiptScannerScreen
import com.canopas.lib.showcase.IntroShowcaseScope

@OptIn(ExperimentalGetImage::class)
@Composable
fun IntroShowcaseScope.Navigation(
  navController: NavHostController,
  dashboardViewModel: DashboardViewModel,
  language: String,
  startDestination: String
) {
  val settingsViewModel: SettingsViewModel = hiltViewModel()
  NavHost(navController = navController, startDestination = startDestination) {
    composable("get_started") {
        AnimatedScreen(tier = AnimationTier.Tier3) {
            GetStartedScreen(
                onGetStartedClick = {
                    settingsViewModel.setGetStartedCompleted(true)
                    navController.navigate(onboardingRoute) {
                        popUpTo("get_started") { inclusive = true }
                    }
                }
            )
        }
    }
    composable(onboardingRoute) {
        AnimatedScreen(tier = AnimationTier.Tier3) {
            OnboardingScreen(navController = navController)
        }
    }
    composable(firstAccountPromptRoute) {
        AnimatedScreen(tier = AnimationTier.Tier3) {
            FirstAccountPromptScreen(navController = navController)
        }
    }

    composable(firstAccountSuccessRoute) {
        AnimatedScreen(tier = AnimationTier.Tier3) {
            FirstAccountSuccessScreen(navController = navController)
        }
    }


    composable("dashboard") {
      AnimatedScreen(tier = AnimationTier.Tier1) {
          DashboardScreen(
            navController = navController,
            viewModel = dashboardViewModel,
            language = language
          )
      }
    }
    composable("accounts") {
        AnimatedScreen(tier = AnimationTier.Tier2) {
            AccountsScreen(navController = navController)
        }
    }
    composable("addEditAccount?accountId={accountId}") {
      val viewModel: AccountsViewModel = hiltViewModel()
      val account by viewModel.account.collectAsState(initial = null)
        AnimatedScreen(tier = AnimationTier.Tier3) {
            AddEditAccountBottomSheet(
                account = account,
                onSave = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("new_account_id", it)
                    navController.popBackStack()
                },
                onDismiss = { navController.popBackStack() }
            )
        }
    }
    composable(
      "budgets?tab={tab}",
      arguments = listOf(
        navArgument("tab") {
          type = NavType.StringType
          nullable = true
          defaultValue = "budgets"
        }
      )
    ) {
      it.arguments?.getString("tab") ?: "budgets"
        AnimatedScreen(tier = AnimationTier.Tier2) {
            BudgetsScreen(
                navController = navController
            )
        }
    }
    composable(
      "categories?showBottomSheet={showBottomSheet}&showCategorySelection={showCategorySelection}",
      arguments = listOf(
        navArgument("showBottomSheet") {
          type = NavType.BoolType
          defaultValue = false
        },
        navArgument("showCategorySelection") {
          type = NavType.BoolType
          defaultValue = false
        }
      )
    ) { backStackEntry ->
      val showBottomSheet =backStackEntry.arguments?.getBoolean("showBottomSheet") == true
      val showCategorySelection =backStackEntry.arguments?.getBoolean("showCategorySelection") == true
        AnimatedScreen(tier = AnimationTier.Tier2) {
            CategoriesScreen(
                showAddSheetInitially = showBottomSheet,
                isPicker = showCategorySelection,
                onCategorySelected = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("new_category", it)
                    navController.popBackStack()
                }
            )
        }
    }
    composable(
      "addEditTransaction/{transactionId}?type={type}",
      arguments = listOf(
        navArgument("transactionId") { type = NavType.IntType },
        navArgument("type") { type = NavType.StringType; nullable = true }
      )
    ) {
        AnimatedScreen(tier = AnimationTier.Tier3) {
            AddEditTransactionScreen(
                navController = navController,
                onSave = { navController.popBackStack() }
            )
        }
    }
    composable("receiptScanner") {
        AnimatedScreen(tier = AnimationTier.Tier3) {
            ReceiptScannerScreen(navController = navController)
        }
    }
    composable("settings") {
        AnimatedScreen(tier = AnimationTier.Tier3) {
            SettingsScreen(navController = navController)
        }
    }
    composable("securitySettings") {
        AnimatedScreen(tier = AnimationTier.Tier3) {
            SecuritySettingsScreen(navController = navController)
        }
    }

    composable("setPasscode") {
        AnimatedScreen(tier = AnimationTier.Tier3) {
            SetPasscodeScreen(
                onPasscodeSet = {

                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("passcode_updated", true)
                    navController.popBackStack()
                }
            )
        }
    }
    composable("homeScreenSettings") {
        AnimatedScreen(tier = AnimationTier.Tier3) {
            HomeScreenSettingsScreen()
        }
    }

    composable("recurringBills") {
        AnimatedScreen(tier = AnimationTier.Tier2) {
            RecurringTransactionsScreen(navController = navController)
        }
    }

    composable(
        "allTransactions?categoryId={categoryId}",
        arguments = listOf(
            navArgument("categoryId") {
                type = NavType.StringType
                nullable = true
            }
        )
    ) { backStackEntry ->
        AnimatedScreen(tier = AnimationTier.Tier2) {
            AllTransactionsScreen(
                navController = navController,
                categoryId = backStackEntry.arguments?.getString("categoryId")
            )
        }
    }
  }
}
