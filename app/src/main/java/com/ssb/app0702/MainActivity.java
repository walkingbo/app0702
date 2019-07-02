package com.ssb.app0702;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    EditText url;
    Button receive;
    TextView result;

    //화면 출력을 위한 Handler
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //전송받은 메시지를 String으로 변환해서 TextView에 출력
            String html = (String) msg.obj;
            result.setText(html);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        result = (TextView) findViewById(R.id.result);
        receive = (Button) findViewById(R.id.receive);
        url = (EditText) findViewById(R.id.url);

        receive.setOnClickListener((view) -> {
            //키보드를 화면에서 제거하기
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            //키보드를 소유하고 있는 객체의 포커스를 제거
            imm.hideSoftInputFromWindow(url.getWindowToken(),0);

            //데이터를 다운로드 받아서 핸들러에게 전송할 스레드 생성해서 시작
            Thread th = new Thread() {
                @Override
                public void run() {
                    try {
                        //URL
                        URL addr = new URL(url.getText().toString().trim());
                        //연결
                        HttpURLConnection con = (HttpURLConnection)addr.openConnection();
                        //옵션 설정
                        con.setConnectTimeout(30000);
                        con.setUseCaches(false);
                        con.setDoInput(true);
                        con.setDoOutput(true);

                        //데이터를 가져오기 위한 스트림 생성
                        if(con.getResponseCode()==200){
                            //데이터를 문자열로 ㅇ릭기 위한 스트림을 생성
                            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                            //문자열 읽기
                            StringBuilder sb = new StringBuilder();
                            //한 줄 읽어서 msg에 추가하고 읽은 것이 없으면 중단
                            while (true){
                                String line = br.readLine();
                                //Log.e("line:",line);
                                if(line==null)
                                    break;
                                sb.append(line);
                            }
                            //데이터를 가져오는 것은 스레드가 담당하고
                            //출력은 핸들러가 담당하도록 작성
                            Message msg = new Message();
                            msg.obj = sb.toString();
                            handler.sendMessage(msg);
                            br.close();
                            con.disconnect();
                        }
                    } catch (Exception e) {
                        Log.e("다운로드예외",e.getMessage());
                        Log.e("추적:",Log.getStackTraceString(e));
                    }
                }
            };
            th.start();
        });

    }
}
