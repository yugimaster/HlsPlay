package wei.yuan.hlsplay;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity";

    private EditText mEt;
    private Button mBtnPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v(TAG, "onCreate");

        mEt = (EditText) findViewById(R.id.et_url);
        mBtnPlay = (Button) findViewById(R.id.btn);
        mBtnPlay.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn:
                Log.v(TAG, "play hls");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setClassName(getApplicationContext(), HlsActivity.class.getName());
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
