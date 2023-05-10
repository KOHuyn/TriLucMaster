package com.mobileplus.dummytriluc.ui.widget

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.*
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.utils.UIHelper
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import com.utils.ext.setTextColorz
import kotlinx.android.synthetic.main.layout_item_spinner.view.*
import kotlinx.android.synthetic.main.layout_spinner.view.*

class CustomSpinner constructor(private val view: View, private val context: Context) {

    private var onSelectedItem: OnSelectedItemSpinner? = null
    private var items: List<SpinnerItem> = emptyList()
    private var popupWindow = PopupWindow(context)
    private val adapterSpinner by lazy { SpinnerAdapter(textSize) }

    private var isShowUp = false

    @Dimension
    private var widthWindow = 0

    @Dimension
    private var textSize = view.resources.getDimension(R.dimen.text_14)

    @ColorInt
    private var backgroundColor: Int = Color.parseColor("#6B6B6B")

    fun setDataSource(itemsSpinner: List<SpinnerItem>): CustomSpinner {
        this.items = itemsSpinner
        adapterSpinner.items = itemsSpinner.toMutableList()
        return this
    }

    fun setBackGroundSpinner(@ColorInt color: Int): CustomSpinner {
        this.backgroundColor = color
        return this
    }

    fun setTextSize(@Dimension textSize: Float): CustomSpinner {
        this.textSize = textSize
        return this
    }

    fun setTextColor(@ColorRes textColor: Int): CustomSpinner {
        adapterSpinner.textColor = textColor
        return this
    }


    fun setShowUp(isShowUp: Boolean = false): CustomSpinner {
        this.isShowUp = isShowUp
        return this
    }

    fun setWidthWindow(@Dimension width: Float): CustomSpinner {
        this.widthWindow = width.toInt()
        return this
    }

    fun build(): CustomSpinner {
        val layout: View = LayoutInflater.from(context).inflate(
            R.layout.layout_spinner,
            LinearLayout(context)
        )
        with(layout) {
            body.setBackgroundColor(backgroundColor)
            recyclerViewSpinner.run {
                adapter = adapterSpinner
                layoutManager = LinearLayoutManager(context)
            }
        }
        popupWindow.run {
            contentView = layout
            width = if (widthWindow != 0) {
                widthWindow
            } else {
                view.width
            }
            this.height = WindowManager.LayoutParams.WRAP_CONTENT
            isOutsideTouchable = true
            isFocusable = true
            setBackgroundDrawable(null)
        }
        popupWindow.contentView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val heightScreen = UIHelper.getScreenHeight(context)
        val out = IntArray(2)
        view.getLocationOnScreen(out)
        val partScreen = (heightScreen * 2) / 3
        if (out[1] > partScreen || isShowUp) {
            popupWindow.animationStyle = R.style.StyleDropUpSpinner
            popupWindow.showAsDropDown(
                view,
                0,
                -view.height - popupWindow.contentView.measuredHeight
            )
        } else {
            popupWindow.animationStyle = R.style.StyleDropDownSpinner
            popupWindow.showAsDropDown(view)
        }
        adapterSpinner.onSelectedItem = object : OnSelectedItemSpinner {
            override fun setOnSelectedItem(position: Int, item: SpinnerItem) {
                if (view is TextView) {
                    view.text = item.name
                }
                onSelectedItem?.setOnSelectedItem(position, item)
                popupWindow.dismiss()
            }
        }
        return this
    }

    fun setOnSelectedItemCallback(listener: (item: SpinnerItem) -> Unit) {
        onSelectedItem = object : OnSelectedItemSpinner {
            override fun setOnSelectedItem(position: Int, item: SpinnerItem) {
                listener.invoke(item)
            }
        }
    }

    class SpinnerAdapter(private val textSize: Float) :
        RecyclerView.Adapter<BaseViewHolder>() {
        var items = mutableListOf<SpinnerItem>()
            set(value) {
                field = value
                notifyDataSetChanged()
            }
        var onSelectedItem: OnSelectedItemSpinner? = null

        @ColorRes
        var textColor: Int = R.color.white
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
            BaseViewHolder(parent.inflateExt(R.layout.layout_item_spinner))

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
            with(holder.itemView) {
                txtTitleSpinner.setTextColorz(textColor)
                txtTitleSpinner.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                txtTitleSpinner.text = items[position].name
                clickWithDebounce {
                    onSelectedItem?.setOnSelectedItem(
                        position,
                        items[position]
                    )
                }
            }
        }
    }

    interface OnSelectedItemSpinner {
        fun setOnSelectedItem(position: Int, item: SpinnerItem)
    }

    data class SpinnerItem(val name: String, val id: String)

}