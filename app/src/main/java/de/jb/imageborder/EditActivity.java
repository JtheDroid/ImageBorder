package de.jb.imageborder;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EditActivity extends AppCompatActivity {
    private static final int READ_REQUEST_CODE = 42;
    int width, height, size, max;
    private ImageView imageView;
    private TextView textViewValue;
    private SeekBar seekBar;
    private Bitmap originalBitmap;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        imageView = findViewById(R.id.imageView);
        textViewValue = findViewById(R.id.textViewValue);
        seekBar = findViewById(R.id.seekBarValue);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                update();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
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
                    width = originalBitmap.getWidth();
                    height = originalBitmap.getHeight();
                    size = width;
                    max = size / 2;
                    imageView.setImageBitmap(getEditedBitmap());
                    seekBar.setMax(max);
                } catch (FileNotFoundException e) {
                    finish();
                }
            }
        }
    }

    private void update() {
        imageView.setImageBitmap(getEditedBitmap());
        textViewValue.setText(seekBar.getProgress() + " px, " + Math.round(seekBar.getProgress() * 100.0 / size) + "% width");
    }

    public void openSettings(View v) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private Bitmap getEditedBitmap() {
        Bitmap editedBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
        int pixels = seekBar.getProgress();
        Canvas canvas = new Canvas(editedBitmap);
        Paint paint = new Paint();
        paint.setColor(
                Color.argb(sharedPreferences.getInt("colorA", 255),
                        sharedPreferences.getInt("colorR", 255),
                        sharedPreferences.getInt("colorG", 255),
                        sharedPreferences.getInt("colorB", 255)));
        if (sharedPreferences.getBoolean("left", true))
            canvas.drawRect(0, 0, pixels, height, paint);
        if (sharedPreferences.getBoolean("right", true))
            canvas.drawRect(width - pixels, 0, width, height, paint);
        if (sharedPreferences.getBoolean("top", true))
            canvas.drawRect(0, 0, width, pixels, paint);
        if (sharedPreferences.getBoolean("bottom", true))
            canvas.drawRect(0, height - pixels, width, height, paint);
        return editedBitmap;
    }

    public void saveImage(View v) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType(sharedPreferences.getString("filetype", "JPEG").equals("JPEG") ? "image/jpeg" : "image/png");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    private void saveImage(Uri uri) {
        if (uri == null) {
            Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show();
        } else try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            Bitmap.CompressFormat compressFormat = sharedPreferences.getString("filetype", "JPEG").equals("JPEG") ? Bitmap.CompressFormat.JPEG : Bitmap.CompressFormat.PNG;
            getEditedBitmap().compress(compressFormat, sharedPreferences.getInt("quality", 95), outputStream);
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
