package com.androidyug.marsrover.common

import android.content.Context
import android.graphics.Typeface

/**
 * Created by IAMONE on 1/30/2016.
 */
object FontsFactory {

    fun robotoBlack(ctx: Context): Typeface {
        return Typeface.createFromAsset(ctx.assets, "fonts/Roboto-Black.ttf")
    }

    fun robotoBlackItalic(ctx: Context): Typeface {
        return Typeface.createFromAsset(ctx.assets, "fonts/Roboto-BlackItalic.ttf")
    }

    fun robotoBold(ctx: Context): Typeface {
        return Typeface.createFromAsset(ctx.assets, "fonts/Roboto-Bold.ttf")
    }

    fun robotoItalic(ctx: Context): Typeface {
        return Typeface.createFromAsset(ctx.assets, "fonts/Roboto-Italic.ttf")
    }

    fun robotoLight(ctx: Context): Typeface {
        return Typeface.createFromAsset(ctx.assets, "fonts/Roboto-Light.ttf")
    }


    fun robotoThin(ctx: Context): Typeface {
        return Typeface.createFromAsset(ctx.assets, "fonts/Roboto-Thin.ttf")
    }

    fun robotoRegular(ctx: Context): Typeface {
        return Typeface.createFromAsset(ctx.assets, "fonts/Roboto-Regular.ttf")
    }

    fun robotoCondensedLight(ctx: Context): Typeface {
        return Typeface.createFromAsset(ctx.assets, "fonts/RobotoCondensed-Light.ttf")
    }

    fun robotoCondensedBold(ctx: Context): Typeface {
        return Typeface.createFromAsset(ctx.assets, "fonts/RobotoCondensed-Bold.ttf")
    }
}
