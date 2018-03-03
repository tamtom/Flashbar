package com.andrognito.flashbar

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.Animation
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.andrognito.flashbar.Flashbar.FlashbarPosition
import com.andrognito.flashbar.Flashbar.FlashbarPosition.BOTTOM
import com.andrognito.flashbar.Flashbar.FlashbarPosition.TOP
import com.andrognito.flashbar.utils.*

/**
 * The actual Flashbar view representation that can consist of the message, button, icon, etc.
 * Its size is adaptive and depends solely on the amount of content present in it. It always matches
 * the width of the screen.
 *
 * It can either be present at the top or at the bottom of the screen. It will always consume touch
 * events and respond as necessary.
 */
class FlashbarView : RelativeLayout {

    private lateinit var flashbarRootView: LinearLayout

    constructor(context: Context) : super(context, null, 0) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        initView()
    }

    private fun initView() {
        inflate(context, R.layout.flash_bar_view, this)
        flashbarRootView = findViewById(R.id.flash_bar_root)
    }

    internal fun adjustWitPositionAndOrientation(activity: Activity, flashbarPosition: FlashbarPosition) {
        val flashbarViewLp = RelativeLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        val statusBarHeight = getStatusBarHeightInPx(activity)

        when (flashbarPosition) {
            TOP -> {
                val flashbarViewContent = findViewById<View>(R.id.flash_bar_content)
                val flashbarViewContentLp = flashbarViewContent.layoutParams as LinearLayout.LayoutParams

                flashbarViewContentLp.topMargin = statusBarHeight
                flashbarViewContent.layoutParams = flashbarViewContentLp
                flashbarViewLp.addRule(RelativeLayout.ALIGN_PARENT_TOP)
            }
            BOTTOM -> {
                flashbarViewLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            }
        }

        layoutParams = flashbarViewLp
    }

    internal fun setBarBackground(drawable: Drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            this.flashbarRootView.background = drawable
        } else {
            this.flashbarRootView.setBackgroundDrawable(drawable)
        }
    }

    internal fun setBarBackgroundColor(@ColorInt color: Int) {
        this.flashbarRootView.setBackgroundColor(color)
    }
}

/**
 * Container view matching the height and width of the parent to hold a FlashbarView.
 * It will occupy the entire screens size but will be completely transparent. The
 * FlashbarView inside is the only visible component in it.
 */
class FlashbarContainerView(context: Context) : RelativeLayout(context) {

    private lateinit var flashbarView: FlashbarView
    private lateinit var enterAnimation: Animation
    private lateinit var exitAnimation: Animation

    private var isBarShowing = false
    private var isBarShown = false
    private var isBarDismissing = false

    private val enterAnimationListener = object : Animation.AnimationListener {

        override fun onAnimationStart(animation: Animation) {
            isBarShowing = true
        }

        override fun onAnimationEnd(animation: Animation) {
            isBarShowing = false
            isBarShown = true
        }

        override fun onAnimationRepeat(animation: Animation) {
            // NO-OP
        }
    }

    private val exitAnimationListener = object : Animation.AnimationListener {

        override fun onAnimationStart(animation: Animation?) {
            isBarDismissing = true
        }

        override fun onAnimationEnd(animation: Animation?) {
            isBarDismissing = false
            isBarShown = false

            removeView(flashbarView)
            (parent as? ViewGroup)?.removeView(this@FlashbarContainerView)
        }

        override fun onAnimationRepeat(animation: Animation?) {
            // NO-OP
        }
    }

    fun add(flashbarView: FlashbarView) {
        this.flashbarView = flashbarView
        addView(flashbarView)
    }

    fun adjustPositionAndOrientation(activity: Activity) {
        val flashbarContainerViewLp = RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)

        val navigationBarPosition = getNavigationBarPosition(activity)
        val navigationBarSize = getNavigationBarSizeInPixels(activity)

        when (navigationBarPosition) {
            NavigationBarPosition.LEFT -> flashbarContainerViewLp.leftMargin = navigationBarSize
            NavigationBarPosition.RIGHT -> flashbarContainerViewLp.rightMargin = navigationBarSize
            NavigationBarPosition.BOTTOM -> flashbarContainerViewLp.bottomMargin = navigationBarSize
        }

        layoutParams = flashbarContainerViewLp
    }

    fun show(activity: Activity) {
        if (isBarShowing || isBarShown) {
            return
        }

        val activityRootView = getActivityRootView(activity)
        activityRootView?.addView(this)

        enterAnimation.setAnimationListener(enterAnimationListener)
        flashbarView.startAnimation(enterAnimation)
    }

    fun dismiss() {
        if (isBarDismissing) {
            return
        }

        exitAnimation.setAnimationListener(exitAnimationListener)
        flashbarView.startAnimation(exitAnimation)
    }

    fun isBarShowing() = isBarShowing

    fun isBarShown() = isBarShown

    fun setEnterAnimation(animation: Animation) {
        enterAnimation = animation
    }

    fun setExitAnimation(animation: Animation) {
        exitAnimation = animation
    }
}
