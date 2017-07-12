package com.example.utsav.imageupload;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final int RC_CAMERA_AND_STORAGE =9001 ;
    private Uri camera, gallary;
    static final int REQUEST_IMAGE_OPEN = 1001;
    static final int REQUEST_IMAGE_CAPTURE = 1002;
    private CircleImageView imageView;
   public static final String URL = "http://192.168.43.141/AndroidImageUpload/upload.php";
  //  public static final String URL="http://127.0.0.1/AndroidUploadImage/upload.php";

    private AppCompatButton mBtUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        methodRequiresTwoPermission();
        imageView = (CircleImageView) findViewById(R.id.profile_image);
        mBtUpload= (AppCompatButton) findViewById(R.id.bt_upload);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select();


            }
        });
        mBtUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageUploadTask imageUploadTask = new ImageUploadTask();
                imageUploadTask.execute(URL);
            }
        });

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_CAMERA_AND_STORAGE)
    private void methodRequiresTwoPermission() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            // ...
        } else {
           //  Do not have permissions, request them now
            EasyPermissions.requestPermissions(this,"Allow permission",
                    RC_CAMERA_AND_STORAGE, perms);

        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    public class ImageUploadTask extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(MainActivity.this, "Uploading", "");
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String json = ImageUpload(params[0]);
                Log.d("TEST", "TEST   :" + json);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
        }
    }

    public String ImageUpload(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String type = mime.getExtensionFromMimeType(cR.getType(gallary));
        Log.d("FILE TYPE","TYPE  :"+type);
        final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/*");


        // Use the imgur image upload API as documented at https://api.imgur.com/endpoints/image
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("name", "Squar")
                .addFormDataPart("image", "1234."+type,
                        RequestBody.create(MEDIA_TYPE_PNG, String.valueOf(gallary)))
                .build();

        Request request = new Request.Builder()

                .url(url)
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();

    }


    private void select() {
        final CharSequence[] utsav = {"Take Photo", "Select From Gallary", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Select Image");
        builder.setItems(utsav, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (utsav[which].equals("Take Photo")) {
                    capturePhoto();

                } else if (utsav[which].equals("Select From Gallary")) {
                    selectImage();
                    //  SigninFragment.newInstance(gallary.getPath());
                } else if (utsav[which].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        })
                .setCancelable(false)
                .show();
    }

    public void selectImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Only the system receives the ACTION_OPEN_DOCUMENT, so no need to test.
        startActivityForResult(intent, REQUEST_IMAGE_OPEN);
    }

    public void capturePhoto() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_OPEN && resultCode == RESULT_OK) {

            gallary = data.getData();
            imageView.setImageURI(gallary);

            // Do work with full size photo saved at fullPhotoUri

        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            camera = data.getData();

            imageView.setImageURI(camera);
            Toast.makeText(MainActivity.this, "" + camera, Toast.LENGTH_SHORT).show();
        }
    }


}

