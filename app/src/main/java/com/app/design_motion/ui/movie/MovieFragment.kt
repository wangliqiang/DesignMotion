package com.app.design_motion.ui.movie

import android.animation.AnimatorInflater
import android.animation.ValueAnimator
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.design_motion.R
import com.app.design_motion.base.BaseFragment
import com.app.design_motion.common.*
import com.app.design_motion.common.listener.BottomNavigationViewListener
import com.app.design_motion.data.model.Subject
import com.app.design_motion.ui.home.HomeFragment
import kotlinx.android.synthetic.main.fragment_movie.*

class MovieFragment : BaseFragment(), MovieAdapter.ViewHolder.Delegate {

    private lateinit var movieViewModel: MovieViewModel
    private lateinit var movieAdapter: MovieAdapter
    var bottomNavListener: BottomNavigationViewListener? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        movieViewModel =
                ViewModelProvider(this).get(MovieViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_movie, container, false)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (activity is BottomNavigationViewListener) {
            bottomNavListener = activity as BottomNavigationViewListener
        } else {
            throw ClassCastException("$activity must implement BottomNavigationViewListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()

        if (savedInstanceState == null) {
            root.doOnLayout {
                toolbar.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(400)
                    .start()
                bottomNavListener?.showBottomNavigationView()
            }
        } else {
            toolbar.alpha = 1f
            toolbar.translationY = 0f
        }
        initData()
    }


    private fun initData(){
        movieViewModel.loadInTheaters()
    }

    private fun setupViews() {
        supportsLollipop {
            details_toolbar_transition_helper.transitionName = TRANSITION_TOOLBAR
        }
        details_toolbar_transition_helper.translationY = -resources.getDimension(R.dimen.details_toolbar_container_height)
        toolbar.translationY = -toolbar.context.getToolbarHeight().toFloat()

        val elevation = resources.getDimension(R.dimen.toolbar_elevation)

        movieAdapter = MovieAdapter(this)
        with(recycler_view) {
            adapter = movieAdapter
            setHasFixedSize(true)
            val lm = layoutManager as LinearLayoutManager
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 0) bottomNavListener?.hideBottomNavigationView()
                    if (dy < 0) bottomNavListener?.showBottomNavigationView()

                    if (lm.findFirstCompletelyVisibleItemPosition() == 0) {
                        if (toolbar.cardElevation == 0f) return
                        animateToolbarElevation(true)
                    } else {
                        if (toolbar.cardElevation > 0f) return
                        toolbar.cardElevation = elevation
                    }
                }
            })
        }

        movieViewModel.inTheaters.observe(viewLifecycleOwner, Observer {
            movieAdapter.submitList(it?.subjects)
        })

    }

    private fun animateToolbarElevation(animateOut: Boolean) {
        var valueFrom = resources.getDimension(R.dimen.toolbar_elevation)
        var valueTo = 0f
        if (!animateOut) {
            valueTo = valueFrom
            valueFrom = 0f
        }
        ValueAnimator.ofFloat(valueFrom, valueTo).setDuration(250).apply {
            startDelay = 0
            addUpdateListener { toolbar.cardElevation = it.animatedValue as Float }
            start()
        }
    }

    override fun onItemClick(view: View, subject: Subject) {
        val fragmentTransaction = initFragmentTransaction(view, subject)
        val copy = view.copyViewImage()
        copy.y += toolbar.height
        root.addView(copy)
        view.visibility = View.INVISIBLE
        startAnimation(copy, fragmentTransaction)
    }

    private fun startAnimation(view: View, fragmentTransaction: FragmentTransaction?) {
        AnimatorInflater.loadAnimator(activity, R.animator.main_list_animator).apply {
            setTarget(recycler_view)
            withStartAction { if (toolbar.cardElevation > 0) animateToolbarElevation(true) }
            withEndAction {
                recycler_view.visibility = View.INVISIBLE

                val toY = view.resources.getDimensionPixelOffset(R.dimen.details_toolbar_container_height) - view.height / 2f

                view.animate().y(toY).start()

                toolbar.animate()
                    .translationY(-toolbar.height.toFloat())
                    .alpha(0f)
                    .setDuration(600)
                    .withStartAction {
                        bottomNavListener?.hideBottomNavigationView()
                        details_toolbar_transition_helper.animate().translationY(0f).setDuration(500).start()
                    }
                    .withEndAction {
                        fragmentTransaction?.commitAllowingStateLoss()
                    }
                    .start()
            }
            start()
        }
    }

    private fun initFragmentTransaction(view: View, subject: Subject): FragmentTransaction? {
        val toY = resources.getDimension(R.dimen.details_toolbar_container_height) - view.height / 2f

        val positions = FloatArray(3)
        positions[0] = view.x
        positions[1] = view.y + toolbar.height
        positions[2] = toY

        val adapterPosition = recycler_view.getChildAdapterPosition(view)
        val detailFragment = MovieDetailsFragment.newInstance(positions, adapterPosition, subject)

        val transaction = parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, detailFragment, MovieDetailsFragment.TAG)
            .addToBackStack(null)

        supportsLollipop {
            val transition = TransitionInflater.from(context)
                .inflateTransition(R.transition.shared_element_transition)
            detailFragment.sharedElementEnterTransition = transition

            transaction.addSharedElement(view, view.transitionName)
                .addSharedElement(
                    details_toolbar_transition_helper,
                    details_toolbar_transition_helper.transitionName
                )
        }

        return transaction
    }

    companion object{
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}