package com.mobileplus.dummytriluc

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Base64
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.multidex.MultiDexApplication
import com.androidnetworking.AndroidNetworking
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.firebase.FirebaseApp
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.di.dummyModule
import com.mobileplus.dummytriluc.transceiver.ITransceiverController
import com.mobileplus.dummytriluc.transceiver.TransceiverControllerImpl
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.mobileplus.dummytriluc.ui.utils.language.LocalManageUtil
import com.mobileplus.dummytriluc.ui.utils.language.SPUtil
import com.utils.LogUtil
import io.github.inflationx.viewpump.ViewPump
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.logger.EmptyLogger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Collections
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class DummyTriLucApplication : MultiDexApplication() {
    private val viewPump: ViewPump by inject()
    private val dataManager by inject<DataManager>()
    private val mBroadcastData: MutableLiveData<String> = MutableLiveData()

    private var mCacheMap: HashMap<String, Any> = hashMapOf()

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            when (action) {
                WifiManager.NETWORK_STATE_CHANGED_ACTION,
                LocationManager.PROVIDERS_CHANGED_ACTION -> mBroadcastData.setValue(action)
            }
        }
    }


    companion object {
        var exoCache: SimpleCache? = null
        private lateinit var dummyTriLucApplication: DummyTriLucApplication
        var isRunningBackground: Boolean = false

        @JvmStatic
        fun getInstance(): DummyTriLucApplication {
            return dummyTriLucApplication
        }
    }

    override fun onCreate() {
        super.onCreate()
        dummyTriLucApplication = this
        LocalManageUtil.setApplicationLanguage(this)
        LogUtil.init(BuildConfig.DEBUG)
        startKoin {
            androidContext(this@DummyTriLucApplication)
            modules(dummyModule)
            logger(EmptyLogger())
            initFastNetworking(isUnSafeNetworking = true)
            ViewPump.init(viewPump)
        }
        ITransceiverController.getInstance().startup()
        setupCacheExo()
        initialSocial()
//        RxJavaPlugins.setErrorHandler {
//            it.logErr()
//        }
        mCacheMap = HashMap()
        val filter = IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        }
        registerReceiver(mReceiver, filter)

        try {
            val info = packageManager.getPackageInfo(
                applicationContext.packageName,
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                logErr("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        unregisterReceiver(mReceiver)
    }

    fun observeBroadcast(owner: LifecycleOwner, observer: Observer<String>) {
        mBroadcastData.observe(owner, observer)
    }

    fun observeBroadcastForever(observer: Observer<String>) {
        mBroadcastData.observeForever(observer)
    }

    fun removeBroadcastObserver(observer: Observer<String>) {
        mBroadcastData.removeObserver(observer)
    }

    fun putCache(key: String, value: Any) {
        mCacheMap[key] = value
    }

    fun takeCache(key: String?): Any? {
        return mCacheMap.remove(key)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocalManageUtil.onConfigurationChanged(applicationContext)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(LocalManageUtil.setLocal(base))
    }

    private fun initialSocial() {
        FacebookSdk.sdkInitialize(this)
        AppEventsLogger.activateApp(this)
        FirebaseApp.initializeApp(this)
    }

    private fun setupCacheExo() {
        val leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(90 * 1024 * 1024)
        val databaseProvider: DatabaseProvider = ExoDatabaseProvider(this)

        if (exoCache == null) {
            exoCache = SimpleCache(cacheDir, leastRecentlyUsedCacheEvictor, databaseProvider)
        }
    }

    private fun initFastNetworking(isUnSafeNetworking: Boolean = false) {
        val spec = ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
            .supportsTlsExtensions(true)
            .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
            .cipherSuites(
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,
                CipherSuite.TLS_ECDHE_RSA_WITH_RC4_128_SHA,
                CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA
            )
            .build()

        val builder = if (isUnSafeNetworking)
            getUnsafeOkHttpClient().newBuilder().connectionSpecs(
                mutableListOf(
                    ConnectionSpec.CLEARTEXT,
                    ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.MODERN_TLS
                )
            )
        else OkHttpClient().newBuilder()
            .connectionSpecs(Collections.singletonList(spec))
        builder.addInterceptor { chain ->
            val original = chain.request()

            val queryLanguage = original.url().newBuilder()
                .addQueryParameter("lang", SPUtil.getInstance(this).getSelectLanguage())
                .addQueryParameter("app_version", BuildConfig.VERSION_NAME)
                .addQueryParameter("app_type", "ANDROID")
                .build()
            val requestBuilder = original.newBuilder().url(queryLanguage)
                .method(original.method(), original.body())

            if (dataManager.getToken() != null) {
                requestBuilder.header(
                    ApiConstants.TOKEN_HEADER,
                    "Bearer ${dataManager.getToken()!!}"
                )
            }
            chain.proceed(requestBuilder.build())
        }

        val okHttpClient = builder
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.initialize(this, okHttpClient)
//        if (BuildConfig.DEBUG) {
//            AndroidNetworking.enableLogging(HttpLoggingInterceptor.Level.BODY)
//        }
    }

    @SuppressLint("TrustAllX509TrustManager", "BadHostnameVerifier")
    private fun getUnsafeOkHttpClient(): OkHttpClient {
        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out java.security.cert.X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun checkServerTrusted(
                    chain: Array<out java.security.cert.X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                    return arrayOf()
                }
            })

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.socketFactory

            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }

            return builder.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}