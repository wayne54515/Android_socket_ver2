package c2.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

    public static Handler mHandler = new Handler();
    TextView response;    // 用來顯示文字訊息
    EditText IP, Port;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 從資源檔裡取得位址後強制轉型成文字方塊
        response = (TextView) findViewById(R.id.response);
        IP =(EditText) findViewById(R.id.ip);
        Port =(EditText) findViewById(R.id.port);

        // 從資源檔裡取得位址後強制轉型成按鈕
        Button connect=(Button) findViewById(R.id.connect);

        // 設定按鈕的事件
        connect.setOnClickListener(new Button.OnClickListener() {
            // 當按下按鈕的時候觸發以下的方法
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                if(TextUtils.isEmpty(IP.getText().toString()) | TextUtils.isEmpty(Port.getText().toString())) {
                    response.setText("IP or Port is empty\n");
                }
                else{
                    response.setText("");
                    bundle.putString("IP", IP.getText().toString());
                    bundle.putInt("Port", Integer.parseInt(Port.getText().toString()));

                    Intent intent = new Intent(MainActivity.this, SocketClient.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }

            }
        });

    }
}
