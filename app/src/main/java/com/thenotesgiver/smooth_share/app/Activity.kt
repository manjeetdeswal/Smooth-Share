package com.thenotesgiver.smooth_share.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager.NameNotFoundException
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.thenotesgiver.smooth_share.R
import com.thenotesgiver.smooth_share.activity.NativeTemplateStyle
import com.thenotesgiver.smooth_share.activity.TemplateView
import com.thenotesgiver.smooth_share.activity.WelcomeActivity
import com.thenotesgiver.smooth_share.backend.Backend
import com.thenotesgiver.smooth_share.util.Permissions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class Activity : AppCompatActivity(), OnSharedPreferenceChangeListener {
    @Inject
    lateinit var backend: Backend

    private var amoledThemeState = false
    private var customFontsState = false
    private var darkThemeState = false

    protected val defaultPreferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(this)

    private val intentFilter = IntentFilter()

    private val amoledThemeEnabled: Boolean
        get() = defaultPreferences.getBoolean("amoled_theme", false)

    private val customFontsEnabled: Boolean
        get() = defaultPreferences.getBoolean("custom_fonts", false)

    private val darkThemeEnabled: Boolean
        get() {
            val value = defaultPreferences.getString("theme", "light")
            val systemWideTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return ("dark" == value || "system" == value && systemWideTheme == Configuration.UI_MODE_NIGHT_YES
                    || "battery" == value && powerSaveEnabled)
        }

    private val powerSaveEnabled: Boolean
        get() {
            if (Build.VERSION.SDK_INT < 23) return false
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            return powerManager.isPowerSaveMode
        }

    private var ongoingRequest: AlertDialog? = null

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_SYSTEM_POWER_SAVE_MODE_CHANGED == intent.action) {
                checkForThemeChange()
            }
        }
    }

    var skipPermissionRequest = false
        protected set

    private var themeLoadingFailed = false

    var welcomePageDisallowed = false
        protected set

    override fun onCreate(savedInstanceState: Bundle?) {



        darkThemeState = darkThemeEnabled
        amoledThemeState = amoledThemeEnabled
        customFontsState = customFontsEnabled
        MobileAds.initialize(
            this
        ) { }
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            getString(R.string.intMain),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    // The mInterstitialAd reference will be null until
                    // an ad is loaded.
                    mInterstitialAd = interstitialAd
                    //   Log.i(TAG, "onAdLoaded");
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error
                    //    Log.i(TAG, loadAdError.getMessage());
                    mInterstitialAd = null
                }
            })

        intentFilter.addAction(ACTION_SYSTEM_POWER_SAVE_MODE_CHANGED)

        if (darkThemeState) try {
            @StyleRes
            val currentThemeRes = packageManager.getActivityInfo(componentName, 0).theme.let {
                Log.d(Activity::class.simpleName, "ActivityTheme=$it AppTheme=" + applicationInfo.theme)
                return@let if (it == 0) applicationInfo.theme else it
            }

            @StyleRes
            val appliedRes = when (currentThemeRes) {
                R.style.Theme_LeafExplorer -> R.style.Theme_LeafExplorer_Dark
                R.style.Theme_LeafExplorer_NoActionBar -> R.style.Theme_LeafExplorer_Dark_NoActionBar
                R.style.Theme_LeafExplorer_NoActionBar_StaticStatusBar -> {
                    R.style.Theme_LeafExplorer_Dark_NoActionBar_StaticStatusBar
                }
                else -> {
                    Log.e(Activity::class.simpleName, "The requested theme ${javaClass.simpleName} is unknown.")
                    0
                }
            }

            themeLoadingFailed = appliedRes == 0

            if (!themeLoadingFailed) {
                setTheme(appliedRes)

                if (amoledThemeState) {
                    theme.applyStyle(R.style.BlackPatch, true)
                }
            }
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
        }

        // Apply the Preferred Font Family as a patch if enabled
        if (customFontsState) {
            Log.d(Activity::class.simpleName, "Custom fonts have been applied")
            theme.applyStyle(R.style.Roundies, true)
        }

        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(receiver, intentFilter)
    }

    override fun onResume() {
        super.onResume()
        checkForThemeChange()

        defaultPreferences.registerOnSharedPreferenceChangeListener(this)

        if (!hasIntroductionShown() && !welcomePageDisallowed) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        } else if (!Permissions.checkRunningConditions(this) && !skipPermissionRequest) {
            requestRequiredPermissions(true)
        }
    }

    override fun onPause() {
        super.onPause()
        defaultPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(receiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        ongoingRequest?.takeIf { it.isShowing }?.dismiss()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if ("custom_fonts" == key || "theme" == key || "amoled_theme" == key) {
            checkForThemeChange()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION && !Permissions.checkRunningConditions(this)) {
            requestRequiredPermissions(
                !skipPermissionRequest,
                permissions.mapIndexed { index, s -> s to (grantResults[index] == PERMISSION_GRANTED) }.toMap()
            )
        }
    }

    fun checkForThemeChange() {
        if ((darkThemeState != darkThemeEnabled || (darkThemeEnabled && amoledThemeState != amoledThemeEnabled))
            && !themeLoadingFailed || customFontsState != customFontsEnabled
        ) recreate()
    }

    fun hasIntroductionShown(): Boolean {
        return defaultPreferences.getBoolean("introduction_shown", false)
    }


    private fun requestRequiredPermissions(finishOtherwise: Boolean, results: Map<String, Boolean>? = null) {
        if (ongoingRequest?.isShowing == true) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestPermissions30(finishOtherwise)
        } else {
            for (permission in Permissions.getAll()) {
                val id = permission.id
                val showRationale = shouldShowRequestPermissionRationale(this@Activity, id)
                val deniedAfterRequest = results?.get(id) == false
                val granted = checkSelfPermission(this, id) == PERMISSION_GRANTED
                val request = {
                    requestPermissions(this@Activity, arrayOf(permission.id), REQUEST_PERMISSION)
                }

                if (granted) continue

                if (deniedAfterRequest && !showRationale) {
                    if (finishOtherwise) finish()
                } else if (showRationale) {
                    AlertDialog.Builder(this).apply {
                        setCancelable(false)
                        setTitle(permission.title)
                        setMessage(permission.description)
                        setPositiveButton(R.string.grant) { _: DialogInterface?, _: Int -> request() }
                        if (finishOtherwise) {
                            setNegativeButton(R.string.reject) { _: DialogInterface?, _: Int -> finish() }
                        } else {
                            setNegativeButton(R.string.close, null)
                        }
                        ongoingRequest = show()
                    }
                } else {
                    request()
                }

                break
            }
        }
    }

    private fun requestPermissions30(finishOtherwise: Boolean) {

        val request = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                    startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    startActivity(intent)
                }
            }
        }

        AlertDialog.Builder(this).apply {
            setCancelable(false)
            setTitle(R.string.storage_permission)
            setMessage(R.string.storage_permission_summary)
            setPositiveButton(R.string.grant) { _: DialogInterface?, _: Int -> request() }
            if (finishOtherwise) {
                setNegativeButton(R.string.reject) { _: DialogInterface?, _: Int -> finish() }
            } else {
                setNegativeButton(R.string.close, null)
            }
            ongoingRequest = show()
        }
    }

    companion object {

        var mInterstitialAd: InterstitialAd? = null
        private const val ACTION_SYSTEM_POWER_SAVE_MODE_CHANGED = "android.os.action.POWER_SAVE_MODE_CHANGED"

        private const val REQUEST_PERMISSION = 1
    }
}