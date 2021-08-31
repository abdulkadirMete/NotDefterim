package com.nophrase.speechtotext.ui;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.nophrase.speechtotext.R;
import com.nophrase.speechtotext.data.RealmHelper;
import com.nophrase.speechtotext.model.Not;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;

public class NotDetailActivity extends AppCompatActivity{
    private static final int REQUEST_CODE_SPEECH_INPUT = 1;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PİCK_GALERY_CODE = 1000;
    private static final int İMAGE_PİCK_CAMERA_CODE = 1001;
    private static final int[] colors = {R.color.colorNot1,R.color.colorNot2,R.color.colorNot3
            ,R.color.colorNot4,R.color.colorNot5,R.color.colorNot6};

    private static final int[] et_colors = {R.color.colorEt1,R.color.colorEt2,R.color.colorEt3
            ,R.color.colorEt4,R.color.colorEt5,R.color.colorEt6};

    private static final int[] shapes = {R.drawable.button_shape1,R.drawable.button_shape2,R.drawable.button_shape3,
    R.drawable.button_shape4,R.drawable.button_shape5,R.drawable.button_shape6};

    private String[] cameraPermission;
    private String[] storagePermission;

    private Uri imageUri;

    private ImageButton btn_sound,btn_camera;
    private Button btn_color;
    private EditText et_text,et_header;
    private Context context = this;
    private Realm realm;
    private Not mNot;
    private StringBuilder oldText,oldHeader;
    private int currentColor;
    private int mId;
    private ImageView img_preview;
    private FragmentManager manager;
    private FragmentTransaction transaction;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_not);
        initTb();
        init();
        initOldItems();
        btn_sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSound();
            }
        });
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImage();
            }
        });
        btn_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment();
            }
        });
    }

    public void onClickColor(View v){
        switch (v.getId()){
            case R.id.btn_not1:
                currentColor = 0;
                break;

            case R.id.btn_not2:
                currentColor = 1;
                break;

            case R.id.btn_not3:
                currentColor = 2;
                break;

            case R.id.btn_not4:
                currentColor = 3;
                break;

            case R.id.btn_not5:
                currentColor = 4;
                break;

            case R.id.btn_not6:
                currentColor = 5;
                btn_color.setBackgroundColor(colors[5]);
                break;

            case R.id.ln_color:
                switchFragment();
                break;
        }
        changeColor(currentColor);
        switchFragment();
    }

    private void changeColor(int currentColor) {
        et_header.setBackgroundResource(et_colors[currentColor]);
        et_text.setBackgroundResource(et_colors[currentColor]);
        btn_color.setBackgroundResource(colors[currentColor]);
        btn_camera.setBackgroundResource(shapes[currentColor]);
        btn_sound.setBackgroundResource(shapes[currentColor]);
    }

    private void switchFragment() {
        manager = getSupportFragmentManager();
        transaction = manager.beginTransaction();
        FragmentColor fb = (FragmentColor) manager.findFragmentByTag("fragmentColor");
        if(fb==null){
            FragmentColor fragment = new FragmentColor();
            transaction.add(R.id.fl_color_box,fragment,"fragmentColor");
            transaction.commit();
        }else{
            getSupportFragmentManager().beginTransaction().remove(fb).commit();
        }
    }

    private void initOldItems() {
        oldText = new StringBuilder();
        oldHeader = new StringBuilder();
        int requestCode = getIntent().getExtras().getInt("requestCode");
        if (requestCode == 1) {
            mNot = (Not) getIntent().getSerializableExtra("not");
            oldText.append(mNot.getNot());
            et_text.setText(oldText);
            oldHeader.append(mNot.getHeader());
            et_header.setText(oldHeader);
            mId = mNot.getId();
            currentColor = mNot.getColorNum();
            changeColor(currentColor);
        }
    }

    private void initTb() {
        Toolbar toolbar = findViewById(R.id.toolbar_add);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

    private void init() {
        btn_color = findViewById(R.id.btn_color);
        et_header = findViewById(R.id.et_header);
        img_preview = findViewById(R.id.img_preview);
        btn_sound = findViewById(R.id.btn_sound);
        btn_camera = findViewById(R.id.btn_camera);
        et_text = findViewById(R.id.et_text);
        realm = Realm.getDefaultInstance();
        cameraPermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA,};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    @Override
    public void onBackPressed() {
        String mNotText = et_text.getText().toString();
        String mHeader = et_header.getText().toString();
        RealmHelper realmHelper = new RealmHelper(context, realm);
        StringBuilder message = new StringBuilder();

        if(!(!mHeader.matches(""))){
            if(et_text.length()<42){
                mHeader = et_text.getText().toString();
            }else{
                mHeader = et_text.getText().toString().substring(0,42);
            }
        }

        if(!mNotText.matches("")){
            if(mNot == null) {
                Not not = new Not(mNotText, getDate(),currentColor,mHeader);
                realmHelper.saveData(not);
                message.append(getText(R.string.saved));
            }else{
                mNot.setNot(mNotText);
                mNot.setColorNum(currentColor);
                mNot.setHeader(mHeader);
                realmHelper.updateData(mNot);
                message.append(getText(R.string.updated));
            }
            Intent returnIntent = new Intent();
            setResult(RESULT_OK, returnIntent);
            if(!mNotText.equalsIgnoreCase(String.valueOf(oldText)))
            Toast.makeText(context,message.toString(),Toast.LENGTH_SHORT).show();

        }
        super.onBackPressed();
    }

    private void getSound() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Dinliyorum");

        try{
            startActivityForResult(intent,REQUEST_CODE_SPEECH_INPUT);
        }catch (Exception e){

        }
    }

    private void getImage() {
        showImageImportDialog();
    }

    private void showImageImportDialog(){
        String[] items = {"Kamera","Galeri"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this,R.style.Theme_AppCompat_Dialog);
        dialog.setTitle(getText(R.string.chooeseImage));
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        if(!checkCameraPermission()){
                            requestCameraPermission();
                        }else{
                            pickCamera();
                        }
                        break;
                    case 1:
                        if(!checkStoragePermission()){
                            requestStoragePermission();
                        }else{
                            pickGalery();
                        }
                        break;
                }
            }
        });
        dialog.create().show();
    }

    private void pickGalery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PİCK_GALERY_CODE);
    }

    private void pickCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"NewPic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Image to Text");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI , values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT , imageUri);
        startActivityForResult(cameraIntent,İMAGE_PİCK_CAMERA_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        pickCamera();
                    } else {
                        Toast.makeText(context, getString(R.string.permission_denied), Toast.LENGTH_SHORT);
                    }
                }
                break;
            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickGalery();
                    } else {
                        Toast.makeText(context, getString(R.string.permission_denied), Toast.LENGTH_SHORT);
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_SPEECH_INPUT && null != data) {
                ArrayList<String> resultList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                StringBuilder currentText = new StringBuilder();
                currentText.append(et_text.getText().toString());
                if (currentText.length() != 0) {
                    currentText.append(" ");
                    currentText.append(resultList.get(0));
                    et_text.setText(currentText);
                } else {
                    et_text.setText(resultList.get(0));
                }
            }
            if (requestCode == IMAGE_PİCK_GALERY_CODE) {
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }

            if (requestCode == İMAGE_PİCK_CAMERA_CODE) {
                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                img_preview.setImageURI(resultUri);
                BitmapDrawable bitmapDrawable = (BitmapDrawable) img_preview.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();

                if (!recognizer.isOperational()) {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                } else {
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items = recognizer.detect(frame);
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < items.size(); i++) {
                        TextBlock myItem = items.valueAt(i);
                        sb.append(myItem.getValue());
                        sb.append("\n");

                    }

                    et_text.append(sb.toString());
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "" + error, Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.not_detail_menu, menu);
        MenuItem delete_item = menu.findItem(R.id.action_delete);
        return super.onCreateOptionsMenu(menu);
    }

    private String getDate() {
        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm  dd/MM/yyyy",Locale.getDefault());
        String formattedDate = df.format(currentDate);
        return  formattedDate;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_delete:
                AlertDialog.Builder alertdialog = new AlertDialog.Builder(context,R.style.Theme_AppCompat_Dialog);
                alertdialog.setTitle(getText(R.string.deleteDialog))
                        .setMessage(getText(R.string.delete))
                        .setPositiveButton(getString(R.string.okayDialog), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(mId != 0){
                                    RealmHelper realmHelper = new RealmHelper(context, realm);
                                    realmHelper.deleteData(mId);
                                }
                                Intent intent = new Intent(context,MainActivity.class);
                                setResult(RESULT_OK,intent);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(getString(R.string.cancelDialog), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                break;
            case R.id.action_share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, et_text.getText().toString());
                startActivity(Intent.createChooser(sharingIntent,getString(R.string.share)));

        }
        return super.onOptionsItemSelected(item);
    }

}
