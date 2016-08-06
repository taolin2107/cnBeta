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
import app.taolin.cnbeta.dao.ListItem;
import app.taolin.cnbeta.dao.ListItemDao;
import app.taolin.cnbeta.utils.Constants;

/**
 * @author taolin
 * @version v1.0
 * @date Jun 29, 2016.
 * @description
 */

public class FavorListActivity extends AppCompatActivity {

    private ContentListAdapter mContentListAdapter;
    private ListItemDao mListItemDao;
    private SwipeMenuListView mContentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.favor_article_layout);
        mContentList = (SwipeMenuListView) findViewById(R.id.article_list);
        initViews();
        initDatabase();
    }

    private void initDatabase() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, Constants.DATABASE_NAME, null);
        SQLiteDatabase database = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(database);
        DaoSession daoSession = daoMaster.newSession();
        mListItemDao = daoSession.getListItemDao();
    }

    @Override
    protected void onStart() {
        super.onStart();
        List<ListItem> dataList = mListItemDao.queryBuilder()
                .where(ListItemDao.Properties.Isfavor.eq(true))
                .list();
        Collections.sort(dataList);
        mContentListAdapter = new ContentListAdapter(dataList, true);
        mContentList.setAdapter(mContentListAdapter);
    }

    private void openContent(final String sid) {
        Intent intent = new Intent(this, ContentActivity.class);
        intent.putExtra(Constants.KEY_EXTRA_SID, sid);
        startActivity(intent);
    }

    private void initViews() {
        mContentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openContent((mContentListAdapter.getItem(position)).getSid());
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
        mContentList.setMenuCreator(creator);
        mContentList.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        mContentList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        removeFavor(position);
                        break;
                }
                return false;
            }
        });
    }

    private void removeFavor(int pos) {
        ListItem article = mContentListAdapter.getItem(pos);
        article.setIsfavor(false);
        mListItemDao.update(article);

        mContentListAdapter.remove(pos);
        mContentListAdapter.notifyDataSetChanged();
    }

    private void removeAllFavors() {
        List<ListItem> dataList = mListItemDao.queryBuilder()
                .where(ListItemDao.Properties.Isfavor.eq(true))
                .list();
        for (ListItem item: dataList) {
            item.setIsfavor(false);
        }
        mListItemDao.updateInTx(dataList);
        mContentListAdapter.cleanList();
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
                                removeAllFavors();
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
