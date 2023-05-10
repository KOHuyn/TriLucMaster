package com.mobileplus.dummytriluc.ui.main.chat.chatmessage

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseFragment
import com.core.BaseFragmentZ
import com.google.gson.Gson
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemChat
import com.mobileplus.dummytriluc.data.model.ItemChatRoom
import com.mobileplus.dummytriluc.data.model.ItemDiscipleGroup
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.data.request.ChatSendRequest
import com.mobileplus.dummytriluc.databinding.FragmentChatMessageBinding
import com.mobileplus.dummytriluc.socket.SocketHelper
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.main.adapter.ChatAdapter
import com.mobileplus.dummytriluc.ui.main.coach.group.detail.CoachGroupDetailFragment
import com.mobileplus.dummytriluc.ui.main.practice.detail.PracticeDetailFragment
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventReloadRoomChat
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.mobileplus.dummytriluc.ui.video.result.VideoResultActivity
import com.utils.ext.*
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

/**
 * Created by KO Huyn on 1/12/2021.
 */
class ChatMessageFragment : BaseFragmentZ<FragmentChatMessageBinding>() {
    override fun getLayoutBinding(): FragmentChatMessageBinding =
        FragmentChatMessageBinding.inflate(layoutInflater)

    private val chatMessageViewModel by viewModel<ChatMessageViewModel>()
    private val gson by inject<Gson>()
    private val rcvChat by lazy { binding.rcvChatMessage }
    private val btnSend by lazy { binding.btnSendMessage }
    private val adapter by lazy { ChatAdapter() }
    private lateinit var socketHelper: SocketHelper
    private var lastMessageId = AppConstants.INTEGER_DEFAULT
    private var isLoadMoreMsg: Boolean = false
    private lateinit var linearLayoutManage: LinearLayoutManager
    private lateinit var dataChatRoom: ItemChatRoom

    companion object {
        const val KEY_DATA_CHAT_ROOM_ARG = "key_data_chat_room_argument"
        fun openFragment(item: ItemChatRoom, gson: Gson) {
            val bundle = Bundle().apply {
                putString(
                    KEY_DATA_CHAT_ROOM_ARG,
                    gson.toJson(item)
                )
            }
            postNormal(EventNextFragmentMain(ChatMessageFragment::class.java, bundle, true))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyBoardWhenClick(rcvChat, binding.btnBackChatMessage)
    }

    private fun hideKeyBoardWhenClick(vararg view: View) {
        for (vieww in view) {
            vieww.setOnTouchListener { v, _ ->
                hideKeyboard()
                try {
                    v.performClick()
                } catch (e: Exception) {
                    e.logErr()
                }

                if (binding.txtContentMessage?.isFocused == true) {
                    binding.txtContentMessage?.clearFocus()
                    true
                } else false
            }
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        dataChatRoom =
            gson.fromJson(argument(KEY_DATA_CHAT_ROOM_ARG, "").value, ItemChatRoom::class.java)
        setupSocketIO()
        callbackViewModel(chatMessageViewModel)
        getChatApi()
        configView()
        nextInformationPractice()
        configSendMessage()
        setupRcvMessage()
    }

    private fun getChatApi() {
        if (dataChatRoom.roomId != null) {
            chatMessageViewModel.getChatRoom(roomId = dataChatRoom.roomId!!)
        } else {
            Handler(Looper.getMainLooper()).postDelayed({ onBackPressed() }, 500)
        }
    }

    private fun setupSocketIO() {
        socketHelper = SocketHelper((activity as MainActivity).getSocket())
            .setSocketID(dataChatRoom.roomKeyId ?: "")
            .build()
            .setOnCallbackSocket {
                try {
                    val itemChat = gson.fromJson(it[0].toString(), ItemChat::class.java)
                    val idProfile = chatMessageViewModel.dataManager.getUserInfo()?.id ?: 0
                    itemChat.isSend = itemChat.userId == idProfile
                    if (itemChat.userId != idProfile) {
                        addItemChat(itemChat)
                    }
                    //when switch to screen diff and update chat
                    if (isCurrentScreenChat().not() && itemChat.userId == idProfile) {
                        addItemChat(itemChat)
                    }
                } catch (e: Exception) {
                    logErr(e.message ?: "")
                }
            }
    }

    private fun nextInformationPractice() {
        binding.btnInformationPractice.clickWithDebounce {
            dataChatRoom.objectId?.let { idPractice ->
                when (dataChatRoom.objectType) {
                    ItemChatRoom.TYPE_CLASS -> {
                        CoachGroupDetailFragment.openFragment(
                            false,
                            ItemDiscipleGroup(
                                idPractice,
                                dataChatRoom.title,
                                dataChatRoom.roomId,
                                dataChatRoom.roomKeyId,
                                null,
                                false
                            ), gson
                        )
                    }
                    ItemChatRoom.TYPE_PRACTICE -> {
                        PracticeDetailFragment.openFragment(idPractice)
                    }
                    null -> Unit
                }
            }
        }
    }

    private fun configView() {
        binding.txtHeaderMessage.setTextNotNull(dataChatRoom.title)
        binding.avatarMessage.show(dataChatRoom.image)
        binding.btnBackChatMessage.clickWithDebounce { onBackPressed() }
    }

    private fun setupRcvMessage() {
        linearLayoutManage = LinearLayoutManager(requireContext())
        linearLayoutManage.stackFromEnd = true
        rcvChat.layoutManager = linearLayoutManage
        rcvChat.adapter = adapter
        rcvChat.addItemDecoration(
            MarginItemDecoration(resources.getDimension(R.dimen.space_4).toInt())
        )
        rcvChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!isLoadMoreMsg && linearLayoutManage.findFirstVisibleItemPosition() in 0..4) {
                    isLoadMoreMsg = true
                    loadMore()
                }
            }
        })
        adapter.onChatMessageListener = object : ChatAdapter.OnChatMessageListener {
            override fun onVideoClick(view: View, position: Int) {
                val item = adapter.chatList[position].dummyResult
                if (item != null) {
                    if (item.videoPath != null && item.dataVideo != null && item.startTime2 != null) {
                        val bundle = Bundle().apply {
                            putString(VideoResultActivity.PRACTICE_FAQ_URL, item.videoPath)
                            putString(VideoResultActivity.PRACTICE_FAQ_DATA, item.dataVideo)
                            putLong(
                                VideoResultActivity.PRACTICE_FAQ_START_TIME,
                                item.startTime2 ?: -1
                            )
                        }
                        startActivity(VideoResultActivity::class.java, bundle)
                    } else {
                        toast(loadStringRes(R.string.error_not_find_video))
                    }
                } else {
                    toast(loadStringRes(R.string.error_not_find_video))
                }
            }
        }
    }

    private fun callbackViewModel(vm: ChatMessageViewModel) {
        addDispose(
            vm.isLoading.subscribe {
                binding.loadingChatMessage.root.setVisibility(it)
            }
        )
        addDispose(vm.rxStatusSendMessage.subscribe {
            val idChat = it.first
            val status = it.second
            logErr("$idChat - ${status.name}")
            adapter.changeStatusChat(idChat, status)
        })
        addDispose(vm.rxMessage.subscribe { toast(it) })
        addDispose(vm.rxChats.subscribe {
            if (it.isNotEmpty()) {
                lastMessageId = it[0].id?.toInt() ?: AppConstants.INTEGER_DEFAULT
                if (adapter.chatList.isEmpty()) {
                    adapter.chatList = it.toMutableList()
                    adapter.notifyItemRangeInserted(0, it.size)
                } else {
                    adapter.addChatMore(it.toMutableList())
                    isLoadMoreMsg = false
                }
            }
        })
    }

    fun isCurrentScreenChat(): Boolean {
        if (activity is MainActivity) {
            if ((activity as MainActivity).currentFrag() == this) {
                return true
            }
        }
        return false
    }

    private fun scrollToBottom() {
        rcvChat.post {
            rcvChat.smoothScrollToPosition(adapter.itemCount - 1)
        }
    }

    private fun configSendMessage() {
        addDispose(
            RxTextView.textChanges(binding.txtContentMessage)
                .debounce(200, TimeUnit.MICROSECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { content ->
                    btnSend.isEnabled = content.isNotEmpty()
                    btnSend.alpha = if (content.isNotEmpty()) 1f else 0.5f
                }
        )

        btnSend.clickWithDebounce {
            val message = binding.txtContentMessage.text.toString()
            val chat =
                ChatSendRequest(roomId = dataChatRoom.roomId ?: 0, message = message)
            val timeSendMessage = System.currentTimeMillis() / 1000
            chatMessageViewModel.sendChat(chat, timeSendMessage)
            adapter.addChatMessageLocalToList(
                message, timeSendMessage,
                chatMessageViewModel.userInfo
            )
            scrollToBottom()
            binding.txtContentMessage.editableText.clear()
        }
    }


    private fun addItemChat(chat: ItemChat) {
        requireActivity().runOnUiThread {
            adapter.add(chat)
            adapter.notifyDataSetChanged()
            scrollToBottom()
        }
    }

    fun loadMore() {
        if (lastMessageId != AppConstants.INTEGER_DEFAULT) {
            dataChatRoom.roomId?.let {
                if (isNoInternet()) return
                chatMessageViewModel.getChatRoom(
                    it,
                    lastMessageId,
                    ApiConstants.UP
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        socketHelper.destroy()
    }
}