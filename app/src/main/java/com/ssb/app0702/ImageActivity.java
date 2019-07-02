package com.ssb.app0702;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageActivity extends AppCompatActivity {

    ImageView imageView;

    //bitmap 데이터를 받아서 imageView에 출력하는 핸들러
    Handler handler = new Handler(){
        public void  handleMessage(Message msg){
            Bitmap image = (Bitmap)msg.obj;
            imageView.setImageBitmap(image);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        imageView = (ImageView)findViewById(R.id.imageview);
        Button imageDisplay =(Button)findViewById(R.id.iamgedisplay);
        Button imageDownload =(Button)findViewById(R.id.imagedownload);

        //버튼을 눌렀을 때 이미지를 바로 출력하기
        imageDisplay.setOnClickListener((view)->{
            Thread th = new Thread(){
                public void run(){
                    try{
                        //이미지 파일의 URL 만들기
                        URL url = new URL( "http://www.onlifezone.com/files/attach/images/962811/376/321/005/2.jpg");
                        //스트림 만들기
                        InputStream is  = url.openStream();
                        //Bitmap 만들기
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        //만든 Bitmap을 Message에 담아서 header에게 전송
                        Message message = new Message();
                        message.obj = bitmap;
                        handler.sendMessage(message);
                    }catch (Exception e){
                        Log.e("출력 실패:",e.getMessage());
                    }
                }
            };
            th.start();
        });

        //이미지가 앱 안에 존재하면 앱 안의 이미지를 출력하고
        //이미지가 없으면 앱 안의 파일로 저장하고 출력
        imageDownload.setOnClickListener((view)->{
            Log.e("버튼클릭","여기까지 된다.");
            //이미지 파일의 경로
            String addr ="http://www.onlifezone.com/files/attach/images/962811/376/321/005/2.jpg";
            //파일 이름 만들기 마지막 / 다음의 문자열
            int idx = addr.lastIndexOf("/");
            String imageName = addr.substring(idx+1);
            //위의 파일이 앱에 존재하는 지 확인
            // 앱 내의 파일 경로 만드릭
            String path = Environment.getDataDirectory().getAbsolutePath();
            path += "/data/com.ssb.app0702/files/" + imageName;

            Log.e("파일경로",path);
            //파일의 경로를 가지고 File 객체 생성- 파일의 존재 여부 확인을 위해서
            File file = new File(path);
            Log.e("파일생성",file.toString());
            if(file.exists()) {
                Toast.makeText(ImageActivity.this, "파일이 존재", Toast.LENGTH_LONG).show();

                //존재하는 파일을 가지고 Bitmap을 만들엇 Handler에게 전송
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                Message msg = new Message();
                msg.obj = bitmap;
                handler.sendMessage(msg);
            }else{
                Toast.makeText(ImageActivity.this,"파일이 없음",Toast.LENGTH_LONG).show();
                //스레드를 이용해서 다운로드 받은 후 파일을 만들고 출력
                Thread th = new Thread(){
                    public void run() {
                        try {
                            String addr ="http://www.onlifezone.com/files/attach/images/962811/376/321/005/2.jpg";
                            int idx = addr.lastIndexOf("/");
                            String imageName = addr.substring(idx+1);
                            String path = Environment.getDataDirectory().getAbsolutePath();
                            path += "/data/com.ssb.app0702/files/" + imageName;
                            //이미지 경로 와 연결
                            URL url = new URL(addr);
                            HttpURLConnection con = (HttpURLConnection)url.openConnection();
                            //다운로드 받는 크기 설정
                            int len = con.getContentLength();
                            //저장할 바이트 배열 만들기
                            byte [] laster = new byte[len];
                            //이미지를 읽을 스트림과 파일에 기록할 스트림 생성
                            InputStream is  = con.getInputStream();
                            FileOutputStream fos = openFileOutput(imageName,0);
                            //is 로 읽은 내용을 fos에 기록
                            while(true){
                                int read = is.read(laster);
                                if(read<0)
                                break;
                                fos.write(laster,0,read);
                            }
                            is.close();
                            fos.close();
                            con.disconnect();

                            Message msg = new Message();
                            msg.obj = BitmapFactory.decodeFile(path);
                            handler.sendMessage(msg);

                        } catch (Exception e) {
                            Log.e("다운로드 예외",e.getMessage());

                        }
                    }
                };
                th.start();
            }
        });


    }
}
