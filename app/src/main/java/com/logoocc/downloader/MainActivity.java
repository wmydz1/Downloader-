package com.logoocc.downloader;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.logoocc.downloader.service.DownloadProgressListener;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private TextView tv_progress;
    private Button bt_dowload;


    private Handler handler = new Handler() {


        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case 1:
                    int size = msg.getData().getInt("size");
                    int length = msg.getData().getInt("length");

                    float result = (float) size / (float) length;
                    int p = (int) (result * 100);

                    tv_progress.setText(p + " % ");

                    break;
                case -1:

                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化控件
        initView();

        bt_dowload.setOnClickListener(btListener);
    }

    private View.OnClickListener btListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bt_download:

                    String path = "http://192.168.0.15:10000/movie/mm.7z";
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        File dir = Environment.getExternalStorageDirectory();//文件保存目录
                        download(path, dir, 1);

                    } else {
                        Toast.makeText(MainActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
                    }

                    break;
            }

        }
    };

    //对于UI控件的更新只能由主线程(UI线程)负责，如果在非UI线程更新UI控件，更新的结果不会反映在屏幕上，某些控件还会出错
    private void download(final String path, final File dir, final int softid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileDownloader loader = new FileDownloader(MainActivity.this, path, dir, 3, softid);
                    final FileDownloader loader2 = loader;
                    final int length = loader.getFileSize();//获取文件的长度

                    loader.download(new DownloadProgressListener() {
                        @Override
                        public void onDownloadSize(int size) {//可以实时得到文件下载的长度
                            Message msg = new Message();
                            msg.what = 1;
                            msg.arg1 = loader2.notifityid;
                            msg.getData().putInt("size", size);
                            msg.getData().putInt("length", length);
                            handler.sendMessage(msg);
                        }
                    });
                } catch (Exception e) {
                    Message msg = new Message();
                    msg.what = -1;
                    msg.getData().putString("error", "下载失败");
                    handler.sendMessage(msg);
                }
            }
        }).start();

    }


    private void initView() {

        tv_progress = (TextView) findViewById(R.id.tv_progress);
        bt_dowload = (Button) findViewById(R.id.bt_download);
    }


}
