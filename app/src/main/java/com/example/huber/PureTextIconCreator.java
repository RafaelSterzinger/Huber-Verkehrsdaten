package com.example.huber;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.TypedValue;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

class PureTextIconCreator {
    private PureTextIconCreator() {
    }

    static BitmapDescriptor createPureTextIcon(String text, Resources resources) {
        Paint textPaint = new Paint();

        int spSize = 17;
        float scaledSizeInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spSize, resources.getDisplayMetrics());
        textPaint.setTextSize(scaledSizeInPixels);

        float textWidth = textPaint.measureText(text);
        float textHeight = textPaint.getTextSize();
        int width = (int) (textWidth);
        int height = (int) (textHeight);

        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);

        canvas.translate(0, height);
        canvas.drawText(text, 0, 0, textPaint);
        return BitmapDescriptorFactory.fromBitmap(image);
    }
}
