package com.example.maplogin.utils;

import android.app.Activity;
import android.net.Uri;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

public class FacebookShare {
    public static void shareLink(Activity activity, String name, String URL) {
        ShareDialog shareDialog = new ShareDialog(activity);
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setQuote(String.format("I have cleared %s.", name))
                    .setContentUrl(Uri.parse(URL))
                    .build();
            shareDialog.show(linkContent);
        }
    }
}
