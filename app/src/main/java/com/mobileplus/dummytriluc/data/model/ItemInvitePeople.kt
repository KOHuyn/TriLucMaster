package com.mobileplus.dummytriluc.data.model

data class ItemInvitePeople(
    val avatar: String,
    val name: String,
    val id: Int,
    var isInvite: Boolean = false
) {
    fun getIdInvite() = "id: $id"
}