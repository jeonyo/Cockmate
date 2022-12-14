package com.example.cockmate.util;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.service.autofill.UserData;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cockmate.R;
import com.example.cockmate.adapter.MultiImageAdapter;
import com.example.cockmate.adapter.MyRecyclerAdapter;
import com.example.cockmate.model.BoardModel;
import com.example.cockmate.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostActivity extends AppCompatActivity {

    long mNow;
    Date mDate;
    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private static final String TAG = "PostActivity";
    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDBReference;
    private SharedPreferences preferences;
    private MyRecyclerAdapter mRecyclerAdapter;
    private RecyclerView mRecyclerView;

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    EditText Title, Content;
    String postTitle, postContent, mName;

    ArrayList<Uri> uriList = new ArrayList<>();     // ???????????? uri??? ?????? ArrayList ??????

    RecyclerView recyclerView;  // ???????????? ????????? ??????????????????
    MultiImageAdapter adapter;  // ????????????????????? ???????????? ?????????

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        // ????????????
        Toolbar toolbar = findViewById(R.id.post_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Title = findViewById(R.id.post_title);
        Content = findViewById(R.id.post_content);


        // ???????????? ???????????? ??????
        Button btn_getImage = findViewById(R.id.getImage);
        btn_getImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // ????????? ?????? ?????? ??????
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "?????? ?????? ??????");
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, 2222);
                    }
                    else {
                        Log.d(TAG, "?????? ?????? ??????");
                        ActivityCompat.requestPermissions(PostActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }
                }

            }
        });

        recyclerView = findViewById(R.id.recyclerView);



    }

    // ???????????? ??????????????? ????????? ??? ???????????? ?????????
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data == null){   // ?????? ???????????? ???????????? ?????? ??????
            Toast.makeText(getApplicationContext(), "???????????? ???????????? ???????????????.", Toast.LENGTH_LONG).show();
        }
        else{   // ???????????? ???????????? ????????? ??????
            if(data.getClipData() == null){     // ???????????? ????????? ????????? ??????
                Log.e("single choice: ", String.valueOf(data.getData()));
                Uri imageUri = data.getData();
                uriList.add(imageUri);

                adapter = new MultiImageAdapter(uriList, getApplicationContext());
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));
            }
            else{      // ???????????? ????????? ????????? ??????
                ClipData clipData = data.getClipData();
                Log.e("clipData", String.valueOf(clipData.getItemCount()));

                if(clipData.getItemCount() > 10){   // ????????? ???????????? 11??? ????????? ??????
                    Toast.makeText(getApplicationContext(), "????????? ??? ?????? 10????????? ?????? ???????????????.", Toast.LENGTH_LONG).show();
                }
                else{   // ????????? ???????????? 1??? ?????? 10??? ????????? ??????
                    Log.e(TAG, "multiple choice");

                    for (int i = 0; i < clipData.getItemCount(); i++){
                        Uri imageUri = clipData.getItemAt(i).getUri();  // ????????? ??????????????? uri??? ????????????.
                        try {
                            uriList.add(imageUri);  //uri??? list??? ?????????.

                        } catch (Exception e) {
                            Log.e(TAG, "File select error", e);
                        }
                    }

                    adapter = new MultiImageAdapter(uriList, getApplicationContext());
                    recyclerView.setAdapter(adapter);   // ????????????????????? ????????? ??????
                    recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));
                    // ?????????????????? ?????? ????????? ??????
                }
            }
        }
    }

    // ?????? ??????, ?????? ?????????
    private String getTime(){
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }

    // ?????? ?????? ????????????
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.post_complete_bar, menu);
        return true;
    }

    // ?????? ?????? ?????????
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        postTitle = Title.getText().toString();
        postContent = Content.getText().toString();
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar??? back??? ????????? ??? ??????
                // ???????????? ??????
                finish();
                return true;
            }
            case R.id.post_complete:{
                if (postTitle.isEmpty() || postContent.isEmpty()){
                    Toast.makeText(getApplicationContext(), "????????? ???????????? ???????????????.", Toast.LENGTH_LONG).show();
                }
                else {
                    saveData();
                    Toast.makeText(getApplicationContext(), "?????? ?????? ?????????", Toast.LENGTH_LONG).show();
                    // ?????????????????? ?????? ??? ????????????
                    //mRecyclerAdapter.notifyDataSetChanged();
                    //mRecyclerView.invalidate();
                    finish();
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveData(){
        preferences = getSharedPreferences("UserName", MODE_PRIVATE);

        //BoardModel boardModel = new BoardModel();
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        String email = user.getEmail();
        //Log.e(TAG, email);
        String uid = user.getUid();
        postTitle = Title.getText().toString();
        postContent = Content.getText().toString();
        String category = "main";
        long date = System.currentTimeMillis();
        String realDate = getTime();




        // SharedPreference ???????????? ????????? ??? ????????????
        String mName = preferences.getString("userName", "??????");
        Log.e(TAG, mName);


        // ???????????????????????? ?????? uid??? ?????? ????????? ????????????
        mDBReference = FirebaseDatabase.getInstance().getReference();
        mDBReference.child("Users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("Name").getValue(String.class);
                if (name != null){
                    //Log.d(TAG, name);

                    // SharedPreference ???????????? name ?????? ????????????
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("userName", name);
                    editor.commit();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Log.e("MainActivity", String.valueOf(databaseError.toException())); // ????????? ??????
            }
        });






        // ????????????????????? ????????????
        //HashMap<String, Object> boardUpdates = new HashMap<>();
        //BoardModel boardModel = new BoardModel(postTitle, category, postContent, date, mName, email);
        //Map<String, Object> boardValue = boardModel.toMap();

        Map<String, Object> board = new HashMap<>();
        board.put("Title", postTitle);
        board.put("Category", "main");
        board.put("Content", postContent);
        board.put("Date", date);
        board.put("Name", mName);
        board.put("Email", email);
        board.put("RealDate", realDate);


        // B

        db.collection("Main_Board")
                .add(board)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "?????? ??????");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "?????? ??????");
                    }
                });



        //boardUpdates.put("/Main_Board/" + uid, boardValue);
        //mDBReference.child("Main_Board").child(uid).setValue(boardValue);
        //mDBReference.updateChildren(boardUpdates);
        //mDBReference.

    }


    // edit text ??? ?????? ?????? ????????? ????????? ?????????
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom())
                ((InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((this.getWindow().getDecorView().getApplicationWindowToken()), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    // ?????? ??????
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult");
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED ) {
            Log.d(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
        }
    }


}


