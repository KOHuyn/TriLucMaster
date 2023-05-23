package com.mobileplus.dummytriluc.ui.main.setupwifi

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.espressif.iot.esptouch.EsptouchTask
import com.espressif.iot.esptouch.IEsptouchListener
import com.espressif.iot.esptouch.IEsptouchResult
import com.espressif.iot.esptouch.IEsptouchTask
import com.espressif.iot.esptouch.util.ByteUtil
import com.espressif.iot.esptouch.util.TouchNetUtil
import com.mobileplus.dummytriluc.DummyTriLucApplication
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.databinding.ActivityEsptouchBinding
import java.lang.ref.WeakReference

/**
 * Created by KO Huyn on 21/05/2023.
 */
class EspTouchActivity : EspTouchActivityAbs() {

    private val REQUEST_PERMISSION = 0x01

    private var mTask: EsptouchAsyncTask4? = null

    private lateinit var mBinding: ActivityEsptouchBinding

    private var mSsid: String? = null
    private var mSsidBytes: ByteArray? = null
    private var mBssid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityEsptouchBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mBinding.tvSend.setOnClickListener { v -> executeEsptouch() }
        mBinding.btnCancel.setOnClickListener { v ->
            showProgress(false)
            if (mTask != null) {
                mTask!!.cancelEsptouch()
            }
        }
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        requestPermissions(permissions, REQUEST_PERMISSION)
        DummyTriLucApplication.getInstance().observeBroadcast(this) { broadcast ->
            Log.d("EspTouchActivity", "onCreate: Broadcast=$broadcast")
            onWifiChanged()
        }
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onWifiChanged()
            } else {
                AlertDialog.Builder(this)
                    .setTitle(R.string.location_permission_title)
                    .setMessage(R.string.location_permission_message)
                    .setCancelable(false)
                    .setPositiveButton(
                        R.string.label_accept,
                        DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int -> finish() })
                    .show()
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun showProgress(show: Boolean) {
        mBinding.progressView.isVisible = show
        mBinding.layoutContent.isVisible = !show
    }

    private fun onWifiChanged() {
        val stateResult: StateResult = checkState()
        mSsid = stateResult.ssid
        mSsidBytes = stateResult.ssidBytes
        mBssid = stateResult.bssid
        var message: CharSequence? = stateResult.message
        var confirmEnable = false
        if (stateResult.wifiConnected) {
            confirmEnable = true
            if (stateResult.is5G) {
                message = getString(R.string.esptouch_message_wifi_frequency)
            }
        } else {
            if (mTask != null) {
                mTask!!.cancelEsptouch()
                mTask = null
                AlertDialog.Builder(this@EspTouchActivity)
                    .setMessage(R.string.esptouch_configure_wifi_change_message)
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        }
        mBinding.tvSend.isEnabled = confirmEnable
        if (!message.isNullOrBlank()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun executeEsptouch() {
        val ssid = if (mSsidBytes == null) ByteUtil.getBytesByString(mSsid) else mSsidBytes!!
        val pwdStr: CharSequence? = mBinding.edtPasswordWifi.text
        val password = if (pwdStr == null) null else ByteUtil.getBytesByString(pwdStr.toString())
        val bssid = TouchNetUtil.parseBssid2bytes(mBssid)
        val deviceCount = 1.toString().toByteArray()
        val broadcast = byteArrayOf(1.toByte())
        if (mTask != null) {
            mTask!!.cancelEsptouch()
        }
        mTask = EsptouchAsyncTask4(this)
        mTask!!.execute(ssid, bssid, password, deviceCount, broadcast)
    }

    class EsptouchAsyncTask4(activity: EspTouchActivity) :
        AsyncTask<ByteArray, IEsptouchResult, List<IEsptouchResult>>() {
        private val mActivity: WeakReference<EspTouchActivity>
        private val mLock = Any()
        private var mResultDialog: AlertDialog? = null
        private var mEsptouchTask: IEsptouchTask? = null

        init {
            mActivity = WeakReference(activity)
        }

        fun cancelEsptouch() {
            cancel(true)
            val activity = mActivity.get()
            activity?.showProgress(false)
            if (mResultDialog != null) {
                mResultDialog!!.dismiss()
            }
            if (mEsptouchTask != null) {
                mEsptouchTask!!.interrupt()
            }
        }

        override fun onPreExecute() {
            val activity = mActivity.get()
            activity?.showProgress(true)
        }

        override fun onProgressUpdate(vararg values: IEsptouchResult?) {
            mActivity.get()?.showProgress(true)
        }

        override fun doInBackground(vararg params: ByteArray): List<IEsptouchResult>? {
            val activity = mActivity.get()
            var taskResultCount: Int
            synchronized(mLock) {
                val apSsid = params[0]
                val apBssid = params[1]
                val apPassword = params[2]
                val deviceCountData = params[3]
                val broadcastData = params[4]
                taskResultCount =
                    if (deviceCountData.isEmpty()) -1 else String(deviceCountData).toInt()
                val context = activity!!.applicationContext
                mEsptouchTask = EsptouchTask(apSsid, apBssid, apPassword, context)
                mEsptouchTask!!.setPackageBroadcast(broadcastData[0].toInt() == 1)
                mEsptouchTask!!.setEsptouchListener(IEsptouchListener { values: IEsptouchResult? ->
                    publishProgress(
                        values
                    )
                })
            }
            return mEsptouchTask!!.executeForResults(taskResultCount)
        }

        override fun onPostExecute(result: List<IEsptouchResult>?) {
            val activity = mActivity.get()
            activity!!.mTask = null
            activity.showProgress(false)
            if (result == null) {
                mResultDialog = AlertDialog.Builder(activity)
                    .setMessage(R.string.esptouch_configure_result_failed_port)
                    .setPositiveButton(R.string.label_accept, null)
                    .show()
                mResultDialog!!.setCanceledOnTouchOutside(false)
                return
            }

            // check whether the task is cancelled and no results received
            val firstResult = result[0]
            if (firstResult.isCancelled) {
                return
            }
            // the task received some results including cancelled while
            // executing before receiving enough results
            if (!firstResult.isSuc) {
                mResultDialog = AlertDialog.Builder(activity)
                    .setMessage(R.string.esptouch_configure_result_failed)
                    .setPositiveButton(R.string.label_accept, null)
                    .show()
                mResultDialog!!.setCanceledOnTouchOutside(false)
                return
            }
            mResultDialog = AlertDialog.Builder(activity)
                .setTitle(R.string.esptouch_configure_result_success)
                .setMessage(R.string.esptouch_configure_result_success_item)
                .setPositiveButton(
                    R.string.label_accept,
                    DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                        activity.finish()
                    })
                .show()
            mResultDialog!!.setCanceledOnTouchOutside(false)
        }
    }

    companion object {
        fun open(activity: Activity) {
            activity.startActivity(Intent(activity, EspTouchActivity::class.java))
        }
    }
}