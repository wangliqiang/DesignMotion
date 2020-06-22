package com.app.design_motion.widget

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.animation.addListener
import androidx.core.math.MathUtils
import androidx.core.view.NestedScrollingParent2
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import com.app.design_motion.R
import kotlinx.android.synthetic.main.activity_ele_ui.view.*
import kotlinx.android.synthetic.main.ele_food_detail_layout.view.*
import kotlinx.android.synthetic.main.ele_shop_buy_layout.view.*

class FoodNestedScrollLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = -1
) : FrameLayout(context, attrs, defStyleAttr), NestedScrollingParent2 {

    val ANIM_DURATION_FRACTION: Long = 200L
    private var progressUpdateListener: ProgressUpdateListener? = null
    private var nestedScrollingParentHelper: NestedScrollingParentHelper =
        NestedScrollingParentHelper(this)

    private var reboundedAnim: ValueAnimator //回弹动画
    private var restoreOrExpandAnimator: ValueAnimator // 收起或展开折叠内容时执行的动画

    private var shopBarHeight = 0f //shopBar高度
    private var ivExpandHegiht = 0f //ivExpand部分高度
    private var iconTransY = 0f //分享、关闭icon初始化transY
    private var contentTransY = 0f //滑动内容初始化TransY
    private var downFlingCutOffY = 0f //下滑时fling上部分回弹临界值
    private var upEndIconTransY = 0f //分享、关闭icon上滑最终transY


    init {
        reboundedAnim = ValueAnimator()
        reboundedAnim.interpolator = DecelerateInterpolator()
        reboundedAnim.addUpdateListener {
            translation(food_ns_view, it.animatedValue as Float)
            alphaTransView(food_ns_view.translationY)
            if (progressUpdateListener != null) {
                progressUpdateListener?.onDownContentCloseProUpdate(getDownContentClosePro())
            }
        }
        reboundedAnim.duration = ANIM_DURATION_FRACTION

        restoreOrExpandAnimator = ValueAnimator()
        restoreOrExpandAnimator.interpolator = AccelerateInterpolator()
        restoreOrExpandAnimator.addUpdateListener {
            translation(food_ns_view, it.animatedValue as Float)
            alphaTransView(food_ns_view.translationY)
            if (progressUpdateListener != null) {
                progressUpdateListener?.onDownContentCloseProUpdate(getDownContentClosePro())
            }
        }
        restoreOrExpandAnimator.addListener {
            val alpha = if (food_ns_view.translationY >= measuredHeight) 0 else 1
            setAlpha(iv_small_close, alpha.toFloat())
            setAlpha(iv_small_share, alpha.toFloat())
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (restoreOrExpandAnimator.isStarted) {
            restoreOrExpandAnimator.cancel()
            restoreOrExpandAnimator.removeAllUpdateListeners()
            restoreOrExpandAnimator.removeAllListeners()
        }
        if (reboundedAnim.isStarted) {
            reboundedAnim.cancel()
            reboundedAnim.removeAllUpdateListeners()
        }
    }

    fun restore(dur: Long) {
        iv_small_close.isClickable = true
        v_mask.isClickable = true
        iv_food_expand.isClickable = true
        if (restoreOrExpandAnimator.isStarted) {
            restoreOrExpandAnimator.cancel()
        }
        restoreOrExpandAnimator.setFloatValues(food_ns_view.translationY, contentTransY)
        restoreOrExpandAnimator.duration = dur
        restoreOrExpandAnimator.start()
    }

    fun expand(dur: Long) {
        if (restoreOrExpandAnimator.isStarted) {
            restoreOrExpandAnimator.cancel()
        }
        restoreOrExpandAnimator.setFloatValues(food_ns_view.translationY, 0f)
        restoreOrExpandAnimator.duration = dur
        restoreOrExpandAnimator.start()
    }

    fun close(dur: Long) {
        food_ns_view.scrollTo(0, 0)
        iv_small_close.isClickable = false
        v_mask.isClickable = false
        iv_food_expand.isClickable = false
        if (restoreOrExpandAnimator.isStarted) {
            restoreOrExpandAnimator.cancel()
        }
        restoreOrExpandAnimator.setFloatValues(food_ns_view.translationY, measuredHeight.toFloat())
        restoreOrExpandAnimator.duration = dur
        restoreOrExpandAnimator.start()
    }

    private fun alphaTransView(transY: Float) {
        val upExpandTransPro = getUpExpandTransPro()
        // 位移购物内容
        val shopBarTransY = (1 - upExpandTransPro) * shopBarHeight
        translation(cl_shop_buy, shopBarTransY)

        // 设置商品信息View的透明度变化
        setAlpha(t_comm, upExpandTransPro)
        setAlpha(t_good_comm_rate, upExpandTransPro)
        setAlpha(t_comm_detail, upExpandTransPro)
        setAlpha(t_food_detail, upExpandTransPro)
        setAlpha(t_comm_count, 1 - upExpandTransPro)
        setAlpha(view_line, 1 - upExpandTransPro)

        // 位移share，close两个Icon,设置展开icon透明度
        if (transY <= contentTransY) {
            val ivExpandUpTransY = upExpandTransPro * -contentTransY
            translation(iv_food_expand, ivExpandUpTransY)
            setAlpha(iv_food_expand, 1 - upExpandTransPro)

            val iconTransY =
                upEndIconTransY + (1 - upExpandTransPro) * (iconTransY - upEndIconTransY)

            translation(iv_small_share, iconTransY)
            translation(iv_small_close, iconTransY)
        } else if (transY > contentTransY && transY <= measuredHeight) {
            val ivExpandDowndTransY = (1 - getDownIvExpandPro()) * ivExpandHegiht
            translation(iv_food_expand, ivExpandDowndTransY)

            val iconTransY = transY + dp2px(16f)
            translation(iv_small_share, iconTransY)
            translation(iv_small_close, iconTransY)
        }
    }

    private fun getDownContentClosePro(): Float {
        return (food_ns_view.translationY - contentTransY) / (measuredHeight - contentTransY)
    }

    private fun getDownIvExpandPro(): Float {
        return ((contentTransY + ivExpandHegiht) - MathUtils.clamp(
            food_ns_view.translationY,
            contentTransY,
            contentTransY + ivExpandHegiht
        )) / ivExpandHegiht
    }

    private fun getUpExpandTransPro(): Float {
        return (contentTransY - MathUtils.clamp(
            food_ns_view.translationY,
            0f,
            contentTransY
        )) / contentTransY
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        shopBarHeight = resources.getDimension(R.dimen.shop_bar_height)
        ivExpandHegiht = resources.getDimension(R.dimen.iv_food_expand)
        contentTransY = resources.getDimension(R.dimen.food_content_trans_y)
        iconTransY = resources.getDimension(R.dimen.iv_food_icon_trans_y)
        // 状态栏高度
        val statusBarHeight = resources.getDimensionPixelSize(
            resources.getIdentifier(
                "status_bar_height",
                "dimen",
                "android"
            )
        )
        downFlingCutOffY = contentTransY + dp2px(92f)
        upEndIconTransY = statusBarHeight + dp2px(8f)
        // 因为开始就是关闭状态，设置Content部分的TransY为满屏高度
        food_ns_view.translationY = measuredHeight.toFloat()
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

    private fun translation(view: View, translationY: Float) {
        view.translationY = translationY
    }

    private fun setAlpha(view: View, alpha: Float) {
        view.alpha = alpha
    }


    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        onNestedScrollAccepted(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH)
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return onStartNestedScroll(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH)
    }

    override fun onStopNestedScroll(child: View) {
        onStopNestedScroll(child, ViewCompat.TYPE_TOUCH)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        onNestedPreScroll(target, dx, dy, consumed, ViewCompat.TYPE_TOUCH)
    }

    override fun onNestedFling(
        target: View,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return false
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        if (velocityY < 0) {
            val translationY = target.translationY
            val dy = translationY - velocityY

            // 从展开状态下滑时处理会谈Fling
            if (translationY >= 0 && translationY < downFlingCutOffY) {
                if (dy < contentTransY) {
                    reboundedAnim.setFloatValues(translationY, dy)
                } else if (dy > contentTransY && dy < downFlingCutOffY) {
                    reboundedAnim.setFloatValues(translationY, dy, contentTransY)
                } else {
                    reboundedAnim.setFloatValues(translationY, downFlingCutOffY, contentTransY)
                }
                target.scrollTo(0, 0)
                reboundedAnim.start()
                return true
            }
            // 从初始状态到关闭下滑百分比超过50%惯性滑动关闭
            val dur = (1 - getDownContentClosePro()) * ANIM_DURATION_FRACTION
            if (translationY <= (measuredHeight / 2f) && translationY > downFlingCutOffY) {
                restore(dur.toLong())
                return true
            } else {
                close(dur.toLong())
                return true
            }
        }
        return false
    }

    override fun getNestedScrollAxes(): Int {
        return nestedScrollingParentHelper.nestedScrollAxes
    }


    //---NestedScrollingParent2---//
    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        nestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes, type)
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL && target.id == R.id.food_ns_view
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        val contentTransY = target.translationY - dy
        // 处理上滑
        if (dy > 0) {
            if (contentTransY >= 0) {
                translationByConsume(target, contentTransY, consumed, dy.toFloat())
            } else {
                translationByConsume(target, 0f, consumed, (target.translationY - 0))
            }
        }
        // 处理下滑
        if (dy < 0 && !target.canScrollVertically(-1)) {
            if (contentTransY >= 0 && contentTransY < measuredHeight) {
                translationByConsume(target, contentTransY, consumed, dy.toFloat())
            } else {
                translationByConsume(
                    target,
                    measuredHeight.toFloat(),
                    consumed,
                    measuredHeight - target.translationY
                )
            }
        }
        alphaTransView(contentTransY)

        if (progressUpdateListener != null) {
            progressUpdateListener?.onDownContentCloseProUpdate(getDownContentClosePro())
        }
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        nestedScrollingParentHelper.onStopNestedScroll(target, type)
        val translationY = target.translationY
        if (translationY == contentTransY || reboundedAnim.isStarted || restoreOrExpandAnimator.isStarted) {
            return
        }
        var dur: Long
        if (translationY < contentTransY) {
            if (getUpExpandTransPro() <= 0.5f) {
                dur = (getUpExpandTransPro() * ANIM_DURATION_FRACTION).toLong()
                restore(dur)
            } else {
                dur = ((1 - getUpExpandTransPro()) * ANIM_DURATION_FRACTION).toLong()
                expand(dur)
            }
        } else {
            if (getDownContentClosePro() >= 0.5f) {
                dur = (getDownContentClosePro() * ANIM_DURATION_FRACTION).toLong()
                close(dur)
            } else {
                dur = ((1 - getDownContentClosePro()) * ANIM_DURATION_FRACTION).toLong()
                restore(dur)
            }
        }

    }


    private fun dp2px(dpVal: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpVal,
            context.resources.displayMetrics
        )
    }

    fun setProgressUpdateListener(progressUpdateListener: ProgressUpdateListener) {
        this.progressUpdateListener = progressUpdateListener
    }

    interface ProgressUpdateListener {
        fun onDownContentCloseProUpdate(pro: Float)
    }
}