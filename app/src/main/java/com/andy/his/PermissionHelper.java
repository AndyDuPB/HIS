package com.andy.his;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class PermissionHelper
{

	private static final String TAG = "PermissionHelper";

	private final static int CAMERA_CODE = 101;

	private final static int READ_EXTERNAL_STORAGE_CODE = 102;

	private final static int WRITE_EXTERNAL_STORAGE_CODE = 103;

	private final static int READ_PHONE_STATE_CODE = 104;

	private final static int ACCESS_WIFI_STATE_CODE = 105;

	private final static int BLUETOOTH_CODE = 106;

	protected static final int REQUEST_OPEN_APPLICATION_SETTINGS_CODE = 1223;

	private ArrayList<PermissionModel> permissionModels;

	private Activity activity;

	private OnApplyPermissionListener mOnApplyPermissionListener;

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public PermissionHelper(Activity activity)
	{
		this.activity = activity;
		this.permissionModels = new ArrayList<PermissionHelper.PermissionModel>();

		this.permissionModels.add(new PermissionModel(this.activity.getString(R.string.permission_key_camera),
				Manifest.permission.CAMERA, this.activity.getString(R.string.permission_key_camera_message),
				CAMERA_CODE));

		this.permissionModels.add(new PermissionModel(this.activity.getString(R.string.permission_key_store),
				Manifest.permission.READ_EXTERNAL_STORAGE, this.activity
				.getString(R.string.permission_key_store_message), READ_EXTERNAL_STORAGE_CODE));

		this.permissionModels.add(new PermissionModel(this.activity.getString(R.string.permission_key_store),
				Manifest.permission.WRITE_EXTERNAL_STORAGE, this.activity
				.getString(R.string.permission_key_store_message), WRITE_EXTERNAL_STORAGE_CODE));


		this.permissionModels.add(new PermissionModel(this.activity.getString(R.string.permission_key_phone_status),
				Manifest.permission.READ_PHONE_STATE, this.activity
				.getString(R.string.permission_key_phone_message), READ_PHONE_STATE_CODE));


		this.permissionModels.add(new PermissionModel(this.activity.getString(R.string.permission_key_wifi_status),
				Manifest.permission.ACCESS_WIFI_STATE, this.activity
				.getString(R.string.permission_key_wifi_message), ACCESS_WIFI_STATE_CODE));


		this.permissionModels.add(new PermissionModel(this.activity.getString(R.string.permission_key_bluetooth_status),
				Manifest.permission.BLUETOOTH, this.activity
				.getString(R.string.permission_key_bluetooth_message), BLUETOOTH_CODE));
	}

	public void setOnApplyPermissionListener(OnApplyPermissionListener onApplyPermissionListener)
	{
		mOnApplyPermissionListener = onApplyPermissionListener;
	}

	public void applyPermissions()
	{
		try
		{
			for (final PermissionModel model : permissionModels)
			{
				if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(activity, model.permission))
				{
					ActivityCompat.requestPermissions(activity, new String[] { model.permission }, model.requestCode);
					return;
				}
			}
			if (mOnApplyPermissionListener != null)
			{
				mOnApplyPermissionListener.onAfterApplyAllPermission();
			}
		}
		catch (Throwable e)
		{
			Log.e(TAG, "", e);
		}
	}

	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		switch (requestCode)
		{
		case CAMERA_CODE:
		case WRITE_EXTERNAL_STORAGE_CODE:
		case READ_EXTERNAL_STORAGE_CODE:
		case READ_PHONE_STATE_CODE:
		case ACCESS_WIFI_STATE_CODE:
		case BLUETOOTH_CODE:
			if (PackageManager.PERMISSION_GRANTED != grantResults[0])
			{
				if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[0]))
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(activity)
							.setTitle(PermissionHelper.this.activity.getString(R.string.permission_settings_title))
							.setMessage(findPermissionExplain(permissions[0]))
							.setPositiveButton(
									PermissionHelper.this.activity.getString(R.string.permission_settings_ok),
									new DialogInterface.OnClickListener()
									{

										@Override
										public void onClick(DialogInterface dialog, int which)
										{
											applyPermissions();
										}
									});
					builder.setCancelable(false);
					builder.show();
				}
				else
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(activity)
							.setTitle(PermissionHelper.this.activity.getString(R.string.permission_settings_title))
							.setMessage(String.format(PermissionHelper.this.activity.getResources().getText(R.string.permission_settings_message).toString(), findPermissionName(permissions[0])))
							.setPositiveButton(
									PermissionHelper.this.activity.getString(R.string.permission_settings_set),
									new DialogInterface.OnClickListener()
									{
										@Override
										public void onClick(DialogInterface dialog, int which)
										{
											openApplicationSettings(REQUEST_OPEN_APPLICATION_SETTINGS_CODE);
										}
									})
							.setNegativeButton(
									PermissionHelper.this.activity.getString(R.string.permission_settings_cancel),
									new DialogInterface.OnClickListener()
									{
										@Override
										public void onClick(DialogInterface dialog, int which)
										{
											activity.finish();
										}
									});
					builder.setCancelable(false);
					builder.show();
				}
				return;
			}

			if (isAllRequestedPermissionGranted())
			{
				if (mOnApplyPermissionListener != null)
				{
					mOnApplyPermissionListener.onAfterApplyAllPermission();
				}
			}
			else
			{
				applyPermissions();
			}
			break;
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{

		switch (requestCode)
		{
		case REQUEST_OPEN_APPLICATION_SETTINGS_CODE:
			if (isAllRequestedPermissionGranted())
			{
				if (mOnApplyPermissionListener != null)
				{
					mOnApplyPermissionListener.onAfterApplyAllPermission();
				}
			}
			else
			{
				activity.finish();
			}
			break;
		}
	}

	/**
	 * 
	 * @return
	 */
	public boolean isAllRequestedPermissionGranted()
	{

		for (PermissionModel model : permissionModels)
		{
			if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(activity, model.permission))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * 打开应用设置界面
	 * 
	 * @param requestCode
	 *            请求码
	 * 
	 * @return
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private boolean openApplicationSettings(int requestCode)
	{
		try
		{
			Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:"
					+ activity.getPackageName()));
			intent.addCategory(Intent.CATEGORY_DEFAULT);

			activity.startActivityForResult(intent, requestCode);
			return true;
		}
		catch (Throwable e)
		{
			Log.e(TAG, "", e);
		}
		return false;
	}

	/**
	 * 查找申请权限的解释短语
	 * 
	 * @param permission
	 *            权限
	 * 
	 * @return
	 */
	private String findPermissionExplain(String permission)
	{
		if (permissionModels != null)
		{
			for (PermissionModel model : permissionModels)
			{
				if (model != null && model.permission != null && model.permission.equals(permission))
				{
					return model.explain;
				}
			}
		}
		return null;
	}

	/**
	 * 查找申请权限的名称
	 * 
	 * @param permission
	 *            权限
	 * 
	 * @return
	 */
	private String findPermissionName(String permission)
	{

		if (permissionModels != null)
		{
			for (PermissionModel model : permissionModels)
			{
				if (model != null && model.permission != null && model.permission.equals(permission))
				{
					return model.name;
				}
			}
		}
		return null;
	}

	private static class PermissionModel
	{

		/**
		 * 权限名称
		 */
		public String name;

		/**
		 * 请求的权限
		 */
		public String permission;

		/**
		 * 解析为什么请求这个权限
		 */
		public String explain;

		/**
		 * 请求代码
		 */
		public int requestCode;

		public PermissionModel(String name, String permission, String explain, int requestCode)
		{
			this.name = name;
			this.permission = permission;
			this.explain = explain;
			this.requestCode = requestCode;
		}
	}

	/**
	 * 权限申请事件监听
	 */
	public interface OnApplyPermissionListener
	{

		/**
		 * 申请所有权限之后的逻辑
		 */
		void onAfterApplyAllPermission();
	}

}
