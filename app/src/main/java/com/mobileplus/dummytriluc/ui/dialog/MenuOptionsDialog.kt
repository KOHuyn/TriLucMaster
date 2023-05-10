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
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import com.utils.ext.setDrawableStart
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.dialog_menu_options.*
import kotlinx.android.synthetic.main.item_menu_option.view.*

/**
 * Created by KOHuyn on 3/23/2021
 */
class MenuOptionsDialog : BaseDialog() {
    override fun getLayoutId(): Int = R.layout.dialog_menu_options

    private val adapterMenu by lazy { MenuOptionAdapter() }

    private val arrItemMenu = mutableListOf<ItemMenuOption>()

    private var onCallbackItemListener: OnSelectedItemListener? = null

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        adapterMenu.items = arrItemMenu
        rcvMenuOptionDialog.run {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = adapterMenu
        }
        adapterMenu.onSelectedItemListener = OnSelectedItemListener {
            onCallbackItemListener?.setOnSelectedItemListener(it)
        }
    }

    class Builder {
        private val menuOption by lazy { MenuOptionsDialog() }

        fun addMenuOption(
            @DrawableRes iconStart: Int,
            name: String,
            type: MenuOption
        ): Builder {
            menuOption.arrItemMenu.add(ItemMenuOption(iconStart, name, type))
            return this
        }

        fun build(fragmentManager: FragmentManager): MenuOptionsDialog {
            menuOption.show(fragmentManager, this::class.java.simpleName)
            return menuOption
        }
    }


    fun setOnCallbackListener(onCallback: (type: MenuOption) -> Unit) {
        onCallbackItemListener = OnSelectedItemListener {
            dismiss()
            onCallback.invoke(it)
        }
    }

    inner class MenuOptionAdapter : RecyclerView.Adapter<BaseViewHolder>() {

        var items = mutableListOf<ItemMenuOption>()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        internal var onSelectedItemListener: OnSelectedItemListener? = null
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
            BaseViewHolder(parent.inflateExt(R.layout.item_menu_option))

        override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
            val item = items[position]
            with(holder.itemView) {
                viewLineMenuOption.setVisibility(position != itemCount - 1)
                txtNameItemMenuOption.setDrawableStart(item.iconStart)
                txtNameItemMenuOption.text = item.name
                clickWithDebounce { onSelectedItemListener?.setOnSelectedItemListener(item.type) }
            }
        }

        override fun getItemCount(): Int = items.size

    }

    fun interface OnSelectedItemListener {
        fun setOnSelectedItemListener(type: MenuOption)
    }

    data class ItemMenuOption(
        @DrawableRes val iconStart: Int,
        val name: String,
        val type: MenuOption
    )

    enum class MenuOption { CREATE_FOLDER, RENAME_FOLDER,MOVE_FOLDER,RENAME_DRAFT,EDITOR_FOLDER,EDITOR_EXERCISE,DELETE }
}