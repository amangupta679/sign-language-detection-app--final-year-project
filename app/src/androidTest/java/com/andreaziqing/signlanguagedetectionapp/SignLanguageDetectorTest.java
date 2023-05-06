package com.andreaziqing.signlanguagedetectionapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Size;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.andreaziqing.signlanguagedetectionapp.Detector.TFLiteInterpreter.Detector;
import com.andreaziqing.signlanguagedetectionapp.Detector.TFLiteInterpreter.TFLiteObjectDetectionAPIModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class SignLanguageDetectorTest {

    // Configuration values for the Object detection SSD model.
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final boolean TF_OD_API_IS_QUANTIZED = true;
    private static final String TF_OD_API_MODEL_FILE = "detect_sign_V4B_meta.tflite";
    private static final String TF_OD_API_LABELS_FILE = "labelmap_signs.txt";
    //private static final DetectorActivity.DetectorMode MODE = DetectorActivity.DetectorMode.TF_OD_API;
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.3f;
    private static final boolean MAINTAIN_ASPECT = false;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    private static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final int cropSize = TF_OD_API_INPUT_SIZE;
    private static Detector signDetector;
    private Context context = ApplicationProvider.getApplicationContext();//InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Before
    public void setUp() {
        try {
            signDetector = TFLiteObjectDetectionAPIModel.create(
                    context,
                    TF_OD_API_MODEL_FILE,
                    TF_OD_API_LABELS_FILE,
                    TF_OD_API_INPUT_SIZE,
                    TF_OD_API_IS_QUANTIZED);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        signDetector.close();
        signDetector = null;
    }

    @Test
    public void SignLanguageDetector_isInitialized(){
        assertNotNull(signDetector);
    }

    @Test
    public void SignLanguageDetector_assertDetection_A(){

        Bitmap inputImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.test_detector_image_a);

        List<Detector.Recognition> results = signDetector.recognizeImage(inputImage);

        for (final Detector.Recognition result : results){
            if (result.getConfidence() > MINIMUM_CONFIDENCE_TF_OD_API) {
                assertEquals("A", result.getTitle());
            }
        }
    }

    @Test
    public void SignLanguageDetector_assertDetection_H(){

        Bitmap inputImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.test_detector_image_h);

        List<Detector.Recognition> results = signDetector.recognizeImage(inputImage);

        for (final Detector.Recognition result : results){
            if (result.getConfidence() > MINIMUM_CONFIDENCE_TF_OD_API) {
                assertEquals("H", result.getTitle());
            }
        }
    }

    @Test
    public void SignLanguageDetector_assertDetection_C(){

        Bitmap inputImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.test_detector_image_c);

        List<Detector.Recognition> results = signDetector.recognizeImage(inputImage);

        for (final Detector.Recognition result : results){
            if (result.getConfidence() > MINIMUM_CONFIDENCE_TF_OD_API) {
                assertEquals("C", result.getTitle());
            }
        }
    }
}
