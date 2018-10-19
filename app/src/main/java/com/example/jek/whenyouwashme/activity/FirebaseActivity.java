package com.example.jek.whenyouwashme.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.jek.whenyouwashme.R;
import com.example.jek.whenyouwashme.model.firebase.Message;
import com.example.jek.whenyouwashme.model.firebase.MessageAdapter;
import com.example.jek.whenyouwashme.model.sqlite.MessageDataSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FirebaseActivity extends AppCompatActivity implements ChildEventListener {
    private static final String TAG = FirebaseActivity.class.getSimpleName();
    private static final int REQUEST_CODE_PERMISSION_GALLERY = 1001;
    private DatabaseReference messageDatabaseReference;
    private DatabaseReference profileDatabaseReference;
    private DatabaseReference userDataBaseReference;
    private MessageAdapter adapter;
    private RecyclerView rv;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    List<Message> mList = new ArrayList<>();
    private MessageDataSource messageDataSource;
    private Toolbar toolbar;

    private static int RESULT_LOAD_IMAGE = 1;
    ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_firebase);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
        rv = (RecyclerView) findViewById(R.id.rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(linearLayoutManager);
        rv.setHasFixedSize(true);
        //adapter = new MessageAdapter(mList);
        adapter = new MessageAdapter(mList);
        rv.setAdapter(adapter);
        messageDataSource = new MessageDataSource(getApplicationContext());
        messageDatabaseReference = FirebaseDatabase.getInstance().getReference("messages");
        profileDatabaseReference = FirebaseDatabase.getInstance().getReference("profiles");

        imageView = (ImageView) findViewById(R.id.settings_toolbar);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    checkPermissions();
                } else {
                    getAvatarFromGallery();
                }

            }
        });

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userDataBaseReference = messageDatabaseReference.child(getUserName());
            //messageDataBaseReference = userDataBaseReference.child(getUserName());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission
                (this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            getAvatarFromGallery();
        } else {
            ActivityCompat.requestPermissions(FirebaseActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISSION_GALLERY);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION_GALLERY && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getAvatarFromGallery();
            } else {
                Snackbar.make(findViewById(android.R.id.content),
                        "no permission - no ava", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void getAvatarFromGallery() {
        Intent i = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    private void showDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_dialog);
        final EditText editTitle = (EditText) dialog.findViewById(R.id.et_title);
        final EditText editMessge = (EditText) dialog.findViewById(R.id.et_message);

        dialog.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMessage(editTitle.getText().toString(), editMessge.getText().toString());
                dialog.cancel();
            }
        });
        dialog.show();
    }

    private void addMessage(String title, String message) {
        Message messageObject = new Message();
        messageObject.setTitle(title);
        messageObject.setMessage(message);
        messageObject.setTime(dateFormat.format(new Date()));
        userDataBaseReference.child(String.valueOf(System.currentTimeMillis())).
                setValue(messageObject);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            startActivity(new Intent(this, SignUpActivity.class));
        } else {
            //System.nanoTime() - 10*1000*1000*1000
            userDataBaseReference.addChildEventListener(this);
            messageDataSource.open();
            mList.clear();
            mList.addAll(messageDataSource.getAllMessages());
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (userDataBaseReference != null) {
            userDataBaseReference.removeEventListener(this);
        }
        if (messageDataSource != null) {
            messageDataSource.close();
        }
    }

    public String getUserName() {
        return FirebaseAuth.getInstance()
                .getCurrentUser().getEmail().replace(".", "");
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        //Log.d("TAG", dataSnapshot.toString());
        for (Message message : mList) {
            Log.d(TAG, "message: " + message);
        }
        Message message = dataSnapshot.getValue(Message.class);
        message.setId(Long.parseLong(dataSnapshot.getKey()));
        Log.d(TAG, "key: " + dataSnapshot.getKey());
        if (!mList.contains(message)) {
            messageDataSource.addMessage(message);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
