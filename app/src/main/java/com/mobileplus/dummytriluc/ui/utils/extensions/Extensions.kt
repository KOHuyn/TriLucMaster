package com.mobileplus.dummytriluc.ui.utils.extensions


import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.core.BaseViewModel
import com.mobileplus.dummytriluc.DummyTriLucApplication
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.utils.GlideApp
import com.mobileplus.dummytriluc.ui.utils.UrlImageParser
import com.utils.LogUtil
import com.utils.UIHelper
import com.utils.ext.isConnectedInternet
import com.utils.ext.setVisibility
import com.widget.Boast
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

fun ImageView.showFitXY(url: String?) {
    GlideApp.with(DummyTriLucApplication.getInstance())
        .load(url)
        .into(this)
}

fun ImageView.show(url: String?) {
    val requestOptions = RequestOptions()
        .error(R.drawable.ic_default_image_rectangle)
        .placeholder(R.drawable.ic_default_image_rectangle)
        .centerCrop()
    GlideApp.with(DummyTriLucApplication.getInstance())
        .load(url).apply(requestOptions)
        .into(this)
}


fun ImageView.showCenterInside(url: String?) {
    val requestOptions = RequestOptions()
        .placeholder(R.color.clr_tab)
        .error(R.drawable.ic_default_image_rectangle)
        .centerInside()
    GlideApp.with(DummyTriLucApplication.getInstance())
        .load(url).apply(requestOptions)
        .transition(DrawableTransitionOptions.withCrossFade())
        .override(UIHelper.getScreenWidth(DummyTriLucApplication.getInstance()), 300)
        .into(this)
}

fun ImageView.showCenterInsideFullHeight(url: String?) {
    val requestOptions = RequestOptions()
        .error(R.drawable.ic_default_image_rectangle)
        .placeholder(R.color.clr_tab)
        .centerInside()
    GlideApp.with(DummyTriLucApplication.getInstance())
        .load(url).apply(requestOptions)
        .transition(DrawableTransitionOptions.withCrossFade())
        .override(
            UIHelper.getScreenWidth(DummyTriLucApplication.getInstance()),
            UIHelper.getScreenHeight(DummyTriLucApplication.getInstance())
        )
        .into(this)
}

fun CircleImageView.show(url: String?) {
    val requestOptions = RequestOptions()
        .error(R.drawable.ic_default_image_circle)
        .placeholder(R.drawable.ic_default_image_circle)
        .centerCrop()
    GlideApp.with(DummyTriLucApplication.getInstance())
        .load(url).apply(requestOptions)
        //            .transition(withCrossFade())
//            .override(400, 300)
        .into(this)
}

fun ImageView.show(url: Int) {
    val requestOptions = RequestOptions()
        .error(R.drawable.ic_default_image_rectangle)
        .centerCrop()
    GlideApp.with(DummyTriLucApplication.getInstance())
        .load(url).apply(requestOptions)
        .override(300, 300)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun ImageView.showFitXY(url: Int) {
    val requestOptions = RequestOptions()
        .error(R.drawable.ic_default_image_rectangle)
    GlideApp.with(DummyTriLucApplication.getInstance())
        .load(url).apply(requestOptions)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun ImageView.setPathLocalImage(path: String, applyCircle: Boolean = false) {
    val file = File(path)
    val glide = GlideApp.with(this).load(file)
    if (applyCircle) {
        glide.apply(RequestOptions.circleCropTransform()).into(this)
    } else {
        glide.into(this)
    }
}

fun String.replaceViToEn(): String {
    var str = this
    str = str.replace("[àáạảãâầấậẩẫăằắặẳẵ]".toRegex(), "a")
    str = str.replace("[èéẹẻẽêềếệểễ]".toRegex(), "e")
    str = str.replace("[ìíịỉĩ]".toRegex(), "i")
    str = str.replace("[òóọỏõôồốộổỗơờớợởỡ]".toRegex(), "o")
    str = str.replace("[ùúụủũưừứựửữ]".toRegex(), "u")
    str = str.replace("[ỳýỵỷỹ]".toRegex(), "y")
    str = str.replace("đ".toRegex(), "d")

    str = str.replace("[ÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴ]".toRegex(), "A")
    str = str.replace("[ÈÉẸẺẼÊỀẾỆỂỄ]".toRegex(), "E")
    str = str.replace("[ÌÍỊỈĨ]".toRegex(), "I")
    str = str.replace("[ÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠ]".toRegex(), "O")
    str = str.replace("[ÙÚỤỦŨƯỪỨỰỬỮ]".toRegex(), "U")
    str = str.replace("[ỲÝỴỶỸ]".toRegex(), "Y")
    str = str.replace("Đ".toRegex(), "D")
    return str
}

fun String.clearSpace(): String {
    return this.replace("\n", "").replace("\t", "").replace("\r", "").replace(" ", "")
}

fun String.replaceStringToIntOrNull(): Int? {
    return this.replace("[^0-9.]".toRegex(), "").toIntOrNull()
}

fun interface OnClickItemAdapter {
    fun setOnClickListener(view: View, position: Int)
}

fun Fragment.popBackStackDelay(delay: Long = 500L) {
    Handler(Looper.getMainLooper()).postDelayed({ this.activity?.onBackPressed() }, delay)
}

fun TextView.setTextNotNull(text: String?) {
    if (text == null || text == "" || text == "null") {
        this.visibility = View.GONE
    } else {
        this.text = text.toString()
        this.visibility = View.VISIBLE
    }
}

fun setVisibleViewWhen(vararg view: View, isVisible: (Boolean) -> Boolean) {
    for (v in view) {
        v.setVisibility(isVisible.invoke(true))
    }
}

fun SwipeRefreshLayout.applyColorRefresh() {
    this.setColorSchemeResources(
        R.color.clr_gradient_orange_start,
        R.color.clr_gradient_purple_start,
        R.color.clr_gradient_orange_end,
        R.color
            .clr_gradient_purple_end
    )
}

fun Fragment.toast(msg: String) {
    context?.let {
        Boast.makeText(it, msg).show()
    }
}

fun Fragment.isNoInternet(): Boolean {
    if (!isConnectedInternet()) {
        toast(loadStringRes(R.string.no_internet))
        return true
    }
    return false
}

fun convertToMoney(money: Any): String {
    val formatter: NumberFormat = DecimalFormat("#,###")
    return formatter.format(money)
}

fun TextView.fillGradientPrimary() {
    val textShader: Shader = LinearGradient(
        0f,
        0f,
        180f,
        0f,
        intArrayOf(Color.parseColor("#FF0000"), Color.parseColor("#FF7A00")),
        floatArrayOf(0f, 1f),
        Shader.TileMode.CLAMP
    )
    this.paint.shader = textShader
}

fun TextView.clearDrawable() {
    this.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
}

fun EditText.clearDrawable() {
    this.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
}

fun loadStringRes(@StringRes resId: Int): String {
    return DummyTriLucApplication.getInstance().getString(resId)
}

fun Any.logErr(msg: String) {
    LogUtil.error(this.javaClass.simpleName, msg)
}

fun Any.logErr(msg: String, throwable: Throwable) {
    LogUtil.error(this.javaClass.simpleName, msg, throwable)
}

fun Throwable.logErr() {
    LogUtil.error("Exception", "", this)
}

fun Any.logInfo(msg: String) {
    LogUtil.info(this.javaClass.simpleName, msg)
}

fun Any.logDebug(msg: String) {
    LogUtil.debug(this.javaClass.simpleName, msg)
}

fun Any.logWarning(msg: String) {
    LogUtil.warning(this.javaClass.simpleName, msg)
}

fun TextView.setHtml(htmlText: String) {
    text = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY)
}

fun TextView.setHtmlWithImage(htmlText: String, context: Context) {
    text = HtmlCompat.fromHtml(
        htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY,
        UrlImageParser(context, this),
        null
    )
}

fun Activity.makeStatusBarTransparent() {
    window.apply {
        window.apply {
            setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
    }
}

fun Activity.clearStatusBarFullScreen() {
    window.apply {
        clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
    }
}

fun Activity.setStatusBarColor(@ColorRes color: Int) {
    window.statusBarColor = ContextCompat.getColor(this, color)
}

fun View.setMarginTop(marginTop: Int) {
    val menuLayoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
    menuLayoutParams.setMargins(
        menuLayoutParams.marginStart,
        marginTop,
        menuLayoutParams.marginEnd,
        menuLayoutParams.bottomMargin
    )
    this.layoutParams = menuLayoutParams
}

fun View.setMargins(start: Int = -1, top: Int = -1, end: Int = -1, bottom: Int = -1) {
    val menuLayoutParams = this.layoutParams as ViewGroup.MarginLayoutParams

    val marginStart = if (start == -1) menuLayoutParams.marginStart else start
    val marginTop = if (top == -1) menuLayoutParams.topMargin else top
    val marginEnd = if (end == -1) menuLayoutParams.marginEnd else end
    val marginBottom = if (bottom == -1) menuLayoutParams.bottomMargin else bottom
    menuLayoutParams.setMargins(
        marginStart,
        marginTop,
        marginEnd,
        marginBottom
    )
    this.layoutParams = menuLayoutParams
}

fun View.setMarginTopBottom(top: Int, bottom: Int) {
    val menuLayoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
    menuLayoutParams.setMargins(0, top, 0, bottom)
    this.layoutParams = menuLayoutParams
}

fun View.setMarginBottom(marginBottom: Int) {
    val menuLayoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
    menuLayoutParams.setMargins(0, 0, 0, marginBottom)
    this.layoutParams = menuLayoutParams
}

inline fun View.showIf(condition: () -> Boolean): View {
    if (visibility != View.VISIBLE && condition()) {
        visibility = View.VISIBLE
    }
    return this
}

inline fun View.hideIf(predicate: () -> Boolean): View {
    if (visibility != View.INVISIBLE && predicate()) {
        visibility = View.INVISIBLE
    }
    return this
}

inline fun View.removeIf(predicate: () -> Boolean): View {
    if (visibility != View.GONE && predicate()) {
        visibility = View.GONE
    }
    return this
}

fun TextView.convertCalendar(date: Calendar, format: String) {
    val inputDateFormat = SimpleDateFormat(format)
    this.text = inputDateFormat.format(date.time)
}

fun TextView.convertDateInOut(input: String, inputFormat: String, outFormat: String): Date? {
    val inputDateFormat = SimpleDateFormat(inputFormat)
    val outDateFormat = SimpleDateFormat(outFormat)
    val inputDate = inputDateFormat.parse(input)
    inputDate?.let {
        val output = outDateFormat.format(inputDate)
        this.text = output
        return inputDate
    }
    return null
}

fun View.getBitmap(): Bitmap {
    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    draw(canvas)
    canvas.save()
    return bmp
}

fun String.isPhone(): Boolean {
    val p = "^0([0-9])\\d{8}\$".toRegex()
    return matches(p)
}

fun String.isLengthPhone(): Boolean {
    return length == 10 || length == 11
}

fun String.isEmail(): Boolean {
    val p = "^(\\w)+(\\.\\w+)*@(\\w)+((\\.\\w+)+)\$".toRegex()
    return matches(p)
}

fun Editable.replaceAll(newValue: String) {
    replace(0, length, newValue)
}

fun Editable.replaceAllIgnoreFilters(newValue: String) {
    val currentFilters = filters
    filters = emptyArray()
    replaceAll(newValue)
    filters = currentFilters
}

val EditText.value
    get() = text.toString()

fun EditText.setValue(msg: String) {
    text = Editable.Factory.getInstance().newEditable(msg)
}

fun AppCompatActivity.logErr(msg: String) {
    LogUtil.error(this::class.java.simpleName, msg)
}

fun Service.logErr(msg: String) {
    LogUtil.error(this::class.java.simpleName, msg)
}

fun BaseViewModel<*>.logErr(msg: String) {
    LogUtil.error(this.javaClass.simpleName, msg)
}


fun AppCompatActivity.logErr(tag: String, msg: String) {
    LogUtil.error(tag, msg)
}

fun Service.logErr(tag: String, msg: String) {
    LogUtil.error(tag, msg)
}

fun BaseViewModel<*>.logErr(tag: String, msg: String) {
    LogUtil.error(tag, msg)
}

fun Any.logErr(tag: String, msg: String) {
    LogUtil.error(tag, msg)
}

fun Fragment.removeLastFragmentInBackStack(container: Int) {
    val fm = childFragmentManager
    val count = fm.backStackEntryCount
    for (i in 0 until count) {
        fm.popBackStackImmediate()
        break
    }
}

val deviceId
    @SuppressLint("HardwareIds")
    get() = Settings.Secure.getString(
        DummyTriLucApplication.getInstance().applicationContext!!.contentResolver,
        Settings.Secure.ANDROID_ID
    )

fun logErrFull(tag: String, msg: String) {
    for (i in 0..msg.length step 4000) {
        if (i > msg.length - 4000) {
            LogUtil.error(tag, msg.substring(i, msg.length))
        } else {
            LogUtil.error(msg.substring(i, i + 4000))
        }
    }
}