package com.azazellj.test.mockgenerator;

import android.app.Application;
import android.graphics.Bitmap;
import com.azazellj.mock.annotations.MockTypeHelper;


public class Utils {
    @MockTypeHelper(types = {Bitmap.class, Application.class})
    public static Bitmap randomBitmap() {
        return null;
    }
}
