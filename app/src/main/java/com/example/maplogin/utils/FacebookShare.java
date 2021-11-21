package com.example.maplogin.utils;

import android.app.Activity;
import android.net.Uri;

import com.facebook.share.ShareApi;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

public class FacebookShare {
    public static void shareLink(Activity activity, String name, String URL) {
        ShareDialog shareDialog = new ShareDialog(activity);
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent content = new ShareLinkContent.Builder()
                    .setQuote(String.format("I have cleared %s.", name))
                    .setContentUrl(Uri.parse(URL))
                    .build();

//        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.ani_cat);
//        SharePhoto photo = new SharePhoto.Builder()
//                .setBitmap(image)
//                .setCaption("#Tutorialwing")
//                .build();
//        SharePhotoContent content = new SharePhotoContent.Builder()
//                .addPhoto(photo)
//                .build();

            ShareApi.share(content, null);
            shareDialog.show(content);
        }
    }
}
