package com.core

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.utils.DisposeBag
import com.utils.KeyboardUtils
import com.utils.LogUtil
import com.utils.ext.disposedBy
import com.widget.Boast
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.Subject

/**
 * Created by KO Huyn on 04/10/2021.
 */
abstract class BaseFragmentZ<T : ViewBinding> : Fragment(),
    ViewTreeObserver.OnGlobalLayoutListener {

    private var rootView: View? = null

    lateinit var binding: T

    protected abstract fun getLayoutBinding(): T

    protected val bag by lazy { DisposeBag.create() }

    protected abstract fun updateUI(savedInstanceState: Bundle?)

    override fun onGlobalLayout() {
        rootView!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    fun addDispose(vararg disposables: Disposable) {
        bag.add(*disposables)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView != null) {
            val parent = rootView!!.parent as ViewGroup?
            parent?.removeView(rootView)
        } else {
            try {
                binding = getLayoutBinding()
                rootView = binding.root
                rootView!!.viewTreeObserver.addOnGlobalLayoutListener(this)
            } catch (e: InflateException) {
                e.printStackTrace()
            }
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUI(savedInstanceState)
    }

    override fun onDestroy() {
        bag.dispose()
        super.onDestroy()
    }

    fun <VH : RecyclerView.ViewHolder> setUpRcv(
        rcv: RecyclerView,
        adapter: RecyclerView.Adapter<VH>
    ) {
        rcv.setHasFixedSize(true)
        rcv.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        rcv.adapter = adapter
    }

    fun <VH : RecyclerView.ViewHolder> setUpRcv(
        rcv: RecyclerView, adapter:
        RecyclerView.Adapter<VH>,
        isHasFixedSize: Boolean,
        isNestedScrollingEnabled: Boolean
    ) {
        rcv.setHasFixedSize(isHasFixedSize)
        rcv.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        rcv.adapter = adapter
        rcv.isNestedScrollingEnabled = isNestedScrollingEnabled
    }

    fun <VH : RecyclerView.ViewHolder> setUpRcv(
        rcv: RecyclerView, adapter:
        RecyclerView.Adapter<VH>,
        isNestedScrollingEnabled: Boolean
    ) {
        rcv.setHasFixedSize(true)
        rcv.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        rcv.adapter = adapter
        rcv.isNestedScrollingEnabled = isNestedScrollingEnabled
    }

    open fun removeFragmentByTag(tag: String) {
        parentFragmentManager.fragments.map {
            if (it.tag == tag) {
                parentFragmentManager.beginTransaction().remove(it).commit()
                parentFragmentManager.popBackStack()
            }
        }
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
                childFragmentManager.popBackStackImmediate(tag, 0)    // IllegalStateException
            if (!isExisted) {
                val fragment: Fragment
                try {
                    fragment = (fragmentClazz.asSubclass(Fragment::class.java)).newInstance()
                        .apply { arguments = args }

                    val transaction = childFragmentManager.beginTransaction()
                    //transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
                    transaction.add(resId, fragment, tag)

                    if (addBackStack) {
                        transaction.addToBackStack(tag)
                    }
                    transaction.commitAllowingStateLoss()

                } catch (e: java.lang.InstantiationException) {
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
            val isExisted =
                childFragmentManager.popBackStackImmediate(tag, 0)    // IllegalStateException
            if (!isExisted) {
                val fragment: Fragment
                try {
                    fragment = (fragmentClazz.asSubclass(Fragment::class.java)).newInstance()
                        .apply { arguments = args }

                    val transaction = childFragmentManager.beginTransaction()
                    transaction.setCustomAnimations(aniInt[0], aniInt[1], aniInt[2], aniInt[3])

                    transaction.add(resId, fragment, tag)

                    if (addBackStack) {
                        transaction.addToBackStack(tag)
                    }
                    transaction.commitAllowingStateLoss()

                } catch (e: java.lang.InstantiationException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun runOnUiThread(action: () -> Unit) {
        if (this.isAdded) {
            if (this::binding.isInitialized) {
                activity?.runOnUiThread { action() } ?: Handler(Looper.getMainLooper()).post(action)
            }
        }
    }

    fun toast(msg: String) {
        try {
            context?.let {
                Boast.showCustom(it, msg)
            }
        } catch (e: Exception) {
            LogUtil.error("hic", "huhu", e)
        }
    }

    fun toast(msg: String, duration: Int, cancelCurrent: Boolean) {
        context?.let {
            Boast.makeText(it, msg, duration).show(cancelCurrent)
        }
    }

    fun showDialog() {
        activity?.let {
            if (it is BaseActivity) {
                it.showDialog()
            }
        }
    }

    fun hideDialog() {
        activity?.let {
            if (it is BaseActivity) {
                it.hideDialog()
            }
        }
    }

    fun showDialogWithMessage(message: String, isCancelable: Boolean) {
        activity?.let {
            if (it is BaseActivity) {
                it.showDialogLoadDataBle(message, isCancelable)
            }
        }
    }
    fun hideDialogWithMessage() {
        activity?.let {
            if (it is BaseActivity) {
                it.hideDialogLoadDataBle()
            }
        }
    }

    fun hideKeyboard() {
        activity?.let {
            if (it is BaseActivity) {
                it.hideKeyboard()
            }
        }
    }

    fun hideKeyboardOutSide(view: View) {
        activity?.let {
            if (it is BaseActivity) {
                it.hideKeyboardOutSide(view)
            }
        }
    }

    fun hideKeyboardOutSideText(view: View) {
        activity?.let {
            if (it is BaseActivity) {
                it.hideKeyboardOutSideText(view)
            }
        }
    }

    fun onBackPressed() {
        activity?.let { KeyboardUtils.hideKeyboard(it) }
        activity?.onBackPressed()
    }

    open fun clearAllBackStack() {
        activity?.let {
            if (it is BaseActivity) {
                it.clearAllBackStack()
            }
        }
    }

    fun finish() {
        activity?.finish()
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