package com.example.mansa.fileuploads;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


public class MainActivity extends AppCompatActivity {

    private static final int SELECT_FILE = 45;
    private static final String TAG = MainActivity.class.getName();
    // the URL where the file will be posted
    String postReceiverUrl = "http://192.168.22.86:8000/api/uploads/store";
    String filePath;
    private int sdk = android.os.Build.VERSION.SDK_INT;

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

                Uri _uri = data.getData();


                if (sdk >= 19) {
                    filePath = getRealPathFromURI_API19(getBaseContext(), _uri);
                } else if (sdk > 10 && sdk < 19) {
                    filePath = getRealPathFromURI_API11to18(getBaseContext(), _uri);
                } else {
                    filePath = getRealPathFromURI_BelowAPI11(getBaseContext(), _uri);

                }
                initUpload();
            }
        }
    }


    private void initUpload(){
        AsyncCallWS task = new AsyncCallWS();
        task.execute();
    }

    private class AsyncCallWS extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute");
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.i(TAG, "doInBackground");
            uploadFile(filePath);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.i(TAG, "Upload Successful");
            Toast.makeText(getBaseContext(), "Upload Successful", Toast.LENGTH_SHORT).show();

        }

    }

    //Upload file to server
    protected void uploadFile(String filePath){

        // new HttpClient
        HttpClient httpClient = new DefaultHttpClient();

        // post header
        HttpPost httpPost = new HttpPost(postReceiverUrl);

        // the file to be posted
        File file = new File(filePath);
        FileBody fileBody = new FileBody(file);

        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        reqEntity.addPart("file", fileBody);
        httpPost.setEntity(reqEntity);

        // execute HTTP post request
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);

            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {

                String responseStr = null;
                try {
                    responseStr = EntityUtils.toString(resEntity).trim();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }

                Log.v(TAG, "Response: " +  responseStr);

                // you can add an if statement here and do other actions based on the response
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }


    }



        @SuppressLint("NewApi")
        public  String getRealPathFromURI_API19(Context context, Uri uri){
            String filePath = "";
            String wholeID = DocumentsContract.getDocumentId(uri);

            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];

            String[] column = { MediaStore.Images.Media.DATA };

            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";

            Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    column, sel, new String[]{ id }, null);

            int columnIndex = cursor.getColumnIndex(column[0]);

            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex);
            }
            cursor.close();
            return filePath;
        }


        @SuppressLint("NewApi")
        public  String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
            String[] proj = { MediaStore.Images.Media.DATA };
            String result = null;

            CursorLoader cursorLoader = new CursorLoader(
                    context,
                    contentUri, proj, null, null, null);
            Cursor cursor = cursorLoader.loadInBackground();

            if(cursor != null){
                int column_index =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                result = cursor.getString(column_index);
            }
            return result;
        }

        public  String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri){
            String[] proj = { MediaStore.Images.Media.DATA };
            Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index
                    = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }

}


