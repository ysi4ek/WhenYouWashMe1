package com.example.jek.whenyouwashme.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.example.jek.whenyouwashme.R;
import com.example.jek.whenyouwashme.model.UserProfile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {
    private static final String KEY_URI = "com.example.jek.whenyouwashme.key.uri";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private final static String TAG = CameraActivity.class.getSimpleName();
    private static final int REQUEST_PHOTO_PERMISSIONS_CODE = 204;
    private static final int REQUEST_VIDEO_PERMISSIONS_CODE = 205;
    private final static int CODE = 203;
    private static final int REQUEST_VIDEO_CAPTURE = 2;
    private TextView textView;
    private ImageView imageView;
    private VideoView videoView;
    private Button btn_left;
    private Button btn_right;
    private String mCurrentPhotoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_camera);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textView = (TextView) findViewById(R.id.textView);
        videoView = (VideoView) findViewById(R.id.videoView);
        imageView = (ImageView) findViewById(R.id.imageView);
        btn_left = (Button) findViewById(R.id.button_left);
        btn_right = (Button) findViewById(R.id.button_right);

        btn_left.setText("Take photo");
        btn_right.setText("Rec video");

        btn_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT || checkMyPermission()) {
                    imageView.setVisibility(View.VISIBLE);
                    videoView.setVisibility(View.GONE);
                    takePhoto();
                } else {
                    requestMyPermission(REQUEST_PHOTO_PERMISSIONS_CODE);

                }

            }
        });
        btn_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT || checkMyPermission()) {
                    videoView.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.GONE);
                    recVideo();
                    videoView.start();
                } else {
                    requestMyPermission(REQUEST_VIDEO_PERMISSIONS_CODE);
                }
            }
        });
        String imageUri = getPreferences(MODE_PRIVATE).getString(KEY_URI, null);
        if (imageUri != null) {
            imageView.setImageURI(Uri.parse(imageUri));
        } else {
            imageView.setImageResource(R.mipmap.ic_launcher);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,
                ".jpg"
                , storageDir
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.jek.whenyouwashme", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageURI(Uri.parse(mCurrentPhotoPath));
            getPreferences(MODE_PRIVATE).edit().
                    putString(KEY_URI, mCurrentPhotoPath).apply();
            saveImageToStorage();
        } else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Log.d(TAG, "Intent hashCode " + intent.hashCode());
            Uri videoUri = intent.getData();
            videoView.setVideoURI(videoUri);
        }
    }

    private void saveImageToStorage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        String storageFileName = "images/" +
                FirebaseAuth.getInstance().getCurrentUser().getUid() + "/avatar.jpg";
        StorageReference storageRef = storage.getReference();
        StorageReference avatarImagesRef = storageRef.child(storageFileName);
        Uri file = Uri.fromFile(new File(mCurrentPhotoPath));
        UploadTask uploadTask = avatarImagesRef.putFile(file);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "avatar upload successful");
                } else {
                    Log.d(TAG, "WTF");
                }
            }
        });
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "image uploading error: " + e.getMessage());
            }
        });
    }

    private void requestMyPermission(int code) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA}, code);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private boolean checkMyPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void recVideo() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            Log.d(TAG, "Intent before startActivity hashCode " + takeVideoIntent.hashCode());
            takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, getVideoFileURi());
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);

        }

        int i = 0;
    }

    private Uri getVideoFileURi() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Hello camera");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Dir creation failed...");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        return Uri.fromFile(mediaFile);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PHOTO_PERMISSIONS_CODE && grantResults.length > 0) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();

            } else {
                new AlertDialog.Builder(this).setMessage(R.string.alert_no_camera_permission).
                        setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).show();

            }
        } else if (requestCode == REQUEST_VIDEO_PERMISSIONS_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                recVideo();

            } else {
                new AlertDialog.Builder(this).setMessage(R.string.alert_no_camera_permission).
                        setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).show();
            }
        }
    }
}