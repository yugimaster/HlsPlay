package wei.yuan.hlsplay;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
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

    private static final String AES_KEY = "b3bb73a922b8ffd01413d875b21419ce";

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
        CustomHlsDataSourceFactory dataSourceFactory = new CustomHlsDataSourceFactory(this, userAgent, AES_KEY);
//        String url = "https://gccncc.v.wscdns.com/gc/yxlcyt_1/index.m3u8?contentid=2820180516001";
        String url = "https://cflstc.r18.com/r18/st1:BFSXnvFPKM0CFupxbBN7I054lI-If0kEvwTH6uYoPwJQHxsY1fTIr-4Ms55Hz9iL0Dqj8EQ6+-DLrKXlLg438Q==/-/cdn/chunklist_b300000.m3u8?ld=oYpNZTaH3gOo5rM2xvA4K44IbahfSWc5mnDcBl6%2F7Zc1xNNZBK%2FD3SEOAbe%2FZ133gQS47ozHBS%2F9s1gSvmitxZ9LDWCAUGhiePi%2BZD1UswL4atLlbrO2ZS0kk%2FaTd8EjzDQl8AMJRuVJWkupyaKsGbQkdyz%2Bj475RrSVHQWA1NRn279%2BqVK%2F3mn646naDq7ran0QuXzJ0otUx6SxKHsI54YZxF%2BbsDe%2BxU82GOOyT48Q7znMAuwbQD%2F63C8Jp2M3Wwae5QyAe9mcnnoYP92dSRt%2BxYpPI316sj7u440TNx0%3D";
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
