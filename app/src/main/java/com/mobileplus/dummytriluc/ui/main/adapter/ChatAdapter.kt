package com.mobileplus.dummytriluc.ui.main.adapter

import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.*
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.*


/**
 * Created by KO Huyn on 1/6/2021.
 */
class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    var chatList: MutableList<ItemChat> = ArrayList()

    var onChatMessageListener: OnChatMessageListener? = null

    companion object {
        private const val TYPE_SENDER_MESSAGE = 1
        private const val TYPE_RECEIVER_MESSAGE = 2
        private const val TYPE_SENDER_VIDEO = 3
        private const val TYPE_RECEIVER_VIDEO = 4
        private const val TYPE_TIME = 5
    }

    fun addChatMore(chats: MutableList<ItemChat>) {
        chatList.addAll(0, chats)
        notifyItemRangeInserted(0, chats.size)
        notifyItemChanged(chats.size)
    }

    fun add(chat: ItemChat) {
        chatList.add(chat)
    }

    fun addChatMessageLocalToList(message: String, timeSendMessage: Long, userInfo: UserInfo?) {
        val chat = ItemChat().apply {
            this.id = timeSendMessage
            this.type = ChatType.MESSAGE
            this.message = message
            this.createdAt = convertTimeStampToCreatedAt(timeSendMessage)
            this.userId = userInfo?.id
            userInfo.let {
                this.userChat = UserChat(it?.id, it?.fullName, it?.avatarPath)
            }
            this.isSend = true
            this.sendStatus = ChatSendStatus.IS_LOADING
        }
        add(chat)
        notifyDataSetChanged()
    }

    fun addChatVideoLocalToList(
        data: DummyResult,
        timeSendMessage: Long,
        videoObjectId: Int,
        userInfo: UserInfo?
    ) {
        val chat = ItemChat().apply {
            this.id = timeSendMessage
            this.type = ChatType.USER_VIDEO
            this.message = ""
            this.createdAt = convertTimeStampToCreatedAt(timeSendMessage)
            this.userId = userInfo?.id
            this.objectId = videoObjectId
            userInfo.let {
                this.userChat = UserChat(it?.id, it?.fullName, it?.avatarPath)
            }
            this.dummyResult = data
            this.isSend = true
            this.sendStatus = ChatSendStatus.IS_LOADING
        }
        add(chat)
        notifyDataSetChanged()
    }

    fun changeStatusChat(idChat: Long, statusSend: ChatSendStatus) {
        chatList.forEach { chat ->
            if (chat.id == idChat) {
                chat.sendStatus = statusSend
                notifyItemChanged(chatList.indexOf(chat))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = chatList[position]
        var type: Int = -1
        if (item.type != null) {
            when (item.type) {
                ChatType.MESSAGE -> {
                    type = if (item.isSend) {
                        TYPE_SENDER_MESSAGE
                    } else {
                        TYPE_RECEIVER_MESSAGE
                    }
                }
                ChatType.USER_VIDEO -> {
                    type = if (item.isSend) {
                        TYPE_SENDER_VIDEO
                    } else {
                        TYPE_RECEIVER_VIDEO
                    }
                }
            }
        }
        return type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return when (viewType) {

            TYPE_SENDER_MESSAGE -> {
                ChatViewHolder(parent.inflateExt(R.layout.item_chat_message_sender))
            }
            TYPE_RECEIVER_MESSAGE -> {
                ChatViewHolder(parent.inflateExt(R.layout.item_chat_message_receiver))
            }
            TYPE_SENDER_VIDEO -> {
                ChatViewHolder(parent.inflateExt(R.layout.item_chat_video_sender))
            }
            TYPE_RECEIVER_VIDEO -> {
                ChatViewHolder(parent.inflateExt(R.layout.item_chat_video_receiver))
            }
            else -> {
                ChatViewHolder(parent.inflateExt(R.layout.item_chat_message_sender))
            }
        }
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        val item = chatList[position]
        holder.txtTimeSystem?.text = item.getTimeDDMMYYYY()
        when {
            position > 0 -> {
                val prevItem = chatList[position - 1]
                if (prevItem.getTimeDDMMYYYY() != item.getTimeDDMMYYYY()) {
                    holder.viewTimeSystem?.show()
                } else {
                    holder.viewTimeSystem?.hide()
                }
            }
            position == 0 -> {
                holder.viewTimeSystem?.show()
            }
        }

        when (viewType) {
            TYPE_SENDER_MESSAGE -> {
                holder.txtMessage?.text = item.message
                when (item.sendStatus) {
                    ChatSendStatus.SEND_SUCCESS -> {
                        holder.imgStatusChat.invisible()
                    }
                    ChatSendStatus.SEND_ERROR -> {
                        holder.imgStatusChat.show()
                        holder.imgStatusChat.setImageResource(R.drawable.ic_chat_error)
                    }
                    ChatSendStatus.IS_LOADING -> {
                        holder.imgStatusChat.show()
                        holder.imgStatusChat.setImageResource(R.drawable.ic_chat_loading)
                    }
                }
            }
            TYPE_RECEIVER_MESSAGE -> {
                with(holder) {
                    if (position > 0) {
                        val prevModel = chatList[position - 1]
                        if (item.userId == prevModel.userId) {
                            lnProfileReceiver?.hide()
                            imgAvatar?.invisible()
                        } else {
                            lnProfileReceiver?.show()
                            imgAvatar?.show()
                        }
                    } else {
                        lnProfileReceiver?.show()
                        imgAvatar?.show()
                    }
                    imgAvatar?.show(item.userChat?.avatarPath)
                    txtName?.text = item.userChat?.fullName ?: "No Name"
                    txtTime?.text = item.getHHMM()
                    txtMessage?.text = item.message
                }
            }
            TYPE_SENDER_VIDEO -> {
                when (item.sendStatus) {
                    ChatSendStatus.SEND_SUCCESS -> {
                        holder.imgStatusChat.invisible()
                    }
                    ChatSendStatus.SEND_ERROR -> {
                        holder.imgStatusChat.show()
                        holder.imgStatusChat.setImageResource(R.drawable.ic_chat_error)
                    }
                    ChatSendStatus.IS_LOADING -> {
                        holder.imgStatusChat.show()
                        holder.imgStatusChat.setImageResource(R.drawable.ic_chat_loading)
                    }
                }
                if (item.dummyResult != null) {
                    holder.imgVideoThumb?.show(item.dummyResult?.videoThumb)
                }
                holder.imgVideoThumb.clickWithDebounce {
                    onChatMessageListener?.onVideoClick(
                        holder.itemView,
                        holder.bindingAdapterPosition
                    )
                }
            }
            TYPE_RECEIVER_VIDEO -> {
                with(holder) {
                    if (position > 0) {
                        val prevModel = chatList[position - 1]
                        if (item.userId == prevModel.userId) {
                            lnProfileReceiver?.hide()
                            imgAvatar?.invisible()
                        } else {
                            lnProfileReceiver?.show()
                            imgAvatar?.show()
                        }
                    } else {
                        lnProfileReceiver?.show()
                        imgAvatar?.show()
                    }
                    imgAvatar?.show(item.userChat?.avatarPath)
                    txtName?.text = item.userChat?.fullName ?: "No Name"
                    txtTime?.text = item.getHHMM()
                    if (item.dummyResult != null) {
                        holder.imgVideoThumb.show(item.dummyResult?.videoThumb)
                    }
                }
                holder.imgVideoThumb?.clickWithDebounce {
                    onChatMessageListener?.onVideoClick(
                        holder.itemView,
                        holder.bindingAdapterPosition
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int = chatList.size

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAvatar = itemView.findViewById<ImageView>(R.id.chatAvatar)
        val txtName = itemView.findViewById<TextView>(R.id.chatName)
        val txtTime = itemView.findViewById<TextView>(R.id.chatTime)
        val txtMessage = itemView.findViewById<TextView>(R.id.chatContent)
        val imgVideoThumb = itemView.findViewById<ImageView>(R.id.chatVideo)
        val lnProfileReceiver = itemView.findViewById<View>(R.id.chatProfileReceive)
        val viewTimeSystem = itemView.findViewById<View>(R.id.viewTimeSystem)
        val txtTimeSystem = itemView.findViewById<TextView>(R.id.chatTimeSystem)
        val imgStatusChat = itemView.findViewById<ImageView>(R.id.chatStatus)
    }

    interface OnChatMessageListener {
        fun onVideoClick(view: View, position: Int)
    }

}