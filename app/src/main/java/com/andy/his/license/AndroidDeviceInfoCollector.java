package com.andy.his.license;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;

import org.apache.common.codec.digest.DigestUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AndroidDeviceInfoCollector {


	public static String getDeviceId(Context context) {

		if(PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE))
		{
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			String deviceId = telephonyManager.getDeviceId();
			if (deviceId == null) {
				return "871223111185";
			} else {
				return deviceId;
			}
		}

		return "";
	}


	public static String getWLANMACAddress(Context context)
	{
		if(PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE ))
		{
			WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
			String macAddress = wifiManager.getConnectionInfo().getMacAddress();
			if (macAddress == null) {
				return "851223111187";
			} else {
				return macAddress;
			}
		}

		return "";
	}

	public static String getBluetoothAddress(Context context)
	{
		if(PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH ))
		{
			BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			String address = bluetoothAdapter.getAddress();
			if (address == null) {
				return "851111122387";
			} else {
				return address;
			}
		}

		return "";
	}

	public static String getDeviceIDShort()
	{
		StringBuffer deviceIDShort = new StringBuffer();
		deviceIDShort.append("35");
		deviceIDShort.append(Build.BOARD.length() % 10);
		deviceIDShort.append(Build.BRAND.length() % 10);
		deviceIDShort.append(Build.BRAND.length() % 10);
		deviceIDShort.append(Build.CPU_ABI.length() % 10);
		deviceIDShort.append(Build.DEVICE.length() % 10);
		deviceIDShort.append(Build.DISPLAY.length() % 10);
		deviceIDShort.append(Build.HOST.length() % 10);
		deviceIDShort.append(Build.ID.length() % 10);
		deviceIDShort.append(Build.MANUFACTURER.length() % 10);
		deviceIDShort.append(Build.MODEL.length() % 10);
		deviceIDShort.append(Build.PRODUCT.length() % 10);
		deviceIDShort.append(Build.TAGS.length() % 10);
		deviceIDShort.append(Build.TYPE.length() % 10);
		deviceIDShort.append(Build.USER.length() % 10);

		return deviceIDShort.toString();
	}

	public static String getAndroidID(Context context) {

		return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
	}

	public static String getDeviceUUID(Context context)
	{
		String deviceLongID = getDeviceId(context) + getDeviceIDShort()
				+ getAndroidID(context)+ getWLANMACAddress(context) + getBluetoothAddress(context);
		deviceLongID = deviceLongID.replace(":","");

		MessageDigest messageDigest = null;
		try
		{
			messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.update(deviceLongID.getBytes(),0,deviceLongID.length());
			byte md5Data[] = messageDigest.digest();

			StringBuffer uniqueIDBuffer = new StringBuffer();

			for (int index = 0; index < md5Data.length; index++)
			{
				int bye = (0xFF & md5Data[index]);
				if (bye <= 0xF)
				{
					uniqueIDBuffer.append("0");
				}
				uniqueIDBuffer.append(Integer.toHexString(bye));
			}

			return uniqueIDBuffer.toString().toUpperCase();
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}

		return "";
	}

	public static String getLicenseSerialNumber(Context context) {

		return DigestUtils.md5Hex(getDeviceUUID(context)).toUpperCase();
	}

}
