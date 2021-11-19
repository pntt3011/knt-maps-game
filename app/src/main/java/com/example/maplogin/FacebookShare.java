package com.example.maplogin;

import android.app.Activity;
import android.net.Uri;

import com.facebook.CallbackManager;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

public class FacebookShare {
    private final CallbackManager callbackManager;
    private final ShareDialog shareDialog;

    public FacebookShare(Activity activity) {
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(activity);
    }

    public void shareLink(String name, String URL) {
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setQuote(String.format("I have cleared %s.", name))
                    .setContentUrl(Uri.parse(URL))
                    .build();
            shareDialog.show(linkContent);
        }
    }
}
