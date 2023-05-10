package com.mobileplus.dummytriluc.ui.main.chat.chatroom.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemChatRoom
import com.mobileplus.dummytriluc.ui.utils.extensions.OnClickItemAdapter
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import kotlinx.android.synthetic.main.item_chat_room.view.*


/**
 * Created by KO Huyn on 1/12/2021.
 */
class ChatRoomAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var items = mutableListOf<ItemChatRoom>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onItemClick: OnClickItemAdapter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        BaseViewHolder(parent.inflateExt(R.layout.item_chat_room))

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView) {
            imgAvatarChatList.show(item.image)
            txtNameReceiverChatList.text = item.title ?: ""
            txtTimeAgoChatList.setTextNotNull(item.timeAgo)
            txtLastMessageChatList.text = item.message
            clickWithDebounce { onItemClick?.setOnClickListener(it, holder.adapterPosition) }
        }
    }

    override fun getItemCount(): Int = items.size
}