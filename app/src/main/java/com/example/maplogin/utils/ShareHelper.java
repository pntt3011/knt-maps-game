package com.example.maplogin.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.maplogin.R;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ShareHelper {
    public static void openShareDialog(Activity activity) {
        final Dialog shareDialog = new Dialog(activity);
        shareDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        shareDialog.setContentView(R.layout.share_screen);

        Window window = shareDialog.getWindow();
        if(window == null){
            return;
        }

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        shareDialog.setCancelable(true);

        changeLocationName(shareDialog, activity);
        changePoint(shareDialog, activity);
        changeAvatar(shareDialog);
        changeUserName(shareDialog);

        ImageButton shareButton = shareDialog.findViewById(R.id.dialog_share_button);
        shareButton.setOnClickListener(v -> {
            Bitmap bitmap = takeSnapshot(window.getDecorView());
            Uri uri = saveImage(activity, bitmap);
            startShareIntent(activity, uri);
        });

        shareDialog.show();
    }

    private static void changeAvatar(Dialog dialog) {
        ImageView avatarView = dialog.findViewById(R.id.dialog_avatar);
        FirebaseUser user = DatabaseAdapter.getInstance().getCurrentUser();

        if (!user.isAnonymous()) {
            Picasso.get().load(user.getPhotoUrl())
                    .fit().into(avatarView);
        } else {
            avatarView.setImageResource(R.mipmap.ic_launcher);
        }
    }

    private static void changeUserName(Dialog dialog) {
        TextView nameView = dialog.findViewById(R.id.dialog_name);
        FirebaseUser user = DatabaseAdapter.getInstance().getCurrentUser();

        if (!user.isAnonymous()) {
            nameView.setText(user.getDisplayName());
        } else {
            nameView.setText("Anonymous");
        }
    }

    private static void changeLocationName(Dialog shareDialog, Activity activity) {
        TextView shareLocation = shareDialog.findViewById(R.id.dialog_location);
        TextView location = activity.findViewById(R.id.bottom_sheet_title);
        shareLocation.setText(location.getText());
    }

    private static void changePoint(Dialog shareDialog, Activity activity) {
        TextView sharePoint = shareDialog.findViewById(R.id.dialog_point);
        TextView point = activity.findViewById(R.id.point);
        sharePoint.setText(point.getText());
    }

    private static Bitmap takeSnapshot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
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
