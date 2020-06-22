package com.app.design_motion.ui.eleUI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.design_motion.R
import kotlinx.android.synthetic.main.fragment_ele_food.*
import java.util.*

class EleFoodFragment : Fragment() {
    private var mAdapterEle: EleFoodAdapter? = null
    private var mDatas = ArrayList<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ele_food, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
    }

    private fun initData() {

        for (i in 0..32) {
            mDatas.add(i)
        }
        mAdapterEle = EleFoodAdapter(mDatas)
        food_rv.layoutManager = LinearLayoutManager(context)
        val footerView = layoutInflater.inflate(R.layout.item_food_footer_layout, food_rv, false)
        mAdapterEle?.addFooterView(footerView)
        food_rv.adapter = mAdapterEle
        mAdapterEle?.setOnItemClickListener { adapter, view, position ->
            (activity as EleUIActivity)?.showFoodLayout()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = EleFoodFragment()
    }
}