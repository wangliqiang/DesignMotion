package com.app.design_motion.ui.eleUI

import com.app.design_motion.R
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class EleFoodAdapter(data: List<Int>) :
    BaseQuickAdapter<Int, BaseViewHolder>(R.layout.item_food_laypout, data.toMutableList()) {

    override fun convert(holder: BaseViewHolder, item: Int) {

    }
}