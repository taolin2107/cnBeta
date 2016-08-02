package app.taolin.cnbeta;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import app.taolin.cnbeta.dao.Article;
import app.taolin.cnbeta.dao.ArticleDao;
import app.taolin.cnbeta.dao.DaoMaster;
import app.taolin.cnbeta.dao.DaoSession;
import app.taolin.cnbeta.models.ArticleModel;
import app.taolin.cnbeta.utils.CommonUtil;
import app.taolin.cnbeta.utils.Constants;
import app.taolin.cnbeta.utils.ContentUtil;
import app.taolin.cnbeta.utils.DiskLruCache;
import app.taolin.cnbeta.utils.EncryptUtil;
import app.taolin.cnbeta.utils.GsonRequest;
import app.taolin.cnbeta.utils.SharedPreferenceUtil;
import app.taolin.cnbeta.utils.VolleySingleton;

/**
 * @author taolin
 * @version v1.0
 * @date Jul 13, 2016.
 * @description
 */

public class ContentActivity extends AppCompatActivity {

    private int mViewWidth;
    private ArticleDao mArticleDao;
    private DiskLruCache mDiskCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewWidth = getResources().getDisplayMetrics().widthPixels -
                getResources().getDimensionPixelSize(R.dimen.content_padding);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
        initCache();
        initDatabase();
        setContentView(R.layout.content);
        initViews();
    }

    private void initDatabase() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, Constants.TABLE_ARTICLE, null);
        SQLiteDatabase database = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(database);
        DaoSession daoSession = daoMaster.newSession();
        mArticleDao = daoSession.getArticleDao();
    }

    private void initViews() {
        final TextView title = (TextView) findViewById(R.id.title);
        final TextView contentDesc = (TextView) findViewById(R.id.content_desc);
        final TextView contentAbstract = (TextView) findViewById(R.id.content_abstract);
        if (contentAbstract != null) {
            contentAbstract.setMovementMethod(LinkMovementMethod.getInstance());
        }
        final TextView content = (TextView) findViewById(R.id.content);
        if (content != null) {
            content.setMovementMethod(LinkMovementMethod.getInstance());
        }
        setFontSize(title, contentAbstract, content);
        final String sid = getIntent().getStringExtra("sid");
        Article article = mArticleDao.queryBuilder().where(ArticleDao.Properties.Sid.eq(sid)).unique();
        if (article != null) {
            title.setText(article.getTitle());
            contentDesc.setText(getString(R.string.content_desc,
                    ContentUtil.getPrettyTime(ContentActivity.this, article.getTime()),
                    Html.fromHtml(article.getSource()), article.getCounter(),
                    article.getGood(), article.getComments()));
            new ImageLoadTask(contentAbstract).execute(ContentUtil.filterContent(article.getHometext()));
            new ImageLoadTask(content).execute(ContentUtil.filterContent(article.getBodytext()));
        } else {
            mArticleDao.queryBuilder().list();
            GsonRequest contentRequest = new GsonRequest<>(ContentUtil.getContentUrl(sid), ArticleModel.class, null,
                    new Response.Listener<ArticleModel>() {
                        @Override
                        public void onResponse(ArticleModel response) {
                            if ("success".equals(response.status)) {
                                title.setText(response.result.title);
                                contentDesc.setText(getString(R.string.content_desc,
                                        ContentUtil.getPrettyTime(ContentActivity.this, response.result.time),
                                        Html.fromHtml(response.result.source), response.result.counter,
                                        response.result.good, response.result.comments));
                                new ImageLoadTask(contentAbstract).execute(ContentUtil.filterContent(response.result.hometext));
                                new ImageLoadTask(content).execute(ContentUtil.filterContent(response.result.bodytext));
                                saveToDatabase(response.result);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Taolin", error.getMessage());
                }
            });
            contentRequest.setShouldCache(false);
            VolleySingleton.getInstance().addToRequestQueue(contentRequest);
        }
    }

    private void saveToDatabase(ArticleModel.Result result) {
        Article article = new Article();
        article.setSid(result.sid);
        article.setTime(result.time);
        article.setTitle(result.title);
        article.setSource(result.source);
        article.setCounter(result.counter);
        article.setGood(result.good);
        article.setComments(result.comments);
        article.setHometext(result.hometext);
        article.setBodytext(result.bodytext);
        mArticleDao.insertOrReplace(article);
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

    private void setFontSize(TextView title, TextView abs, TextView content) {
        switch (SharedPreferenceUtil.read(Constants.KEY_FONT_SIZE, 1)) {
            case 0:
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                abs.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                break;
            case 1:
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                abs.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                break;
            case 2:
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
                abs.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                break;
            case 3:
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                abs.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                break;
        }
    }

    private Html.ImageGetter mImageGetter = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(final String source) {
            try {
                DiskLruCache.Snapshot snapShot = mDiskCache.get(EncryptUtil.md5(source));
                if (snapShot == null) {
                    DiskLruCache.Editor editor = mDiskCache.edit(EncryptUtil.md5(source));
                    if (editor != null) {
                        OutputStream os = editor.newOutputStream(0);
                        if (downloadUrlToStream(source, os)) {
                            editor.commit();
                        } else {
                            editor.abort();
                        }
                    }
                    mDiskCache.flush();
                    snapShot = mDiskCache.get(EncryptUtil.md5(source));
                }
                Bitmap bitmap = null;
                if (snapShot != null) {
                    InputStream is = snapShot.getInputStream(0);
                    bitmap = BitmapFactory.decodeStream(is);
                }
                if (bitmap != null) {
                    Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                    float scale = mViewWidth * 1.0f / bitmap.getWidth();
                    final int height = (int) (bitmap.getHeight() * scale);
                    drawable.setBounds(0, 0, mViewWidth, height);
                    return drawable;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    };

    private class ImageLoadTask extends AsyncTask<String, Void, Spanned> {

        private TextView mTextView;

        ImageLoadTask(TextView textView) {
            mTextView = textView;
        }

        @Override
        protected Spanned doInBackground(String[] params) {
            return Html.fromHtml(params[0], mImageGetter, null);
        }

        @Override
        protected void onPostExecute(Spanned spanned) {
            super.onPostExecute(spanned);
            mTextView.setText(spanned);
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

    private boolean downloadUrlToStream(String src, OutputStream os) {
        HttpURLConnection connect = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;
        try {
            final URL url = new URL(src);
            connect = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(connect.getInputStream(), 8 * 1024);
            out = new BufferedOutputStream(os, 8 * 1024);
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connect != null) {
                connect.disconnect();
            }
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
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
}
