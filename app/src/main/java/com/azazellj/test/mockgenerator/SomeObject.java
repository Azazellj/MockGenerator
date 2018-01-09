package com.azazellj.test.mockgenerator;


import android.graphics.Bitmap;
import com.azazellj.mock.annotations.Mock;

@Mock
public class SomeObject {
    public boolean aBoolean;
    public byte aByte;
    public Byte aByteObj;
    public char aChar;
    public double aDouble;

    public EnumType enumType;
    public EnumType enumTypeInited = EnumType.ENUM_1;

    public enum EnumType {
        ENUM_1, ENUM_2
    }

    public float aFloat;
    public int anInt;
    public long aLong;
    public short aShort;
    public Object object;

    public String string;

    public Bitmap mBitmap;
    public SomeObject mSomeObject;
}
