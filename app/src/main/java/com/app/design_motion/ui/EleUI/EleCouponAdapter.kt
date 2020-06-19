package com.app.design_motion.ui.EleUI

import com.app.design_motion.R
import com.app.design_motion.bean.DataBean
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class EleCouponAdapter() : BaseMultiItemQuickAdapter<DataBean, BaseViewHolder>(dataList) {

    init {
        addItemType(FIRST_TITLE, R.layout.item_text_first_title_layout)
        addItemType(TITLE, R.layout.item_text_title_layout)
        addItemType(COUPON, R.layout.item_text_coupon_layout)
        addItemType(PUBLISH, R.layout.item_text_publish_layout)
        addItemType(VIP, R.layout.item_vip_layout)
    }

    companion object {
        val FIRST_TITLE = 0
        val TITLE = 1
        val COUPON = 2
        val PUBLISH = 3
        val VIP = 4

        val dataList = listOf(
            DataBean(FIRST_TITLE, "优惠", "", "", ""),
            DataBean(COUPON, "", "特价", "特价商品15.5元起", ""),
            DataBean(COUPON, "", "会员", "超级会员领7元无门槛红包", ""),
            DataBean(COUPON, "", "折扣", "折扣商品55着起", ""),
            DataBean(COUPON, "", "限时", "限时秒杀甜品饮料商品", ""),
            DataBean(TITLE, "公告", "", "", ""),
            DataBean(PUBLISH, "", "", "", "春节不打烊，金喜送到家。新品金凤来福鸡排堡、金尊肉酱厚牛堡上线，配上扭扭薯条，等您品尝！"),
            DataBean(TITLE, "店铺会员卡", "", "", ""),
            DataBean(VIP, "", "", "", "")
        )
    }

    override fun convert(helper: BaseViewHolder, item: DataBean) {
        when (item.type) {
            FIRST_TITLE -> {
                helper.setText(R.id.tv_item_text_first_title, item.title)
                helper.addOnClickListener(R.id.iv_item_text_close)
            }
            TITLE -> {
                helper.setText(R.id.tv_item_text_title, item.title);
            }
            COUPON -> {
                helper.setText(R.id.tv_item_text_coupon_title, item.couponTitle)
                helper.setText(R.id.tv_item_text_coupon_title, item.couponContent)
            }
            PUBLISH -> {
                helper.setText(R.id.tv_item_text_publish, item.publishContent);
            }
            else -> {
            }
        }
    }
}
