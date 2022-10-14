package com.example.addblindmarkapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.Glide;
import com.example.addblindmarkapp.utils.ImgUtils;
import com.example.addblindmarkapp.utils.OpcvImgUtils;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView iv_picture, iv_result;
    private Button btn_choose_pic, btn_add_watermark, btn_get_watermark;
    private String path = "", filePath = "";
    private final int PERMISSION_REQUEST_CODE = 1;
    private final int IMAGE_REQUEST_CODE = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        //初始化
        if (OpenCVLoader.initDebug()) {
            LogUtils.e("success");
        }else  {
            LogUtils.e("fail");
        }
    }

    private void initView() {
        iv_picture = findViewById(R.id.iv_picture);
        iv_result = findViewById(R.id.iv_result);

        findViewById(R.id.btn_choose).setOnClickListener(this);
        findViewById(R.id.btn_add_watermark).setOnClickListener(this);
        findViewById(R.id.btn_getBlindWaterMark).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_choose:
                LogUtils.i("Button Choose");
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                        .WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                } else {
                    //執行啟動相簿的方法
                    openAlbum();
                }
                break;

            case  R.id.btn_add_watermark:
                LogUtils.i("Add Water Mark");
                addWaterMark();
//                convert2Grey();
                break;

            case R.id.btn_getBlindWaterMark:
                LogUtils.i("Get Water Mark");
//                getWatermark();
                extractWaterMark();

        }
    }

    private void addWaterMark() {
        try{
            // 可以放圖片加文字
//            Bitmap bt = ImgUtils.drawableToBitmap(getResources().getDrawable(R.drawable.b));
            Bitmap bt =ImgUtils.drawableToBitmap(iv_picture.getDrawable());
            Mat src = new Mat(bt.getHeight(), bt.getWidth(), CvType.CV_8U);
            Utils.bitmapToMat(bt, src);
            // 加入盲水印
            Mat imageMat = OpcvImgUtils.addImageWatermarkWithText(src, "12345");
//            Mat imageMat = OpcvImgUtils.addImgWaterMarkTxt(src, "1234567", new Point(50, 50), 1D, new Scalar(255, 0, 0));
            Bitmap bt3 = null;
            bt3 = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(imageMat, bt3);

            File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "AddedWaterMark");
            if ((root.exists() || root.mkdir()) && root.isDirectory()) {
                filePath = root.getAbsolutePath();
                LogUtils.e(filePath);
            }
            LogUtils.e(filePath);
            path = filePath + "/" + ImgUtils.getTimeStampFileName(0);
            LogUtils.e(filePath);
            ImageUtils.save(bt3, path,  Bitmap.CompressFormat.PNG);
            Toast.makeText(this, "Save path :" + path, Toast.LENGTH_SHORT).show();
//            Glide.with(this).load(path).into(iv_result);
            LogUtils.i("Set into imageView");
            // 測試有無水印圖片
            // 有水印
//            Mat showMat = OpcvImgUtils.getImageWatermarkWithText(imageMat);
////            // 無水印
////            Mat showMat = OpcvImgUtils.getImageWatermarkWithText(src);
//            Bitmap bt4 = Bitmap.createBitmap(showMat.cols(), showMat.rows(), Bitmap.Config.RGB_565);
//
//            Utils.matToBitmap(showMat, bt4);
//            iv_result.setImageBitmap(bt4);
//            ImageUtils.save(bt4, path , Bitmap.CompressFormat.PNG);
//            LogUtils.i("Save blind water");
        }
        catch (Exception e) {
            LogUtils.e(e);
        }

    }

    private void extractWaterMark() {
        try {
//            Bitmap bitmap = BitmapFactory.decodeFile(path);
            Bitmap bitmap =ImgUtils.drawableToBitmap(iv_picture.getDrawable());
//            Bitmap bitmap = ImgUtils.drawableToBitmap(getResources().getDrawable(R.drawable.b));
            LogUtils.i(path);
            Mat temp = new Mat();
            Utils.bitmapToMat(bitmap, temp);
            iv_result.setImageBitmap(bitmap);
            Mat showMat = OpcvImgUtils.getImageWatermarkWithText(temp);
            Bitmap  bt4 = Bitmap.createBitmap(showMat.cols(), showMat.rows(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(showMat, bt4);

            iv_result.setImageBitmap(bt4);
            String paths = filePath + "/" + ImgUtils.getTimeStampFileName(0);
            LogUtils.i("Save to " + paths);
            ImageUtils.save(bt4, paths , Bitmap.CompressFormat.PNG);
            Toast.makeText(this, "Save blind image to : " + paths, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openAlbum();
            } else {
                Toast.makeText(MainActivity.this, "你拒絕了", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case IMAGE_REQUEST_CODE://這裡的requestCode是我自己設定的，就是確定返回到那個Activity的標誌
                if (resultCode == RESULT_OK) {//resultcode是setResult裡面設定的code值
                    try {
                        Uri selectedImage = data.getData(); //獲取系統返回的照片的Uri
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(selectedImage,
                                filePathColumn, null, null, null);//從系統表中查詢指定Uri對應的照片
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        path = cursor.getString(columnIndex);  //獲取照片路徑
                        cursor.close();
                        Bitmap bitmap = BitmapFactory.decodeFile(path);
                        iv_picture.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        // TODO Auto-generatedcatch block
                        e.printStackTrace();
                    }
                }
                break;
        }


    }



    private void openAlbum() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }

    private void convert2Grey() {
        Mat src = new Mat();//Mat是OpenCV的一种图像格式
        Mat temp = new Mat();
        Mat dst = new Mat();
        Bitmap bitmap = ImageUtils.drawable2Bitmap(iv_picture.getDrawable());
        Utils.bitmapToMat(bitmap, src);
        Imgproc.cvtColor(src, temp, Imgproc.COLOR_RGB2BGR);
        Imgproc.cvtColor(temp, dst, Imgproc.COLOR_BGR2GRAY);
        Utils.matToBitmap(dst, bitmap);
        iv_result.setImageBitmap(bitmap);
        src.release();
        temp.release();
        dst.release();
    }


}
