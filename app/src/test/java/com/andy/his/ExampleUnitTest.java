package com.andy.his;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        String Str = new String("1-户主身份信息-01.jpg");
        System.out.println("返回值 :" + Str.matches("(.*)户主身份信息(.*)01(.*)"));
    }
}