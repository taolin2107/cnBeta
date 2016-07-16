package app.taolin.cnbeta;

import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.viewpagerindicator.IconPagerAdapter;
import com.viewpagerindicator.PageIndicator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import app.taolin.cnbeta.adapter.ContentListAdapter;
import app.taolin.cnbeta.models.ContentList;
import app.taolin.cnbeta.models.Headline;
import app.taolin.cnbeta.utils.ContentUtil;
import app.taolin.cnbeta.utils.GsonRequest;
import app.taolin.cnbeta.utils.VolleySingleton;
import cn.appsdream.nestrefresh.base.AbsRefreshLayout;
import cn.appsdream.nestrefresh.base.OnPullListener;
import cn.appsdream.nestrefresh.normalstyle.NestRefreshLayout;

public class MainActivity extends AppCompatActivity implements OnPullListener {

    private List<View> mHeadlineViews;
    private ViewPagerAdapter mHeadlineAdapter;
    private PageIndicator mPageIndicator;

    private List<ContentList.Result> mDataList;
    private ContentListAdapter mContentListAdapter;
    private NestRefreshLayout mLoader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        requestData(true);
    }

    private void initView() {
        ListView contentList = (ListView) findViewById(R.id.content_list);
        // add header
        View header = LayoutInflater.from(this).inflate(R.layout.header_view, contentList, false);
        final ViewPager headlines = (ViewPager) header.findViewById(R.id.headline_list);
        mHeadlineViews = new ArrayList<>();
        mHeadlineAdapter = new ViewPagerAdapter();
        headlines.setAdapter(mHeadlineAdapter);
        mPageIndicator = (PageIndicator) header.findViewById(R.id.indicator);
        mPageIndicator.setViewPager(headlines);
        if (contentList != null) {
            contentList.addHeaderView(header);
        }

        mDataList = new ArrayList<>();
        mContentListAdapter = new ContentListAdapter(mDataList);
        contentList.setAdapter(mContentListAdapter);
        contentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openContent(mContentListAdapter.getItem(position).sid);
            }
        });

        mLoader = new NestRefreshLayout(contentList);
        mLoader.setPullRefreshEnable(true);
        mLoader.setPullLoadEnable(true);
        mLoader.setOnLoadingListener(this);
    }

    private void requestData(boolean isRefresh) {
        final int size = mDataList.size();
        final String sid = (isRefresh || size == 0) ? Integer.MAX_VALUE + "" : mDataList.get(size - 1).sid;
        GsonRequest contentRequest = new GsonRequest<>(ContentUtil.getContentListUrl(sid), ContentList.class, null,
                new Response.Listener<ContentList>() {
                    @Override
                    public void onResponse(ContentList response) {
                        if ("success".equals(response.status)) {
                            mDataList.addAll(response.result);
                            removeDuplicate(mDataList);
                            mContentListAdapter.notifyDataSetChanged();
                        }
                        mLoader.onLoadFinished();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Taolin", error.getMessage());
                mLoader.onLoadFinished();
            }
        });
        contentRequest.setShouldCache(false);
        VolleySingleton.getInstance().addToRequestQueue(contentRequest);

        requestHeadline();
    }

    private void requestHeadline() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document document = Jsoup.connect("http://www.cnbeta.com/").timeout(10000).get();
                    Elements elements = document.select(".main_content .headline dl");
                    List<Headline> headlineList = new ArrayList<>();
                    for (Element e: elements) {
                        Headline headline = new Headline();
                        final String link = e.select("dt a").first().attr("href");
                        headline.sid = link.substring(link.lastIndexOf("/") + 1, link.lastIndexOf("."));
                        headline.title = e.select("dt a").first().text();
                        headline.thumb = e.select("dd a img").first().attr("src");
                        headlineList.add(headline);
                    }
                    if (headlineList.size() == 3) {
                        refreshHeadline(headlineList);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void refreshHeadline(List<Headline> headlineList) {
        mHeadlineViews.clear();
        final LayoutInflater inflator = LayoutInflater.from(MainActivity.this);
        for (final Headline h: headlineList) {
            final View headline = inflator.inflate(R.layout.headline, null);
            final NetworkImageView thumb = (NetworkImageView) headline.findViewById(R.id.headline_thumb);
            thumb.setImageUrl(h.thumb, VolleySingleton.getInstance().getImageLoader());
            final TextView title = (TextView) headline.findViewById(R.id.headline_title);
            title.setText(h.title);
            mHeadlineViews.add(headline);
            headline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openContent(h.sid);
                }
            });
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mHeadlineAdapter.notifyDataSetChanged();
                mPageIndicator.notifyDataSetChanged();
            }
        });
    }

    private void openContent(final String sid) {
        Intent intent = new Intent(MainActivity.this, ContentActivity.class);
        intent.putExtra("sid", sid);
        startActivity(intent);
    }

    private void removeDuplicate(List<ContentList.Result> list) {
        ArrayList l = new ArrayList<>(new LinkedHashSet<>(list));
        list.clear();
        list.addAll(l);
    }

    @Override
    public void onRefresh(AbsRefreshLayout listLoader) {
        requestData(true);
    }

    @Override
    public void onLoading(AbsRefreshLayout listLoader) {
        requestData(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ViewPagerAdapter extends PagerAdapter implements IconPagerAdapter {

        public View getItem(int pos) {
            return (mHeadlineViews != null && mHeadlineViews.size() > pos) ? mHeadlineViews.get(pos): null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(getItem(position), 0);
            return mHeadlineViews.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mHeadlineViews.get(position));
        }

        @Override
        public int getIconResId(int index) {
            return R.drawable.viewpager_indicator;
        }

        @Override
        public int getCount() {
            return mHeadlineViews.size();
        }
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
