package com.mobileplus.dummytriluc.transceiver.ext

import org.json.JSONObject

/**
 * Created by KO Huyn on 22/06/2023.
 */

inline fun <reified T> JSONObject.getOrNull(key: String): T? {
    return if (this.has(key)) {
        this.get(key) as? T
    } else null
}