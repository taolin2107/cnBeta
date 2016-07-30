package app.taolin.cnbeta;

import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.viewpagerindicator.IconPagerAdapter;
import com.viewpagerindicator.PageIndicator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import app.taolin.cnbeta.adapter.ContentListAdapter;
import app.taolin.cnbeta.dao.DaoMaster;
import app.taolin.cnbeta.dao.DaoSession;
import app.taolin.cnbeta.dao.FavorItem;
import app.taolin.cnbeta.dao.FavorItemDao;
import app.taolin.cnbeta.dao.ListItem;
import app.taolin.cnbeta.dao.ListItemDao;
import app.taolin.cnbeta.models.ArticleModel;
import app.taolin.cnbeta.models.ListItemModel;
import app.taolin.cnbeta.models.HeadlineModel;
import app.taolin.cnbeta.utils.Constants;
import app.taolin.cnbeta.utils.ContentUtil;
import app.taolin.cnbeta.utils.GsonRequest;
import app.taolin.cnbeta.utils.VolleySingleton;
import cn.appsdream.nestrefresh.base.AbsRefreshLayout;
import cn.appsdream.nestrefresh.base.OnPullListener;
import cn.appsdream.nestrefresh.normalstyle.NestRefreshLayout;

public class MainActivity extends AppCompatActivity implements OnPullListener {

    private static final String MAX_TIME = "max_time";
    private static final long REFRESH_INTERVEL = 5 * 60 * 1000;

    private List<View> mHeadlineViews;
    private ViewPagerAdapter mHeadlineAdapter;
    private PageIndicator mPageIndicator;

    private List<ListItemModel.Result> mDataList;
    private ContentListAdapter mContentListAdapter;
    private NestRefreshLayout mLoader;

    private FavorItemDao mFavorItemDao;
    private ListItemDao mListItemDao;

    private long mLastRefreshTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDatabase();
        initView();
        requestData(true);
    }

    private void initView() {
        SwipeMenuListView contentList = (SwipeMenuListView) findViewById(R.id.content_list);
        addHeaderView(contentList);
        addSwipeMenu(contentList);

        mDataList = new ArrayList<>();
        mContentListAdapter = new ContentListAdapter(mDataList, false);
        contentList.setAdapter(mContentListAdapter);
        contentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //position 0 is the listview header
                ListItemModel.Result result = (ListItemModel.Result) mContentListAdapter.getItem(position - 1);
                result.is_read = true;
                openContent(result.sid);
                mContentListAdapter.notifyDataSetChanged();
                updateDatabase(result);
            }
        });

        mLoader = new NestRefreshLayout(contentList);
        mLoader.setPullRefreshEnable(true);
        mLoader.setPullLoadEnable(true);
        mLoader.setOnLoadingListener(this);
    }

    private void addHeaderView(final SwipeMenuListView listView) {
        // add header
        View header = LayoutInflater.from(this).inflate(R.layout.header_view, listView, false);
        final ViewPager headlines = (ViewPager) header.findViewById(R.id.headline_list);
        mHeadlineViews = new ArrayList<>();
        mHeadlineAdapter = new ViewPagerAdapter();
        headlines.setAdapter(mHeadlineAdapter);
        mPageIndicator = (PageIndicator) header.findViewById(R.id.indicator);
        mPageIndicator.setViewPager(headlines);
        listView.addHeaderView(header);
    }

    private void addSwipeMenu(final SwipeMenuListView listView) {
        // add swipe menu
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                Resources res = MainActivity.this.getResources();
                SwipeMenuItem menuItem = new SwipeMenuItem(MainActivity.this);
                menuItem.setBackground(R.color.colorPrimary);
                menuItem.setWidth(res.getDimensionPixelSize(R.dimen.swipe_menu_item_width));
                menuItem.setTitle(R.string.settings_favor);
                menuItem.setTitleSize(16);
                menuItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(menuItem);
            }
        };
        listView.setMenuCreator(creator);
        listView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        collectArticle(position);
                        Toast.makeText(MainActivity.this, R.string.favor_toast, Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });
    }

    private void collectArticle(int pos) {
        ListItemModel.Result result = mDataList.get(pos);
        FavorItem favorItem = new FavorItem();
        favorItem.setSid(result.sid);
        favorItem.setTitle(result.title);
        favorItem.setPubtime(result.pubtime);
        favorItem.setCollecttime(ContentUtil.getFormatTime());
        try {
            mFavorItemDao.insert(favorItem);
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
        }
    }

    private void requestData(final boolean isRefresh) {
        if (isRefresh) {
            final long currentTime = System.currentTimeMillis();
            if (currentTime - mLastRefreshTime < REFRESH_INTERVEL) {
                mLoader.onLoadFinished();
                return;
            }
        }
        final int size = mDataList.size();
        final String sid = (isRefresh || size == 0) ? Integer.MAX_VALUE + "" : mDataList.get(size - 1).sid;
        GsonRequest contentRequest = new GsonRequest<>(ContentUtil.getContentListUrl(sid), ListItemModel.class, null,
                new Response.Listener<ListItemModel>() {
                    @Override
                    public void onResponse(ListItemModel response) {
                        if ("success".equals(response.status)) {
                            mDataList.addAll(response.result);
                            removeDuplicate(mDataList);
                            saveToDatabase(mDataList);
                            mContentListAdapter.notifyDataSetChanged();
                            if (isRefresh) {
                                mLastRefreshTime = System.currentTimeMillis();
                            }
                        } else {
                            if (!isRefresh || mDataList.size() == 0) {
                                loadFromDatabase(mDataList.size() == 0 ? MAX_TIME: mDataList.get(size - 1).pubtime);
                                mContentListAdapter.notifyDataSetChanged();
                            }
                            if (isRefresh) {
                                mLastRefreshTime = 0;
                            }
                        }
                        mLoader.onLoadFinished();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Taolin", error.getMessage());
                if (!isRefresh || mDataList.size() == 0) {
                    loadFromDatabase(mDataList.size() == 0 ? MAX_TIME: mDataList.get(size - 1).pubtime);
                    mContentListAdapter.notifyDataSetChanged();
                }
                if (isRefresh) {
                    mLastRefreshTime = 0;
                }
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
                    List<HeadlineModel> headlineList = new ArrayList<>();
                    for (Element e: elements) {
                        HeadlineModel headline = new HeadlineModel();
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

    private void refreshHeadline(List<HeadlineModel> headlineList) {
        mHeadlineViews.clear();
        final LayoutInflater inflator = LayoutInflater.from(this);
        for (final HeadlineModel headData: headlineList) {
            final View headView = inflator.inflate(R.layout.headline, null);
            mHeadlineViews.add(headView);
            requestHeadlineImage(headData, headView);
            headView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openContent(headData.sid);
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

    private void requestHeadlineImage(final HeadlineModel data, final View view) {
        GsonRequest contentRequest = new GsonRequest<>(ContentUtil.getContentUrl(data.sid), ArticleModel.class, null,
                new Response.Listener<ArticleModel>() {
                    @Override
                    public void onResponse(ArticleModel response) {
                        try {
                            if ("success".equals(response.status)) {
                                data.title = response.result.title.trim();
                                Document document = Jsoup.parse(response.result.bodytext);
                                Element imgElement = document.select("img").first();
                                if (imgElement != null) {
                                    data.thumb = imgElement.attr("src");
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        final NetworkImageView thumb = (NetworkImageView) view.findViewById(R.id.headline_thumb);
                                        final TextView title = (TextView) view.findViewById(R.id.headline_title);
                                        thumb.setImageUrl(data.thumb, VolleySingleton.getInstance().getImageLoader());
                                        title.setText(data.title);
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
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

    private void initDatabase() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, Constants.TABLE_FAVOR_ITEM, null);
        SQLiteDatabase database = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(database);
        DaoSession daoSession = daoMaster.newSession();
        mFavorItemDao = daoSession.getFavorItemDao();
        mListItemDao = daoSession.getListItemDao();
    }

    private void openContent(final String sid) {
        Intent intent = new Intent(this, ContentActivity.class);
        intent.putExtra("sid", sid);
        startActivity(intent);
    }

    private void removeDuplicate(List<ListItemModel.Result> list) {
        ArrayList<ListItemModel.Result> l = new ArrayList<>(new LinkedHashSet<>(list));
        list.clear();
        list.addAll(l);
        Collections.sort(list);
    }

    private void loadFromDatabase(final String pubTime) {
        List<ListItem> list = mListItemDao.queryBuilder()
                .where(ListItemDao.Properties.Pubtime.lt(pubTime))
                .orderDesc(ListItemDao.Properties.Pubtime)
                .limit(20)
                .list();
        for (ListItem li: list) {
            ListItemModel.Result result = new ListItemModel.Result();
            result.sid = li.getSid();
            result.title = li.getTitle();
            result.pubtime = li.getPubtime();
            result.is_read = li.getIsread();
            mDataList.add(result);
        }
    }

    private void saveToDatabase(final List<ListItemModel.Result> list) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (ListItemModel.Result r: list) {
                    ListItem item = new ListItem();
                    item.setSid(r.sid);
                    item.setTitle(r.title);
                    item.setPubtime(r.pubtime);
                    item.setIsread(r.is_read);
                    try {
                        mListItemDao.insert(item);
                    } catch (SQLiteConstraintException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void updateDatabase(final ListItemModel.Result result) {
        ListItem item = new ListItem();
        item.setSid(result.sid);
        item.setTitle(result.title);
        item.setPubtime(result.pubtime);
        item.setIsread(result.is_read);
        try {
            mListItemDao.insertOrReplace(item);
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
        }
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

        View getItem(int pos) {
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
