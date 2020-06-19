package com.app.design_motion.ui.EleUI

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.app.design_motion.R
import com.app.design_motion.widget.ElemeNestedScrollLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.jaeger.library.StatusBarUtil
import kotlinx.android.synthetic.main.ele_header_layout.*
import kotlinx.android.synthetic.main.ele_slide_layout.*
import kotlinx.android.synthetic.main.ele_top_bar_layout.*
import kotlinx.android.synthetic.main.fragment_ele_ui.*

class EleUIFragment : Fragment() {

    val titles = listOf("点餐", "评价", "商家")
    var adapterEle: EleCouponAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_ele_ui, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initEvent()
    }

    private fun initEvent() {

        adapterEle?.setOnItemChildClickListener { adapter, view, position ->
            eleme_nested_scroll_layout.restore(ANIM_DURATION_FRACTION)
        }

        rv_collasped.layoutManager = LinearLayoutManager(context)
        rv_collasped.adapter = adapterEle

        viewPager2.adapter = object : FragmentStateAdapter(childFragmentManager, lifecycle) {
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
            Navigation.findNavController(it).popBackStack()
        }

        tv_coupon_count.setOnClickListener {
            eleme_nested_scroll_layout.expand(ANIM_DURATION_FRACTION)
        }

        eleme_nested_scroll_layout.setProgressUpdateListener(object :
            ElemeNestedScrollLayout.ProgressUpdateListener {
            override fun onUpCollapsedContentTransProUpdate(float: Float) {
            }

            override fun onUpAlphaScaleProUpdate(float: Float) {
            }

            override fun onUpAlphaGradientProUpdate(float: Float) {
                if (float > 0.5f) {
                    StatusBarUtil.setLightMode(activity)
                } else {
                    StatusBarUtil.setDarkMode(activity)
                }
            }

            override fun onDownCollapsedAlphaProUpdate(float: Float) {
            }

            override fun onDownContentAlphaProUpdate(float: Float) {
            }

            override fun onDownShopBarTransProUpdate(float: Float) {
            }
        })

    }

    private fun initData() {
        adapterEle = EleCouponAdapter()
    }

    fun dp2px(dpVal: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpVal, resources.displayMetrics
        ).toInt()
    }

    var ANIM_DURATION_FRACTION: Long = 200

    fun showFoodLayout() {
        eleme_nested_scroll_layout.restore(ANIM_DURATION_FRACTION)
    }
}