package com.sonny.xianwan.customview

import android.app.Dialog
import android.content.Context
import android.view.*
import androidx.annotation.StyleRes

class CustomDialog @JvmOverloads constructor(
    context: Context,
    @StyleRes themeResId: Int = 0
) : Dialog(context, themeResId) {

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    class Builder(private val context: Context) {
        private var view: View? = null
        private var gravity: Int = Gravity.CENTER or Gravity.CENTER_HORIZONTAL
        private var cancelable = true
        private var dimEnable = true
        private var fullScreen = false
        private var x = 0
        private var y = 0
        private var windowAnimation = 0
        private var themeResId = 0
        private var viewResId = 0


        fun build(): CustomDialog {
            val dialog = CustomDialog(context, themeResId)
            if (view != null) {
                dialog.setContentView(view!!)
            }

            if (viewResId != 0) {
                dialog.setContentView(viewResId)
            }


            dialog.setCancelable(cancelable)
            val window = dialog.window
            if (windowAnimation != 0) {
                window?.setWindowAnimations(windowAnimation)
            }

            val attributes = window?.attributes
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
            attributes?.gravity = gravity
            if (x != 0 || y != 0) {
                attributes?.gravity = Gravity.TOP or Gravity.START
                attributes?.x = x
                attributes?.y = y
            }

            if (fullScreen) {
                dialog.window?.decorView?.setPadding(0, 0, 0, 0)
                attributes?.width = WindowManager.LayoutParams.MATCH_PARENT
                attributes?.height = WindowManager.LayoutParams.WRAP_CONTENT
            }

            if (!dimEnable) {
                dialog.window?.setDimAmount(0f)//背景不变暗
            }

            dialog.window?.attributes = attributes

            return dialog
        }


        fun View(view: View): Builder {
            this.view = view
            return this
        }

//        fun View(@StyleRes viewResId: Int): Builder {
//            this.viewResId = viewResId
//            return  this
//        }


        fun gravity(gravity: Int): Builder {
            this.gravity = gravity
            return this
        }

        fun fullScreen(fullScreen: Boolean): Builder {
            this.fullScreen = fullScreen
            return this
        }

        fun cancelable(cancelable: Boolean): Builder {
            this.cancelable = cancelable
            return this
        }

        fun windowAnimation(animations: Int): Builder {
            this.windowAnimation = animations
            return this
        }

        fun theme(@StyleRes themeResId: Int): Builder {
            this.themeResId = themeResId
            return this
        }

        fun xy(x: Int, y: Int): Builder {
            this.x = x
            this.y = y
            return this
        }


    }


    fun showAsDropDown(anchor: View?, xOffset: Int, yOffset: Int) {
        if (anchor == null) {
            return
        }
        val anchorHeight = anchor.height
        val anchorWidth = anchor.width
        val drawingLocation = IntArray(2)
        anchor.getLocationInWindow(drawingLocation)
        val attributes = window!!.attributes
        attributes.gravity = Gravity.TOP or Gravity.START
        attributes.x = drawingLocation[0] + xOffset
        attributes.y = drawingLocation[1] + anchorHeight + yOffset
        window!!.attributes = attributes
        show()
    }

}