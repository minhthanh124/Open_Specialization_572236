package com.example.object_keypoint_application;

import android.graphics.Bitmap;
import org.json.JSONObject;


public interface TransferData {
    void dataSending(Bitmap bitmap);
    void jsonImageNode(JSONObject jsonImageObject);
    void jsonAnnotationsNode(JSONObject jsonAnnotationsObject);
}
