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

    private static final String AES_KEY = "767538de076ac0f9e4ecb1bb03e4549c";

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
        String url = "https://cflstc.r18.com/r18/st1:huslnahMnSUtRS1l4fTGykD4xolYF58aC8N3FI6RYMVCJVdW-QxP0QWi3h+BD3oU2FRvMQcCfcwGkHeJiVKlgQ==/-/cdn/chunklist_b300000.m3u8?ld=1hKJw6gtIFzBS42MXtTirpr7yBAKci49TNqSa2Y0ulovybIsnk5Zkt5B6n3QGnAm5rWJlLQs65q%2B1aTXUYs%2FPO1laesCAHhJ%2BE3P3tI8yK1O0%2BETQQ%2BoCPBL0X%2BApv81KGPsoXBTfQ0NEzJDELDmOHCIDPm1F1%2B86ldNz9doXbpQ6C9gYumR3%2F2dZ2ecD%2BOpyn2kzGqqkYq1KPmRV%2FC7Sytm%2FxQ357DszyI8VJYT%2FNsz5ZYn1Te8zfWjH1J6W4VFS0hMUNkU2zAD145s9c91feKaKgDOqMoTq%2BGv3vIaM7JAeXjG%2FrkOG0BXObfIWzOi";
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
