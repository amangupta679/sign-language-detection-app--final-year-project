package com.andreaziqing.signlanguagedetectionapp.HelperClasses.CustomView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.andreaziqing.signlanguagedetectionapp.Detector.TFLiteInterpreter.Detector;
import java.util.List;

/**
 * Recognitions Score View
 *
 * View that shows the sign language detection model recognitions and scores.
 * Draws the detected class (i.e. sign letter) as well as the confidence score.
 */
public class RecognitionScoreView extends View implements ResultsView {
    private static final float TEXT_SIZE_DIP = 14;
    private final float textSizePx;
    private final Paint fgPaint;
    private final Paint bgPaint;
    private List<Detector.Recognition> results;

    public RecognitionScoreView(final Context context, final AttributeSet set) {
        super(context, set);

        textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        fgPaint = new Paint();
        fgPaint.setTextSize(textSizePx);

        bgPaint = new Paint();
        bgPaint.setColor(0xcc4285f4);
    }

    @Override
    public void setResults(final List<Detector.Recognition> results) {
        this.results = results;
        postInvalidate();
    }

    @Override
    public void onDraw(final Canvas canvas) {
        final int x = 10;
        int y = (int) (fgPaint.getTextSize() * 1.5f);

        canvas.drawPaint(bgPaint);

        if (results != null) {
            for (final Detector.Recognition recog : results) {
                canvas.drawText(recog.getTitle() + ": " + recog.getConfidence(), x, y, fgPaint);
                y += (int) (fgPaint.getTextSize() * 1.5f);
            }
        }
    }
}
