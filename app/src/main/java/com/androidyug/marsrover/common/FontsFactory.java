package com.androidyug.marsrover.common;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by IAMONE on 1/30/2016.
 */
public class FontsFactory {

    public static Typeface robotoBlack(Context ctx){
        Typeface tf = Typeface.createFromAsset(ctx.getAssets(), "fonts/Roboto-Black.ttf");
        return  tf;
    }

    public static Typeface robotoBlackItalic(Context ctx){
        Typeface tf = Typeface.createFromAsset(ctx.getAssets(), "fonts/Roboto-BlackItalic.ttf");
        return  tf;
    }

    public static Typeface robotoBold(Context ctx){
        Typeface tf = Typeface.createFromAsset(ctx.getAssets(), "fonts/Roboto-Bold.ttf");
        return  tf;
    }

    public static Typeface robotoItalic(Context ctx){
        Typeface tf = Typeface.createFromAsset(ctx.getAssets(), "fonts/Roboto-Italic.ttf");
        return  tf;
    }

    public static Typeface robotoLight(Context ctx){
        Typeface tf = Typeface.createFromAsset(ctx.getAssets(), "fonts/Roboto-Light.ttf");
        return  tf;
    }


    public static Typeface robotoThin(Context ctx){
        Typeface tf = Typeface.createFromAsset(ctx.getAssets(), "fonts/Roboto-Thin.ttf");
        return  tf;
    }

    public static Typeface robotoRegular(Context ctx){
        Typeface tf = Typeface.createFromAsset(ctx.getAssets(), "fonts/Roboto-Regular.ttf");
        return  tf;
    }

    public static Typeface robotoCondensedLight(Context ctx){
        Typeface tf = Typeface.createFromAsset(ctx.getAssets(), "fonts/RobotoCondensed-Light.ttf");
        return  tf;
    }

    public static Typeface robotoCondensedBold(Context ctx){
        Typeface tf = Typeface.createFromAsset(ctx.getAssets(), "fonts/RobotoCondensed-Bold.ttf");
        return  tf;
    }
}
