package com.example.object_keypoint_application;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity implements TransferData {

    private JSONObject jsonObject;
    private JSONArray jsonArray;
    private Socket socket;
    private Button btn_CollectData;
    private Button btn_Train;
    private Button btn_Validate;
    private Bitmap bitmap;
    private String modelPath = "/data/user/0/com.example.object_keypoint_application/files/model.onnx";
    private boolean isConnected = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_CollectData = findViewById(R.id.Btn_CollectData);
        btn_Train = findViewById(R.id.Btn_Train);
        btn_Validate = findViewById(R.id.Btn_Validate);

        try {
            jsonObject = new JSONObject(Constants.jsonString);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        btn_CollectData.setOnClickListener(v -> gotoCameraFragmentForDataCollect());
        btn_Train.setOnClickListener(v -> sendTrainReq());
        btn_Validate.setOnClickListener(v -> gotoCameraFragmentForTesting());
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    private void gotoCameraFragmentForDataCollect() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        CameraFragment cameraFragment = new CameraFragment();
//        btn_Validate.setVisibility(View.GONE);
//        btn_Train.setVisibility(View.GONE);
//        btn_CollectData.setVisibility(View.GONE);
        Bundle bundle = new Bundle();
        bundle.putString("message", "Train");
        cameraFragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.main_container, cameraFragment, CameraFragment.class.getName())
                           .addToBackStack(CameraFragment.class.getName()).commit();
    }

    private void gotoCameraFragmentForTesting() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        CameraFragment cameraFragment = new CameraFragment();
        btn_Validate.setVisibility(View.GONE);
        btn_Train.setVisibility(View.GONE);
        btn_CollectData.setVisibility(View.GONE);
        Bundle bundle = new Bundle();
        bundle.putString("message", "Test");
        bundle.putString("messagemodel", modelPath);
        cameraFragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.main_container, cameraFragment, CameraFragment.class.getName())
                .addToBackStack(CameraFragment.class.getName()).commit();
    }

    private void downloadModel() {
        try {
          //  socket = new Socket(Constants.HOST_NAME, Constants.PORT);
           // socket.shutdownOutput();
            InputStream in = new BufferedInputStream(socket.getInputStream());
            File modelFile = new File(getFilesDir(), Constants.MODEL_FILENAME);
            OutputStream outputStream = new FileOutputStream(modelFile);
            byte[] buffer = new byte[8192];
            int bytesRead = 0;
            while ((bytesRead = in.read(buffer)) != -1) {
                Log.d(TAG,"Model reading byte ");
                outputStream.write(buffer, 0, bytesRead);
            }

            modelPath = modelFile.getAbsolutePath();
            outputStream.flush();
            outputStream.close();
            in.close();
            socket.close();
            Log.d(TAG, "Model downloaded to: " + modelFile.getAbsolutePath());
            Toast.makeText(getApplicationContext(), "Download new model successfully.", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error downloading model", e);
        }
    }

    private void sendTrainReq() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!isConnected) {
                        socket = new Socket(Constants.HOST_NAME, Constants.PORT);
                        isConnected = true;
                    }
                    ByteBuffer buffers = ByteBuffer.allocate(4);
                    buffers.putInt(0xD9);//Length of JSON data
                    writeData(buffers);
                    downloadModel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sendData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!isConnected) {
                        socket = new Socket(Constants.HOST_NAME, Constants.PORT);
                        isConnected = true;
                    }
                    byte[] jsonBytes = jsonObject.toString().getBytes();
                    // Convert bitmap to byte array
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byte[] imageBytes = byteArrayOutputStream.toByteArray();

                    // Combine JSON and image byte arrays
                    ByteBuffer buffer= ByteBuffer.allocate(4 + 4 + jsonBytes.length + 4 + imageBytes.length);
                    buffer.putInt(0x99);
                    buffer.putInt(jsonBytes.length);  // Length of JSON data
                    buffer.put(jsonBytes); // JSON data
                    buffer.putInt(imageBytes.length);
                    buffer.put(imageBytes);
                    writeData(buffer);
                    downloadModel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void writeData(ByteBuffer buffer) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(ByteBuffer.allocate(4).putInt(buffer.array().length).array());
        outputStream.write(buffer.array());
        outputStream.flush();
    }

    @Override
    public void dataSending(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public void jsonImageNode(JSONObject jsonImageObject) {
        try {
            jsonObject = new JSONObject(Constants.jsonString);
            jsonArray = jsonObject.getJSONArray("images");
            jsonArray.put(jsonImageObject);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void jsonAnnotationsNode(JSONObject jsonAnnotationsObject) {
        try {
            jsonArray = jsonObject.getJSONArray("annotations");
            jsonArray.put(jsonAnnotationsObject);
            sendData();

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}