package app.taolin.cnbeta.utils;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import app.taolin.cnbeta.App;

public class VolleySingleton {

    private static VolleySingleton mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    private VolleySingleton() {
        mRequestQueue = getRequestQueue();
        final int maxCacheSize = (int) Runtime.getRuntime().maxMemory() / (1024 * 8);
        mImageLoader = new ImageLoader(mRequestQueue, new LruImageCache(maxCacheSize));
    }

    public static synchronized VolleySingleton getInstance() {
        if (mInstance == null) {
            mInstance = new VolleySingleton();
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
            return get(EncryptUtil.md5(url));
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            put(EncryptUtil.md5(url), bitmap);
        }
    }
}
