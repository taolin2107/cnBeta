package app.taolin.cnbeta;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.util.Collections;
import java.util.List;

import app.taolin.cnbeta.adapter.ContentListAdapter;
import app.taolin.cnbeta.dao.DaoMaster;
import app.taolin.cnbeta.dao.DaoSession;
import app.taolin.cnbeta.dao.FavorItem;
import app.taolin.cnbeta.dao.FavorItemDao;
import app.taolin.cnbeta.utils.Constants;

/**
 * @author taolin
 * @version v1.0
 * @date Jun 29, 2016.
 * @description
 */

public class FavorListActivity extends AppCompatActivity {

    private ContentListAdapter mContentListAdapter;
    private FavorItemDao mFavorItemDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.favor_article_layout);
        SwipeMenuListView contentList = (SwipeMenuListView) findViewById(R.id.article_list);
        initViews(contentList);
        initData(contentList);
    }

    private void initData(SwipeMenuListView contentList) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, Constants.DATABASE_NAME, null);
        SQLiteDatabase database = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(database);
        DaoSession daoSession = daoMaster.newSession();
        mFavorItemDao = daoSession.getFavorItemDao();

        List<FavorItem> dataList = mFavorItemDao.queryBuilder().list();
        Collections.sort(dataList);
        mContentListAdapter = new ContentListAdapter(dataList, true);
        contentList.setAdapter(mContentListAdapter);
    }

    private void openContent(final String sid) {
        Intent intent = new Intent(this, ContentActivity.class);
        intent.putExtra(Constants.KEY_EXTRA_SID, sid);
        startActivity(intent);
    }

    private void initViews(SwipeMenuListView listView) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openContent(((FavorItem) mContentListAdapter.getItem(position)).getSid());
            }
        });
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                Resources res = FavorListActivity.this.getResources();
                SwipeMenuItem menuItem = new SwipeMenuItem(FavorListActivity.this);
                menuItem.setBackground(android.R.color.holo_red_light);
                menuItem.setWidth(res.getDimensionPixelSize(R.dimen.swipe_menu_item_width));
                menuItem.setTitle(R.string.favor_delete);
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
                        deleteArticle(position);
                        break;
                }
                return false;
            }
        });
    }

    private void deleteArticle(int pos) {
        FavorItem article = (FavorItem) mContentListAdapter.getItem(pos);
        mFavorItemDao.deleteByKey(article.getSid());

        mContentListAdapter.remove(pos);
        mContentListAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.favor_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.clean:
                new AlertDialog.Builder(this)
                        .setMessage(R.string.favor_clean_confirm_dialog)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mFavorItemDao.deleteAll();
                                mContentListAdapter.cleanList();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .create()
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
