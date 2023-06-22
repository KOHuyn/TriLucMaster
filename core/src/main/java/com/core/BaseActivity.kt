package com.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.utils.DisposeBag
import com.utils.KeyboardUtils
import com.utils.ext.disposedBy
import com.widget.Boast
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.layout_loading_ble.view.*
import kotlinx.android.synthetic.main.layout_loading_dummy.view.image_loading

abstract class BaseActivity : AppCompatActivity() {

    lateinit var loading: AlertDialog
    lateinit var loading2: AlertDialog

    private var isCancelable = false
    private var isCancelable2 = false

    protected val bag by lazy { DisposeBag.create() }

    @LayoutRes
    protected abstract fun getLayoutId(): Int

    @LayoutRes
    open fun getLayoutIdLoading(): Int = -1

    open fun getThemResId(): Int = -1

    protected abstract fun updateUI(savedInstanceState: Bundle?)

    override fun onDestroy() {
        bag.dispose()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        initDialog()
        initDialog2("")
        updateUI(savedInstanceState)
    }

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount
        if (count == 1) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    fun addDispose(vararg disposables: Disposable) {
        bag.add(*disposables)
    }

    @Throws
    open fun openFragment(
        resId: Int,
        fragmentClazz: Class<*>,
        args: Bundle?,
        addBackStack: Boolean
    ) {
        val tag = fragmentClazz.simpleName
        try {
            val isExisted =
                supportFragmentManager.popBackStackImmediate(tag, 0)    // IllegalStateException
            if (!isExisted) {
                val fragment: Fragment
                try {
                    fragment = (fragmentClazz.asSubclass(Fragment::class.java)).newInstance()
                        .apply { arguments = args }

                    val transaction = supportFragmentManager.beginTransaction()
                    transaction.add(resId, fragment, tag)

                    if (addBackStack) {
                        transaction.addToBackStack(tag)
                    }
                    transaction.commitAllowingStateLoss()

                } catch (e: InstantiationException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws
    open fun openFragment(
        resId: Int, fragmentClazz: Class<*>, args: Bundle?, addBackStack: Boolean,
        vararg aniInt: Int
    ) {
        val tag = fragmentClazz.simpleName
        try {
            val isExisted = supportFragmentManager.popBackStackImmediate(tag, 0)
            if (!isExisted) {
                val fragment: Fragment
                try {
                    fragment = (fragmentClazz.asSubclass(Fragment::class.java)).newInstance()
                        .apply { arguments = args }

                    val transaction = supportFragmentManager.beginTransaction()
                    transaction.setCustomAnimations(aniInt[0], aniInt[1], aniInt[2], aniInt[3])

                    transaction.add(resId, fragment, tag)

                    if (addBackStack) {
                        transaction.addToBackStack(tag)
                    }
                    transaction.commitAllowingStateLoss()

                } catch (e: InstantiationException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Show toast
     * @param msg
     */
    fun toast(msg: String) {
        runOnUiThread {
            Boast.showCustom(this, msg)
        }
    }

    fun toast(msg: String, duration: Int, cancelCurrent: Boolean) {
        Boast.makeText(this, msg, duration).show(cancelCurrent)
    }

    /**
     * Init dialog loading
     */
    private fun initDialog() {
        val builder: AlertDialog.Builder = if (getThemResId() != -1)
            AlertDialog.Builder(this, getThemResId()) else AlertDialog.Builder(this)
        builder.setCancelable(isCancelable)
        val view = if (getLayoutIdLoading() == -1) LayoutInflater.from(this).inflate(
            R.layout.layout_loading_dummy, null
        )
        else LayoutInflater.from(this).inflate(
            getLayoutIdLoading(), null
        )
        builder.setView(view)
        with(view) {
            Glide.with(context)
                .asGif()
                .load(R.drawable.loading)
                .into(image_loading)
        }
        loading =
            builder.create()
                .apply { window?.setBackgroundDrawableResource(android.R.color.transparent) }

    }

    /**
     * Init dialog loading 2
     */
    private fun initDialog2(msg: String, isCancelable: Boolean = false) {
        val builder: AlertDialog.Builder = if (getThemResId() != -1)
            AlertDialog.Builder(this, getThemResId()) else AlertDialog.Builder(this)

        builder.setCancelable(isCancelable2 || isCancelable)
        val view = LayoutInflater.from(this).inflate(
            R.layout.layout_loading_ble, null
        )
        builder.setView(view)
        with(view) {
            Glide.with(context)
                .asGif()
                .load(R.drawable.loading)
                .into(image_loading2)
            text_loading?.text = msg
        }
        loading2 = builder.create()
            .apply {
                if (!isCancelable) {
                    window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                }
                window?.setBackgroundDrawableResource(android.R.color.transparent)
            }
    }

    /**
     * Show dialog loading
     */
    open fun showDialog() {
        runOnUiThread {
            if (!loading.isShowing) {
                loading.show()
            }
        }
    }

    /**
     * Show dialog loading 2
     */
    open fun showDialogLoadDataBle(msg: String, isCancelable: Boolean = false) {
        runOnUiThread {
            if (!loading2.isShowing) {
                initDialog2(msg, isCancelable)
                loading2.show()
            }
        }
    }

    /**
     * Hide dialog loading 2
     */
    open fun hideDialogLoadDataBle() {
        runOnUiThread {
            if (loading2.isShowing) {
                loading2.dismiss()
            }
        }
    }

    /**
     * Hide dialog loading
     */
    open fun hideDialog() {
        runOnUiThread {
            if (loading.isShowing) {
                loading.dismiss()
            }
        }
    }

    /**
     * Set cancelable dialog
     */
    fun setCancelableDialog(isCancelable: Boolean) {
        this.isCancelable = isCancelable
    }

    /**
     * Set cancelable dialog 2
     */
    fun setCancelableDialog2(isCancelable: Boolean) {
        this.isCancelable2 = isCancelable
    }

    fun hideKeyboard() {
        KeyboardUtils.hideKeyboard(this)
    }

    fun hideKeyboardOutSide(view: View) {
        KeyboardUtils.hideKeyBoardWhenClickOutSide(view, this)
    }

    fun hideKeyboardOutSideText(view: View) {
        KeyboardUtils.hideKeyBoardWhenClickOutSideText(view, this)
    }

    fun overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
    }

    fun overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }

    fun <VH : RecyclerView.ViewHolder> setUpRcv(
        rcv: RecyclerView,
        adapter: RecyclerView.Adapter<VH>
    ) {
        rcv.setHasFixedSize(true)
        rcv.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        rcv.adapter = adapter
    }

    fun <VH : RecyclerView.ViewHolder> setUpRcv(
        rcv: RecyclerView, adapter:
        RecyclerView.Adapter<VH>,
        isHasFixedSize: Boolean,
        isNestedScrollingEnabled: Boolean
    ) {
        rcv.setHasFixedSize(isHasFixedSize)
        rcv.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        rcv.adapter = adapter
        rcv.isNestedScrollingEnabled = isNestedScrollingEnabled
    }

    fun <VH : RecyclerView.ViewHolder> setUpRcv(
        rcv: RecyclerView, adapter:
        RecyclerView.Adapter<VH>,
        isNestedScrollingEnabled: Boolean
    ) {
        rcv.setHasFixedSize(true)
        rcv.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        rcv.adapter = adapter
        rcv.isNestedScrollingEnabled = isNestedScrollingEnabled
    }

    open fun clearAllBackStack() {
        val fm = supportFragmentManager
        for (i in 0 until fm.backStackEntryCount) {
            fm.popBackStack()
        }
    }

    fun Subject<String>.receiveTextChangesFrom(editText: EditText) {
        RxTextView.textChanges(editText)
            .subscribe { newText -> this.onNext(newText.toString()) }
            .disposedBy(bag)
    }

    fun Subject<Unit>.receiveClicksFrom(view: View) {
        RxView.clicks(view)
            .subscribe { this.onNext(Unit) }
            .disposedBy(bag)
    }
}