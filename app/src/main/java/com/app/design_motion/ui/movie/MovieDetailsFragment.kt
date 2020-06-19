package com.app.design_motion.ui.movie

import android.animation.AnimatorInflater
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.api.load
import com.app.design_motion.R
import com.app.design_motion.base.BaseFragment
import com.app.design_motion.common.TRANSITION_CARD
import com.app.design_motion.common.TRANSITION_TOOLBAR
import com.app.design_motion.common.listener.OnBackPressedListener
import com.app.design_motion.common.supportsLollipop
import com.app.design_motion.common.withEndAction
import com.app.design_motion.data.model.Subject
import com.google.common.base.Strings
import kotlinx.android.synthetic.main.fragment_movie_detail.*
import kotlinx.android.synthetic.main.fragment_movie_detail.toolbar
import kotlinx.android.synthetic.main.fragment_movie.*
import kotlinx.android.synthetic.main.item_card.*

class MovieDetailsFragment : BaseFragment(), OnBackPressedListener {

    companion object {
        private lateinit var coordinates: FloatArray
        private var adapterPosition: Int = 0
        private lateinit var subject: Subject

        const val TAG = "DetailsFragment"

        fun newInstance(
            coordinates: FloatArray,
            adapterPosition: Int,
            subject: Subject
        ): MovieDetailsFragment {
            this.subject = subject
            this.coordinates = coordinates
            this.adapterPosition = adapterPosition

            return MovieDetailsFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_movie_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        detail_toolbar.let {
            it.title = subject.title
            it.setTitleTextColor(Color.WHITE)
            it.setNavigationOnClickListener { onBackPressed() }
        }

        setupViews(adapterPosition)

        if (savedInstanceState == null) {
            animateToolbar()
        } else {
            toolbar.alpha = 1f
        }
    }

    private fun setupViews(position: Int) {

        supportsLollipop {
            details_card.transitionName = TRANSITION_CARD + position
            toolbar_container.transitionName = TRANSITION_TOOLBAR
        }
        (details_card.layoutParams as ViewGroup.MarginLayoutParams).topMargin =
            coordinates[2].toInt()

        item_title.text = subject.title
        img_card.load(subject.images.large) {
            crossfade(true)
            allowHardware(false)
        }

        val pubCountry = StringBuffer()
        for (i in subject.pubdates.indices) {
            subject.pubdates[i].indexOf("(")
            pubCountry.append(
                subject.pubdates[i].substring(
                    subject.pubdates[i].indexOf("(") + 1,
                    subject.pubdates[i].indexOf(")")
                ) + " "
            )
        }

        val genres = StringBuffer()
        for (i in subject.genres.indices) {
            genres.append(subject.genres[i] + " ")
        }

        val casts = StringBuffer()
        for (i in subject.casts.indices) {
            casts.append(subject.casts[i].name + " ")
        }

        item_description.text = (
                subject.year + " / " + pubCountry.toString() + " / " + genres.toString() + (if (subject.directors.isEmpty()) " " else " / " +
                        subject.directors[0].name) + if (Strings.isNullOrEmpty(casts.toString())) " " else " / $casts"
                )
    }

    override fun onBackPressed() {
        animateViewOut()
    }

    private fun animateToolbar(alphaTo: Float = 1f, duration: Long = 200) {
        toolbar.animate().alpha(alphaTo).setDuration(duration).start()
    }

    private fun animateViewOut() {
        AnimatorInflater.loadAnimator(activity, R.animator.main_list_animator).apply {
            setTarget(recycler_view)
            start()
        }
            .withEndAction {
                activity?.supportFragmentManager?.popBackStack()
            }
        animateToolbar(0f, 200)
    }

}