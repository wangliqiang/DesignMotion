package com.app.design_motion.ui.EleUI

import com.app.design_motion.R
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class EleFoodAdapter(data: List<Int>) :
    BaseQuickAdapter<Int, BaseViewHolder>(R.layout.item_food_laypout, data) {

    override fun convert(helper: BaseViewHolder?, item: Int?) {
    }
}