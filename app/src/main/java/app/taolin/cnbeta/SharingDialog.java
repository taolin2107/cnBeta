package app.taolin.cnbeta;

import android.app.DialogFragment;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import app.taolin.cnbeta.dao.Article;
import app.taolin.cnbeta.dao.ArticleDao;
import app.taolin.cnbeta.dao.DaoMaster;
import app.taolin.cnbeta.dao.DaoSession;
import app.taolin.cnbeta.dao.FavorItem;
import app.taolin.cnbeta.dao.FavorItemDao;
import app.taolin.cnbeta.utils.Constants;
import app.taolin.cnbeta.utils.ContentUtil;

/**
 * @author taolin
 * @version v1.0
 * @date Aug 4, 2016.
 * @description
 */

public class SharingDialog extends DialogFragment {

    private Article mArticle;
    private boolean mIsFavor;
    private FavorItemDao mFavorItemDao;
    private TextView mFavorBtn;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.share_layout, container, false);
        Bundle bundle = getArguments();
        final String sid = bundle.getString(Constants.KEY_EXTRA_SID);
        initDatabase(sid);
        initViews(view);
        return view;
    }

    private void initViews(View root) {
        root.findViewById(R.id.share_wechat_friend).setOnClickListener(mClickListener);
        root.findViewById(R.id.share_wechat_moment).setOnClickListener(mClickListener);
        root.findViewById(R.id.share_qq).setOnClickListener(mClickListener);
        mFavorBtn = (TextView) root.findViewById(R.id.share_favor);
        mFavorBtn.setOnClickListener(mClickListener);
        if (mIsFavor) {
            mFavorBtn.setText(R.string.sharing_favor_cancel);
        }
    }

    private void initDatabase(final String sid) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(App.getInstance(), Constants.DATABASE_NAME, null);
        SQLiteDatabase database = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(database);
        DaoSession daoSession = daoMaster.newSession();
        ArticleDao articleDao = daoSession.getArticleDao();
        mFavorItemDao = daoSession.getFavorItemDao();
        mArticle = articleDao.queryBuilder().where(ArticleDao.Properties.Sid.eq(sid)).unique();
        mIsFavor = mFavorItemDao.queryBuilder().where(FavorItemDao.Properties.Sid.eq(sid)).unique() != null;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.share_wechat_friend:

                    break;

                case R.id.share_wechat_moment:

                    break;

                case R.id.share_qq:

                    break;

                case R.id.share_favor:
                    if (mIsFavor) {
                        mFavorItemDao.deleteByKey(mArticle.getSid());
                        mFavorBtn.setText(R.string.sharing_favor);
                        Toast.makeText(App.getInstance(), R.string.favor_toast_cancel, Toast.LENGTH_SHORT).show();
                    } else {
                        FavorItem favorItem = new FavorItem();
                        favorItem.setSid(mArticle.getSid());
                        favorItem.setTitle(mArticle.getTitle());
                        favorItem.setPubtime(mArticle.getTime());
                        favorItem.setCollecttime(ContentUtil.getFormatTime());
                        try {
                            mFavorItemDao.insert(favorItem);
                        } catch (SQLiteConstraintException e) {
                            Log.e("Taolin", "primary key duplicated, skip this error.");
                        }
                        mFavorBtn.setText(R.string.sharing_favor_cancel);
                        Toast.makeText(App.getInstance(), R.string.favor_toast, Toast.LENGTH_SHORT).show();
                    }
                    dismiss();
                    break;
            }
        }
    };
}
