package wei.yuan.hlsplay.hls;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Assertions;

import java.io.IOException;

import wei.yuan.hlsplay.util.ParseSystemUtil;

/**
 * 自定义的数据工厂类
 * Created by yugimaster on 2020/08/03.
 */

public class CustomHlsDataSource implements DataSource {

    private static final String TAG = "CustomHlsDataSource";

    private static final String SCHEME_ASSET = "asset";
    private static final String SCHEME_CONTENT = "content";
    private static final String SCHEME_RTMP = "rtmp";
    private static final String SCHEME_HTTP = "http";
    private static final String SCHEME_HTTPS = "https";

    private final Context context;
    private final TransferListener listener;

    private final DataSource baseDataSource;
    private final String aesKey;

    // Lazily initialized.
    private DataSource fileDataSource;
    private DataSource assetDataSource;
    private DataSource contentDataSource;
    private DataSource rtmpDataSource;

    private DataSource dataSource;

    /**
     * Constructs a new instance, optionally configured to follow cross-protocol redirects.
     *
     * @param context                     A context.
     * @param listener                    An optional listener.
     * @param userAgent                   The User-Agent string that should be used when requesting remote data.
     * @param allowCrossProtocolRedirects Whether cross-protocol redirects (i.e. redirects from HTTP
     *                                    to HTTPS and vice versa) are enabled when fetching remote data.
     */
    public CustomHlsDataSource(Context context, TransferListener listener,
                               String userAgent, boolean allowCrossProtocolRedirects, String aesKey) {
        this(context, listener, userAgent, DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS, allowCrossProtocolRedirects, aesKey);
    }

    /**
     * Constructs a new instance, optionally configured to follow cross-protocol redirects.
     *
     * @param context                     A context.
     * @param listener                    An optional listener.
     * @param userAgent                   The User-Agent string that should be used when requesting remote data.
     * @param connectTimeoutMillis        The connection timeout that should be used when requesting remote
     *                                    data, in milliseconds. A timeout of zero is interpreted as an infinite timeout.
     * @param readTimeoutMillis           The read timeout that should be used when requesting remote data,
     *                                    in milliseconds. A timeout of zero is interpreted as an infinite timeout.
     * @param allowCrossProtocolRedirects Whether cross-protocol redirects (i.e. redirects from HTTP
     *                                    to HTTPS and vice versa) are enabled when fetching remote data.
     */
    public CustomHlsDataSource(Context context, TransferListener listener,
                               String userAgent, int connectTimeoutMillis, int readTimeoutMillis,
                               boolean allowCrossProtocolRedirects, String aesKey) {
        this(context, listener,
                new DefaultHttpDataSource(userAgent, null, connectTimeoutMillis,
                        readTimeoutMillis, allowCrossProtocolRedirects, null),
                aesKey);
    }

    /**
     * Constructs a new instance that delegates to a provided {@link DataSource} for URI schemes other
     * than file, asset and content.
     *
     * @param context        A context.
     * @param listener       An optional listener.
     * @param baseDataSource A {@link DataSource} to use for URI schemes other than file, asset and
     *                       content. This {@link DataSource} should normally support at least http(s).
     */
    public CustomHlsDataSource(Context context, TransferListener listener, DataSource baseDataSource,
                               String aesKey) {
        this.context = context;
        this.listener = listener;
        this.baseDataSource = Assertions.checkNotNull(baseDataSource);
        this.aesKey = aesKey;
    }

    @Override
    public void addTransferListener(TransferListener transferListener) {
        this.baseDataSource.addTransferListener(transferListener);
    }

    @Override
    public long open(DataSpec dataSpec) throws IOException {
        Assertions.checkState(dataSource == null);
        // Choose the correct source for the scheme.
        String scheme = dataSpec.uri.getScheme();
        String path = dataSpec.uri.getPath();
        Log.d(TAG, "Decrypt: " + scheme + ", path: " + path);
        if (path != null && !path.isEmpty() && path.contains("drm")) {
            Log.d(TAG, "drm url");
            try {
                dataSource = baseDataSource;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (SCHEME_HTTPS.equals(scheme)) {
            // 判断是否为ts片段
            if (path != null && !path.isEmpty() && path.endsWith(".ts") && !aesKey.isEmpty()) {
                byte[] aesKeyBytes = null;
                String uri = dataSpec.uri.toString();
                Log.d(TAG, "Decrypt: ts uri: " + uri);
                if (aesKey.length() == 32) {
                    Log.d(TAG, "Decrypt: key hex -> " + aesKey);
                    aesKeyBytes = ParseSystemUtil.parseHexStr2Byte(aesKey);
                } else if (aesKey.length() == 16) {
                    aesKeyBytes = aesKey.getBytes();
                }
                String index = getTsIndex(path);
                String hexIndex = getTsIndexHex(index);
                Log.d(TAG, "Decrypt: ts index -> " + index);
                Log.d(TAG, "Decrypt: ts index hex -> " + hexIndex);
                byte[] aesIvBytes = ParseSystemUtil.parseHexStr2Byte(hexIndex);
                Aes128DataSource aes128DataSource = new Aes128DataSource(baseDataSource,
                        aesKeyBytes, aesIvBytes);
//                long l = aes128DataSource.open(dataSpec);
                dataSource = aes128DataSource;
                long l = dataSource.open(dataSpec);
                Log.d(TAG, "aes data source return value: " + l);
                return l;
            } else {
                dataSource = baseDataSource;
            }
        } else {
            dataSource = baseDataSource;
        }

        // Open the source and return.
        long lon = dataSource.open(dataSpec);
        Log.d(TAG, "return value: " + lon);
        return lon;
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        return dataSource.read(buffer, offset, readLength);
    }

    @Nullable
    @Override
    public Uri getUri() {
        return dataSource == null ? null : dataSource.getUri();
    }

    @Override
    public void close() throws IOException {
        if (dataSource != null) {
            try {
                dataSource.close();
            } finally {
                dataSource = null;
            }
        }
    }

    private String getTsIndexHex(String index) {
        int i = Integer.valueOf(index);
        return String.format("%032x", i);
    }

    private String getTsIndex(String tsPath) {
        try {
            String[] strs = tsPath.split("\\.");
            String name = strs[0];
            String[] s = name.split("_");
            String index = s[2];
            return index;
        } catch (Exception e) {
            Log.e(TAG, "getTsIndex()" + e.toString());
            return "";
        }
    }
}
