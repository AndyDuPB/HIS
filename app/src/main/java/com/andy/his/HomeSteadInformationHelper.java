package com.andy.his;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.andy.his.homestead.HomeSteadTableDetail;
import com.andy.his.module.ModuleDetail;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class HomeSteadInformationHelper
{
	public static String getAltitudeCameraBaseDir()
	{
		File directory;
		if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
		{
			directory = Environment.getExternalStorageDirectory();
		}
		else
		{
			directory = Environment.getRootDirectory();
		}

		return directory.getAbsolutePath() + "/HIS/";
	}

	public static String getAltitudeCameraTempImageFile(String fileName)
	{
		return getAltitudeCameraTempImageDir() + "/" + fileName;
	}

	public static String getAltitudeCameraTempImageDir()
	{
		return HomeSteadInformationHelper.getAltitudeCameraBaseDir() + "TEMP";
	}

	public static String getLicenseFileDirectory()
	{
		return HomeSteadInformationHelper.getAltitudeCameraBaseDir() + "License";
	}

	public static String getLicenseFilePath()
	{
		return HomeSteadInformationHelper.getLicenseFileDirectory() + "/License.dat";
	}

	public static void deleteTempImageFiles()
	{
		File directory = new File(getAltitudeCameraTempImageDir());
		File[] childFiles = directory.listFiles();
		if(childFiles != null)
		{
			for (File file: childFiles)
			{
				if(file.exists() && file.isFile())
				{
					file.delete();
				}
			}
		}
	}

	public static void deleteDirectory(File directory) {

		if (!directory.exists()) return;

		if (directory.isFile() || directory.list() == null)
		{
			directory.delete();
		}
		else {
			File[] childFiles = directory.listFiles();
			if(childFiles != null)
			{
				for (File file : childFiles)
				{
					deleteDirectory(file);
				}
			}
			directory.delete();
		}
	}

	public static String getModuleInfo(ModuleDetail key)
	{
		return key.getModuleCounty() + "-" + key.getModuleTown() + "-" + key.getModuleVillage() + "-" + key.getModuleGroup();
	}

	public static String getHomeSteadInfo(HomeSteadTableDetail key)
	{
		return key.getModuleInfo() + "/" + key.getHomeSteadNumber() + "-" + key.getHouseHolder();
	}

	public static String getHomeSteadDirectoryPath(HomeSteadTableDetail key)
	{
		return HomeSteadInformationHelper.getAltitudeCameraBaseDir() +  HomeSteadInformationHelper.getHomeSteadInfo(key) ;
	}

	public static String getModuleInfoDirectoryPath(ModuleDetail key)
	{
		return HomeSteadInformationHelper.getAltitudeCameraBaseDir() +  HomeSteadInformationHelper.getModuleInfo(key) ;
	}

	public static void createDirectory(String basePath)
	{
		File baseDir = new File(basePath);
		if(!baseDir.exists())
		{
			baseDir.mkdirs();
		}
	}

	public static boolean saveFile(String content, File file) {

		BufferedWriter bufferedWriter = null;
		try
		{

			File dir = file.getParentFile();
			if(!dir.exists()){
				dir.mkdirs();
			}

			boolean existed = false;
			if (!file.exists()) {
				existed = file.createNewFile();
			} else {
				existed = true;
			}

			if (existed) {
				bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
				bufferedWriter.write(content);

				bufferedWriter.flush();
				bufferedWriter.close();

				return true;
			}
		}
		catch (Exception e)
		{
		}
		finally
		{
			closeStream(bufferedWriter);
		}

		return false;
	}

	public static boolean savePicture(Bitmap bitmap, File file)
	{
		FileOutputStream stream = null;
		try
		{
			File dir = file.getParentFile();
			if(!dir.exists()){
				dir.mkdirs();
			}

			boolean existed = false;
			if (!file.exists())
			{
				existed = file.createNewFile();
			}
			else
			{
				existed = true;
			}

			if (existed)
			{
				stream = new FileOutputStream(file);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

				stream.flush();
				stream.close();

				return true;
			}
		}
		catch (Exception e)
		{
		}
		finally
		{
			closeStream(stream);
		}

		return false;
	}

	public static void closeStream(Closeable closeable)
	{
		try
		{
			if(closeable != null)
			{
				closeable.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static Bitmap getBitmapByFilePath(String currentCameraPath) {

		Bitmap bitmap = null;
		FileInputStream inputStream = null;
		try
		{
			inputStream = new FileInputStream(currentCameraPath);
			bitmap = BitmapFactory.decodeStream(inputStream);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		finally
		{
			HomeSteadInformationHelper.closeStream(inputStream);
		}
		return bitmap;
	}

	public static void setListViewHeightBasedOnChildren(ListView listView) {

		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() + 3));
		listView.setLayoutParams(params);
	}

}