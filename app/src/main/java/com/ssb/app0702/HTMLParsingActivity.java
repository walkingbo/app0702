package com.ssb.app0702;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class HTMLParsingActivity extends AppCompatActivity {
    ListView listView;
    ArrayAdapter<String> adapter;
    ArrayList<String> list;
    String html;


    //ListView를 재출력하는 핸들러
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //어댑터가 리스트 뷰에 데이터의 변경이 발생했으니
            //데이터를 다시 출력하라고 메시지를 전송
            adapter.notifyDataSetChanged();

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_htmlparsing);

        //리스트 뷰 출력
        listView = (ListView) findViewById(R.id.htmllistview);
        list = new ArrayList<>();
        adapter = new ArrayAdapter<>(HTMLParsingActivity.this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        Button btn1 = (Button) findViewById(R.id.htmlparsing);

        btn1.setOnClickListener((view) -> {
            Thread th = new Thread() {
                public void run() {
                    String addr = "https://finance.naver.com";
                    try {
                        URL url = new URL(addr);
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        BufferedReader br = new BufferedReader(
                                new InputStreamReader(con.getInputStream(), "EUC-KR"));
                        StringBuilder sb = new StringBuilder();

                        while (true) {
                            String line = br.readLine();
                            if (line == null)
                                break;
                            sb.append(line + "\n");
                        }
                        br.close();
                        con.disconnect();
                        html = sb.toString();
                        Log.e("html", html);
                    } catch (Exception e) {
                        Log.e("다운로드예외",e.getMessage());
                    }
                    //html 파싱
                    try{
                        //html을 객체로 만들기
                        Document doc = Jsoup.parse(html);
                        //원하는 선택자의 데이터를 찾아오기
                        Elements elements = doc.select("span > a");
                        //선택된 데이터 순회
                        for(Element element : elements){
                            list.add(element.attr("href"));
                            //list.add(element.attr(element.text().trim()));
                        }
                        //핸들러에게 listview 출력을 다시하라고 메시지를 전송
                        handler.sendEmptyMessage(0);
                    }catch (Exception e){
                        Log.e("파싱예외",e.getMessage());
                    }

                }
            };
            th.start();
        });


    }
}
