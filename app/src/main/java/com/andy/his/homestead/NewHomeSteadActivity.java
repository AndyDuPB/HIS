package com.andy.his.homestead;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.andreabaccega.widget.FormEditText;
import com.andy.his.BuildConfig;
import com.andy.his.ExitApplication;
import com.andy.his.HomeSteadInformationHelper;
import com.andy.his.R;
import com.andy.his.module.ModuleSpinnerDetail;
import com.andy.his.sql.HISDatabaseFactory;
import com.andy.his.sql.HISDatabaseHelper;
import com.wildma.idcardcamera.camera.IDCardCamera;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NewHomeSteadActivity extends AppCompatActivity {

    Button btnSave;
    Button btnReset;

    Spinner moduleSpinner;
    TableLayout censusInfoForm;
    TableLayout homeSteadInfoForm;
    TableLayout otherInfoForm;

    private int CENSUS_INFO_NUMBER = 3;
    private static final int UP_LIMIT_CENSUS_INFO_NUMBER = 20;
    private static final int DOWN_LIMIT_CENSUS_INFO_NUMBER = 3;

    private int HOMESTEAD_INFO_NUMBER = 3;
    private static final int UP_LIMIT_HOMESTEAD_INFO_NUMBER = 20;
    private static final int DOWN_LIMIT_HOMESTEAD_INFO_NUMBER = 3;

    private int OTHER_INFO_NUMBER = 2;
    private static final int UP_LIMIT_OTHER_INFO_NUMBER = 10;
    private static final int DOWN_LIMIT_OTHER_INFO_NUMBER = 2;

    Map<String, String> cameraBitmapMap;

    Integer currentCameraImageButtonId;

    String currentCameraPath;

    boolean clickSaveBtn = false;

    public boolean isClickSaveBtn() {
        return clickSaveBtn;
    }

    public void setClickSaveBtn(boolean clickSaveBtn) {
        this.clickSaveBtn = clickSaveBtn;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_home_stead);
        ExitApplication.getInstance().addActivity(this);

        CENSUS_INFO_NUMBER = 3;
        HOMESTEAD_INFO_NUMBER = 3;
        OTHER_INFO_NUMBER = 2;
        cameraBitmapMap = new HashMap<String, String>();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                setResult(0);
                finish();
            }
        });

        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                ModuleSpinnerDetail spinnerDetail = (ModuleSpinnerDetail) moduleSpinner.getSelectedItem();
                                if (spinnerDetail == null) {
                                    Toast.makeText(NewHomeSteadActivity.this, getText(R.string.need_create_module_info), Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                boolean allValid = true;
                                FormEditText[] allFields = {findViewById(R.id.homeSteadNumber), findViewById(R.id.houseHolder), findViewById(R.id.holderPhone)};
                                for (FormEditText field : allFields) {
                                    if (!field.testValidity()) {
                                        allValid = false;
                                        break;
                                    }
                                }

                                if (allValid) {
                                    saveHomeStead();
                                }
                            }
                        });
                    }
                }).start();
            }
        });

        initResetButton();
        initHomeSteadView();
        initModuleSpinnerView();
        setPictureEditTextNotEditable();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void initModuleSpinnerView() {

        moduleSpinner = (Spinner) findViewById(R.id.moduleInfo);
        List<ModuleSpinnerDetail> moduleSpinnerDetails = getModuleSpinnerDetails();
        ArrayAdapter<ModuleSpinnerDetail> adapter = new ArrayAdapter<ModuleSpinnerDetail>(NewHomeSteadActivity.this, R.layout.module_spinner_item, moduleSpinnerDetails);
        adapter.setDropDownViewResource(R.layout.module_list_item_single_choice);
        moduleSpinner.setAdapter(adapter);
    }

    private ArrayList<ModuleSpinnerDetail> getModuleSpinnerDetails() {

        ArrayList<ModuleSpinnerDetail> moduleSpinnerDetails = new ArrayList<ModuleSpinnerDetail>();
        ModuleSpinnerDetail allModuleSpinner = new ModuleSpinnerDetail();

        SQLiteDatabase readableDatabase = HISDatabaseFactory.getReadableSqLiteDatabase(getApplicationContext());
        try {
            Cursor cursor = readableDatabase.rawQuery("SELECT moduleID, (moduleCounty || '-' || moduleTown ||  '-' || moduleVillage || '-' ||  moduleGroup) AS showText FROM MODULE ", new String[]{});
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    ModuleSpinnerDetail bean = new ModuleSpinnerDetail();
                    bean.setModuleID(cursor.getInt(0));
                    bean.setShowText(cursor.getString(1));
                    moduleSpinnerDetails.add(bean);
                }
                cursor.close();
            }
        } catch (Exception ex) {
            Toast.makeText(NewHomeSteadActivity.this, getText(R.string.failure_query_module), Toast.LENGTH_SHORT).show();
        } finally {
            readableDatabase.close();
        }

        return moduleSpinnerDetails;
    }

    @SuppressLint("StringFormatInvalid")
    public void saveHomeStead() {
        if (clickSaveBtn) {
            return;
        }

        setClickSaveBtn(true);

        ModuleSpinnerDetail spinnerDetail = (ModuleSpinnerDetail) moduleSpinner.getSelectedItem();
        String moduleInfo = spinnerDetail.getShowText();
        String homeSteadNumber = ((EditText) findViewById(R.id.homeSteadNumber)).getText().toString();
        String houseHolder = ((EditText) findViewById(R.id.houseHolder)).getText().toString();
        String holderPhone = ((EditText) findViewById(R.id.holderPhone)).getText().toString();
        String basePath = getHomeSteadBasePath(moduleInfo, homeSteadNumber, houseHolder);
        HomeSteadInformationHelper.createDirectory(basePath);

        String homeSteadId = UUID.randomUUID().toString();
        List<ContentValues> homeSteadContentValues = getHomeSteadContentValues(homeSteadId);
        saveHomeStead2DB(homeSteadContentValues);

        boolean saveFlag = true;
        for (ContentValues values : homeSteadContentValues) {
            String key = values.getAsString(HomeSteadConstant.KEY_HOMESTEAD_TYPE);
            if (key.equals(HomeSteadConstant.HOME_STEAD_TYPE_HANDLER) || key.equals(HomeSteadConstant.HOME_STEAD_TYPE_AGENT)
                    || key.equals(HomeSteadConstant.HOME_STEAD_TYPE_CENSUS) || key.equals(HomeSteadConstant.HOME_STEAD_TYPE_HOMESTEAD)
                    || key.equals(HomeSteadConstant.HOME_STEAD_TYPE_OTHERS)) {
                String fileName = values.getAsString(HomeSteadConstant.KEY_HOMESTEAD_VALUE);
                if (!"".equals(fileName) && cameraBitmapMap.containsKey(fileName)) {
                    String filePath = cameraBitmapMap.get(fileName);
                    if (filePath != null && !filePath.equals("")) {
                        boolean currentSaveFlag = new File(filePath).renameTo(new File(basePath + fileName));
                        //boolean currentSaveFlag = HomeSteadInformationHelper.savePicture(bitmap, new File(basePath + fileName));
                        if (currentSaveFlag) {
                            Log.e("NewHomeSteadActivity", fileName + " picture save success.");
                        } else {
                            Log.e("NewHomeSteadActivity", fileName + " picture save failure.");
                        }
                        saveFlag = saveFlag && currentSaveFlag;
                    }
                }
            } else if (key.equals(HomeSteadConstant.HOME_STEAD_TYPE_NOTE)) {
                String fileName = getResources().getString(R.string.homestead_note_filename);
                fileName = MessageFormat.format(fileName, homeSteadNumber, houseHolder);
                boolean currentSaveFlag = HomeSteadInformationHelper.saveFile(((EditText) findViewById(R.id.noteInfo)).getText().toString(), new File(basePath + fileName));
                if (currentSaveFlag) {
                    Log.e("NewHomeSteadActivity", fileName + " note file save success.");
                } else {
                    Log.e("NewHomeSteadActivity", fileName + " note file save failure.");
                }
                saveFlag = saveFlag && currentSaveFlag;
            }
        }

        if (saveFlag) {
            combinBitMap(basePath);
            setClickSaveBtn(false);
            showSuccessMessage();
            setResult(0);
            finish();
        } else {
            setClickSaveBtn(false);
            showErrorMessage();
        }
    }

    public void saveBitmapFile(Map<String, Bitmap> bitmapMap, String filePath) {
        for (Map.Entry<String, Bitmap> entry : bitmapMap.entrySet()) {
            Bitmap combinPic = entry.getValue();
            String fileName = entry.getKey();
            File temp = new File(filePath);//要保存文件先创建文件夹
            if (!temp.exists()) {
                temp.mkdir();
            }
            //重复保存时，覆盖原同名图片
            File file = new File(filePath + fileName);//将要保存图片的路径和图片名称
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                combinPic.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                bos.flush();
                bos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void combinBitMap(final String basePath) {
        final Map<String, List<Bitmap>> combinBitmapMap = new HashMap<>(4);
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
                combinBitmapMap.put(getApplicationContext().getString(R.string.handlerInfo), idCardGroup1);
                combinBitmapMap.put(getApplicationContext().getString(R.string.agentInfo), idCardGroup2);
                combinBitmapMap.put(getApplicationContext().getString(R.string.censusInfo), censusInfoGroup);
                combinBitmapMap.put(getApplicationContext().getString(R.string.homeSteadInfo), homeSteadGroup);
                combineBitmapByGroup(combinBitmapMap, basePath);
            }
        }).start();
    }

    private void combineBitmapByGroup(Map<String, List<Bitmap>> combinBitmapMap, String basePath) {
        String combinePath = "合并" + File.separator;
        File temp = new File(basePath + combinePath);//要保存文件先创建文件夹
        if (!temp.exists()) {
            temp.mkdir();
        }
        for (Map.Entry<String, List<Bitmap>> entry : combinBitmapMap.entrySet()) {
            String key = entry.getKey();
            List<Bitmap> bitmapByGroupList = entry.getValue();
            if (bitmapByGroupList.size() == 2) {
                Bitmap newBmpGroup = newBitmap(bitmapByGroupList.get(0), bitmapByGroupList.get(1));
                save(newBmpGroup, new File(basePath + combinePath + key + "合并.jpg"), Bitmap.CompressFormat.JPEG, true);
            } else {
                for (int i = 0; i < bitmapByGroupList.size(); i = i + 2) {
                    Bitmap bitmap1 = bitmapByGroupList.get(i);
                    Bitmap bitmap2 = null;
                    if (bitmapByGroupList.size() > (i + 1)) {
                        bitmap2 = bitmapByGroupList.get(i + 1);
                        Bitmap newBmpGroup = newBitmap(bitmap1, bitmap2);
                        save(newBmpGroup, new File(basePath +  combinePath + key + "合并_" + (i / 2 + 1) + ".jpg"), Bitmap.CompressFormat.JPEG, true);
                    } else {
                        save(bitmap1, new File(basePath + key + "合并_" + (i / 2 + 1) + ".jpg"), Bitmap.CompressFormat.JPEG, true);
                    }
                }
            }
        }
    }

    public static Bitmap newBitmap(Bitmap bmp1, Bitmap bmp2) {
        Bitmap retBmp = null;
        if (bmp1 != null && bmp2 != null) {
            int width = bmp1.getWidth();
            int bm1Andbm2Space = 50;
            if (bmp2.getWidth() != width) {
                //以第一张图片的宽度为标准，对第二张图片进行缩放。
                int h2 = bmp2.getHeight() * width / bmp2.getWidth();
                int totalHeight = bmp1.getHeight() + h2 + bm1Andbm2Space;
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
            } else {
                //两张图片宽度相等，则直接拼接。
                retBmp = Bitmap.createBitmap(width, bmp1.getHeight() + bmp2.getHeight() + bm1Andbm2Space, Bitmap.Config.ARGB_8888);
                //绘制白色矩形背景
                Canvas canvas = new Canvas(retBmp);
                Paint paint = new Paint();
                paint.setAlpha(0x40);
                paint.setColor(Color.WHITE);
                canvas.drawRect(0, 0, width, bmp1.getHeight() + bmp2.getHeight(), paint);

                canvas.drawBitmap(bmp1, 0, 0, null);
                canvas.drawBitmap(bmp2, 0, bmp1.getHeight() + bm1Andbm2Space, null);
            }
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

    /**
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
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * Bitmap对象是否为空。
     */
    public static boolean isEmptyBitmap(Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }

    private String getHomeSteadBasePath(String moduleInfo, String homeSteadNumber, String houseHolder) {
        return HomeSteadInformationHelper.getAltitudeCameraBaseDir() + moduleInfo + "/" + homeSteadNumber + "-" + houseHolder + "/";
    }

    private void saveHomeStead2DB(List<ContentValues> homeSteadList) {

        SQLiteDatabase writableDatabase = HISDatabaseFactory.getWritableSqLiteDatabase(getApplicationContext());
        writableDatabase.beginTransaction();
        try {
            for (ContentValues homeStead : homeSteadList) {
                writableDatabase.insert(HISDatabaseHelper.TABLE_HOMESTEAD_NAME, null, homeStead);
            }
            writableDatabase.setTransactionSuccessful();
        } catch (Exception ex) {
            showErrorMessage();
        } finally {
            writableDatabase.endTransaction();
            writableDatabase.close();
        }
    }

    private void showSuccessMessage() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(NewHomeSteadActivity.this, getText(R.string.success_created_homestead), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showErrorMessage() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(NewHomeSteadActivity.this, getText(R.string.failure_created_homestead), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initResetButton() {

        btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                resetAllFields();
            }
        });
    }

    private void initHomeSteadView() {

        findViewById(R.id.agentCheckbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    findViewById(R.id.agentRow01).setVisibility(View.VISIBLE);
                    findViewById(R.id.agentRow02).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.agentRow01).setVisibility(View.GONE);
                    findViewById(R.id.agentRow02).setVisibility(View.GONE);
                    ((EditText) findViewById(R.id.agentIDCard01)).setText("");
                    ((EditText) findViewById(R.id.agentIDCard02)).setText("");
                }
            }
        });

        findViewById(R.id.otherCheckbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    findViewById(R.id.btnOtherAdd).setVisibility(View.VISIBLE);
                    findViewById(R.id.btnOtherDelete).setVisibility(View.VISIBLE);
                    findViewById(R.id.otherRow01).setVisibility(View.VISIBLE);
                    findViewById(R.id.otherRow02).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.btnOtherAdd).setVisibility(View.INVISIBLE);
                    findViewById(R.id.btnOtherDelete).setVisibility(View.INVISIBLE);
                    findViewById(R.id.otherRow01).setVisibility(View.INVISIBLE);
                    for (int i = 2; i <= UP_LIMIT_OTHER_INFO_NUMBER; i++) {
                        String key = getNumberKey(i);
                        int tableRowId = getResources().getIdentifier("otherRow" + key, "id", NewHomeSteadActivity.this.getPackageName());
                        if (findViewById(tableRowId) != null) {
                            findViewById(tableRowId).setVisibility(View.GONE);
                        }
                    }

                    for (int i = 1; i <= UP_LIMIT_OTHER_INFO_NUMBER; i++) {
                        String key = getNumberKey(i);
                        int editTextId = getResources().getIdentifier("other" + key, "id", NewHomeSteadActivity.this.getPackageName());
                        if (findViewById(editTextId) != null) {
                            ((EditText) findViewById(editTextId)).setText("");
                        }
                    }
                }
            }
        });

        findViewById(R.id.noteCheckbox).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    findViewById(R.id.noteInfoRow).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.noteInfoRow).setVisibility(View.GONE);
                    ((EditText) findViewById(R.id.noteInfo)).setText("");
                }
            }
        });

        censusInfoForm = findViewById(R.id.censusInfoForm);
        homeSteadInfoForm = findViewById(R.id.homeSteadInfoForm);
        otherInfoForm = findViewById(R.id.otherInfoForm);
    }

    private String getNumberKey(int i) {

        String key = String.valueOf(i);
        if (i < 10) {
            key = "0" + key;
        }
        return key;
    }

    private void addNewHomeSteadRow() {

        if (HOMESTEAD_INFO_NUMBER < UP_LIMIT_HOMESTEAD_INFO_NUMBER) {
            HOMESTEAD_INFO_NUMBER++;
        }
        setTableRowVisibility(HOMESTEAD_INFO_NUMBER, "homeSteadRow", true);
    }

    private void addNewOtherRow() {

        if (OTHER_INFO_NUMBER < UP_LIMIT_OTHER_INFO_NUMBER) {
            OTHER_INFO_NUMBER++;
        }
        setTableRowVisibility(OTHER_INFO_NUMBER, "otherRow", true);
    }

    private void addNewCensusRow() {

        if (CENSUS_INFO_NUMBER < UP_LIMIT_CENSUS_INFO_NUMBER) {
            CENSUS_INFO_NUMBER++;
        }
        setTableRowVisibility(CENSUS_INFO_NUMBER, "censusRow", true);
    }

    public void addButtonClickHandler(View view) {
        ImageButton button = (ImageButton) view;
        if (R.id.btnCensusAdd == button.getId()) {
            addNewCensusRow();
        } else if (R.id.btnHomeSteadAdd == button.getId()) {
            addNewHomeSteadRow();
        } else if (R.id.btnOtherAdd == button.getId()) {
            addNewOtherRow();
        }
    }

    public void cameraButtonClickHandler(View view) {
        ImageButton button = (ImageButton) view;

        currentCameraImageButtonId = button.getId();
        currentCameraPath = HomeSteadInformationHelper.getAltitudeCameraTempImageFile(generatePictureNameByCameraID(currentCameraImageButtonId, false));
        HomeSteadInformationHelper.createDirectory(HomeSteadInformationHelper.getAltitudeCameraTempImageDir());

        if (currentCameraImageButtonId == getResources().getIdentifier("btnHandlerIDCardCamera01", "id", NewHomeSteadActivity.this.getPackageName())
                || currentCameraImageButtonId == getResources().getIdentifier("btnAgentIDCardCamera01", "id", NewHomeSteadActivity.this.getPackageName())) {
            IDCardCamera.create(this).openCamera(IDCardCamera.TYPE_IDCARD_FRONT, currentCameraPath);
        } else if (currentCameraImageButtonId == getResources().getIdentifier("btnHandlerIDCardCamera02", "id", NewHomeSteadActivity.this.getPackageName())
                || currentCameraImageButtonId == getResources().getIdentifier("btnAgentIDCardCamera02", "id", NewHomeSteadActivity.this.getPackageName())) {
            IDCardCamera.create(this).openCamera(IDCardCamera.TYPE_IDCARD_BACK, currentCameraPath);
        } else {
            Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(NewHomeSteadActivity.this, BuildConfig.APPLICATION_ID + ".FileProvider", new File(currentCameraPath));
                openCameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                openCameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                uri = Uri.fromFile(new File(currentCameraPath));
            }
            openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

            startActivityForResult(openCameraIntent, 200);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 200 || resultCode == IDCardCamera.RESULT_CODE) {
            String fileName = generatePictureNameByCameraID(currentCameraImageButtonId, true);
            if (fileName != null && !"".equals(fileName) && new File(currentCameraPath).exists()) {
                cameraBitmapMap.put(fileName, currentCameraPath);
            }
        }

        currentCameraPath = null;
        currentCameraImageButtonId = null;
    }

    private String generatePictureNameByCameraID(Integer imageButtonID, boolean needSetValue) {

        String fileName;
        //Handler Info
        fileName = generatePictureNameAndSetValue2EditText(imageButtonID, 2, "btnHandlerIDCardCamera", HomeSteadConstant.HOME_STEAD_TYPE_HANDLER, R.string.handlerInfo, "handlerIDCard", needSetValue);
        if (fileName != null) {
            return fileName;
        }

        //Agent Info
        if (((CheckBox) findViewById(R.id.agentCheckbox)).isChecked()) {
            fileName = generatePictureNameAndSetValue2EditText(imageButtonID, 2, "btnAgentIDCardCamera", HomeSteadConstant.HOME_STEAD_TYPE_AGENT, R.string.agentInfo, "agentIDCard", needSetValue);
            if (fileName != null) {
                return fileName;
            }
        }

        //Census Info
        fileName = generatePictureNameAndSetValue2EditText(imageButtonID, UP_LIMIT_CENSUS_INFO_NUMBER, "btnCensusCamera", HomeSteadConstant.HOME_STEAD_TYPE_CENSUS, R.string.censusInfo, "census", needSetValue);
        if (fileName != null) {
            return fileName;
        }

        //HomeStead Info
        fileName = generatePictureNameAndSetValue2EditText(imageButtonID, UP_LIMIT_HOMESTEAD_INFO_NUMBER, "btnHomeSteadCamera", HomeSteadConstant.HOME_STEAD_TYPE_HOMESTEAD, R.string.homeSteadInfo, "homeStead", needSetValue);
        if (fileName != null) {
            return fileName;
        }

        //Others Info
        if (((CheckBox) findViewById(R.id.otherCheckbox)).isChecked()) {
            fileName = generatePictureNameAndSetValue2EditText(imageButtonID, UP_LIMIT_OTHER_INFO_NUMBER, "btnOtherCamera", HomeSteadConstant.HOME_STEAD_TYPE_OTHERS, R.string.othersInfo, "other", needSetValue);
            if (fileName != null) {
                return fileName;
            }
        }

        return null;
    }

    private String generatePictureNameAndSetValue2EditText(int imageButtonID, int maxNumber, String previewName, String homeSteadType, int recourseId, String editViewPreviewId, boolean needSetValue) {
        for (int i = 1; i <= maxNumber; i++) {
            String key = getNumberKey(i);
            int cameraButtonId = getResources().getIdentifier(previewName + key, "id", NewHomeSteadActivity.this.getPackageName());
            if (cameraButtonId == imageButtonID) {
                String fileNameText = getJPGName(homeSteadType, recourseId, key);
                int editTextId = getResources().getIdentifier(editViewPreviewId + key, "id", NewHomeSteadActivity.this.getPackageName());
                if (findViewById(editTextId) != null && needSetValue) {
                    ((EditText) findViewById(editTextId)).setText(fileNameText);
                    ((EditText) findViewById(editTextId)).requestFocus();
                }
                return fileNameText;
            }
        }

        return null;
    }

    private String getJPGName(String homeSteadType, int recourseId, String key) {

        return homeSteadType + "-" + getResources().getString(recourseId).toString() + "-" + key + ".jpg";
    }

    public void previewButtonClickHandler(View view) {
        String fileName = "";
        ImageButton button = (ImageButton) view;

        View v;
        TableRow tableRow;
        if (button.getParent() instanceof TableRow) {
            tableRow = (TableRow) button.getParent();
            for (int i = 0; i < tableRow.getChildCount(); i++) {
                v = tableRow.getChildAt(i);
                if (v instanceof EditText) {
                    fileName = ((EditText) v).getText().toString();
                    break;
                }
            }
        }

        if (!"".equals(fileName) && cameraBitmapMap.containsKey(fileName)) {
            String filePath = cameraBitmapMap.get(fileName);
            if (filePath != null && !filePath.equals("")) {
                Intent imagePreviewIntent = new Intent(NewHomeSteadActivity.this, ImagePreviewActivity.class);
                imagePreviewIntent.putExtra("filePath", filePath);
                startActivity(imagePreviewIntent);
            } else {
                Toast.makeText(NewHomeSteadActivity.this, getResources().getString(R.string.no_picture_to_review),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(NewHomeSteadActivity.this, getResources().getString(R.string.no_picture_to_review),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteButtonClickHandler(View view) {
        ImageButton button = (ImageButton) view;
        if (getResources().getString(R.string.delete_census_desc).equals(button.getContentDescription().toString())) {
            deleteNewCensusRow();
        } else if (getResources().getString(R.string.delete_homestead_desc).equals(button.getContentDescription().toString())) {
            deleteNewHomeSteadRow();
        } else if (getResources().getString(R.string.delete_other_desc).equals(button.getContentDescription().toString())) {
            deleteNewOtherRow();
        }
    }

    private void deleteNewHomeSteadRow() {

        if (HOMESTEAD_INFO_NUMBER > DOWN_LIMIT_HOMESTEAD_INFO_NUMBER) {
            setTableRowVisibility(HOMESTEAD_INFO_NUMBER, "homeSteadRow", false);
            setEditTextEmptyValue(HOMESTEAD_INFO_NUMBER, "homeStead");
            HOMESTEAD_INFO_NUMBER--;
        }
    }

    private void deleteNewOtherRow() {

        if (OTHER_INFO_NUMBER > DOWN_LIMIT_OTHER_INFO_NUMBER) {
            setTableRowVisibility(OTHER_INFO_NUMBER, "otherRow", false);
            setEditTextEmptyValue(OTHER_INFO_NUMBER, "other");
            OTHER_INFO_NUMBER--;
        }
    }

    private void deleteNewCensusRow() {

        if (CENSUS_INFO_NUMBER > DOWN_LIMIT_CENSUS_INFO_NUMBER) {
            setTableRowVisibility(CENSUS_INFO_NUMBER, "censusRow", false);
            setEditTextEmptyValue(CENSUS_INFO_NUMBER, "census");
            CENSUS_INFO_NUMBER--;
        }
    }

    private void setEditTextEmptyValue(int rowNumber, String rowPreviewKey) {
        String key = getNumberKey(rowNumber);
        int editTextId = getResources().getIdentifier(rowPreviewKey + key, "id", NewHomeSteadActivity.this.getPackageName());
        if (findViewById(editTextId) != null && findViewById(editTextId) instanceof EditText) {
            ((EditText) findViewById(editTextId)).setText("");
        }
    }

    private void setTableRowVisibility(int rowNumber, String rowPreviewKey, boolean visibility) {
        String key = getNumberKey(rowNumber);
        int tableRowId = getResources().getIdentifier(rowPreviewKey + key, "id", NewHomeSteadActivity.this.getPackageName());
        if (findViewById(tableRowId) != null) {
            if (visibility) {
                findViewById(tableRowId).setVisibility(View.VISIBLE);
            } else {
                findViewById(tableRowId).setVisibility(View.GONE);
            }
        }
    }

    private List<ContentValues> getHomeSteadContentValues(String homeSteadId) {

        List<ContentValues> homeSteadContentValues = new ArrayList<ContentValues>();

        ModuleSpinnerDetail spinnerDetail = (ModuleSpinnerDetail) moduleSpinner.getSelectedItem();
        int moduleID = spinnerDetail.getModuleID();
        String moduleInfo = spinnerDetail.getShowText();

        //Basic Info
        homeSteadContentValues.add(getModuleInfoContentValues(homeSteadId, moduleID, moduleInfo));
        homeSteadContentValues.add(getHomeSteadNumberContentValues(homeSteadId, moduleID));
        homeSteadContentValues.add(getHouseHolderContentValues(homeSteadId, moduleID));
        homeSteadContentValues.add(getHolderPhoneContentValues(homeSteadId, moduleID));

        //Handler Info
        addContentValuesByHomeSteadType(2, "handlerIDCard", homeSteadId, moduleID, HomeSteadConstant.HOME_STEAD_TYPE_HANDLER, homeSteadContentValues);

        //Agent Info
        if (((CheckBox) findViewById(R.id.agentCheckbox)).isChecked()) {
            addContentValuesByHomeSteadType(2, "agentIDCard", homeSteadId, moduleID, HomeSteadConstant.HOME_STEAD_TYPE_AGENT, homeSteadContentValues);
        }

        //Census Info
        addContentValuesByHomeSteadType(UP_LIMIT_CENSUS_INFO_NUMBER, "census", homeSteadId, moduleID, HomeSteadConstant.HOME_STEAD_TYPE_CENSUS, homeSteadContentValues);

        //HomeStead Info
        addContentValuesByHomeSteadType(UP_LIMIT_HOMESTEAD_INFO_NUMBER, "homeStead", homeSteadId, moduleID, HomeSteadConstant.HOME_STEAD_TYPE_HOMESTEAD, homeSteadContentValues);

        //Others Info
        if (((CheckBox) findViewById(R.id.otherCheckbox)).isChecked()) {
            addContentValuesByHomeSteadType(UP_LIMIT_OTHER_INFO_NUMBER, "other", homeSteadId, moduleID, HomeSteadConstant.HOME_STEAD_TYPE_OTHERS, homeSteadContentValues);
        }

        //Notes Info
        if (((CheckBox) findViewById(R.id.noteCheckbox)).isChecked()) {
            homeSteadContentValues.add(getNoteInfoContentValues(homeSteadId, moduleID));
        }

        return homeSteadContentValues;
    }

    private ContentValues getNoteInfoContentValues(String homeSteadId, int moduleID) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(HomeSteadConstant.KEY_HOMESTEAD_ID, homeSteadId);
        contentValues.put(HomeSteadConstant.KEY_MODULE_ID, String.valueOf(moduleID));
        contentValues.put(HomeSteadConstant.KEY_HOMESTEAD_TYPE, HomeSteadConstant.HOME_STEAD_TYPE_NOTE);
        contentValues.put(HomeSteadConstant.KEY_HOMESTEAD_KEY, "noteInfo");
        contentValues.put(HomeSteadConstant.KEY_HOMESTEAD_VALUE, ((EditText) findViewById(R.id.noteInfo)).getText().toString());

        return contentValues;
    }

    private void addContentValuesByHomeSteadType(int maxNumber, String previewKey, String homeSteadId, int moduleID, String homeSteadType, List<ContentValues> homeSteadContentValues) {
        for (int i = 1; i <= maxNumber; i++) {
            String key = getNumberKey(i);
            String idStr = previewKey.concat(key);
            int editTextId = getResources().getIdentifier(idStr, "id", NewHomeSteadActivity.this.getPackageName());
            if (findViewById(editTextId) != null && findViewById(editTextId).getParent() instanceof TableRow && ((View) (((EditText) findViewById(editTextId)).getParent())).getVisibility() == View.VISIBLE) {

                ContentValues contentValues = new ContentValues();
                contentValues.put(HomeSteadConstant.KEY_HOMESTEAD_ID, homeSteadId);
                contentValues.put(HomeSteadConstant.KEY_MODULE_ID, String.valueOf(moduleID));
                contentValues.put(HomeSteadConstant.KEY_HOMESTEAD_TYPE, homeSteadType);
                contentValues.put(HomeSteadConstant.KEY_HOMESTEAD_KEY, idStr);
                contentValues.put(HomeSteadConstant.KEY_HOMESTEAD_VALUE, ((EditText) findViewById(editTextId)).getText().toString());

                homeSteadContentValues.add(contentValues);
            }
        }
    }

    private ContentValues getHouseHolderContentValues(String homeSteadId, int moduleID) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(HomeSteadConstant.KEY_HOMESTEAD_ID, homeSteadId);
        contentValues.put(HomeSteadConstant.KEY_MODULE_ID, String.valueOf(moduleID));
        contentValues.put(HomeSteadConstant.KEY_HOMESTEAD_TYPE, HomeSteadConstant.HOME_STEAD_TYPE_BASIC);
        contentValues.put(HomeSteadConstant.KEY_HOMESTEAD_KEY, "houseHolder");
        contentValues.put(HomeSteadConstant.KEY_HOMESTEAD_VALUE, ((EditText) findViewById(R.id.houseHolder)).getText().toString());

        return contentValues;
    }

    private ContentValues getHolderPhoneContentValues(String homeSteadId, int moduleID) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(HomeSteadConstant.KEY_HOMESTEAD_ID, homeSteadId);
        contentValues.put(HomeSteadConstant.KEY_MODULE_ID, String.valueOf(moduleID));
        contentValues.put(HomeSteadConstant.KEY_HOMESTEAD_TYPE, HomeSteadConstant.HOME_STEAD_TYPE_BASIC);
        contentValues.put(HomeSteadConstant.KEY_HOMESTEAD_KEY, "holderPhone");
        contentValues.put(HomeSteadConstant.KEY_HOMESTEAD_VALUE, ((EditText) findViewById(R.id.holderPhone)).getText().toString());

        return contentValues;
    }

    private ContentValues getHomeSteadNumberContentValues(String homeSteadId, int moduleID) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(HomeSteadConstant.KEY_HOMESTEAD_ID, homeSteadId);
        contentValues.put(HomeSteadConstant.KEY_MODULE_ID, String.valueOf(moduleID));
        contentValues.put(HomeSteadConstant.KEY_HOMESTEAD_TYPE, HomeSteadConstant.HOME_STEAD_TYPE_BASIC);
        contentValues.put(HomeSteadConstant.KEY_HOMESTEAD_KEY, "homeSteadNumber");
        contentValues.put(HomeSteadConstant.KEY_HOMESTEAD_VALUE, ((EditText) findViewById(R.id.homeSteadNumber)).getText().toString());

        return contentValues;
    }

    private ContentValues getModuleInfoContentValues(String homeSteadId, int moduleID, String moduleInfo) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(HomeSteadConstant.KEY_HOMESTEAD_ID, homeSteadId);
        contentValues.put(HomeSteadConstant.KEY_MODULE_ID, String.valueOf(moduleID));
        contentValues.put(HomeSteadConstant.KEY_HOMESTEAD_TYPE, HomeSteadConstant.HOME_STEAD_TYPE_BASIC);
        contentValues.put(HomeSteadConstant.KEY_HOMESTEAD_KEY, "moduleInfo");
        contentValues.put(HomeSteadConstant.KEY_HOMESTEAD_VALUE, moduleInfo);

        return contentValues;
    }

    public void resetAllFields() {
        //Basic Info
        ((EditText) findViewById(R.id.homeSteadNumber)).setText("");
        ((EditText) findViewById(R.id.houseHolder)).setText("");

        //Handler Info
        ((EditText) findViewById(R.id.handlerIDCard01)).setText("");
        ((EditText) findViewById(R.id.handlerIDCard02)).setText("");

        //Agent Info
        ((EditText) findViewById(R.id.agentIDCard01)).setText("");
        ((EditText) findViewById(R.id.agentIDCard02)).setText("");

        //Census Info
        for (int i = 1; i <= UP_LIMIT_CENSUS_INFO_NUMBER; i++) {
            String key = getNumberKey(i);
            int tableRowId = getResources().getIdentifier("census" + key, "id", NewHomeSteadActivity.this.getPackageName());
            if (findViewById(tableRowId) != null) {
                ((EditText) findViewById(tableRowId)).setText("");
            }
        }

        //HomeStead Info
        for (int i = 1; i <= UP_LIMIT_HOMESTEAD_INFO_NUMBER; i++) {
            String key = getNumberKey(i);
            int tableRowId = getResources().getIdentifier("homeStead" + key, "id", NewHomeSteadActivity.this.getPackageName());
            if (findViewById(tableRowId) != null) {
                ((EditText) findViewById(tableRowId)).setText("");
            }
        }

        //Others Info
        for (int i = 1; i <= UP_LIMIT_OTHER_INFO_NUMBER; i++) {
            String key = getNumberKey(i);
            int tableRowId = getResources().getIdentifier("other" + key, "id", NewHomeSteadActivity.this.getPackageName());
            if (findViewById(tableRowId) != null) {
                ((EditText) findViewById(tableRowId)).setText("");
            }
        }

        //Notes Info
        ((EditText) findViewById(R.id.noteInfo)).setText("");
    }

    private void setPictureEditTextNotEditable() {
        //Handler Info
        setEditTextNotEditable(2, "handlerIDCard");

        //Agent Info
        setEditTextNotEditable(2, "agentIDCard");

        //Census Info
        setEditTextNotEditable(UP_LIMIT_CENSUS_INFO_NUMBER, "census");

        //HomeStead Info
        setEditTextNotEditable(UP_LIMIT_HOMESTEAD_INFO_NUMBER, "homeStead");

        //Others Info
        setEditTextNotEditable(UP_LIMIT_OTHER_INFO_NUMBER, "other");
    }

    private void setEditTextNotEditable(int maxNumber, String previewKey) {
        EditText editText;
        for (int i = 1; i <= maxNumber; i++) {
            String key = getNumberKey(i);
            String idStr = previewKey.concat(key);
            int editTextId = getResources().getIdentifier(idStr, "id", NewHomeSteadActivity.this.getPackageName());
            if (findViewById(editTextId) != null && findViewById(editTextId) instanceof EditText) {
                editText = (EditText) findViewById(editTextId);
                editText.setCursorVisible(false);
                editText.setFocusable(false);
                editText.setFocusableInTouchMode(false);
            }
        }
    }
}
