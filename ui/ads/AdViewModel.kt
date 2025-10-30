package com.achievemeaalk.freedjf.ui.ads

import com.achievemeaalk.freedjf.BuildConfig
import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import javax.inject.Inject
import javax.inject.Singleton
import com.achievemeaalk.freedjf.util.findActivity

@Singleton
class AdViewModel @Inject constructor() {

  private var mInterstitialAd: InterstitialAd? = null
  private var mSettingsInterstitialAd: InterstitialAd? = null

  // Cached premium state to avoid blocking UI when deciding to show/load ads
  @Volatile private var isPremiumCached: Boolean = true

  private val adUnitId = BuildConfig.ADMOB_INTERSTITIAL_AD_UNIT_ID
  private val settingsAdUnitId = BuildConfig.ADMOB_INTERSTITIAL_AD_UNIT_ID

  // Preload an ad for transaction completion
  fun preloadTransactionCompletionAd(context: Context) {
    if (isPremiumCached) return
    loadInterstitialAd(context, adUnitId) { ad ->
      mInterstitialAd = ad
    }
  }

  // Preload an ad for settings actions
  fun preloadSettingsInterstitial(context: Context) {
    if (isPremiumCached) return
    loadInterstitialAd(context, settingsAdUnitId) { ad ->
      mSettingsInterstitialAd = ad
    }
  }

  // Show the transaction completion ad
  fun showTransactionCompletionAd(context: Context) {
    if (isPremiumCached) return
    context.findActivity()?.let { activity ->
        showInterstitialAd(activity, mInterstitialAd) {
            mInterstitialAd = null
            preloadTransactionCompletionAd(context) // Preload next ad
        }
    }
  }

  // Show the settings action ad
  fun showSettingsActionAd(context: Context) {
    if (isPremiumCached) return
    context.findActivity()?.let { activity ->
        showInterstitialAd(activity, mSettingsInterstitialAd) {
            mSettingsInterstitialAd = null
            preloadSettingsInterstitial(context) // Preload next ad
        }
    }
  }

  private fun loadInterstitialAd(context: Context, adUnitId: String, onAdLoaded: (InterstitialAd) -> Unit) {
    if (isPremiumCached) return
    val adRequest = AdRequest.Builder().build()
    InterstitialAd.load(context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
      override fun onAdLoaded(interstitialAd: InterstitialAd) {
        onAdLoaded(interstitialAd)
        Log.d("AdViewModel", "Ad was loaded successfully.")
      }

      override fun onAdFailedToLoad(loadAdError: LoadAdError) {
        Log.e("AdViewModel", "Ad failed to load: ${loadAdError.message}")
      }
    })
  }

  private fun showInterstitialAd(activity: Activity, interstitialAd: InterstitialAd?, onAdDismissed: () -> Unit) {
    if (isPremiumCached) {
      onAdDismissed()
      return
    }

    if (interstitialAd != null) {
      interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
          onAdDismissed()
        }

        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
          Log.e("AdViewModel", "Ad failed to show: ${adError.message}")
          onAdDismissed()
        }

        override fun onAdShowedFullScreenContent() {
          Log.d("AdViewModel", "Ad showed successfully.")
        }
      }
      interstitialAd.show(activity)
    } else {
      Log.d("AdViewModel", "The interstitial ad wasn't ready yet.")
      onAdDismissed() 
    }
  }
}