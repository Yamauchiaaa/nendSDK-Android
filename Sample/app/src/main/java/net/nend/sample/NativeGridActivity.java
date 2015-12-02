package net.nend.sample;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import net.nend.android.NendAdNative;
import net.nend.android.NendAdNativeClient;
import net.nend.android.NendAdNativeListListener;
import net.nend.android.NendAdNativeViewBinder;
import net.nend.android.NendAdNativeViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NativeGridActivity extends AppCompatActivity {

    private final int NORMAL = 0;
    private final int AD = 1;

    private Handler mHandler = new Handler();
    private ArrayList<NendAdNative> mLoadedAd = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.native_grid);

        ArrayList<String> list = new ArrayList<>();
        for (int i = 1; i < 100; i++) {
            list.add("item"+i);
        }

        GridView gridView = (GridView) findViewById(R.id.grid);
        NativeGridAdapter adapter = new NativeGridAdapter(this, 0, list);
        gridView.setAdapter(adapter);
    }

    class NativeGridAdapter extends ArrayAdapter<String> {

        private NendAdNativeClient mClient;
        private NendAdNativeViewBinder mBinder;

        public NativeGridAdapter(Context context, int resource, List<String> list) {
            super(context, resource, list);

            mBinder = new NendAdNativeViewBinder.Builder()
                    .adImageId(R.id.ad_image)
                    .titleId(R.id.ad_title)
                    .prId(R.id.ad_pr, NendAdNative.AdvertisingExplicitly.SPONSORED)
                    .build();

            mClient = new NendAdNativeClient(context, 485517, "186927de979c5e542ee1ef111cc69c52e37a58f9");
            mClient.setListener(new NendAdNativeListListener() {
                @Override
                public void onReceiveAd(NendAdNative nendAdNative, int i, final View view, NendAdNativeClient.NendError nendError) {
                    if (nendError == null) {
                        Log.i(getClass().getSimpleName(), "広告取得成功");
                        mLoadedAd.add(nendAdNative);
                    } else {
                        Log.i(getClass().getSimpleName(), "広告取得失敗 " + nendError.getMessage());

                        // 広告リクエスト制限を越えた場合
                        if(nendError == NendAdNativeClient.NendError.EXCESSIVE_AD_CALLS){
                            // すでに取得済みの広告をランダムで表示
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    NendAdNative ad = mLoadedAd.get(new Random().nextInt(mLoadedAd.size()));
                                    ad.intoView(view, mBinder);
                                }
                            });
                        }
                    }
                }

                @Override
                public void onClick(NendAdNative nendAdNative) {
                    Log.i(getClass().getSimpleName(), "クリック");
                }

                @Override
                public void onDisplayAd(Boolean result, View view) {
                    Log.i(getClass().getSimpleName(), "広告表示 = " + result);
                }
            });
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return (position != 0 && position % 5 == 0)? AD : NORMAL;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            final NendAdNativeViewHolder adHolder;
            switch (getItemViewType(position)){
                case NORMAL:
                    if (convertView == null) {
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.native_grid_row, parent, false);
                        holder = new ViewHolder();
                        holder.textView = (TextView) convertView.findViewById(R.id.title);
                        holder.imageView = (ImageView) convertView.findViewById(R.id.thumbnail);
                        convertView.setTag(holder);
                    }else{
                        holder = (ViewHolder) convertView.getTag();
                    }
                    holder.textView.setText(getItem(position));
                    holder.imageView.setBackgroundColor(Color.LTGRAY);
                    break;
                case AD:
                    if (convertView == null) {
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.native_ad_grid_row, parent, false);
                        adHolder = mBinder.createViewHolder(convertView);
                        convertView.setTag(adHolder);
                    }else{
                        adHolder = (NendAdNativeViewHolder) convertView.getTag();
                    }
                    mClient.loadAd(adHolder, position);
                    break;
            }
            return convertView;
        }

        class ViewHolder {
            TextView textView;
            ImageView imageView;
        }
    }
}