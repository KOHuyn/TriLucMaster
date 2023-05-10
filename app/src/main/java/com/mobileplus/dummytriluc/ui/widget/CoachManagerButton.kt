package com.mobileplus.dummytriluc.ui.widget

import android.content.Context
import android.content.res.TypedArray
import android.media.Image
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.core.BaseCustomLayout
import com.mobileplus.dummytriluc.R

class CoachManagerButton(
    context: Context,
    attrs: AttributeSet
) : BaseCustomLayout(context, attrs) {
    @DrawableRes
    var srcImg: Int? = null
    var title: String? = null

    override fun getStyleableId(): IntArray? = R.styleable.CoachManagerButton

    override fun initDataFromStyleable(a: TypedArray) {
        title = a.getString(R.styleable.CoachManagerButton_title)
        srcImg = a.getResourceId(
            R.styleable.CoachManagerButton_srcImg,
            R.drawable.ic_default_image_rectangle
        )
        super.initDataFromStyleable(a)
    }

    override fun getLayoutId(): Int = R.layout.ui_coach_manager_button

    override fun updateUI() {
        findViewById<TextView>(R.id.txtTitleCoachMenu).text = title
        findViewById<ImageView>(R.id.imgSrcCoachMenu).setImageResource(
            srcImg ?: R.drawable.ic_default_image_rectangle
        )
    }

    fun setTitleCoachMenu(title: String) {
        findViewById<TextView>(R.id.txtTitleCoachMenu).text = title
    }

    fun setSrcImgCoachMenu(@DrawableRes src: Int) {
        findViewById<ImageView>(R.id.imgSrcCoachMenu).setImageResource(src)
    }
}