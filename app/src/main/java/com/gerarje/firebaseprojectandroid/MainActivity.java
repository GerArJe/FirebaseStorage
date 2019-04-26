package com.gerarje.firebaseprojectandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int CHOSER_IMAGES = 1;
    private static final String TAG = "MainActivity";
    private Button btnDownload;
    private Button btnUpload;
    private ImageView imvImage;

    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storageReference = FirebaseStorage.getInstance().getReference();

        btnDownload = findViewById(R.id.btnDownload);
        btnUpload = findViewById(R.id.btnUpload);
        imvImage = findViewById(R.id.imvImage);

        imvImage.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Serleciona una imagen"), CHOSER_IMAGES);
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StorageReference imagenRef = storageReference.child("imagen_ejemplo.png");
                imvImage.setDrawingCacheEnabled(true);
                imvImage.buildDrawingCache();

                Bitmap bitmap = imvImage.getDrawingCache();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

                byte[] imagenByte = byteArrayOutputStream.toByteArray();

                UploadTask uploadTask = imagenRef.putBytes(imagenByte);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Ocurrió un error en la subida");
                        e.printStackTrace();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(MainActivity.this, "Subida con Exito", Toast.LENGTH_SHORT).show();
                        String downloadUri = taskSnapshot.getUploadSessionUri().getPath();
                        Log.w(TAG, "image URL: " + downloadUri);
                    }
                });
            }
        });

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final File file;
                try{
                    file = File.createTempFile("Create acount", "png");
                    storageReference.child("Create acount.png").getFile(file)
                            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                   Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                   imvImage.setImageBitmap(bitmap);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Ocurrió un error al mostar la imagen");
                            e.printStackTrace();
                        }
                    });
                }catch (Exception e){
                    Log.e(TAG, "Ocurrió un error en la descara de imaganes");
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOSER_IMAGES){
            Uri imageUri = data.getData();
            if (imageUri != null){
                imvImage.setImageURI(imageUri);
            }
        }
    }
}
