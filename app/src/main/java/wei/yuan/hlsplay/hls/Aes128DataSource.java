/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wei.yuan.hlsplay.hls;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.DataSchemeDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSourceInputStream;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Assertions;

import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Exoplayer官方Demo的AesDataSource
 * A {@link DataSource} that decrypts data read from an upstream source, encrypted with AES-128 with
 * a 128-bit key and PKCS7 padding.
 * <p>
 * Note that this {@link DataSource} does not support being opened from arbitrary offsets. It is
 * designed specifically for reading whole files as defined in an HLS media playlist. For this
 * reason the implementation is private to the HLS package.
 */
/* package */ final class Aes128DataSource implements DataSource {

    private final DataSource upstream;
    private final byte[] encryptionKey;
    private final byte[] encryptionIv;

    private CipherInputStream cipherInputStream;

    private long mBytesRemaining = 0;

    private boolean isOpen = false;

    /**
     * @param upstream The upstream {@link DataSource}.
     * @param encryptionKey The encryption key.
     * @param encryptionIv The encryption initialization vector.
     */
    public Aes128DataSource(DataSource upstream, byte[] encryptionKey, byte[] encryptionIv) {
        this.upstream = upstream;
        this.encryptionKey = encryptionKey;
        this.encryptionIv = encryptionIv;
    }

    @Override
    public long open(DataSpec dataSpec) throws IOException {
        if (isOpen) {
            Log.d("CustomDataSource", "data source has opened");
            return mBytesRemaining;
        }

        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/CBC/NoPadding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }

        SecretKeySpec cipherKey = new SecretKeySpec(encryptionKey, "AES");
        IvParameterSpec cipherIV = new IvParameterSpec(encryptionIv);

        try {
            cipher.init(Cipher.DECRYPT_MODE, cipherKey, cipherIV);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }

//        long dataLength = upstream.open(dataSpec);
        DataSourceInputStream dataSourceInputStream = new DataSourceInputStream(upstream, dataSpec);
        cipherInputStream = new CipherInputStream(dataSourceInputStream, cipher);
        cipherInputStream.skip(dataSpec.position);
        dataSourceInputStream.open();
        computeBytesRemaining(dataSpec);

        isOpen = true;

//        return C.LENGTH_UNSET;
        return mBytesRemaining;
    }

    @Override
    public void close() throws IOException {
        Log.d("CustomDataSource", "data source close");
        if (cipherInputStream != null) {
            cipherInputStream = null;
            upstream.close();
        }
        if (isOpen)
            isOpen = false;
    }

//    @Override
//    public int read(byte[] buffer, int offset, int readLength) throws IOException {
//        //Assertions.checkState(cipherInputStream != null);
//        if (cipherInputStream == null) {
//            Log.d("CustomHlsDataSource", "cipher input stream is null");
//        } else {
//            Log.d("CustomHlsDataSource", "cipher input stream is not null");
//        }
//        Assertions.checkNotNull(cipherInputStream);
//        Log.d("CustomHlsDataSource", "readLength: " + readLength);
//        int bytesRead = cipherInputStream.read(buffer, offset, readLength);
//        Log.d("CustomHlsDataSource", "bytesRead: " + bytesRead);
//        if (bytesRead < 0) {
//            return C.RESULT_END_OF_INPUT;
//        }
//        mBytesRemaining = bytesRead;a
//        return bytesRead;
//    }


    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        if (cipherInputStream == null) {
            Log.d("CustomHlsDataSource", "cipher input stream is null");
        } else {
            Log.d("CustomHlsDataSource", "cipher input stream is not null");
        }
        Assertions.checkNotNull(cipherInputStream);
        Log.d("CustomHlsDataSource", "readLength: " + readLength);

        if (readLength == 0) {
            Log.d("CustomHlsDataSource", "readLength is 0");
            return 0;
        }
        else if (mBytesRemaining == 0) {
            Log.d("CustomHlsDataSource", "mBytesRemaining is 0");
            return C.RESULT_END_OF_INPUT;
        }
        int bytesToRead = getBytesToRead(readLength);
        Log.d("CustomHlsDataSource", "bytesToRead: " + bytesToRead);
        int bytesRead;
        bytesRead = cipherInputStream.read(buffer, offset, bytesToRead);

        if (bytesRead == -1) {
            Log.d("CustomHlsDataSource", "bytesRead is -1");
            return C.RESULT_END_OF_INPUT;
        }

        if (mBytesRemaining != C.LENGTH_UNSET) {
            mBytesRemaining -= bytesRead;
        }

        Log.d("CustomHlsDataSource", "mBytesRemaining: " + mBytesRemaining);
        Log.d("CustomHlsDataSource", "bytesRead: " + bytesRead);
        return bytesRead;
    }

    @Override
    public Uri getUri() {
        return upstream.getUri();
    }

    @Override
    public void addTransferListener(TransferListener transferListener) {
        upstream.addTransferListener(transferListener);
    }

    private void computeBytesRemaining(DataSpec dataSpec) throws IOException {
        long dataLength = upstream.open(dataSpec);
        Log.d("CustomHlsDataSource", "dataSpec length is: " + dataLength);
        if (dataLength != C.LENGTH_UNSET) {
            mBytesRemaining = dataLength;
        } else {
            mBytesRemaining = cipherInputStream.available();
            Log.d("CustomHlsDataSource", "cipher input stream available int: " + mBytesRemaining);
            if (mBytesRemaining == Integer.MAX_VALUE) {
                mBytesRemaining = C.LENGTH_UNSET;
            }
        }
    }

    private int getBytesToRead(int bytesToRead) {
        if (mBytesRemaining == C.LENGTH_UNSET) {
            return bytesToRead;
        }
        return (int) Math.min(mBytesRemaining, bytesToRead);
    }
}
