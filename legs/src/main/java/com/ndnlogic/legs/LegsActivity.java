package com.ndnlogic.legs;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LegsActivity extends Activity implements View.OnTouchListener {
    static final int NONE = 0;
    int mode = NONE;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    private static final String TAG = "LEGZ";
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final String IMAGE_DIRECTORY_NAME = "legfie gallery";
    List<DrawerItem> dataList;
    ImageView legsImageView;
    int status = 0;
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;
    ImageView hashtag;
    private CharSequence mTitle;
    private Uri fileUri; // file url to store image/video
    private Menu topBarMenu;
    private TextView watermarkTextView;
    Bitmap finalImage = null;
    MenuItem saveItemActionBar, cameraItemActionBar, settingsItemActionBar = null;

    private ShareActionProvider mShareActionProvider;
    Uri imageUri = null;


    private static File getOutputMediaFile(boolean isPublic) {
        // External sdcard location
        File mediaStorageDir = null;
        if (isPublic) {
            mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), IMAGE_DIRECTORY_NAME);
        } else {
            mediaStorageDir = new File(Environment.getExternalStorageDirectory(), IMAGE_DIRECTORY_NAME);
        }

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create " + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator + "legfie-" + timeStamp + ".jpg");

    }

    private String generateFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return "legfie-" + timeStamp + ".png";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legs);

        //getActionBar().setDisplayHomeAsUpEnabled(true);
        // getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(false);

        hashtag = (ImageView) findViewById(R.id.hashtag);


        dataList = new ArrayList<DrawerItem>();
        addListenerOnButton();
    }

    public void addListenerOnButton() {
        ImageButton imageButton = (ImageButton) findViewById(R.id.legs1);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                selectLegs(R.drawable.legs1);
            }
        });

        ImageButton imageButton1 = (ImageButton) findViewById(R.id.legs2);
        imageButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                selectLegs(R.drawable.legs2);
            }
        });
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    /**
     * Receiving activity result method will be called after closing the camera
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // successfully captured the image
                // display it in image view
                previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(), "User cancelled image capture", Toast.LENGTH_SHORT).show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(), "Sorry! Failed to capture image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void previewCapturedImage() {
        try {
            // hide video preview
            ImageView mainPhotoImgView = (ImageView) findViewById(R.id.img_photo);
            mainPhotoImgView.setVisibility(View.VISIBLE);
            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();
            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 2;
            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(), options);
            mainPhotoImgView.setImageBitmap(bitmap);
            mainPhotoImgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public Uri getOutputMediaFileUri(boolean isPublic) {
        return Uri.fromFile(getOutputMediaFile(isPublic));
    }

    public void selectLegs(int position) {

        FrameLayout mainFrame = (FrameLayout) findViewById(R.id.content_frame);
        if (legsImageView != null) {
            mainFrame.removeView(legsImageView);
        }

        legsImageView = new ImageView(this);
        legsImageView.setImageResource(position);
        legsImageView.setScaleType(ImageView.ScaleType.MATRIX);
        mainFrame.addView(legsImageView);
        legsImageView.bringToFront();
        legsImageView.setOnTouchListener(this);

        hashtag.setVisibility(View.VISIBLE);
        hashtag.bringToFront();

        saveItemActionBar.setVisible(true);

    }

    public String saveImage() {
        File rootPath = new File(Environment.getExternalStorageDirectory(), IMAGE_DIRECTORY_NAME);

        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }

        File dataFile = new File(rootPath, generateFileName());
        Bitmap bm = null;
        FrameLayout savedImage = null;
        savedImage = (FrameLayout) findViewById(R.id.content_frame);
        savedImage.setDrawingCacheEnabled(true);
        savedImage.buildDrawingCache();
        bm = savedImage.getDrawingCache();

        try {
            FileOutputStream out = new FileOutputStream(dataFile, false);
            bm.compress(Bitmap.CompressFormat.PNG, 95, out);
            finalImage = bm;
            imageUri = Uri.fromFile(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageUri.toString();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;

        // Handle touch events here...
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                Log.d(TAG, "mode=DRAG");
                mode = DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                Log.d(TAG, "mode=NONE");
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    // ...
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY()
                            - start.y);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    Log.d(TAG, "newDist=" + newDist);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }

        view.setImageMatrix(matrix);
        return true;
    }


    /**
     * Determine the space between the first two fingers
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }


    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        //mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
       /* if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
*/
        if (item.getItemId() == R.id.action_camera) {
            captureImage();
        } else if (item.getItemId() == R.id.action_save) {
            AsyncTaskRunner runner = new AsyncTaskRunner();
            runner.execute();

        } else if (item.getItemId() == R.id.action_settings) {
            openSettingsActivity();
        }

        return false;
    }

    private void openSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void openShareActivity() {
        Intent intent = new Intent(this, SharePhoto.class);
        intent.putExtra("IMAGE_URI", imageUri);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        topBarMenu = menu;
        getMenuInflater().inflate(R.menu.legs, menu);
        saveItemActionBar = menu.findItem(R.id.action_save);
        cameraItemActionBar = menu.findItem(R.id.action_camera);
        settingsItemActionBar = menu.findItem(R.id.action_settings);
        //initializeShareAction(menu.findItem(R.id.action_share));
        return true;
    }


    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        private String resp;

        @Override
        protected String doInBackground(String... params) {
            publishProgress("saving image..."); // Calls onProgressUpdate()
            return saveImage();
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            File originalFile = new File(fileUri.getPath());
            if (originalFile.exists()) {
                originalFile.delete();
            }
            openShareActivity();
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            Toast.makeText(LegsActivity.this, "Saving your photo. Please wait...", Toast.LENGTH_LONG).show();
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onProgressUpdate(Progress[])
         */
        @Override
        protected void onProgressUpdate(String... text) {
            Log.d(TAG, "---------->" + text);
        }
    }
}

