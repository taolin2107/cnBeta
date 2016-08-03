package app.taolin.cnbeta;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import app.taolin.cnbeta.utils.CommonUtil;
import app.taolin.cnbeta.utils.Constants;
import app.taolin.cnbeta.utils.DiskLruCache;

/**
 * @author taolin
 * @version v1.0
 * @date Jul 15, 2016.
 * @description Settings
 */

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{

    private DiskLruCache mDiskCache;
    private TextView mCacheSizeText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
        initCache();
        setContentView(R.layout.settings);
        initViews();
    }

    private void initViews() {
        mCacheSizeText = (TextView) findViewById(R.id.cache_size);
        refreshCacheSize();
    }

    private void refreshCacheSize() {
        long cacheSize = mDiskCache.size();
        if (cacheSize > 0) {
            mCacheSizeText.setText(formatFileSize(cacheSize));
            mCacheSizeText.setVisibility(View.VISIBLE);
        } else {
            mCacheSizeText.setVisibility(View.GONE);
        }
    }

    private String formatFileSize(long size) {
        final long B = 1;
        final long K = 1024 * B;
        final long M = 1024 * K;
        final long G = 1024 * M;
        if (size < K) {
            return size + " B";
        } else if (size < M) {
            return String.format(Locale.US, "%.02f K", size * 1f/ K);
        } else if (size < G) {
            return String.format(Locale.US, "%.02f M", size * 1f/ M);
        } else {
            return String.format(Locale.US, "%.02f G", size * 1f/ G);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.font_settings:
                new FontSettingsDialog().show(getFragmentManager(), "font_settings");
                break;

            case R.id.favor:
                startActivity(new Intent(this, FavorListActivity.class));
                break;

            case R.id.clean_cache:
                try {
                    mDiskCache.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                initCache();
                refreshCacheSize();
                Toast.makeText(this, R.string.clean_cache_success, Toast.LENGTH_SHORT).show();
                break;

            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
    }

    private void initCache() {
        try {
            File cacheDir = CommonUtil.getDiskCacheDir(this);
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            mDiskCache = DiskLruCache.open(cacheDir, CommonUtil.getAppVersion(this), 1, Constants.MAX_CACHE_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mDiskCache.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
