package wei.yuan.hlsplay.hls;

import android.content.Context;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;

/**
 * 自定义资源工厂
 * Created by yugimaster on 2020/08/03.
 */

public class CustomHlsDataSourceFactory implements DataSource.Factory {

    private final Context context;
    private final TransferListener listener;
    private final DataSource.Factory baseDataSourceFactory;

    /**
     * @param context A context.
     * @param userAgent The User-Agent string that should be used.
     */
    public CustomHlsDataSourceFactory(Context context, String userAgent) {
        this(context, userAgent, null);
    }

    /**
     * @param context A context.
     * @param userAgent The User-Agent string that should be used.
     * @param listener An optional listener.
     */
    public CustomHlsDataSourceFactory(Context context, String userAgent,
                                      TransferListener listener) {
        this(context, listener, new DefaultHttpDataSourceFactory(userAgent, listener));
    }

    /**
     * @param context A context.
     * @param listener An optional listener.
     * @param baseDataSourceFactory A {@link DataSource.Factory} to be used to create a base {@link DataSource}
     *     for {@link DefaultDataSource}.
     * @see DefaultDataSource#DefaultDataSource(Context, TransferListener, DataSource)
     */
    public CustomHlsDataSourceFactory(Context context, TransferListener listener,
                                      DataSource.Factory baseDataSourceFactory) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        this.baseDataSourceFactory = baseDataSourceFactory;
    }

    @Override
    public CustomHlsDataSource createDataSource() {
        return new CustomHlsDataSource(context, listener, baseDataSourceFactory.createDataSource());
    }
}
