package com.app.design_motion.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.navigation.Navigation
import com.app.design_motion.R
import com.app.design_motion.base.BaseFragment
import com.app.design_motion.common.listener.BottomNavigationViewListener
import com.app.design_motion.ui.eleUI.EleUIActivity
import com.jaeger.library.StatusBarUtil
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : BaseFragment() {

    var bottomNavListener: BottomNavigationViewListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        StatusBarUtil.setTranslucentForImageView(activity, 0, null)
        if (activity is BottomNavigationViewListener) {
            bottomNavListener = activity as BottomNavigationViewListener
        } else {
            throw ClassCastException("$activity must implement BottomNavigationViewListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            root.doOnLayout {
                bottomNavListener?.showBottomNavigationView()
            }
        }

        elm.setOnClickListener {
            startActivity(Intent(context, EleUIActivity::class.java))
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}