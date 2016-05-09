package com.example.mansa.fileuploads;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final int SELECT_FILE = 45;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.text_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFolder();
            }
        });
    }


    public void openFolder() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()
                + "/myFolder/");
        intent.setDataAndType(uri, "*/*");
        startActivity(Intent.createChooser(intent, "Open folder"));

        try {
            startActivityForResult (Intent.createChooser(intent, "Open folder"), SELECT_FILE);
        }
        catch(Exception e) {}

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {

                Uri myUri = data.getData();
                Cursor cursor = getContentResolver().query(myUri,new String []{"_data"}, null, null, null);
                cursor.moveToFirst();
                String filePath = cursor.getString(cursor.getColumnIndex("_data"));
                cursor.close();
                Log.d("xxxxx", filePath);
            }
        }

    }
}


