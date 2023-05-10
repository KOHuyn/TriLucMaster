package com.mobileplus.dummytriluc.ui.dialog

import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseDialog
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.mobileplus.dummytriluc.ui.utils.extensions.showFitXY
import com.utils.applyClickShrink
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import kotlinx.android.synthetic.main.dialog_choose_mode_practice.*
import kotlinx.android.synthetic.main.item_choose_mode_practice.view.*

/**
 * Created by KOHuyn on 3/29/2021
 */
class ChooseModePracticeDialog : BaseDialog() {
    override fun getLayoutId(): Int = R.layout.dialog_choose_mode_practice

    private val adapterChooseMode by lazy { ChooseModeAdapter() }

    override fun updateUI(savedInstanceState: Bundle?) {
        fillData()
        rcvChooseModePractice.run {
            adapter = adapterChooseMode
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            addItemDecoration(MarginItemDecoration(resources.getDimension(R.dimen.space_8).toInt()))
        }
    }

    fun show(fm: FragmentManager): ChooseModePracticeDialog {
        show(fm, this::class.java.simpleName)
        return this
    }

    fun setCallbackChooseMode(callback: (TypeModePractice) -> Unit) {
        adapterChooseMode.chooseModeCallback = ChooseModeCallback { type ->
            dismiss()
            callback.invoke(type)
        }
    }

    private fun fillData() {
        val items = mutableListOf<ItemChooseMode>()
        items.add(
            ItemChooseMode(
                R.drawable.img_thumb_free_fight,
                getString(R.string.free_fight),
                getString(R.string.free_fight_raw),
                TypeModePractice.FREE_FIGHT
            )
        )
        items.add(
            ItemChooseMode(
                R.drawable.img_thumb_according_to_led,
                getString(R.string.according_to_led),
                getString(R.string.according_to_led_raw),
                TypeModePractice.ACCORDING_LED
            )
        )
        items.add(
            ItemChooseMode(
                R.drawable.img_thumb_according_to_course,
                getString(R.string.according_to_course),
                getString(R.string.according_to_course_raw),
                TypeModePractice.COURSE
            )
        )
        adapterChooseMode.items = items
    }

    class ChooseModeAdapter : RecyclerView.Adapter<BaseViewHolder>() {

        var items = mutableListOf<ItemChooseMode>()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        var chooseModeCallback: ChooseModeCallback? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
            BaseViewHolder(parent.inflateExt(R.layout.item_choose_mode_practice))

        override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
            with(holder.itemView) {
                val item = items[position]
                imgThumbChooseModePractice.showFitXY(item.img)
                txtTitleChooseModePractice.setTextNotNull(item.title)
                txtDescriptionChooseModePractice.setTextNotNull(item.description)
                applyClickShrink()
                clickWithDebounce { chooseModeCallback?.setOnChooseModeCallback(item.type) }
            }
        }

        override fun getItemCount(): Int = items.size
    }

    class ItemChooseMode(
        @DrawableRes val img: Int,
        val title: String,
        val description: String,
        val type: TypeModePractice
    )

    fun interface ChooseModeCallback {
        fun setOnChooseModeCallback(type: TypeModePractice)
    }

    enum class TypeModePractice { FREE_FIGHT, ACCORDING_LED, COURSE }
}