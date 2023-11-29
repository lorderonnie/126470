package com.example.blurpic;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import jp.wasabeef.glide.transformations.BlurTransformation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button camera, gallery;
    private ImageView imageView;
    private Bitmap blurredBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = findViewById(R.id.button);
        gallery = findViewById(R.id.button2);
        imageView = findViewById(R.id.imageView);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("BlurPic");

        camera.setOnClickListener(view -> requestCameraPermission());

        gallery.setOnClickListener(view ->  {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch("image/*");
        });



        Button blurImageButton = findViewById(R.id.button3);
        blurImageButton.setOnClickListener(view -> blurImage(imageView));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            saveImageToDevice();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestCameraPermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
        }
    }

    private void startCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        someActivityResultLauncher.launch(cameraIntent);
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    // Process the result here
                    if (data != null) {
                        // Handle the captured image or other data
                        Bundle extras = data.getExtras();
                        if (extras != null && extras.containsKey("data")) {
                            Bitmap imageBitmap = (Bitmap) extras.get("data");
                            imageView.setImageBitmap(imageBitmap);
                        }
                    }
                }
            }
    );

    ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                // Handle the picked image URI here
                if (uri != null) {
                    imageView.setImageURI(uri);
                }
            }
    );


    private void applyBlur(ImageView imageView, Bitmap bitmap) {
        MultiTransformation<Bitmap> multiTransformation = new MultiTransformation<>(new BlurTransformation(25, 8));

        Glide.with(this)
                .asBitmap()
                .load(bitmap)
                .transform(multiTransformation)
                .transition(BitmapTransitionOptions.withCrossFade())
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        blurredBitmap = resource;
                        imageView.setImageBitmap(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Handle any clearing of the load
                    }
                });
    }



    private void blurImage(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap originalBitmap = bitmapDrawable.getBitmap();

            if (originalBitmap != null) {
                applyBlur(imageView, originalBitmap);
            }
        } else {
            Toast.makeText(this, "Select a picture first", Toast.LENGTH_SHORT).show();
        }
    }

    // onActivityResult() method
    // ... (code for onActivityResult method)

    private void saveImageToDevice() {
        if (blurredBitmap != null) {
            try {
                File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String fileName = "blurred_image_" + timeStamp + ".png"; // Append timestamp to create a unique filename
                File file = new File(directory, fileName);

                OutputStream outputStream = new FileOutputStream(file);
                blurredBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.flush();
                outputStream.close();

                Toast.makeText(this, "Blurred image saved to Downloads folder", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save blurred image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No blurred image available to save", Toast.LENGTH_SHORT).show();
        }
    }


}
