package com.example.object_keypoint_application;


import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AnnotationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AnnotationFragment extends Fragment {

    public static final String TAG = AnnotationFragment.class.getName();
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private Button btn_AddKeypoint;
    private Button btn_AddBoundingBox;
    private Button btn_AddSegmentation;
    private AnnotationOverlayView touchImageView;
    private Button btn_TakeMorePhoto;
    private Button btn_UpdateJSON;
    private Bitmap bitmap;
    private TransferData tranfer;
    private int width;
    private int height;
    private float scaled_X;
    private float scaled_Y;
    private List<float[]> keypoints_List = new ArrayList<>();
    private List<float[]> boundingBox_List = new ArrayList<>();
    private List<float[]> segmentations_List = new ArrayList<>();

    public AnnotationFragment() {
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AnnotationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AnnotationFragment newInstance(String param1, String param2) {
        AnnotationFragment fragment = new AnnotationFragment();
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

    public Bitmap resizeBitmap(Bitmap getBitmap, int maxSize) {
        int width = getBitmap.getWidth();
        int height = getBitmap.getHeight();
        double x;

        if (width >= height && width > maxSize) {
            x = width / height;
            width = maxSize;
            height = (int) (maxSize / x);
        } else if (height >= width && height > maxSize) {
            x = height / width;
            height = maxSize;
            width = (int) (maxSize / x);
        }
        return Bitmap.createScaledBitmap(getBitmap, width, height, false);
    }

    public void setBitmapAnnotation(String photoPath) {
        bitmap = BitmapFactory.decodeFile(photoPath);
        if (bitmap != null) {
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            touchImageView.setImageBitmap(bitmap);
            tranfer.dataSending(bitmap);
            touchImageView.setVisibility(View.VISIBLE);
            touchImageView.setKeypoints(bitmap);
            keypointAnnotation();
       }
}

@SuppressLint("ClickableViewAccessibility")
    private void keypointAnnotation() {
        touchImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    float x = event.getX();
                    float y = event.getY();
                    touchImageView.drawPoint(x, y);
                }
                return true;
            }
        });
    }

    private void dataPackage(String requestMessage) {
        switch (requestMessage) {
            case "Keypoints": {
                keypoints_List = new ArrayList<>(touchImageView.circles);
                btn_AddKeypoint.setBackgroundColor(Color.BLUE);
                break;
            }
            case "BBox": {
                boundingBox_List = new ArrayList<>(touchImageView.circles);
                btn_AddBoundingBox.setBackgroundColor(Color.BLUE);
                break;
            }
            case "Segmentations": {
                segmentations_List = new ArrayList<>(touchImageView.circles);
                btn_AddSegmentation.setBackgroundColor(Color.BLUE);
                break;
            }
            default: {
                Log.w(TAG,"Annotations not found! ");
                break;
            }
        }

        btn_TakeMorePhoto.setVisibility(View.VISIBLE);
        btn_UpdateJSON.setVisibility(View.VISIBLE);
        touchImageView.clearPoints();

    }

    private JSONArray getAnnotationsArray(List<float[]> annotation_List, String reqString) throws JSONException {
        scaled_X = (float) bitmap.getWidth() / width;
        scaled_Y = (float) bitmap.getHeight() / height;
        Log.d("scaled_number: ", " "+scaled_X + " " + scaled_Y);
        JSONArray jsonArray = new JSONArray();
        for (float[] circle : annotation_List) {
            jsonArray.put(circle[0] * scaled_X);
            jsonArray.put(circle[1] * scaled_Y);
            if (reqString.equals("keypoints")) {
                jsonArray.put(2);
            }
        }
        return jsonArray;
    }

    private void updateJsonFile(JSONObject jsonImageObject, JSONObject jsonAnnotationsObject) {

        try {
            int randomNum = ThreadLocalRandom.current().nextInt(1000, 8900 + 1);
            String fileName  = "00000000" + randomNum + ".jpg";

            JSONArray keypoints = getAnnotationsArray(keypoints_List, "keypoints");
            JSONArray segmentations = getAnnotationsArray(segmentations_List, "segmentations");
            JSONArray boundingBox   = getAnnotationsArray(boundingBox_List, "boundingbox");
            JSONArray segmentArray = new JSONArray();
            segmentArray.put(segmentations);
            jsonImageObject.put("license",3);
            jsonImageObject.put("file_name", fileName);
            jsonImageObject.put("coco_url", "http://images.cocodataset.org/train2017/000000581328.jpg");
            jsonImageObject.put("width", bitmap.getWidth());
            jsonImageObject.put("height", bitmap.getHeight());
            jsonImageObject.put("date_captured", "2013-11-19 17:55:37");
            jsonImageObject.put("flickr_url", "http://farm4.staticflickr.com/3448/5750092136_9d64bf13bd_z.jpg");
            jsonImageObject.put("id", randomNum);

            jsonAnnotationsObject.put("segmentation", segmentArray);
            jsonAnnotationsObject.put("num_keypoints", 17);
            jsonAnnotationsObject.put("area", 24825.87695);
            jsonAnnotationsObject.put("iscrowd", 0);
            jsonAnnotationsObject.put("keypoints",keypoints);
            jsonAnnotationsObject.put("image_id", randomNum);
            jsonAnnotationsObject.put("bbox", boundingBox);
            jsonAnnotationsObject.put("category_id", 1);
            jsonAnnotationsObject.put("id", randomNum);

            tranfer.jsonImageNode(jsonImageObject);
            tranfer.jsonAnnotationsNode(jsonAnnotationsObject);
            Toast.makeText(getContext(), "Update JSON successfully.", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void backToMainActivity() {
        getFragmentManager().popBackStack();
    }


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_annotation, container, false);

        JSONObject jsonImageObject = new JSONObject();
        JSONObject jsonAnnotationsObject = new JSONObject();
        tranfer = (TransferData) getActivity();

        touchImageView = view.findViewById(R.id.keypointOverlayView);
        touchImageView.post(new Runnable() {
            @Override
            public void run() {
                width = touchImageView.getWidth();
                height = touchImageView.getHeight();
            }
        });
        btn_AddKeypoint = view.findViewById(R.id.addKeypoint);
        btn_AddBoundingBox = view.findViewById(R.id.addBBox);
        btn_AddSegmentation = view.findViewById(R.id.addSegmentation);
        btn_TakeMorePhoto = view.findViewById(R.id.Btn_TakeMorePhoto);
        btn_UpdateJSON    = view.findViewById(R.id.Btn_Update);

        btn_AddKeypoint.setOnClickListener(v -> dataPackage("Keypoints"));
        btn_AddBoundingBox.setOnClickListener(v -> dataPackage("BBox"));
        btn_AddSegmentation.setOnClickListener(v -> dataPackage("Segmentations"));
        btn_TakeMorePhoto.setOnClickListener(v -> backToMainActivity());
        btn_UpdateJSON.setOnClickListener(v -> updateJsonFile(jsonImageObject, jsonAnnotationsObject));
        setBitmapAnnotation(this.getArguments().getString("message"));
        return view;
    }
}