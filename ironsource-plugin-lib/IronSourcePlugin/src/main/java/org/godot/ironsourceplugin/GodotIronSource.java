package org.godot.ironsourceplugin;

import android.app.Activity;
import androidx.collection.ArraySet;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.integration.IntegrationHelper;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.BannerListener;
import com.ironsource.mediationsdk.sdk.InterstitialListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;
import org.godotengine.godot.plugin.UsedByGodot;

import java.util.Set;

public class GodotIronSource extends GodotPlugin {
    public static final String TAG = "GodotIronSource";

    private IronSourceBannerLayout banner;
    private String appKey = null;
    private boolean isInitialized = false;
    private FrameLayout layout = null;
    private Activity activity;


    @Nullable
    @Override
    public View onMainCreate(Activity activity) {
        this.activity = getActivity();
        layout = new FrameLayout(activity);
        return layout;
    }

    public GodotIronSource(Godot godot) {
        super(godot);
    }



    @NonNull
    @Override
    public String getPluginName() {
        return "GodotIronSource";
    }


    @Override
    public void onMainPause() {
        super.onMainPause();
        IronSource.onPause(getActivity());
    }

    
    @Override
    public void onMainResume() {
        super.onMainResume();
        IronSource.onResume(getActivity());
    }

    @NonNull
    @Override
    public Set<SignalInfo> getPluginSignals() {
        Set<SignalInfo> signals = new ArraySet<>();

        // General
        signals.add(new SignalInfo("on_plugin_error", String.class));

        // Rewarded
        signals.add(new SignalInfo("on_rewarded_availability_changed",Boolean.class));
        signals.add(new SignalInfo("on_rewarded_opened"));
        signals.add(new SignalInfo("on_rewarded_closed"));
        signals.add(new SignalInfo("on_rewarded"));

        // Interstitial
        signals.add(new SignalInfo("on_interstitial_loaded"));
        signals.add(new SignalInfo("on_interstitial_opened"));
        signals.add(new SignalInfo("on_interstitial_closed"));


        // Banner
        signals.add(new SignalInfo("on_banner_loaded"));


        return signals;
    }

    @UsedByGodot
    public void init(String appKey , boolean consent){
        isInitialized = true;
        IronSource.setConsent(consent);
        this.appKey = appKey;
    }


    @UsedByGodot
    public void initRewarded(){
        if (!isInitialized){
            Log.d(TAG, "ERROR : You should call init first");
            emitSignal("on_plugin_error","You should call init first");
            return;
        }
        activity.runOnUiThread(() -> {
            RewardedVideoListener rewardedVideoListener = new RewardedVideoListener() {
                @Override
                public void onRewardedVideoAdOpened() {
                    emitSignal("on_rewarded_opened");
                }

                @Override
                public void onRewardedVideoAdClosed() {
                    emitSignal("on_rewarded_closed");
                }

                @Override
                public void onRewardedVideoAvailabilityChanged(boolean b) {
                    emitSignal("on_rewarded_availability_changed",b);
                }

                @Override
                public void onRewardedVideoAdStarted() {

                }

                @Override
                public void onRewardedVideoAdEnded() {

                }

                @Override
                public void onRewardedVideoAdRewarded(Placement placement) {
                    emitSignal("on_rewarded");
                }

                @Override
                public void onRewardedVideoAdShowFailed(IronSourceError ironSourceError) {
                    Log.e(TAG, "onRewardedVideoAdShowFailed: " + ironSourceError.getErrorMessage());
                    emitSignal("on_plugin_error",ironSourceError.getErrorMessage());
                    // Not implemented yet
                }

                @Override
                public void onRewardedVideoAdClicked(Placement placement) {
                    // Not implemented yet
                }
            };
            IronSource.setRewardedVideoListener(rewardedVideoListener);
            IronSource.init(getActivity(),appKey, IronSource.AD_UNIT.REWARDED_VIDEO);
        });
    }


    @UsedByGodot
    public void showRewarded(){
        if (!IronSource.isRewardedVideoAvailable()){
            emitSignal("on_plugin_error","Rewarded is NOT READY");
            return;
        }
        IronSource.showRewardedVideo();
    }

    
    @UsedByGodot
    public void initInterstitial(){
        if (!isInitialized){
            Log.d(TAG, "ERROR : You should call init first");
            emitSignal("on_plugin_error","You should call init first");
            return;
        }

        activity.runOnUiThread(() -> {
            InterstitialListener interstitialListener = new InterstitialListener() {
                @Override
                public void onInterstitialAdReady() {
                    emitSignal("on_interstitial_loaded");
                    Log.d(TAG, "onInterstitialAdReady: ");
                    IntegrationHelper.validateIntegration(activity);
                }

                @Override
                public void onInterstitialAdLoadFailed(IronSourceError ironSourceError) {
                    emitSignal("on_plugin_error",ironSourceError.getErrorMessage());
                }

                @Override
                public void onInterstitialAdOpened() {
                    emitSignal("on_interstitial_opened");
                    Log.d(TAG, "onInterstitialAdOpened: ");
                }

                @Override
                public void onInterstitialAdClosed() {
                    emitSignal("on_interstitial_closed");
                    IronSource.loadInterstitial();
                    Log.d(TAG, "onInterstitialAdClosed: ");
                }

                @Override
                public void onInterstitialAdShowSucceeded() {

                }

                @Override
                public void onInterstitialAdShowFailed(IronSourceError ironSourceError) {
                    Log.e(TAG, "onInterstitialAdShowFailed: " + ironSourceError.getErrorMessage());
                    emitSignal("on_plugin_error",ironSourceError.getErrorMessage());

                }

                @Override
                public void onInterstitialAdClicked() {

                }
            };
            IronSource.setInterstitialListener(interstitialListener);
            IronSource.init(activity,appKey, IronSource.AD_UNIT.INTERSTITIAL);
            loadInterstitial();
        });
    }

    
    @UsedByGodot
    public void loadInterstitial(){
        if (!isInitialized){
            Log.d(TAG, "ERROR : You should call init first");
            emitSignal("on_plugin_error","You should call init first");
            return;
        }
        IronSource.loadInterstitial();
    }


    @UsedByGodot
    public void showInterstitial(){
        if (!IronSource.isInterstitialReady()){
            emitSignal("on_plugin_error","Interstitial is NOT READY");
            return;
        }
        IronSource.showInterstitial();
    }


    @UsedByGodot
    public void initBanner(boolean isTop){
        if (!isInitialized){
            Log.d(TAG, "ERROR : You should call init first");
            emitSignal("on_plugin_error","You should call init first");
            return;
        }
        activity.runOnUiThread(() -> {
            banner = IronSource.createBanner(activity, ISBannerSize.SMART);

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    isTop? Gravity.TOP : Gravity.BOTTOM);

            layout.addView(banner,0,layoutParams);

            BannerListener bannerListener = new BannerListener() {
                @Override
                public void onBannerAdLoaded() {
                    emitSignal("on_banner_loaded");
                    Log.d(TAG, "onBannerAdLoaded: ");
                }

                @Override
                public void onBannerAdLoadFailed(IronSourceError ironSourceError) {
                    Log.e(TAG, "onBannerAdLoadFailed: " + ironSourceError.getErrorMessage());
                    emitSignal("on_plugin_error",ironSourceError.getErrorMessage());
                }

                @Override
                public void onBannerAdClicked() {

                }

                @Override
                public void onBannerAdScreenPresented() {

                }

                @Override
                public void onBannerAdScreenDismissed() {

                }

                @Override
                public void onBannerAdLeftApplication() {

                }
            };
            banner.setBannerListener(bannerListener);
            IronSource.init(activity, appKey, IronSource.AD_UNIT.BANNER);
            IronSource.loadBanner(banner);
        });
    }


    @UsedByGodot
    public void showBanner(){
        if(banner == null){
            return;
        }
        banner.setVisibility(View.VISIBLE);
    }

    
    @UsedByGodot
    public void hideBanner() {
        if(banner == null){
            return;
        }
        banner.setVisibility(View.INVISIBLE);
    }



}
