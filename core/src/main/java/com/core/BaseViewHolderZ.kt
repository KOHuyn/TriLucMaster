package com.core

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * Created by KO Huyn on 04/10/2021.
 */
class BaseViewHolderZ<V : ViewBinding>(val binding: V) : RecyclerView.ViewHolder(binding.root)