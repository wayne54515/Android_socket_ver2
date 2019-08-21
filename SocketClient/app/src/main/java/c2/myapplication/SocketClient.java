package c2.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class SocketClient extends Activity {

    public EditText inputText;
    public TextView responseMsg, serverState;
    public String IP;
    public int Port;
    public String tmp;
    public Socket socket_client;
    private Thread thread;
    private BufferedReader br;
    private BufferedWriter bw;
    public Handler handler = new Handler();
    public Button send,disconnect;
    private Runnable runnable;
    private Handler handler2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.socket_client);
        Bundle bundle = this.getIntent().getExtras();
        IP = bundle.getString("IP");
        Port = bundle.getInt("Port");
        inputText = findViewById(R.id.inputText);
        responseMsg = findViewById(R.id.responseMsg);
        send = findViewById(R.id.send);
        disconnect = findViewById(R.id.disconnect);
        serverState = findViewById(R.id.serverState);
        socket_client = null;
        StrictMode.ThreadPolicy tp = StrictMode.ThreadPolicy.LAX;
        StrictMode.setThreadPolicy(tp);
        thread = new Thread(readData);
        thread.start();
        handler2 = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                handler2.postDelayed(this, 1500);
                Log.e("text","test");
                try{
                    bw = new BufferedWriter( new OutputStreamWriter(socket_client.getOutputStream()));
                    // 寫入訊息
                    bw.write("ServerState\n");
                    // 立即發送
                    bw.flush();
                    SocketClient.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            serverState.setText("Server Alive");
                        }
                    });
                }
                catch (Exception e){
                    SocketClient.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            serverState.setText("Server Not Alive");
                        }
                    });
                    try {
                        Thread.sleep(1000); //1000為1秒
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                    try{
                        socket_client.close();
                    }
                    catch (Exception e2){}
                    stopCheck();
                    finish();
                }
            }
        };
        handler2.postDelayed(runnable, 2500);

    }
    public void stopCheck(){
        handler2.removeCallbacks(runnable);
    }

    public void Disconnect(View v){
        stopCheck();
        try{
            bw = new BufferedWriter( new OutputStreamWriter(socket_client.getOutputStream()));
            // 寫入訊息
            bw.write("ClientDisconnect\n");
            // 立即發送
            bw.flush();

        }
        catch (Exception e){}
        if(socket_client != null)
            socket_client = null;

        finish();
    }

    private View.OnClickListener btnlistener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            if(socket_client.isConnected()){
                Log.e("text","write");
                try {
                    // 取得網路輸出串流
                    bw = new BufferedWriter( new OutputStreamWriter(socket_client.getOutputStream()));

                    // 寫入訊息
                    bw.write(inputText.getText()+"\n");

                    // 立即發送
                    bw.flush();
                } catch (IOException e) {
                    Log.e("text", e.toString());
                    responseMsg.append("Send Fail\n");
                    try {
                        Thread.sleep(2000); //1000為1秒
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                    if(socket_client != null)
                        try{
                            socket_client.close();
                        }catch (Exception e2){
                            Log.e("text", e2.toString());
                        }
                    finish();
                }
                // 將文字方塊清空
                inputText.setText("");
            }
        }

    };

    private Runnable readData = new Runnable() {
        public void run() {
            send.setOnClickListener(btnlistener);
            // server端的IP
            InetAddress serverIp;

            try {
                serverIp = InetAddress.getByName(IP);
                socket_client = new Socket();
                SocketAddress Address = new InetSocketAddress(IP, Port);
                socket_client.connect(Address, 1500);
                SocketClient.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        serverState.setText("Server Alive");
                    }
                });

                Log.e("text","connect");
                // 取得網路輸入串流
                br = new BufferedReader(new InputStreamReader(
                        socket_client.getInputStream()));


                // 當連線後
                while (socket_client.isConnected()) {

                    // 取得網路訊息
                    tmp = br.readLine();

                    // 如果不是空訊息則
                    if(tmp!=null)
                        // 顯示新的訊息
                        if(tmp.equals("ServerClose")){
                            break;
                        }
                        else if(!tmp.equals("ClientState"))
                            handler.post(updateText);

                }

            } catch (Exception e) {
                Log.e("text", e.toString());
                SocketClient.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        serverState.setText("Server Not Alive");
                        responseMsg.append("Server Disconnect\n");
                    }
                });
            }finally {
                try {
                    Thread.sleep(2000); //1000為1秒
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
                if(socket_client != null)
                    try{
                        socket_client.close();
                    }catch (Exception e2){
                        Log.e("text", e2.toString());
                    }
                finish();
            }
        }
    };

    private Runnable updateText = new Runnable() {
        public void run() {
            // 加入新訊息並換行
            responseMsg.append(tmp + "\n");
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            bw = new BufferedWriter( new OutputStreamWriter(socket_client.getOutputStream()));
            // 寫入訊息
            bw.write("ClientDisconnect\n");
            // 立即發送
            bw.flush();
            bw.close();
            br.close();
            socket_client.close();
        }
        catch (Exception e){}
    }

}
