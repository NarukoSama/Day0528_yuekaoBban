package com.bwie.day0528_yuekaobban;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bwie.day0528_yuekaobban.XlistView.XListView;
import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements XListView.IXListViewListener {

    private XListView xlv;

    List<AppB.AppBean> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xlv = (XListView) findViewById(R.id.xlv);
        //http://mapp.qzone.qq.com/cgi-bin/mapp/mapp_subcatelist_qq?yyb_cateid=-10&categoryName=%E8%85%BE%E8%AE%AF%E8%BD%AF%E4%BB%B6&pageNo=1&pageSize=20&type=app&platform=touch&network_type=unknown&resolution=412x732

        xlv.setPullRefreshEnable(true);

        xlv.setPullLoadEnable(true);

        xlv.setXListViewListener(this);

        getData();

        xlv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                AppB.AppBean appBean = list.get(position-1);

                Toast.makeText(MainActivity.this,position+"",Toast.LENGTH_SHORT).show();

                final String url = appBean.getUrl();

                /**
                 * 6.长按频道中的条目弹出AlertDialog(如图2中的选择网络)(10分)
                 7.选择wifi就直接弹出AlertDialog(如图2中的版本更新)(10分)
                 8.选择手机流量提醒用户跳转到设置wifi页面(10分)
                 */

                //http://imtt.dd.qq.com/16891/E4E087B63E27B87175F4B9BC7CFC4997.apk?fsname=com.tencent.qlauncher_6.0.2_64170111.apk&csr=97c2
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("网络选择");

                String[] arr = {"WIFI","手机流量"};

                builder.setSingleChoiceItems(arr, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (which == 0) {//选择wifi

                            dialog.dismiss();

                            AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);

                            builder2.setTitle("版本更新");

                            builder2.setMessage("现在检测到新版本，是否更新？");

                            builder2.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                            builder2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    downloadApk(url);

                                }
                            });

                            builder2.show();

                        }else{//选择手机流量提醒用户跳转到设置wifi页面(10分)

                            dialog.dismiss();

                            Toast.makeText(MainActivity.this,"跳转wifi界面",Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                            MainActivity.this.startActivity(intent);
                        }

//                        Toast.makeText(getContext(),which+"",Toast.LENGTH_SHORT).show();


                    }
                });

                builder.show();

            }
        });
    }

    //下载apk
    protected void downloadApk(String url) {
        //微信v-url : "http://imtt.dd.qq.com/16891/722607E77ADA0E2BB6BE2FD1411F4A86.apk?fsname=com.tencent.mm_6.5.8_1060.apk&csr=97c2"

        //apk下载地址，放置apk的路径
        //1.获取sd卡路径
        String path = Environment.getExternalStorageDirectory().getPath() + "/my.apk";
        //2.发送请求，获取apk，并放到指定路径
        RequestParams rp = new RequestParams(url);
        rp.setSaveFilePath(path);
        rp.setAutoRename(true);
        x.http().get(rp, new Callback.ProgressCallback<File>() {

            private ProgressBar pb;
            private AlertDialog.Builder builderPb;
            private View view;

            //下载成功
            @Override
            public void onSuccess(File result) {
                Toast.makeText(MainActivity.this, "下载完成,开始安装!", Toast.LENGTH_SHORT).show();
                installApk(result);
            }

            //下载出现问题
            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.v("tag", "失败");
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

                builderPb.show().dismiss();
                Log.v("tag", "结束");

            }

            @Override
            public void onWaiting() {

            }

            //刚刚开始下载
            @Override
            public void onStarted() {

                builderPb = new AlertDialog.Builder(MainActivity.this);

                builderPb.setTitle("下载进度……");

                view = View.inflate(MainActivity.this, R.layout.progressbar, null);

                pb = (ProgressBar) view.findViewById(R.id.pb);

                builderPb.setView(view);

                builderPb.show();

                Log.v("tag", "开始");
            }

            //下载过程中方法
            @Override
            public void onLoading(final long total, final long current, boolean isDownloading) {

                System.out.println("total = " + total +"   current = " + current);



                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        int progress = (int) (current  * 1000 / total);

                        pb.setProgress(progress);

                    }
                }).start();



            }
        });

    }

    public void installApk(File file){
        //系统应用界面,源码,安装apk入口
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        //文件作为数据源
        //设置安装的类型
        intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
        startActivity(intent);


    }
    private MyAdapter adapter;

    private void getData() {

        RequestParams params = new RequestParams("http://mapp.qzone.qq.com/cgi-bin/mapp/mapp_subcatelist_qq?yyb_cateid=-10&categoryName=%E8%85%BE%E8%AE%AF%E8%BD%AF%E4%BB%B6&pageNo=1&pageSize=20&type=app&platform=touch&network_type=unknown&resolution=412x732");

        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                System.out.println("result = " + result);

                Gson gson = new Gson();

                String res = result.substring(0, result.length() - 1);

                System.out.println("res = " + res);

//                String[] split = result.split(";");
//
//                System.out.println("split =------------ " + split[0]);

//                System.out.println("---------------------"+res);

                AppB appB = gson.fromJson(res, AppB.class);

                List<AppB.AppBean> app = appB.getApp();

                list.addAll(app);

                adapter = new MyAdapter();

                xlv.setAdapter(adapter);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                System.out.println("==================失败");
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });

    }




    //刷新
    @Override
    public void onRefresh() {

        list.clear();

        getData();

        xlv.stopRefresh();

        xlv.setRefreshTime("刚刚");

    }

    //加载更多
    @Override
    public void onLoadMore() {

    }

    class MyAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {

                convertView = View.inflate(MainActivity.this, android.R.layout.simple_list_item_1, null);

            }

            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);

            AppB.AppBean appBean = list.get(position);

            tv.setText(appBean.getName());

            return convertView;
        }
    }


}
