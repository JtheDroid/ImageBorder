package de.jb.imageborder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EditActivity extends AppCompatActivity {
    private static final int READ_REQUEST_CODE = 42;
    private ImageView imageView;
    private Bitmap originalBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        imageView = findViewById(R.id.imageView);
        Intent intent = getIntent();
        if (intent == null) finish();
        else {
            Uri uri =
                    (intent.getClipData() != null && intent.getClipData().getItemCount() > 0)
                            ? intent.getClipData().getItemAt(0).getUri()
                            : intent.getData();
            if (uri == null)
                finish();
            else {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    originalBitmap = BitmapFactory.decodeStream(inputStream);
                    imageView.setImageBitmap(getEditedBitmap());
                } catch (FileNotFoundException e) {
                    finish();
                }
            }
        }
    }

    private Bitmap getEditedBitmap() {
        Bitmap editedBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
        //...
        return editedBitmap;
    }

    public void saveImage(View v) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("image/png");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    private void saveImage(Uri uri) {
        if (uri == null) {
            Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show();
        } else try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            getEditedBitmap().compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            Toast.makeText(this, "Saved: " + uri.getLastPathSegment(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                saveImage(uri);
            }
        }
    }
}
