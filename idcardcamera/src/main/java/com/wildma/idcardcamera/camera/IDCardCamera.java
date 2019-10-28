package com.wildma.idcardcamera.camera;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.lang.ref.WeakReference;

/**
 * Author       wildma
 * Github       https://github.com/wildma
 * Date         2019/04/28
 * Desc	        ${身份证相机}
 */
public class IDCardCamera {

    public final static int    TYPE_IDCARD_FRONT     = 1;//身份证正面
    public final static int    TYPE_IDCARD_BACK      = 2;//身份证反面
    public final static int    RESULT_CODE           = 0X11;//结果码
    public final static int    PERMISSION_CODE_FIRST = 0x12;//权限请求码
    public final static String TAKE_TYPE             = "take_type";//拍摄类型标记
    public final static String IMAGE_PATH            = "image_path";//图片路径标记

    private final WeakReference<Activity> mActivity;

    public static IDCardCamera create(Activity activity) {
        return new IDCardCamera(activity);
    }

    private IDCardCamera(Activity activity) {
        this.mActivity = new WeakReference(activity);
    }

    /**
     * 打开相机
     *
     * @param IDCardDirection 身份证方向（TYPE_IDCARD_FRONT / TYPE_IDCARD_BACK）
     */
    public void openCamera(int IDCardDirection, String filePath) {
        Activity activity = this.mActivity.get();
        Intent intent = new Intent(activity, CameraActivity.class);
        intent.putExtra(TAKE_TYPE, IDCardDirection);
        intent.putExtra(IMAGE_PATH, filePath);
        activity.startActivityForResult(intent, IDCardDirection);
    }

    /**
     * 获取图片路径
     *
     * @param data Intent
     * @return 图片路径
     */
    public static String getImagePath(Intent data) {
        if (data != null) {
            return data.getStringExtra(IMAGE_PATH);
        }
        return "";
    }
}

