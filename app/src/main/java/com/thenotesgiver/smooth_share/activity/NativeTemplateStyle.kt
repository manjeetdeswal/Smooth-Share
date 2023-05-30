package com.thenotesgiver.smooth_share.activity

import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable

class NativeTemplateStyle {

    private var callToActionTextTypeface: Typeface? = null

    // Size of call to action text.
    private var callToActionTextSize = 0f

    // Call to action typeface color in the form 0xAARRGGBB.
    private var callToActionTypefaceColor = 0

    // Call to action background color.
    private var callToActionBackgroundColor: ColorDrawable? = null

    // All templates have a primary text area which is populated by the native ad's headline.

    // All templates have a primary text area which is populated by the native ad's headline.
    // Primary text typeface.
    private var primaryTextTypeface: Typeface? = null

    // Size of primary text.
    private var primaryTextSize = 0f

    // Primary text typeface color in the form 0xAARRGGBB.
    private var primaryTextTypefaceColor = 0

    // Primary text background color.
    private var primaryTextBackgroundColor: ColorDrawable? = null

    // The typeface, typeface color, and background color for the second row of text in the template.
    // All templates have a secondary text area which is populated either by the body of the ad or
    // by the rating of the app.

    // The typeface, typeface color, and background color for the second row of text in the template.
    // All templates have a secondary text area which is populated either by the body of the ad or
    // by the rating of the app.
    // Secondary text typeface.
    private var secondaryTextTypeface: Typeface? = null

    // Size of secondary text.
    private var secondaryTextSize = 0f

    // Secondary text typeface color in the form 0xAARRGGBB.
    private var secondaryTextTypefaceColor = 0

    // Secondary text background color.
    private var secondaryTextBackgroundColor: ColorDrawable? = null

    // The typeface, typeface color, and background color for the third row of text in the template.
    // The third row is used to display store name or the default tertiary text.

    // The typeface, typeface color, and background color for the third row of text in the template.
    // The third row is used to display store name or the default tertiary text.
    // Tertiary text typeface.
    private var tertiaryTextTypeface: Typeface? = null

    // Size of tertiary text.
    private var tertiaryTextSize = 0f

    // Tertiary text typeface color in the form 0xAARRGGBB.
    private var tertiaryTextTypefaceColor = 0

    // Tertiary text background color.
    private var tertiaryTextBackgroundColor: ColorDrawable? = null

    // The background color for the bulk of the ad.
    private var mainBackgroundColor: ColorDrawable? = null

    fun getCallToActionTextTypeface(): Typeface? {
        return callToActionTextTypeface
    }

    fun getCallToActionTextSize(): Float {
        return callToActionTextSize
    }

    fun getCallToActionTypefaceColor(): Int {
        return callToActionTypefaceColor
    }

    fun getCallToActionBackgroundColor(): ColorDrawable? {
        return callToActionBackgroundColor
    }

    fun getPrimaryTextTypeface(): Typeface? {
        return primaryTextTypeface
    }

    fun getPrimaryTextSize(): Float {
        return primaryTextSize
    }

    fun getPrimaryTextTypefaceColor(): Int {
        return primaryTextTypefaceColor
    }

    fun getPrimaryTextBackgroundColor(): ColorDrawable? {
        return primaryTextBackgroundColor
    }

    fun getSecondaryTextTypeface(): Typeface? {
        return secondaryTextTypeface
    }

    fun getSecondaryTextSize(): Float {
        return secondaryTextSize
    }

    fun getSecondaryTextTypefaceColor(): Int {
        return secondaryTextTypefaceColor
    }

    fun getSecondaryTextBackgroundColor(): ColorDrawable? {
        return secondaryTextBackgroundColor
    }

    fun getTertiaryTextTypeface(): Typeface? {
        return tertiaryTextTypeface
    }

    fun getTertiaryTextSize(): Float {
        return tertiaryTextSize
    }

    fun getTertiaryTextTypefaceColor(): Int {
        return tertiaryTextTypefaceColor
    }

    fun getTertiaryTextBackgroundColor(): ColorDrawable? {
        return tertiaryTextBackgroundColor
    }

    fun getMainBackgroundColor(): ColorDrawable? {
        return mainBackgroundColor
    }

    /** A class that provides helper methods to build a style object. *  */
    class Builder {
        private val styles: NativeTemplateStyle
        fun withCallToActionTextTypeface(callToActionTextTypeface: Typeface?): Builder {
            styles.callToActionTextTypeface = callToActionTextTypeface
            return this
        }

        fun withCallToActionTextSize(callToActionTextSize: Float): Builder {
            styles.callToActionTextSize = callToActionTextSize
            return this
        }

        fun withCallToActionTypefaceColor(callToActionTypefaceColor: Int): Builder {
            styles.callToActionTypefaceColor = callToActionTypefaceColor
            return this
        }

        fun withCallToActionBackgroundColor(callToActionBackgroundColor: ColorDrawable?): Builder {
            styles.callToActionBackgroundColor = callToActionBackgroundColor
            return this
        }

        fun withPrimaryTextTypeface(primaryTextTypeface: Typeface?): Builder {
            styles.primaryTextTypeface = primaryTextTypeface
            return this
        }

        fun withPrimaryTextSize(primaryTextSize: Float): Builder {
            styles.primaryTextSize = primaryTextSize
            return this
        }

        fun withPrimaryTextTypefaceColor(primaryTextTypefaceColor: Int): Builder {
            styles.primaryTextTypefaceColor = primaryTextTypefaceColor
            return this
        }

        fun withPrimaryTextBackgroundColor(primaryTextBackgroundColor: ColorDrawable?): Builder {
            styles.primaryTextBackgroundColor = primaryTextBackgroundColor
            return this
        }

        fun withSecondaryTextTypeface(secondaryTextTypeface: Typeface?): Builder {
            styles.secondaryTextTypeface = secondaryTextTypeface
            return this
        }

        fun withSecondaryTextSize(secondaryTextSize: Float): Builder {
            styles.secondaryTextSize = secondaryTextSize
            return this
        }

        fun withSecondaryTextTypefaceColor(secondaryTextTypefaceColor: Int): Builder {
            styles.secondaryTextTypefaceColor = secondaryTextTypefaceColor
            return this
        }

        fun withSecondaryTextBackgroundColor(secondaryTextBackgroundColor: ColorDrawable?): Builder {
            styles.secondaryTextBackgroundColor = secondaryTextBackgroundColor
            return this
        }

        fun withTertiaryTextTypeface(tertiaryTextTypeface: Typeface?): Builder {
            styles.tertiaryTextTypeface = tertiaryTextTypeface
            return this
        }

        fun withTertiaryTextSize(tertiaryTextSize: Float): Builder {
            styles.tertiaryTextSize = tertiaryTextSize
            return this
        }

        fun withTertiaryTextTypefaceColor(tertiaryTextTypefaceColor: Int): Builder {
            styles.tertiaryTextTypefaceColor = tertiaryTextTypefaceColor
            return this
        }

        fun withTertiaryTextBackgroundColor(tertiaryTextBackgroundColor: ColorDrawable?): Builder {
            styles.tertiaryTextBackgroundColor = tertiaryTextBackgroundColor
            return this
        }

        fun withMainBackgroundColor(mainBackgroundColor: ColorDrawable?): Builder {
            styles.mainBackgroundColor = mainBackgroundColor
            return this
        }

        fun build(): NativeTemplateStyle {
            return styles
        }

        init {
            styles = NativeTemplateStyle()
        }
    }
}