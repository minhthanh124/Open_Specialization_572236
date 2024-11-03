package com.example.object_keypoint_application;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.common.util.concurrent.ListenableFuture;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EvaluationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EvaluationFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private VideoView video;
    private ModelHandler modelHandler;
    private ImageView imageView;
    private TextView performanceView;
    private TextView performanceView2;
    private TextView measuredTime;
    private PreviewView previewView;
    private AnnotationOverlayView keypointOverlayView;
    private int width;
    private int height;

    public EvaluationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EvaluationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EvaluationFragment newInstance(String param1, String param2) {
        EvaluationFragment fragment = new EvaluationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private void startModel() {
        long startTime = System.currentTimeMillis();
        Bitmap resultBitmap = modelHandler.runModel();

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        resultBitmap = Bitmap.createBitmap(resultBitmap, 0, 0, resultBitmap.getWidth(), resultBitmap.getHeight(), matrix, true);

        imageView.setImageBitmap(resultBitmap);
        long endTime = System.currentTimeMillis();
        long elapseTime = endTime - startTime;
        List<String> keypointConfidenceList = modelHandler.getKeypointConfidenceList();
        int index = keypointConfidenceList.size() / 2;
        List<String> part_1 = new ArrayList<>();
        List<String> part_2 = new ArrayList<>();
        for (int i = 0; i < index + 1; i++) {
            part_1.add(keypointConfidenceList.get(i));
        }
        for (int i = index + 1; i < keypointConfidenceList.size(); i++) {
            part_2.add(keypointConfidenceList.get(i));
        }
        String result_1 = String.join("\n", part_1);
        String result_2 = String.join("\n", part_2);
        performanceView.setText(result_1);
        performanceView2.setText(result_2);
        measuredTime.setText(elapseTime + " ms");
    }

    private void predictImageKeypoint(String photoPath) {
        try {
            File file = new File(photoPath);
            InputStream imageStream = new FileInputStream(file);//getActivity().getAssets().open(photoPath);
            modelHandler.setImageStream(imageStream);
            startModel();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(getActivity());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (InterruptedException | ExecutionException e) {
            }
        }, ContextCompat.getMainExecutor(getActivity()));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                //.setTargetRotation(previewView.getDisplay().getRotation())
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis.Builder builder = new ImageAnalysis.Builder();
        builder.setTargetResolution(new android.util.Size(256,192));
        builder.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST);//.setTargetRotation(previewView.getDisplay().getRotation())
        ImageAnalysis imageAnalysis = builder
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(getActivity()), new MyImageAnalyzer());
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_evaluation, container, false);
        assert this.getArguments() != null;
        imageView = view.findViewById(R.id.imgview);
        video = view.findViewById(R.id.videoView);
        performanceView = view.findViewById(R.id.performanceInfo);
        performanceView2 = view.findViewById(R.id.performanceInfo2);
        measuredTime = view.findViewById(R.id.delaytime);
        previewView = view.findViewById(R.id.previewView);
        keypointOverlayView = view.findViewById(R.id.keypointOverlayView);
        previewView.post(new Runnable() {
            @Override
            public void run() {
                width = previewView.getWidth();
                height = previewView.getHeight();
            }
        });

        try {
            File file = new File(this.getArguments().getString("messagemodel"));
            InputStream modelInputStream = getActivity().getAssets().open("transpose_r_a4_car.onnx");
            modelHandler = new ModelHandler(modelInputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (this.getArguments().getString("message").contains(".mp4")) {
            startCamera();
        }
        else {
            predictImageKeypoint(this.getArguments().getString("message"));
        }
        return view;
    }

    private class MyImageAnalyzer implements ImageAnalysis.Analyzer {

        @Override
        public void analyze(@NonNull ImageProxy image) {
            long start_time = System.currentTimeMillis();
            Bitmap bitmap = imageProxyToBitmap(image);
            float[][][][] inputBuffer = modelHandler.preprocessImage_1(bitmap);
            float[][][][] outputArray = modelHandler.runInference(inputBuffer);
            float[][] keypoints = modelHandler.processOutput_1(outputArray, bitmap);
            if (bitmap != null) {
                long end_time = System.currentTimeMillis();
                long total_time = end_time - start_time;
                Log.d("TimeTranspose: ", "" + total_time);
            }
            image.close();
        }

        private Bitmap imageProxyToBitmap(ImageProxy image) {

            ImageProxy.PlaneProxy[] planes = image.getPlanes();
            ByteBuffer yBuffer = planes[0].getBuffer();
            ByteBuffer uBuffer = planes[1].getBuffer();
            ByteBuffer vBuffer = planes[2].getBuffer();

            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();

            byte[] nv21 = new byte[ySize + uSize + vSize];
            //U and V are swapped
            yBuffer.get(nv21, 0, ySize);
            vBuffer.get(nv21, ySize, vSize);
            uBuffer.get(nv21, ySize + vSize, uSize);

            YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

            byte[] imageBytes = out.toByteArray();
            Bitmap bm = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        }
    }
}