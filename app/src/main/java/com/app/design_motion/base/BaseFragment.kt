package com.app.design_motion.base

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.app.design_motion.common.getStatusBarHeight
import com.app.design_motion.R

open class BaseFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupToolbarMargin(view)
    }

    private fun setupToolbarMargin(view: View) {
        val toolbar = view.findViewById<View?>(R.id.toolbar)
        toolbar?.let {
            val statusBarHeight = context?.getStatusBarHeight()
            val lp = it.layoutParams as ViewGroup.LayoutParams
            lp.height += statusBarHeight as Int
            if (it is CardView) {
                it.setContentPadding(0, statusBarHeight, 0, 0)
            } else {
                it.updatePadding(top = statusBarHeight)
            }
        }
    }
}