package com.ndnlogic.legs;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.entities.Photo;
import com.sromku.simple.fb.entities.Privacy;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnPublishListener;

import java.io.IOException;

public class SharePhoto extends Activity implements CompoundButton.OnCheckedChangeListener {
    SimpleFacebook mSimpleFacebook;
    private static final String TAG = "LEGZ-SHARE";
    Switch facebookSwitch = null;
    Switch twitterSwitch = null;
    Button shareButton = null;
    Bitmap sharePhoto = null;
    EditText caption = null;
    private ProgressDialog mProgress;

    // Login listener
    private OnLoginListener mOnLoginListener = new OnLoginListener() {

        @Override
        public void onFail(String reason) {
            Log.w(TAG, "Failed to login");
        }

        @Override
        public void onException(Throwable throwable) {
            Log.e(TAG, "Bad thing happened", throwable);
        }

        @Override
        public void onThinking() {
            // show progress bar or something to the user while login is
            // happening
        }

        @Override
        public void onLogin() {
            // change the state of the button or do whatever you want
            toast("You are logged in");
        }

        @Override
        public void onNotAcceptingPermissions(Permission.Type type) {
            toast(String.format("You didn't accept %s permissions", type.name()));
        }
    };

    /**
     * Show toast
     *
     * @param message
     */
    private void toast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_photo);

        Permission[] permissions = new Permission[]{
                Permission.USER_PHOTOS,
                Permission.EMAIL,
                Permission.PUBLISH_ACTION,
                Permission.PUBLISH_STREAM
        };

        SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
                .setAppId(getResources().getString(R.string.app_id))
                .setNamespace(getResources().getString(R.string.fb_namespace))
                .setPermissions(permissions)
                .build();

        SimpleFacebook.setConfiguration(configuration);


        ImageView thumb = (ImageView) findViewById(R.id.photo_thumb);
        caption = (EditText) findViewById(R.id.photo_caption);

        shareButton = (Button) findViewById(R.id.share_button);
        System.out.println(getIntent().getExtras().get("IMAGE_URI").getClass());
        thumb.setImageURI((Uri) getIntent().getExtras().get("IMAGE_URI"));

        facebookSwitch = (Switch) findViewById(R.id.facebook_switch);
        facebookSwitch.setOnCheckedChangeListener(this);

        try {
            sharePhoto = MediaStore.Images.Media.getBitmap(this.getContentResolver(), (Uri) getIntent().getExtras().get("IMAGE_URI"));
        } catch (IOException e) {
            e.printStackTrace();
        }


        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // take screenshot


                // set privacy
                Privacy privacy = new Privacy.Builder()
                        .setPrivacySettings(Privacy.PrivacySettings.SELF)
                        .build();


                // create Photo instance and add some properties
                Photo photo = new Photo.Builder()
                        .setImage(sharePhoto)
                        .setName(caption.getText().toString())
                        .setPrivacy(privacy)
                        .build();

                // publish
                mSimpleFacebook.publish(photo, new OnPublishListener() {

                    @Override
                    public void onFail(String reason) {
                        hideDialog();
                        // insure that you are logged in before publishing
                        Log.w(TAG, "Failed to publish");
                        toast(reason);
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        hideDialog();
                        Log.e(TAG, "Bad thing happened", throwable);
                    }

                    @Override
                    public void onThinking() {
                        // show progress bar or something to the user while
                        // publishing
                        showDialog();
                    }

                    @Override
                    public void onComplete(String id) {
                        hideDialog();
                        toast("Published successfully. The new image id = " + id);
                    }
                });
            }
        });
    }

    private void showDialog() {
        mProgress = ProgressDialog.show(this, "Thinking", "Waiting for Facebook", true);
    }

    private void hideDialog() {
        if (mProgress != null) {
            mProgress.hide();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            mSimpleFacebook.login(mOnLoginListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mSimpleFacebook = SimpleFacebook.getInstance(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }


    /*

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.share_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
*/


}
