package com.mobileplus.dummytriluc.ui.main.coach.group.detail.message

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseFragment
import com.google.gson.Gson
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemChat
import com.mobileplus.dummytriluc.data.model.ItemDiscipleGroup
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.data.request.ChatSendRequest
import com.mobileplus.dummytriluc.socket.SocketHelper
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.main.adapter.ChatAdapter
import com.mobileplus.dummytriluc.ui.main.coach.group.detail.CoachGroupDetailFragment
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.extensions.isNoInternet
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.mobileplus.dummytriluc.ui.video.result.VideoResultActivity
import com.utils.ext.argument
import com.utils.ext.clickWithDebounce
import com.utils.ext.setVisibility
import com.utils.ext.startActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_coach_group_detail_message.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

/**
 * Created by KOHuyn on 3/12/2021
 */
class CoachGroupDetailMessageFragment : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_coach_group_detail_message
    private val chatMessageViewModel by viewModel<CoachGroupDetailMessageViewModel>()
    private val gson by inject<Gson>()
    val groupInfo: ItemDiscipleGroup? by lazy { (requireParentFragment() as CoachGroupDetailFragment).groupInfo }
    private val rcvChat by lazy { rcvChatMessage }
    private val btnSend by lazy { btnSendMessage }
    private val adapter by lazy { ChatAdapter() }
    private lateinit var socketHelper: SocketHelper
    private var lastMessageId = AppConstants.INTEGER_DEFAULT
    private var isLoadMoreMsg: Boolean = false
    private lateinit var linearLayoutManage: LinearLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyBoardWhenClick(rcvChat)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    private fun hideKeyBoardWhenClick(vararg view: View) {
        for (vieww in view) {
            vieww.setOnTouchListener { v, _ ->
                v.performClick()
                hideKeyboard()
                if (txtContentMessage?.isFocused == true) {
                    txtContentMessage?.clearFocus()
                }
                false
            }
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        setupSocketIO()
        callbackViewModel(chatMessageViewModel)
        if (groupInfo?.roomId != null) {
            chatMessageViewModel.getChatRoom(roomId = groupInfo?.roomId!!)
        } else {
            Handler(Looper.getMainLooper()).postDelayed({ onBackPressed() }, 500)
        }
        configSendMessage()
        setupRcvMessage()
    }

    private fun setupSocketIO() {
        socketHelper = SocketHelper((activity as MainActivity).getSocket())
            .setSocketID(groupInfo?.roomKeyId ?: "")
            .build()
            .setOnCallbackSocket {
                try {
                    val itemChat = gson.fromJson(it[0].toString(), ItemChat::class.java)
                    val idProfile = chatMessageViewModel.dataManager.getUserInfo()?.id ?: 0
                    itemChat.isSend = itemChat.userId == idProfile
                    if (itemChat.userId != idProfile) {
                        addItemChat(itemChat)
                    }
                } catch (e: Exception) {
                    logErr(e.message ?: "")
                }
            }
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

    private fun callbackViewModel(vm: CoachGroupDetailMessageViewModel) {
        addDispose(
            vm.isLoading.subscribe {
                loadingChatMessage.setVisibility(it)
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
                    adapter.notifyDataSetChanged()
                } else {
                    adapter.addChatMore(it.toMutableList())
                    isLoadMoreMsg = false
                }
            }
        })
    }

    private fun scrollToBottom() {
        rcvChat.post {
            rcvChat.smoothScrollToPosition(adapter.itemCount - 1)
        }
    }

    private fun configSendMessage() {
        addDispose(
            RxTextView.textChanges(txtContentMessage)
                .debounce(200, TimeUnit.MICROSECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { content ->
                    btnSend.isEnabled = content.isNotEmpty()
                    btnSend.alpha = if (content.isNotEmpty()) 1f else 0.5f
                }
        )

        btnSend.clickWithDebounce {
            val message = txtContentMessage.text.toString()
            val chat =
                ChatSendRequest(roomId = groupInfo?.roomId ?: 0, message = message)
            val timeSendMessage = System.currentTimeMillis() / 1000
            chatMessageViewModel.sendChat(chat, timeSendMessage)
            adapter.addChatMessageLocalToList(
                message, timeSendMessage,
                chatMessageViewModel.userInfo
            )
            scrollToBottom()
            txtContentMessage.editableText.clear()
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
            groupInfo?.roomId?.let {
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
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }
}