package com.example.maplogin.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ShareHelper {
    public static void shareScreenshot(Activity activity) {
        Bitmap bitmap = takeSnapshot(activity);
        Uri uri = saveImage(activity, bitmap);
        startShareIntent(activity, uri);
    }

    private static Bitmap takeSnapshot(Activity activity) {
        // TODO
        return null;
    }

    private static Uri saveImage(Activity activity, Bitmap image) {
        Uri uri = null;
        try {
            File imagesFolder = new File(activity.getCacheDir(), "images");
            imagesFolder.mkdirs();
            File file = new File(imagesFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(activity, "com.example.maplogin.fileprovider", file);

        } catch (IOException e) {
            Toast.makeText(activity,
                    "IOException while trying to write file for sharing: " + e.getMessage()
                    ,Toast.LENGTH_LONG).show();
        }
        return uri;
    }

    private static void startShareIntent(Activity activity, Uri uri) {
        if (uri != null) {
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("image/png");
            activity.startActivity(intent);
        }
    }
}
