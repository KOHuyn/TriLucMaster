package com.mobileplus.dummytriluc.ui.main.notification.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolderZ
import com.mobileplus.dummytriluc.data.model.NotificationObjService
import com.mobileplus.dummytriluc.databinding.ItemNotificationBinding
import com.mobileplus.dummytriluc.ui.utils.extensions.OnClickItemAdapter
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.utils.ext.*

/**
 * Created by KOHuyn on 2/22/2021
 */
class NotificationAdapter : RecyclerView.Adapter<BaseViewHolderZ<ItemNotificationBinding>>() {

    var items = mutableListOf<NotificationObjService>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onItemClick: OnClickItemAdapter? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolderZ<ItemNotificationBinding> =
        BaseViewHolderZ(
            ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: BaseViewHolderZ<ItemNotificationBinding>, position: Int) {
        val item = items[position]
        with(holder) {
            binding.txtTitleNotification.setTextNotNull(item.title)
            binding.txtContentNotification.setTextNotNull(item.content)
            binding.txtDateCreatedNotification.setTextNotNull(item.getDateCreated())
            if (item.isRead()) binding.imgIsReadNotification.invisible() else binding.imgIsReadNotification.show()
            binding.root.clickWithDebounce {
                onItemClick?.setOnClickListener(it, position)
                if (!item.isRead()) {
                    item.status = 2
                    notifyItemChanged(position)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}