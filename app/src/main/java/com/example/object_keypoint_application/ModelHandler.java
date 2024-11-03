package com.example.object_keypoint_application;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ai.onnxruntime.*;

public class ModelHandler extends AppCompatActivity {

    private InputStream imageStream;
    private OrtEnvironment env;
    private OrtSession session;
    private List<String> keypointConfidenceList = new ArrayList<>();

    public ModelHandler(InputStream inputStream) {
        try {
            KeypointDetection(inputStream);
        } catch (OrtException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setImageStream(InputStream imageStream) {
        this.imageStream = imageStream;
    }

    private void KeypointDetection(InputStream modelInputStream) throws OrtException, IOException {
        env = OrtEnvironment.getEnvironment();
        byte[] modelBytes = new byte[modelInputStream.available()];
        modelInputStream.read(modelBytes);
        session = env.createSession(modelBytes, new OrtSession.SessionOptions());
    }

    public float[][][][] runInference(float[][][][] inputData) {
        // Prepare input tensor
        float[][][][] outputs = new float[1][17][64][48];
        try {
            OnnxTensor inputTensor = OnnxTensor.createTensor(env, inputData);
            OrtSession.Result output = session.run(Collections.singletonMap("input", inputTensor));
            outputs = (float[][][][]) output.get(0).getValue();
        } catch (OrtException e) {
            throw new RuntimeException(e);
        }
        // Run the model
        return outputs;
    }

    public Bitmap runModel() {
        Bitmap bitmap = loadImage();
        if (bitmap != null) {
            float[][][][] input = preprocessImage_1(bitmap);
            float[][][][] output =  runInference(input);
            float[][] keypoints = processOutput_1(output, bitmap);
            bitmap = drawKeypoints_1(bitmap, keypoints);
        }
        return bitmap;
    }

    private Bitmap loadImage() {
        return BitmapFactory.decodeStream(imageStream);
    }

    public float[][][][] preprocessImage_1(Bitmap bitmap) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 256, 192, true);
        float[][][][] input = new float[1][3][256][192];

        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 192; j++) {
                int pixel = resizedBitmap.getPixel(i, j);
                input[0][0][i][j] = (Color.red(pixel) - 128.0f) / 128.0f;
                input[0][1][i][j] = (Color.green(pixel) - 128.0f) / 128.0f;
                input[0][2][i][j] = (Color.blue(pixel) - 128.0f) / 128.0f;
            }
        }
        return input;
    }

    public float[][] processOutput_1(float[][][][] output, Bitmap bitmap) {
        int numKeypoints = output[0].length;
        int width = output[0][0].length;
        int height = output[0][0][0].length;
        float[][] keypoints = new float[numKeypoints][3];

        for (int k = 0; k < numKeypoints; k++) {
            float maxConfidence = -Float.MAX_VALUE;
            int maxH = -1;
            int maxW = -1;

            for (int w = 0; w < width; w++) {
                for (int h = 0; h < height; h++) {
                    if (output[0][k][w][h] > maxConfidence) {
                        maxConfidence = output[0][k][w][h];
                        maxH = h;
                        maxW = w;
                    }
                }
            }

            keypoints[k][0] = Math.min((maxW * bitmap.getWidth()) / width, bitmap.getWidth() - 1);
            keypoints[k][1] = Math.min((maxH * bitmap.getHeight()) / height, bitmap.getHeight() - 1);
            keypoints[k][2] = maxConfidence;
        }

        return keypoints;
    }
    private Bitmap drawKeypoints_1(Bitmap bitmap, float[][] keypoints) {
        int index = 0;
        keypointConfidenceList.clear();
        Bitmap resultBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(resultBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(5f);

        for (float[] keypoint : keypoints) {
            float x = keypoint[0];
            float y = keypoint[1];
            float confidence = keypoint[2];
            keypointConfidenceList.add("Keypoint "+index + ": "+confidence);
            index = index + 1;
            if (confidence >= 0.5) {
                canvas.drawCircle(x, y, 10, paint);
            }
        }
        return resultBitmap;
    }

    public List<String> getKeypointConfidenceList() {
        return keypointConfidenceList;
    }
}
