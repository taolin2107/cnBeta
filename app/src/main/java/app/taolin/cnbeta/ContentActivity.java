package app.taolin.cnbeta;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import app.taolin.cnbeta.models.Content;
import app.taolin.cnbeta.utils.ContentUtil;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewWidth = getResources().getDisplayMetrics().widthPixels -
                getResources().getDimensionPixelSize(R.dimen.content_padding);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.content);
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
        GsonRequest contentRequest = new GsonRequest<>(ContentUtil.getContentUrl(sid), Content.class, null,
                new Response.Listener<Content>() {
                    @Override
                    public void onResponse(Content response) {
                        if ("success".equals(response.status)) {
                            title.setText(response.result.title);
                            contentDesc.setText(getString(R.string.content_desc,
                                    ContentUtil.getPrettyTime(ContentActivity.this, response.result.time),
                                    Html.fromHtml(response.result.source), response.result.counter,
                                    response.result.good, response.result.comments));
                            new ImageLoadTask(contentAbstract).execute(ContentUtil.filterContent(response.result.hometext));
                            new ImageLoadTask(content).execute(ContentUtil.filterContent(response.result.bodytext));
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

    private void setFontSize(TextView title, TextView abs, TextView content) {
        switch (SharedPreferenceUtil.read(SharedPreferenceUtil.KEY_FONT_SIZE, 1)) {
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
            Drawable drawable = null;
            try {
                URL url = new URL(source);
                InputStream is = url.openStream();
                drawable = Drawable.createFromStream(is, null);
                float scale = mViewWidth * 1.0f / drawable.getIntrinsicWidth();
                final int height = (int) (drawable.getIntrinsicHeight() * scale);
                drawable.setBounds(0, 0, mViewWidth, height);
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return drawable;
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
}
