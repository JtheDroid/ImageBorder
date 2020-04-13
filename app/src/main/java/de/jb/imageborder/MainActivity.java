package de.jb.imageborder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int READ_REQUEST_CODE = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openImage(View v) {
        startActivityForResult(
                new Intent(Intent.ACTION_OPEN_DOCUMENT)
                        .addCategory(Intent.CATEGORY_OPENABLE)
                        .setType("image/*"),
                READ_REQUEST_CODE);
    }

    public void openSettings(View v) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK && resultData != null)
            startActivity(new Intent(this, EditActivity.class)
                    .setAction(Intent.ACTION_SEND)
                    .setDataAndType(resultData.getData(), "image/*"));
    }
}
