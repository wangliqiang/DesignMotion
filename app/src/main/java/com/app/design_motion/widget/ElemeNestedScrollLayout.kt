package com.app.design_motion.widget

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.math.MathUtils
import androidx.core.view.NestedScrollingChild2
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.app.design_motion.R
import kotlinx.android.synthetic.main.ele_header_layout.view.*
import kotlinx.android.synthetic.main.ele_shop_cart_layout.view.*
import kotlinx.android.synthetic.main.ele_slide_layout.view.*
import kotlinx.android.synthetic.main.ele_top_bar_layout.view.*

class ElemeNestedScrollLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), NestedScrollingChild2 {

    val ANIM_DURATION_FRACTION: Long = 200

    private var progressUpdateListener: ProgressUpdateListener? = null

    private var nestedScrollingParentHelper: NestedScrollingParentHelper
    private var iconArgbEvaluator: ArgbEvaluator //返回icon、拼团icon颜色渐变的Evaluator
    private var topBarArgbEvaluator: ArgbEvaluator //topbar颜色渐变的Evaluator
    private var reboundedAnim: ValueAnimator //回弹动画
    private var restoreOrExpandAnimator: ValueAnimator // 收起或展开折叠内容时执行的动画

    private var topBarHeight = 0f //topBar高度
    private var shopBarHeight = 0f //shopBar高度
    private var contentTransY = 0f //滑动内容初始化TransY
    private var upAlphaScaleY = 0f //上滑时logo，收藏icon缩放、搜索icon、分享icon透明度临界值
    private var upAlphaGradientY = 0f //上滑时搜索框、topBar背景，返回icon、拼团icon颜色渐变临界值
    private var downFlingCutOffY = 0f //从折叠状态下滑产生fling时回弹到初始状态的临界值
    private var downCollapsedAlphaY = 0f //下滑时折叠内容透明度临界值
    private var downShopBarTransY = 0f //下滑时购物内容位移临界值
    private var downContentAlphaY = 0f//下滑时收起按钮和滑动内容透明度临界值
    private var downEndY = 0f//下滑时终点值


    init {
        nestedScrollingParentHelper = NestedScrollingParentHelper(this)
        iconArgbEvaluator = ArgbEvaluator()
        topBarArgbEvaluator = ArgbEvaluator()

        reboundedAnim = ValueAnimator()
        reboundedAnim.interpolator = DecelerateInterpolator()
        reboundedAnim.addUpdateListener {
            translation(cl_content, it.getAnimatedValue() as Float)

            // 根据upAlphaScalePro，设置logo、收藏icon缩放，搜索icon、分享icon透明度
            val upAlphaScalePro = getUpAlphaScalePro()
            alphaScaleByPro(upAlphaScalePro)

            //根据upAlphaGradientPro,设置topBar背景、返回icon、拼团icon颜色渐变值，搜索框透明度
            val upAlphaGradientPro = getUpAlphaGradientPro()
            alphaGradientByPro(upAlphaGradientPro)

            //根据downCollapsedAlphaPro,设置折叠内容透明度
            val downCollapsedAlphaPro = getDownCollapsedAlphaPro()
            alphaCollapsedContentByPro(downCollapsedAlphaPro)

            //根据downShopBarTransPro,设置购物内容内容位移
            val downShopBarTransPro = getDownShopBarTransPro()
            transShopBarByPro(downShopBarTransPro)

            //根据upCollapsedContentTransPro,设置折叠内容位移
            val upCollapsedContentTransPro = getUpCollapsedContentTransPro()
            transCollapsedContentByPro(upCollapsedContentTransPro)

            if (progressUpdateListener != null) {
                progressUpdateListener?.onUpAlphaScaleProUpdate(upAlphaScalePro)
                progressUpdateListener?.onUpAlphaGradientProUpdate(upAlphaGradientPro)
                progressUpdateListener?.onDownCollapsedAlphaProUpdate(downCollapsedAlphaPro)
                progressUpdateListener?.onDownShopBarTransProUpdate(downShopBarTransPro)
                progressUpdateListener?.onUpCollapsedContentTransProUpdate(
                    upCollapsedContentTransPro
                )
            }

        }

        reboundedAnim.duration = ANIM_DURATION_FRACTION

        restoreOrExpandAnimator = ValueAnimator()
        restoreOrExpandAnimator.interpolator = AccelerateInterpolator()
        restoreOrExpandAnimator.addUpdateListener {
            translation(cl_content, it.animatedValue as Float)

            //根据downShopBarTransPro,设置购物内容内容位移
            val downShopBarTransPro = getDownShopBarTransPro()
            transShopBarByPro(downShopBarTransPro)

            //根据downCollapsedAlphaPro,设置折叠内容透明度
            val downCollapsedAlphaPro = getDownCollapsedAlphaPro()
            alphaCollapsedContentByPro(downCollapsedAlphaPro)

            val downContentAlphaPro = getDownContentAlphaPro()
            alphaContentByPro(downContentAlphaPro)

            if (progressUpdateListener != null) {
                progressUpdateListener?.onDownCollapsedAlphaProUpdate(downCollapsedAlphaPro)
                progressUpdateListener?.onDownContentAlphaProUpdate(downContentAlphaPro)
                progressUpdateListener?.onDownShopBarTransProUpdate(downShopBarTransPro)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 设置滑动内容View的高度
        topBarHeight = cl_top_bar.measuredHeight.toFloat()
        val layoutParams = cl_content.layoutParams
        layoutParams.height = (measuredHeight - topBarHeight).toInt()
        // 重新测量
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        shopBarHeight = resources.getDimension(R.dimen.shop_bar_height)
        contentTransY = resources.getDimension(R.dimen.content_trans_y)
        downShopBarTransY = contentTransY + shopBarHeight
        upAlphaScaleY = contentTransY - dp2px(32f)
        upAlphaGradientY = contentTransY - dp2px(64f)
        downFlingCutOffY = contentTransY + dp2px(28f)
        downCollapsedAlphaY = contentTransY + dp2px(32f)
        downContentAlphaY = resources.getDimension(R.dimen.down_content_alpha_y)
        downEndY = height - resources.getDimension(R.dimen.iv_close_height)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        iv_close.setOnClickListener {
            if (cl_content.translationY == downEndY) {
                setAlpha(iv_close, 0f)
                iv_close.setVisibility(View.GONE)
                restore(ANIM_DURATION_FRACTION)
            }
        }
    }

    /**
     * 释放资源
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        if (restoreOrExpandAnimator.isStarted) {
            restoreOrExpandAnimator.cancel()
            restoreOrExpandAnimator.removeAllUpdateListeners()
        }
        if (reboundedAnim.isStarted) {
            reboundedAnim.cancel()
            reboundedAnim.removeAllUpdateListeners()
        }
    }

    // NestedScrollingParent
    override fun onStartNestedScroll(child: View?, target: View?, nestedScrollAxes: Int): Boolean {
        return onStartNestedScroll(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH)
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        onNestedScrollAccepted(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH)
    }

    override fun onStopNestedScroll(target: View?) {
        onStopNestedScroll(target!!, ViewCompat.TYPE_TOUCH)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int
    ) {
        onNestedScroll(
            target,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            ViewCompat.TYPE_TOUCH
        )
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        onNestedPreScroll(target, dx, dy, consumed, ViewCompat.TYPE_TOUCH)
    }

    override fun onNestedFling(
        target: View?,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return false
    }

    override fun onNestedPreFling(target: View?, velocityX: Float, velocityY: Float): Boolean {
        if (velocityY < 0) {
            val translationY: Float = cl_content.getTranslationY()
            val dy = translationY - velocityY
            if (translationY > topBarHeight && translationY <= downFlingCutOffY) {
                if (dy < contentTransY) {
                    reboundedAnim.setFloatValues(translationY, dy)
                } else if (dy > contentTransY && dy < downFlingCutOffY) {
                    reboundedAnim.setFloatValues(translationY, dy, contentTransY)
                } else {
                    reboundedAnim.setFloatValues(translationY, downFlingCutOffY, contentTransY)
                }
                reboundedAnim.start()
                return true
            }
        }
        return false
    }

    override fun getNestedScrollAxes(): Int {
        return nestedScrollingParentHelper.nestedScrollAxes
    }

    // NestedScrollingChild
    fun onStartNestedScroll(
        child: View?,
        target: View?,
        axes: Int,
        type: Int
    ): Boolean {
        //只接受内容View的垂直滑动
        return child?.id == cl_content.id && axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    fun onNestedScrollAccepted(
        child: View,
        target: View,
        axes: Int,
        type: Int
    ) {
        nestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes, type)
        if (restoreOrExpandAnimator.isStarted) {
            restoreOrExpandAnimator.cancel()
        }
        checkIvCloseVisi()
    }


    fun onStopNestedScroll(target: View, type: Int) {
        nestedScrollingParentHelper.onStopNestedScroll(target, type)
        //如果不是从初始状态转换到展开状态过程触发返回
        //如果不是从初始状态转换到展开状态过程触发返回
        if (cl_content.translationY <= contentTransY) {
            return
        }
        //根据百分比计算动画执行的时长

        val downCollapsedAlphaPro = getDownCollapsedAlphaPro()
        val downContentAlphaYPro = getDownContentAlphaPro()
        if (downCollapsedAlphaPro == 0f) {
            expand((downContentAlphaYPro * ANIM_DURATION_FRACTION).toLong())
        } else {
            restore((downCollapsedAlphaPro * ANIM_DURATION_FRACTION).toLong())
        }
    }

    fun onNestedScroll(
        target: View?,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
    }

    fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        val contentTransY = cl_content.translationY - dy

        //处理上滑
        if (dy > 0) {
            if (contentTransY >= topBarHeight) {
                translationByConsume(cl_content, contentTransY, consumed, dy.toFloat())
            } else {
                translationByConsume(
                    cl_content,
                    topBarHeight,
                    consumed,
                    cl_content!!.translationY - topBarHeight
                )
            }
        }

        if (dy < 0 && !target.canScrollVertically(-1)) {
            //下滑时处理Fling,完全折叠时，下滑Recycler(或NestedScrollView) Fling滚动到列表顶部（或视图顶部）停止Fling
            if (type == ViewCompat.TYPE_NON_TOUCH && cl_content!!.translationY == topBarHeight) {
                return
            }

            //处理下滑
            if (contentTransY >= topBarHeight && contentTransY <= downEndY) {
                translationByConsume(cl_content, contentTransY, consumed, dy.toFloat())
            } else {
                translationByConsume(
                    cl_content,
                    downEndY,
                    consumed,
                    downEndY - cl_content.translationY
                )
                if (target is RecyclerView) {
                    (target as RecyclerView).stopScroll()
                }
                if (target is NestedScrollView) {
                    //模拟DONW事件停止滚动，注意会触发onNestedScrollAccepted()
                    val motionEvent = MotionEvent.obtain(
                        SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_DOWN,
                        0f,
                        0f,
                        0
                    )
                    target.onTouchEvent(motionEvent)
                }
            }
        }

        //根据upAlphaScalePro,设置logo、收藏icon缩放，搜索icon、分享icon透明度
        val upAlphaScalePro = getUpAlphaScalePro()
        alphaScaleByPro(upAlphaScalePro)

        //根据upAlphaGradientPro,设置topBar背景、返回icon、拼团icon颜色渐变值，搜索框透明度
        val upAlphaGradientPro = getUpAlphaGradientPro()
        alphaGradientByPro(upAlphaGradientPro)

        //根据downCollapsedAlphaPro,设置折叠内容透明度
        val downCollapsedAlphaPro = getDownCollapsedAlphaPro()
        alphaCollapsedContentByPro(downCollapsedAlphaPro)

        //根据downContentAlphaPro,设置滑动内容、收起按钮的透明度
        val downContentAlphaPro = getDownContentAlphaPro()
        alphaContentByPro(downContentAlphaPro)

        //根据downShopBarTransPro,设置购物内容内容位移
        val downShopBarTransPro = getDownShopBarTransPro()
        transShopBarByPro(downShopBarTransPro)

        //根据upCollapsedContentTransPro,设置折叠内容位移
        val upCollapsedContentTransPro = getUpCollapsedContentTransPro()
        transCollapsedContentByPro(upCollapsedContentTransPro)

        if (progressUpdateListener != null) {
            progressUpdateListener?.onUpAlphaScaleProUpdate(upAlphaScalePro)
            progressUpdateListener?.onUpAlphaGradientProUpdate(upAlphaGradientPro)
            progressUpdateListener?.onDownCollapsedAlphaProUpdate(downCollapsedAlphaPro)
            progressUpdateListener?.onDownContentAlphaProUpdate(downContentAlphaPro)
            progressUpdateListener?.onDownShopBarTransProUpdate(downShopBarTransPro)
            progressUpdateListener?.onUpCollapsedContentTransProUpdate(upCollapsedContentTransPro)
        }
    }

    private fun translationByConsume(
        view: View?,
        translationY: Float,
        consumed: IntArray,
        consumedDy: Float
    ) {
        consumed[1] = consumedDy.toInt()
        view?.translationY = translationY
    }


    private fun checkIvCloseVisi() {
        if (cl_content.translationY < downContentAlphaY) {
            iv_close.setAlpha(0f)
            iv_close.setVisibility(View.GONE)
        } else {
            iv_close.setAlpha(1f)
            iv_close.setVisibility(View.VISIBLE)
        }
    }

    fun restore(dur: Long) {
        if (restoreOrExpandAnimator.isStarted) {
            restoreOrExpandAnimator.cancel()
        }
        restoreOrExpandAnimator.setFloatValues(cl_content.translationY, contentTransY)
        restoreOrExpandAnimator.duration = dur
        restoreOrExpandAnimator.start()
    }

    fun expand(dur: Long) {

        if (restoreOrExpandAnimator.isStarted) {
            restoreOrExpandAnimator.cancel()
        }
        restoreOrExpandAnimator.setFloatValues(cl_content.translationY, downEndY)
        restoreOrExpandAnimator.duration = dur
        restoreOrExpandAnimator.start()
    }


    /**
     * 根据downContentAlphaPro,设置滑动内容、收起按钮的透明度
     */
    private fun alphaContentByPro(downContentAlphaPro: Float) {
        setAlpha(viewPager2, downContentAlphaPro)
        setAlpha(tabLayout, downContentAlphaPro)
        setAlpha(iv_close, 1 - downContentAlphaPro)
        if (iv_close.alpha == 0f) {
            iv_close.visibility = View.GONE
        } else {
            iv_close.visibility = View.VISIBLE
        }
    }

    /**
     * 根据upCollapsedContentTransPro,设置折叠内容位移
     */
    private fun transCollapsedContentByPro(upCollapsedContentTransPro: Float) {
        val collapsedContentTranY = -(upCollapsedContentTransPro * (contentTransY - topBarHeight))
        translation(cl_collapsed_content, collapsedContentTranY)
    }

    /**
     * 根据downShopBarTransPro,设置购物内容内容位移
     */
    private fun transShopBarByPro(downShopBarTransPro: Float) {
        val shopBarTransY = (1 - downShopBarTransPro) * shopBarHeight
        translation(cl_shop_bar, shopBarTransY)
    }

    /**
     * 根据downCollapsedAlphaPro,设置折叠内容透明度
     */
    private fun alphaCollapsedContentByPro(downCollapsedAlphaPro: Float) {
        setAlpha(cl_collapsed_header, downCollapsedAlphaPro)
        setAlpha(rv_collasped, 1 - downCollapsedAlphaPro)
    }

    /**
     * 根据upAlphaGradientPro,设置topBar背景、返回icon、拼团icon颜色渐变值，搜索框透明度
     */
    private fun alphaGradientByPro(upAlphaGradientPro: Float) {
        setAlpha(tv_search, upAlphaGradientPro)
        val iconColor = iconArgbEvaluator?.evaluate(
            upAlphaGradientPro,
            context.resources.getColor(R.color.white),
            context.resources.getColor(R.color.black)
        ) as Int
        val topBarColor = topBarArgbEvaluator?.evaluate(
            upAlphaGradientPro,
            context.resources.getColor(R.color.trans_white),
            context.resources.getColor(R.color.white)
        ) as Int

        cl_top_bar.setBackgroundColor(topBarColor)
        iv_back.drawable.mutate().setTint(iconColor)
        iv_assemble.drawable.mutate().setTint(iconColor)
    }

    /**
     * 根据upAlphaScalePro,设置logo、收藏icon缩放，搜索icon、分享icon透明度
     */
    private fun alphaScaleByPro(upAlphaScalePro: Float) {
        val alpha = 1 - upAlphaScalePro
        val scale = 1 - upAlphaScalePro

        setAlpha(iv_search, alpha)
        setAlpha(iv_share, alpha)

        setScaleAlpha(iv_logo, scale, scale, alpha)
        setScaleAlpha(iv_collect, scale, scale, alpha)

    }

    private fun setScaleAlpha(view: View, scaleX: Float, scaleY: Float, alpha: Float) {
        setAlpha(view, alpha)
        setsetScale(view, scaleX, scaleY)
    }

    private fun setsetScale(view: View, scaleX: Float, scaleY: Float) {
        view.scaleX = scaleX
        view.scaleY = scaleY
    }

    private fun setAlpha(view: View, alpha: Float) {
        view.alpha = alpha
    }

    private fun getDownContentAlphaPro(): Float {
        return (downEndY - MathUtils.clamp(
            cl_content.translationY,
            downContentAlphaY,
            downEndY
        )) / (downEndY - downContentAlphaY)
    }

    private fun getUpCollapsedContentTransPro(): Float {
        return (contentTransY - MathUtils.clamp(
            cl_content.translationY,
            topBarHeight,
            contentTransY
        )) / (contentTransY - topBarHeight)
    }

    private fun getDownShopBarTransPro(): Float {
        return (downShopBarTransY - MathUtils.clamp(
            cl_content.translationY,
            contentTransY,
            downShopBarTransY
        )) / (downShopBarTransY - contentTransY)
    }

    private fun getDownCollapsedAlphaPro(): Float {
        return (downCollapsedAlphaY - MathUtils.clamp(
            cl_content.translationY,
            contentTransY,
            downCollapsedAlphaY
        )) / (downCollapsedAlphaY - contentTransY)
    }

    private fun getUpAlphaGradientPro(): Float {
        return (upAlphaScaleY - MathUtils.clamp(
            cl_content.translationY,
            upAlphaGradientY,
            upAlphaScaleY
        )) / (upAlphaScaleY - upAlphaGradientY)
    }

    private fun getUpAlphaScalePro(): Float {
        return (contentTransY - MathUtils.clamp(
            cl_content.translationY,
            upAlphaScaleY,
            contentTransY
        )) / (contentTransY - upAlphaScaleY)
    }

    private fun dp2px(dpVal: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpVal,
            context.resources.displayMetrics
        ).toInt()
    }


    private fun translation(view: View, translationY: Float) {
        view.translationY = translationY
    }

    fun setProgressUpdateListener(progressUpdateListener: ProgressUpdateListener) {
        this.progressUpdateListener = progressUpdateListener
    }

    interface ProgressUpdateListener {
        fun onUpCollapsedContentTransProUpdate(float: Float)

        fun onUpAlphaScaleProUpdate(float: Float)

        fun onUpAlphaGradientProUpdate(float: Float)

        fun onDownCollapsedAlphaProUpdate(float: Float)

        fun onDownContentAlphaProUpdate(float: Float)

        fun onDownShopBarTransProUpdate(float: Float)
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun stopNestedScroll(type: Int) {
        TODO("Not yet implemented")
    }

    override fun hasNestedScrollingParent(type: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun startNestedScroll(axes: Int, type: Int): Boolean {
        TODO("Not yet implemented")
    }
}