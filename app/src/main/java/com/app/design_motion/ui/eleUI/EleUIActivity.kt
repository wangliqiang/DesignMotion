package com.app.design_motion.ui.eleUI

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.app.design_motion.R
import com.app.design_motion.widget.FoodNestedScrollLayout
import com.app.design_motion.widget.NestedScrollLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.jaeger.library.StatusBarUtil
import kotlinx.android.synthetic.main.activity_ele_ui.*
import kotlinx.android.synthetic.main.ele_header_layout.*
import kotlinx.android.synthetic.main.ele_slide_layout.*
import kotlinx.android.synthetic.main.ele_top_bar_layout.*

class EleUIActivity : AppCompatActivity() {

    val titles = listOf("点餐", "评价", "商家")
    var adapterEle: EleCouponAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ele_ui)

        initData()
        initEvent()
    }


    private fun initEvent() {

        adapterEle?.setOnItemChildClickListener { adapter, view, position ->
            nested_scroll_layout.restore(ANIM_DURATION_FRACTION)
        }

        rv_collasped.layoutManager = LinearLayoutManager(this)
        rv_collasped.adapter = adapterEle

        viewPager2.adapter = object : FragmentStateAdapter(supportFragmentManager, lifecycle) {
            override fun getItemCount(): Int = 3

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> EleFoodFragment.newInstance()
                    1 -> TabFragment.newInstance("评价页面")
                    else -> TabFragment.newInstance("商家页面")
                }
            }
        }
        TabLayoutMediator(tabLayout, viewPager2,
            TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                tab.text = titles[position]
            }).attach()

        iv_back.setOnClickListener {
            finish()
        }

        tv_coupon_count.setOnClickListener {
            nested_scroll_layout.expand(ANIM_DURATION_FRACTION)
        }

        nested_scroll_layout.setProgressUpdateListener(object :
            NestedScrollLayout.ProgressUpdateListener {
            override fun onUpCollapsedContentTransProUpdate(float: Float) {
            }

            override fun onUpAlphaScaleProUpdate(float: Float) {
            }

            override fun onUpAlphaGradientProUpdate(float: Float) {
                if (float > 0.5f) {
                    StatusBarUtil.setLightMode(this@EleUIActivity)
                } else {
                    StatusBarUtil.setDarkMode(this@EleUIActivity)
                }
            }

            override fun onDownCollapsedAlphaProUpdate(float: Float) {
            }

            override fun onDownContentAlphaProUpdate(float: Float) {
            }

            override fun onDownShopBarTransProUpdate(float: Float) {
            }
        })

        food_nested_scroll_layout.setProgressUpdateListener(object :
            FoodNestedScrollLayout.ProgressUpdateListener {
            override fun onDownContentCloseProUpdate(pro: Float) {
                nested_scroll_layout.scaleX = 0.9f + (pro * 0.1f)
                nested_scroll_layout.scaleY = 0.9f + (pro * 0.1f)
                v_mask.alpha = 1 - pro
                if (pro == 1f) {
                    v_mask.visibility = View.GONE
                } else {
                    v_mask.visibility = View.VISIBLE
                }
            }
        })

        iv_small_close.setOnClickListener {
            food_nested_scroll_layout.close(ANIM_DURATION_FRACTION)
        }
        v_mask.setOnClickListener {
            food_nested_scroll_layout.close(ANIM_DURATION_FRACTION)
        }
        iv_food_expand.setOnClickListener {
            food_nested_scroll_layout.expand(ANIM_DURATION_FRACTION)
        }
        iv_small_close.isClickable = false
        v_mask.isClickable = false
        iv_food_expand.isClickable = false
    }

    private fun initData() {
        adapterEle = EleCouponAdapter()
    }

    private var ANIM_DURATION_FRACTION: Long = 200

    fun showFoodLayout() {
        food_nested_scroll_layout.restore(ANIM_DURATION_FRACTION)
    }
}