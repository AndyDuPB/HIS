package com.andy.his;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.andy.his.homestead.HomeSteadTableDetail;
import com.andy.his.module.ModuleDetail;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public static void mergeBitMap(final AppCompatActivity activity, final String basePath, final Map<String,String> cameraBitmapMap) {

		final Map<String, List<Bitmap>> mergeBitmapMap = new HashMap<>(4);
		final List<Bitmap> idCardGroup1 = new ArrayList<>(2);
		final List<Bitmap> idCardGroup2 = new ArrayList<>(2);
		final List<Bitmap> censusInfoGroup = new ArrayList<>(5);
		final List<Bitmap> homeSteadGroup = new ArrayList<>(3);
		new Thread(new Runnable() {
			@Override
			public void run() {
				String reg1 = "(.*)户主(.*)身份证(.*)";
				String reg2 = "(.*)代理人(.*)身份证(.*)";
				String reg3 = "(.*)户籍信息(.*)";
				String reg4 = "(.*)宅基地信息(.*)";
				for (Map.Entry<String, String> entry : cameraBitmapMap.entrySet()) {
					String key = entry.getKey();
					if (key.matches(reg1)) {
						Bitmap bitmap = BitmapFactory.decodeFile(basePath + key);
						if (key.contains("-01")) {
							idCardGroup1.add(0, bitmap);
						} else {
							idCardGroup1.add(bitmap);
						}
					} else if (key.matches(reg2)) {
						Bitmap bitmap = BitmapFactory.decodeFile(basePath + key);
						if (key.contains("-01")) {
							idCardGroup2.add(0, bitmap);
						} else {
							idCardGroup2.add(bitmap);
						}
					} else if (key.matches(reg3)) {
						Bitmap bitmap = BitmapFactory.decodeFile(basePath + key);
						if (key.contains("-01")) {
							censusInfoGroup.add(0, bitmap);
						} else {
							censusInfoGroup.add(bitmap);
						}
					} else if (key.matches(reg4)) {
						Bitmap bitmap = BitmapFactory.decodeFile(basePath + key);
						if (key.contains("-01")) {
							homeSteadGroup.add(0, bitmap);
						} else {
							homeSteadGroup.add(bitmap);
						}
					}
				}
				mergeBitmapMap.put(activity.getApplicationContext().getString(R.string.handlerInfo), idCardGroup1);
				mergeBitmapMap.put(activity.getApplicationContext().getString(R.string.agentInfo), idCardGroup2);
				mergeBitmapMap.put(activity.getApplicationContext().getString(R.string.censusInfo), censusInfoGroup);
				mergeBitmapMap.put(activity.getApplicationContext().getString(R.string.homeSteadInfo), homeSteadGroup);
				mergeBitmapByGroup(mergeBitmapMap, basePath);
			}
		}).start();
	}

	public static void mergeBitmapByGroup(Map<String, List<Bitmap>> mergeBitmapMap, String basePath) {
		String mergePath = "合并" + File.separator;
		File temp = new File(basePath + mergePath);//要保存文件先创建文件夹
		if (!temp.exists()) {
			temp.mkdir();
		}
		for (Map.Entry<String, List<Bitmap>> entry : mergeBitmapMap.entrySet()) {
			String key = entry.getKey();
			List<Bitmap> bitmapByGroupList = entry.getValue();
			if (bitmapByGroupList.size() == 2) {
				Bitmap bitmap1 = bitmapByGroupList.get(0);
				Bitmap bitmap2 = bitmapByGroupList.get(1);
				Bitmap newBmpGroup = newBitmap(bitmap1, bitmap2);
				save(newBmpGroup, new File(basePath + mergePath + key + "合并.jpg"), Bitmap.CompressFormat.JPEG, true);
				bitmap1.recycle();
				bitmap2.recycle();
			} else {
				for (int i = 0; i < bitmapByGroupList.size(); i = i + 2) {
					Bitmap bitmap1 = bitmapByGroupList.get(i);
					Bitmap bitmap2 = null;
					if (bitmapByGroupList.size() > (i + 1)) {
						bitmap2 = bitmapByGroupList.get(i + 1);
						Bitmap newBmpGroup = newBitmap(bitmap1, bitmap2);
						save(newBmpGroup, new File(basePath + mergePath + key + "合并_" + (i / 2 + 1) + ".jpg"), Bitmap.CompressFormat.JPEG, true);
						bitmap1.recycle();
						bitmap2.recycle();
					} else {
						save(bitmap1, new File(basePath + mergePath + key + "合并_" + (i / 2 + 1) + ".jpg"), Bitmap.CompressFormat.JPEG, true);
						bitmap1.recycle();
					}
				}
			}
		}
	}

    public static Bitmap newBitmap(Bitmap bmp1, Bitmap bmp2) {
		Bitmap retBmp = null;
		if (bmp1 != null && bmp2 != null) {
			int width = bmp1.getWidth() + 20;
			int bm1Andbm2Space = 50;
			//以第一张图片的宽度为标准，对第二张图片进行缩放。
			int h2 = bmp2.getHeight() + 10 * width / bmp2.getWidth();
			int totalHeight = bmp1.getHeight() + 10 + h2 + bm1Andbm2Space;
			retBmp = Bitmap.createBitmap(width, bmp1.getHeight() + h2 + bm1Andbm2Space, Bitmap.Config.ARGB_8888);

			//绘制白色矩形背景
			Canvas canvas = new Canvas(retBmp);
			Paint paint = new Paint();
			paint.setAlpha(0x40);
			paint.setColor(Color.WHITE);
			canvas.drawRect(0, 0, width, totalHeight, paint);

			Bitmap newSizeBmp2 = resizeBitmap(bmp2, width, h2);
			canvas.drawBitmap(bmp1, 0, 0, null);
			canvas.drawBitmap(newSizeBmp2, 0, bmp1.getHeight() + bm1Andbm2Space, null);
		}
		return retBmp;
	}

    public static Bitmap resizeBitmap(Bitmap bitmap, int newWidth, int newHeight) {
		float scaleWidth = ((float) newWidth) / bitmap.getWidth();
		float scaleHeight = ((float) newHeight) / bitmap.getHeight();
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap bmpScale = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		return bmpScale;
	}

	/**6
	 * 保存图片到文件File。
	 *
	 * @param src     源图片
	 * @param file    要保存到的文件
	 * @param format  格式
	 * @param recycle 是否回收
	 * @return true 成功 false 失败
	 */
    public static boolean save(Bitmap src, File file, Bitmap.CompressFormat format, boolean recycle) {
		if (isEmptyBitmap(src)) {
			return false;
		}
		OutputStream os;
		boolean ret = false;
		try {
			os = new BufferedOutputStream(new FileOutputStream(file));
			ret = src.compress(format, 100, os);
			if (recycle && !src.isRecycled())
				src.recycle();
		} catch (IOException e) {
		}

		return ret;
	}

	/**
	 * Bitmap对象是否为空。
	 */
    public static boolean isEmptyBitmap(Bitmap src) {
		return src == null || src.getWidth() == 0 || src.getHeight() == 0;
	}

}