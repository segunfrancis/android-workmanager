package com.example.background.workers;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.example.background.Constants;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class BlurWorker extends Worker {

    private static final String TAG = BlurWorker.class.getSimpleName();

    public BlurWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context applicationContext = getApplicationContext();
        String resourceUri = getInputData().getString(Constants.KEY_IMAGE_URI);
        try {
            //Bitmap picture = BitmapFactory.decodeResource(applicationContext.getResources(), R.drawable.test);
            if (TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "doWork: Invalid Input Uri");
                throw new IllegalArgumentException("Invalid Input Uri");
            }
            ContentResolver resolver = applicationContext.getContentResolver();
            Bitmap picture = BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)));
            // Blur the bitmap
            Bitmap output = WorkerUtils.blurBitmap(picture, applicationContext);
            // Write bitmap to a temporary file
            Uri outputUri = WorkerUtils.writeBitmapToFile(applicationContext, output);
            Data outputData = new Data.Builder()
                    .putString(Constants.KEY_IMAGE_URI, outputUri.toString())
                    .build();
            WorkerUtils.makeStatusNotification("Output is " + outputUri.toString(), applicationContext);
            return Result.success(outputData);
        } catch (Throwable throwable) {
            Log.e(TAG, "doWork: Error applying blur", throwable);
            return Result.failure();
        }
    }
}
