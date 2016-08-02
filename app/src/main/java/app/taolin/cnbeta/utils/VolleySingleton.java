package app.taolin.cnbeta.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import app.taolin.cnbeta.App;

public class VolleySingleton {

    private static VolleySingleton mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private DiskLruCache mDiskCache;

    private VolleySingleton(Context context) {
        initDiskCache(context);
        mRequestQueue = getRequestQueue();
        final int maxCacheSize = (int) Runtime.getRuntime().maxMemory() / (1024 * 8);
        mImageLoader = new ImageLoader(mRequestQueue, new LruImageCache(maxCacheSize));
    }

    public static synchronized VolleySingleton getInstance() {
        if (mInstance == null) {
            mInstance = new VolleySingleton(App.getInstance());
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(App.getInstance());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setShouldCache(false);
        getRequestQueue().getCache().clear();
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    private void initDiskCache(Context context) {
        try {
            File cacheDir = CommonUtil.getDiskCacheDir(context);
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            mDiskCache = DiskLruCache.open(cacheDir, CommonUtil.getAppVersion(context), 1, Constants.MAX_CACHE_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class LruImageCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {

        LruImageCache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount() / 1024;
        }

        @Override
        public Bitmap getBitmap(String url) {
            final String key = EncryptUtil.md5(url);
            Bitmap bitmap = get(key);
            if (bitmap == null) {
                try {
                    DiskLruCache.Snapshot snapShot = mDiskCache.get(key);
                    if (snapShot != null) {
                        InputStream is = snapShot.getInputStream(0);
                        bitmap = BitmapFactory.decodeStream(is);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return bitmap;
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            final String key = EncryptUtil.md5(url);
            put(key, bitmap);
            try {
                DiskLruCache.Editor editor = mDiskCache.edit(key);
                if (editor != null) {
                    OutputStream os = editor.newOutputStream(0);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                    os.close();
                    editor.commit();
                }
                mDiskCache.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
