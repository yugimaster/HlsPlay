package wei.yuan.hlsplay;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import wei.yuan.hlsplay.hls.CustomHlsDataSource;
import wei.yuan.hlsplay.hls.CustomHlsDataSourceFactory;

public class HlsActivity extends Activity implements Player.EventListener {

    private static final String TAG = "HlsActivity";

    private SimpleExoPlayer mSimpleExoPlayer;
    private SimpleExoPlayerView mExoPlayerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hls);
        Log.v(TAG, "onCreate()");
        initPlayer();
    }

    private void initPlayer() {
        Log.v(TAG, "initPlayer()");
//        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        String userAgent = Util.getUserAgent(this, "HlsExoPlayer");
//        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this,
//                new DefaultHttpDataSourceFactory(userAgent, null));
        CustomHlsDataSourceFactory dataSourceFactory = new CustomHlsDataSourceFactory(this, userAgent);
//        String url = "https://gccncc.v.wscdns.com/gc/yxlcyt_1/index.m3u8?contentid=2820180516001";
        String url = "https://cflstc.r18.com/r18/st1:HznC5xIWCqJkHkzOHk4IAVtxHB0E8kRx-0RqxrNhrXVCeHBBZhwkcN9npeL-dasN+OlZnXt5Tv4bjLH7EIeeUw==/-/cdn/chunklist_b300000.m3u8?ld=DnSdmxpre6ru2tcg%2BKfQhBvCQ2T9PwFkaEnXOX%2FsokMwffTMh9ZIg%2B2KExWoGOq4s9dB100R67xAIqXkkw7XVKwXibeBmqrVpe5UDd2nky7Myt%2BU%2FiZL1nvwHh4NvHWet3gJNApEeYTBQXj1ymDDvyvxaEjCcdMfR8PcXUJScTkbMI5oT%2BNC%2BO8T6TlftXT2rG%2FrTIp01ix50y5mq3QpFHigficCo5PqB%2FA5mVUmJP%2FlmC%2BNiETgI%2FqE2RFbWsx%2B776y%2FaQ2BJ6Fr8gD6wEuSWA%2BTKwVz0v6m2kLr20U6PXMkFwYnTyx9MzzU8u0454W";
        Uri uri = Uri.parse(url);
        MediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);

        TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory();
        // 创建一个默认的 TrackSelector
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);
        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this);

        // 创建SimpleExoPlayer
        mSimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);
        // 创建SimpleExoPlayerView
        mExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.exoView);
        // SimpleExoPlayer设置播放器
        mExoPlayerView.setPlayer(mSimpleExoPlayer);
        mSimpleExoPlayer.addListener(this);
        mSimpleExoPlayer.setPlayWhenReady(true);
        mSimpleExoPlayer.prepare(mediaSource);
        Log.d(TAG, "player prepare...");
    }
}
