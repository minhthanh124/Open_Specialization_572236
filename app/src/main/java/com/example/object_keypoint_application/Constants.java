package com.example.object_keypoint_application;

import android.Manifest;

import org.checkerframework.checker.index.qual.PolyUpperBound;

public class Constants {
    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";
    public static final int REQUEST_CODE_PERMISSIONS = 1;
    public static final String[] REQUIRED_PERMISSIONS = new String[]{android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static final String MODEL_FILENAME = "model.onnx";
    public static final String HOST_NAME = "127.0.0.1";
    public static final int PORT = 5000;
    public static final String jsonString = "{\n" +
            "\"info\": {\n" +
            "\"description\": \"COCO 2017 Dataset\",\n" +
            "\"url\": \"http://cocodataset.org\",\n" +
            "\"version\": \"1.0\",\n" +
            "\"year\": 2017,\n" +
            "\"contributor\": \"COCO Consortium\",\n" +
            "\"date_created\": \"2017/09/01\"\n" +
            "},\n" +
            "\"licenses\": [\n" +
            "{\n" +
            "\"url\": \"http://creativecommons.org/licenses/by-nc-sa/2.0/\",\n" +
            "\"id\": 1,\n" +
            "\"name\": \"Attribution-NonCommercial-ShareAlike License\"\n" +
            "},\n" +
            "{\n" +
            "\"url\": \"http://creativecommons.org/licenses/by-nc/2.0/\",\n" +
            "\"id\": 2,\n" +
            "\"name\": \"Attribution-NonCommercial License\"\n" +
            "},\n" +
            "{\n" +
            "\"url\": \"http://creativecommons.org/licenses/by-nc-nd/2.0/\",\n" +
            "\"id\": 3,\n" +
            "\"name\": \"Attribution-NonCommercial-NoDerivs License\"\n" +
            "},\n" +
            "{\n" +
            "\"url\": \"http://creativecommons.org/licenses/by/2.0/\",\n" +
            "\"id\": 4,\n" +
            "\"name\": \"Attribution License\"\n" +
            "},\n" +
            "{\n" +
            "\"url\": \"http://creativecommons.org/licenses/by-sa/2.0/\",\n" +
            "\"id\": 5,\n" +
            "\"name\": \"Attribution-ShareAlike License\"\n" +
            "},\n" +
            "{\n" +
            "\"url\": \"http://creativecommons.org/licenses/by-nd/2.0/\",\n" +
            "\"id\": 6,\n" +
            "\"name\": \"Attribution-NoDerivs License\"\n" +
            "},\n" +
            "{\n" +
            "\"url\": \"http://flickr.com/commons/usage/\",\n" +
            "\"id\": 7,\n" +
            "\"name\": \"No known copyright restrictions\"\n" +
            "},\n" +
            "{\n" +
            "\"url\": \"http://www.usa.gov/copyright.shtml\",\n" +
            "\"id\": 8,\n" +
            "\"name\": \"United States Government Work\"\n" +
            "}\n" +
            "],\n" +
            "\"images\": [],\n" +
            "\"annotations\": [],\n" +
            "\"categories\": [\n" +
            "{\n" +
            "\"supercategory\": \"person\",\n" +
            "\"id\": 1,\n" +
            "\"name\": \"person\",\n" +
            "\"keypoints\": [\n" +
            "\"nose\",\n" +
            "\"left_eye\",\n" +
            "\"right_eye\",\n" +
            "\"left_ear\",\n" +
            "\"right_ear\",\n" +
            "\"left_shoulder\",\n" +
            "\"right_shoulder\",\n" +
            "\"left_elbow\",\n" +
            "\"right_elbow\",\n" +
            "\"left_wrist\",\n" +
            "\"right_wrist\",\n" +
            "\"left_hip\",\n" +
            "\"right_hip\",\n" +
            "\"left_knee\",\n" +
            "\"right_knee\",\n" +
            "\"left_ankle\",\n" +
            "\"right_ankle\"\n" +
            "],\n" +
            "\"skeleton\": [\n" +
            "[12, 11],\n" +
            "[11, 17],\n" +
            "[11, 13],\n" +
            "[13, 14],\n" +
            "[14, 15],\n" +
            "[14, 16],\n" +
            "[11, 10],\n" +
            "[10, 9],\n" +
            "[9, 8],\n" +
            "[8, 3],\n" +
            "[3, 4],\n" +
            "[4, 5],\n" +
            "[5, 6],\n" +
            "[5, 7],\n" +
            "[3, 2],\n" +
            "[2, 1]\n" +
            "]\n" +
            "}\n" +
            "]\n" +
            "}";
}
