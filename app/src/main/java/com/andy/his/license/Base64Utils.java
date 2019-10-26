package com.andy.his.license;


import com.andy.his.HomeSteadInformationHelper;

import org.apache.common.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Base64Utils {

	private static final int CACHE_SIZE = 1024;

	public static byte[] decode(String base64) throws Exception {

		return Base64.decodeBase64(base64.getBytes("GBK"));
	}

	public static String encode(byte[] bytes) throws Exception {

		return new String(Base64.encodeBase64(bytes));
	}

	public static String encodeFile(String filePath) throws Exception {

		String context = fileToByte(filePath);
		if(context == null)
		{
			context =  "";
		}
		return context;
	}

	public static String fileToByte(String filePath) throws Exception {

		String lineStr = null;
		BufferedReader reader = null;
		File file = new File(filePath);
		if (file.exists()) {
			try
			{
				reader = new BufferedReader(new FileReader(file));
				lineStr = reader.readLine();
			}
			catch (Exception e)
			{
			}
			finally {
				HomeSteadInformationHelper.closeStream(reader);
			}
		}
		return lineStr;
	}

}
