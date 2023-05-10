package com.mobileplus.dummytriluc.ui.main.practice.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.core.BaseViewHolderZ
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.*
import com.mobileplus.dummytriluc.databinding.*
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.mobileplus.dummytriluc.ui.utils.extensions.showFitXY
import com.utils.applyClickShrink
import com.utils.ext.clickWithDebounce

class PracticeAdapter : RecyclerView.Adapter<BaseViewHolderZ<ViewBinding>>() {
    var items = mutableListOf<BasePracticeItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onItemChildClick: OnItemClickPractice? = null
    override fun getItemViewType(position: Int): Int {
        return when (items[position].getType()) {
            BasePracticeItem.TYPE_HEADER -> BasePracticeItem.TYPE_HEADER
            BasePracticeItem.TYPE_BODY -> BasePracticeItem.TYPE_BODY
            else -> BasePracticeItem.TYPE_BODY
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolderZ<ViewBinding> {
        return when (viewType) {
            BasePracticeItem.TYPE_HEADER -> {
                BaseViewHolderZ(
                    ItemPracticeTitleMoreBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            BasePracticeItem.TYPE_BODY -> {
                BaseViewHolderZ(
                    ItemPracticeContentBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> {
                BaseViewHolderZ(
                    ItemPracticeContentBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolderZ<ViewBinding>, position: Int) {
        when (getItemViewType(position)) {
            BasePracticeItem.TYPE_HEADER -> {
                with(holder as BaseViewHolderZ<ItemPracticeTitleMoreBinding>) {
                    val item = items[position] as ItemTitlePractice
                    binding.txtMorePractice.text = loadStringRes(R.string.more)
                    binding.txtTitlePractice.text = item.title
                    binding.txtMorePractice.clickWithDebounce {
                        onItemChildClick?.setOnItemClickPractice(
                            item,
                            BasePracticeItem.TYPE_HEADER
                        )
                    }
                }
            }
            BasePracticeItem.TYPE_BODY -> {
                val itemContentAdapter by lazy { AdapterItemContent() }
                with(holder as BaseViewHolderZ<ItemPracticeContentBinding>) {
                    val item = items[position] as ItemPracticeContent
                    itemContentAdapter.items = item.list
                    binding.rcvPracticeContent.run {
                        if (itemDecorationCount < 1) {
                            addItemDecoration(
                                MarginItemDecoration(
                                    resources.getDimension(R.dimen.space_8).toInt(),
                                    isHorizontalRecyclerView = true
                                )
                            )
                        }
                        setHasFixedSize(true)
                        layoutManager =
                            if (item.isLocal)
                                GridLayoutManager(context, 2)
                            else LinearLayoutManager(
                                context,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        adapter = itemContentAdapter
                    }
                    itemContentAdapter.clickItem =
                        OnItemClickPractice { itemChild, typeView ->
                            onItemChildClick?.setOnItemClickPractice(itemChild, typeView)
                        }
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}


class AdapterItemContent : RecyclerView.Adapter<BaseViewHolderZ<ViewBinding>>() {

    var clickItem: OnItemClickPractice? = null

    var items = mutableListOf<BasePracticeItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemViewType(position: Int): Int {
        return when (items[position].getType()) {
            BasePracticeItem.TYPE_CHILD_MASTER -> BasePracticeItem.TYPE_CHILD_MASTER
            BasePracticeItem.TYPE_CHILD_LOCAL -> BasePracticeItem.TYPE_CHILD_LOCAL
            BasePracticeItem.TYPE_CHILD_PRACTICE -> {
                return when ((items[position] as ItemPracticeItemContent).type) {
                    ItemPracticeItemContent.TYPE_FOLDER -> BasePracticeItem.TYPE_FOLDER_PRACTICE
                    else -> BasePracticeItem.TYPE_SINGLE_PRACTICE
                }
            }
            else -> BasePracticeItem.TYPE_SINGLE_PRACTICE

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolderZ<ViewBinding> {
        return when (viewType) {
            BasePracticeItem.TYPE_SINGLE_PRACTICE -> BaseViewHolderZ(
                ItemPracticeItemContentBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            BasePracticeItem.TYPE_FOLDER_PRACTICE -> BaseViewHolderZ(
                ItemPracticeFolderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            BasePracticeItem.TYPE_CHILD_MASTER -> BaseViewHolderZ(
                ItemPracticeMasterBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            BasePracticeItem.TYPE_CHILD_LOCAL -> BaseViewHolderZ(
                ItemPracticeLocalBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            else -> BaseViewHolderZ(
                ItemPracticeItemContentBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolderZ<ViewBinding>, position: Int) {
        when (val typeView = getItemViewType(position)) {
            BasePracticeItem.TYPE_SINGLE_PRACTICE -> {
                val item = items[position] as ItemPracticeItemContent
                with(holder as BaseViewHolderZ<ItemPracticeItemContentBinding>) {
                    binding.imgPracticeThumb.show(item.img)
                    binding.txtTitleItemPractice.text = item.title
                    binding.txtNameSubjectPractice.setTextNotNull(item.subject?.title)
                    binding.root.clickWithDebounce {
                        clickItem?.setOnItemClickPractice(
                            item,
                            typeView
                        )
                    }
                }
            }
            BasePracticeItem.TYPE_FOLDER_PRACTICE -> {
                val item = items[position] as ItemPracticeItemContent
                with(holder as BaseViewHolderZ<ItemPracticeFolderBinding>) {
                    binding.imgFolderPracticeThumb.show(item.img)
                    binding.imgFolderPracticeThumbClone1.show(item.img)
                    binding.imgFolderPracticeThumbClone2.show(item.img)
                    binding.txtNameSubjectPracticeFolder.setTextNotNull(item.subject?.title)
                    binding.txtFolderTitleItemPractice.text = item.title
                    binding.root.clickWithDebounce {
                        clickItem?.setOnItemClickPractice(
                            item,
                            typeView
                        )
                    }
                }
            }
            BasePracticeItem.TYPE_CHILD_MASTER -> {
                val item = items[position] as ItemPracticeItemMaster
                with(holder as BaseViewHolderZ<ItemPracticeMasterBinding>) {
                    binding.imgMasterPractice.show(item.imgMaster)
                    binding.txtNameMasterPractice.text = item.nameMaster
                    binding.txtSubjectMasterPractice.setTextNotNull(
                        item.subject?.title ?: "Unknown"
                    )
                    binding.txtCountPractice.setTextNotNull(item.getCountPracticeMaster())
                    binding.txtCountDisciple.setTextNotNull(item.getCountDiscipleMaster())
                    binding.root.clickWithDebounce {
                        clickItem?.setOnItemClickPractice(
                            item,
                            typeView
                        )
                    }
                }
            }
            BasePracticeItem.TYPE_CHILD_LOCAL -> {
                val item = items[position] as ItemPracticeLocal
                with(holder as BaseViewHolderZ<ItemPracticeLocalBinding>) {
                    binding.imgThumbChooseModePractice.showFitXY(item.imgPath)
                    binding.txtTitleChooseModePractice.setTextNotNull(item.title)
                    binding.txtDescriptionChooseModePractice.setTextNotNull(item.description)
                    binding.root.applyClickShrink()
                    binding.root.clickWithDebounce {
                        clickItem?.setOnItemClickPractice(item, typeView)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}

fun interface OnItemClickPractice {
    fun setOnItemClickPractice(item: BasePracticeItem, type: Int)
}

