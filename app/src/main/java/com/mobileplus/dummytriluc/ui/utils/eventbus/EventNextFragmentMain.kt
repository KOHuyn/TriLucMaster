package com.mobileplus.dummytriluc.ui.utils.eventbus

import android.os.Bundle

class EventNextFragmentMain {
    var clazz: Class<*>
    var bundle: Bundle? = null
    var isAddToBackStack: Boolean = true

    constructor(clazz: Class<*>, bundle: Bundle, addToBackStack: Boolean) {
        this.clazz = clazz
        this.bundle = bundle
        this.isAddToBackStack = addToBackStack
    }

    constructor(clazz: Class<*>, addToBackStack: Boolean) {
        this.clazz = clazz
        this.isAddToBackStack = addToBackStack
    }

    constructor(clazz: Class<*>) {
        this.clazz = clazz
    }
}