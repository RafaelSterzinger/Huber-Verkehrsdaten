package com.example.huber;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.TypedValue;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

class PureTextIconCreator {
    private PureTextIconCreator(){}

    // returns a BitmapDescriptor that can be used with setIcon() of a Marker
    // https://stackoverflow.com/questions/25544370/google-maps-api-for-android-v2-how-to-add-text-with-no-background
    static BitmapDescriptor createPureTextIcon(String text, Resources resources) {
        Paint textPaint = new Paint(); // Adapt to your needs

        // TODO: size should be relative to screen after zooming out/not shown at all
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

        // For development only:
        // Set a background in order to see the
        // full size and positioning of the bitmap.
        // Remove that for a fully transparent icon.
        //canvas.drawColor(Color.LTGRAY);

        canvas.drawText(text, 0, 0, textPaint);
        return BitmapDescriptorFactory.fromBitmap(image);
    }
}
