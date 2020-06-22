package com.app.design_motion.bean

import com.chad.library.adapter.base.entity.MultiItemEntity

data class DataBean(
    var type: Int,
    var title: String,
    var couponTitle: String,
    var couponContent: String,
    var publishContent: String
) : MultiItemEntity {
    override val itemType: Int
        get() = type
}

