package msgcopy.com.zxingdemo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private TextView textView;
    private ImageView imageView;
    private Bitmap scanBitmap;

    private static final int REQUEST_GALLERY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        imageView = (ImageView) findViewById(R.id.imageView);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                final String seannedInfo = result.getContents();
                if (!CommonUtil.isBlank(seannedInfo)) {
                    if (seannedInfo.startsWith("http://") || seannedInfo.startsWith("https://")) {
                        Intent intent = new Intent(MainActivity.this, WebActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("url", seannedInfo);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                    textView.setText("识别二维码(相机)\n"+seannedInfo);
                    Log.i(TAG, "seannedInfo : " + seannedInfo);
                }
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_GALLERY:
                    if (null != data && null != data.getData()) {
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(data.getData(), filePathColumn, null, null, null);
                        if (cursor.moveToFirst()) {
                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            String filePath = cursor.getString(columnIndex);
                            cursor.close();
                            Result r = CommonUtil.scanningImage(filePath,scanBitmap);
                            if (null != r) {
                                Log.i(TAG, "filePath : " + filePath);
                                Log.i(TAG, "Result : " + r.toString());

                                Intent intent = new Intent(MainActivity.this, WebActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("url", r.toString());
                                intent.putExtras(bundle);
                                startActivity(intent);
                                textView.setText("识别二维码(图片)\n" + r.toString());
                            }else {
                                Toast.makeText(MainActivity.this,"不是二维码图片",Toast.LENGTH_LONG).show();
                            }

                        }
                    }
                    break;
            }
        }

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_code:
                IntentIntegrator intentIntegrator = new IntentIntegrator(MainActivity.this);
                intentIntegrator.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                int width = getResources().getDisplayMetrics().widthPixels - (CommonUtil.dip2px(getApplicationContext(), 112));
                intentIntegrator.setScanningRectangle(width, width);
                intentIntegrator.setResultDisplayDuration(0);
                intentIntegrator.initiateScan();
                break;
            case R.id.bt_code_image:
                CommonUtil.startIntentToPickPhoto(MainActivity.this, REQUEST_GALLERY);
                break;
            case R.id.create_code:
                CommonUtil.encode("https://www.baidu.com", textView, imageView);
                break;
        }
    }

}
