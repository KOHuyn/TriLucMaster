package com.mobileplus.dummytriluc.bluetooth.request

import com.google.gson.Gson
import com.mobileplus.dummytriluc.bluetooth.BluetoothResponse
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.data.response.LevelPractice
import org.json.JSONArray
import org.json.JSONObject
import org.koin.core.KoinComponent
import org.koin.core.inject

object TransferBluetoothData : KoinComponent {
    private val gson by inject<Gson>()
    fun transferDataArrayToDataString(
        request: List<BluetoothResponse>,
        level: LevelPractice?
    ): String? {
        val dataTransform = JSONArray(gson.toJson(request))
        val arrFail = mutableListOf<Int>()
        for (i in 0 until dataTransform.length()) {
            val dataBleObj: JSONObject = dataTransform.getJSONObject(i)
            if (dataBleObj.has(ApiConstants.DATA)) {
                dataBleObj.remove(ApiConstants.DATA)
                if (request[i].data.isEmpty()) {
                    if (request[i].error == null) {
                        arrFail.add(i)
                    } else {
                        dataBleObj.put(ApiConstants.DATA, gson.toJson(request[i].data))
                    }
                } else {
                    dataBleObj.put(ApiConstants.DATA, gson.toJson(request[i].data))
                }
            }
            level?.value?.let {
                dataBleObj.put(ApiConstants.LEVEL_VALUE, it)
            }
        }
        arrFail.forEach {
            dataTransform.remove(it)
        }
        return if (dataTransform.length() == 0) null else dataTransform.toString()
    }
}