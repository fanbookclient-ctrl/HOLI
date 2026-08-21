/*
Minetest
Copyright (C) 2014-2020 MoNTE48, Maksim Gamarnik <MoNTE48@mail.ua>
Copyright (C) 2014-2020 ubulem,  Bektur Mambetov <berkut87@gmail.com>

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation; either version 2.1 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License along
with this program; if not, see <https://www.gnu.org/licenses/>.
*/

package net.minetest.minetest;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class AdManager {
    private static final String TAG = "AdManager";
    
    // Test Ad Unit ID for interstitial ads (Google's test ID)
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";
    
    private static AdManager instance;
    private InterstitialAd interstitialAd;
    private Activity activity;
    private boolean isLoading = false;
    
    private AdManager(Activity activity) {
        this.activity = activity;
    }
    
    /**
     * Get singleton instance of AdManager
     */
    public static AdManager getInstance(Activity activity) {
        if (instance == null) {
            instance = new AdManager(activity);
        }
        return instance;
    }
    
    /**
     * Load interstitial ad
     */
    public void loadInterstitialAd() {
        // Don't load if already loading or ad is already loaded
        if (isLoading || interstitialAd != null) {
            return;
        }
        
        isLoading = true;
        
        InterstitialAd.load(
            activity,
            AD_UNIT_ID,
            new AdRequest.Builder().build(),
            new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd ad) {
                    Log.d(TAG, "Interstitial ad was loaded.");
                    interstitialAd = ad;
                    isLoading = false;
                    setFullScreenContentCallback();
                    // Show the ad immediately when loaded
                    showInterstitialAd();
                }
                
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    Log.d(TAG, "Failed to load interstitial ad: " + loadAdError.getMessage());
                    interstitialAd = null;
                    isLoading = false;
                    // Try to load another ad
                    loadInterstitialAd();
                }
            }
        );
    }
    
    /**
     * Set up callbacks for ad lifecycle events
     */
    private void setFullScreenContentCallback() {
        if (interstitialAd == null) {
            return;
        }
        
        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad was dismissed.");
                interstitialAd = null;
                // Load and show next ad
                loadInterstitialAd();
            }
            
            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                Log.d(TAG, "Ad failed to show: " + adError.getMessage());
                interstitialAd = null;
                // Try to load another ad
                loadInterstitialAd();
            }
            
            @Override
            public void onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content.");
            }
            
            @Override
            public void onAdImpression() {
                Log.d(TAG, "Ad recorded an impression.");
            }
            
            @Override
            public void onAdClicked() {
                Log.d(TAG, "Ad was clicked.");
            }
        });
    }
    
    /**
     * Show interstitial ad if available
     */
    public void showInterstitialAd() {
        if (interstitialAd != null && activity != null) {
            try {
                interstitialAd.show(activity);
            } catch (Exception e) {
                Log.e(TAG, "Error showing interstitial ad: " + e.getMessage());
                interstitialAd = null;
                loadInterstitialAd();
            }
        } else {
            Log.d(TAG, "Interstitial ad is not ready.");
        }
    }
    
    /**
     * Check if ad is ready to be displayed
     */
    public boolean isAdReady() {
        return interstitialAd != null;
    }
    
    /**
     * Release resources
     */
    public void release() {
        interstitialAd = null;
        activity = null;
    }
}
