package com.example.project;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.widget.Toolbar;

import com.example.project.utils.ImageUpload;
import com.example.project.utils.ImageUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PredictActivity extends AppCompatActivity {
    private static final int REQUEST_TAKE_PHOTO = 111;
    private static final int REQUEST_PICK_PHOTO = 222;
    private final int mRequestCode = 100;
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss";
    private String currentPhotoPath;
    private Uri mCameraUri;
    private ImageView imageView;
    private Button uploadButton;
    private TextView Label1;
    private TextView Score1;
    private TextView Label2;
    private TextView Score2;
    private TextView Label3;
    private TextView Score3;
    AlertDialog mPermissionDialog;
    String mPackName = "com.example.project";

    String[] permissions = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE};

    List<String> mPermissionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predict);
        initPermission();
        Toolbar toolbar = findViewById(R.id.toolbar);
        imageView = findViewById(R.id.imageView);
        uploadButton = findViewById(R.id.uploadBtn);
        Label1 = findViewById(R.id.label1);
        Score1 = findViewById(R.id.score1);
        Label2 = findViewById(R.id.label2);
        Score2 = findViewById(R.id.score2);
        Label3 = findViewById(R.id.label3);
        Score3 = findViewById(R.id.score3);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        initListening();
    }

    private void initPermission(){
        mPermissionList.clear();//清空已经允许的没有通过的权限
        //逐个判断是否还有未通过的权限
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);//添加还未授予的权限到mPermissionList中
            }
        }
        //申请权限
        if (mPermissionList.size()>0){//有权限没有通过，需要申请
            ActivityCompat.requestPermissions(this, permissions, mRequestCode);
        }
    }

    private void initListening() {
        imageView.setOnClickListener(v->{
            new XPopup.Builder(this)
                    .isDestroyOnDismiss(true)
                    .asBottomList("请选择图片", new String[]{"拍照", "从图册选取", "取消"},
                            new OnSelectListener() {
                                @Override
                                public void onSelect(int position, String text) {
                                    if(position == 0) {
                                        OpenCamera();
                                    }
                                    else if (position == 1) {
                                        OpenGallery();
                                    }
                                }
                            })
                    .show();
        });
        uploadButton.setOnClickListener(v -> {
            try {
                uploadImage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @SuppressLint({"ShowToast", "QueryPermissionsNeeded"})
    private void OpenCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            Toast.makeText(this, "The Function createImageFile Failed", Toast.LENGTH_SHORT);
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.example.project.fileprovider",
                    photoFile);
            mCameraUri = photoURI;
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
        // }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void OpenGallery() {
        Intent pickPictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        pickPictureIntent.setAction(Intent.ACTION_PICK);
//        pickPictureIntent.setType("image/*");
        startActivityForResult(pickPictureIntent, REQUEST_PICK_PHOTO);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault())
                .format(new Date());
        System.out.println(timeStamp);
        String imageFileName = "IMG_" + timeStamp;
        System.out.println(imageFileName);
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        System.out.println(image);
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        System.out.println(currentPhotoPath);
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void uploadImage() throws Exception {
        //System.out.println(currentPhotoPath);
        File file = new File(currentPhotoPath);
        ImageUpload.run(file, PredictActivity.this,Label1, Score1, Label2, Score2, Label3, Score3);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if (requestCode==REQUEST_PICK_PHOTO) {
                assert data != null;
                Uri uri=data.getData();
                currentPhotoPath = new ImageUtils().getRealPathFromUri(PredictActivity.this, uri);
                imageView.setImageURI(uri);
            }
            else if(requestCode==REQUEST_TAKE_PHOTO){
                imageView.setImageURI(mCameraUri);
                galleryAddPic();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermissionDismiss = false;//有权限没有通过
        if (mRequestCode==requestCode){
            for (int grantResult : grantResults) {
                if (grantResult == -1) {
                    hasPermissionDismiss = true;
                    break;
                }
            }
        }
        if (hasPermissionDismiss){//如果有没有被允许的权限
            showPermissionDialog();
        }
    }

    private void showPermissionDialog() {
        if (mPermissionDialog == null) {
            mPermissionDialog = new AlertDialog.Builder(this)
                    .setMessage("已禁用权限，请手动授予")
                    .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelPermissionDialog();

                            Uri packageURI = Uri.parse("package:" + mPackName);
                            Intent intent = new Intent(Settings.
                                    ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //关闭页面或者做其他操作
                            cancelPermissionDialog();
                            PredictActivity.this.finish();
                        }
                    })
                    .create();
        }
        mPermissionDialog.show();
    }

    private void cancelPermissionDialog() {
        mPermissionDialog.cancel();
    }

}
