package app.taolin.cnbeta.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import app.taolin.cnbeta.App;
import app.taolin.cnbeta.R;
import app.taolin.cnbeta.dao.ListItem;
import app.taolin.cnbeta.utils.ContentUtil;

/**
 * @author taolin
 * @version v1.0
 * @date Jul 11, 2016.
 * @description
 */

public class ContentListAdapter extends BaseAdapter {

    private List<ListItem> mDataList;
    private boolean mIsFavor;
    private String mFavorTimePrefix;

    public ContentListAdapter(List<ListItem> list, boolean isFavor) {
        mDataList = list;
        mIsFavor = isFavor;
        mFavorTimePrefix = App.getInstance().getString(R.string.favor_time_prefix);
    }

    public void cleanList() {
        mDataList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public ListItem getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void remove(int position) {
        mDataList.remove(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(App.getInstance()).inflate(R.layout.list_item, parent, false);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.time = (TextView) convertView.findViewById(R.id.time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ListItem listItem = getItem(position);
        holder.title.setText(listItem.getTitle().trim());
        if (mIsFavor) {
            holder.time.setText(mFavorTimePrefix + ContentUtil.getPrettyTime(App.getInstance(), listItem.getCollecttime()));
        } else {
            holder.title.setTextColor(Color.parseColor(listItem.getIsread()? "#999999": "#444444"));
            holder.time.setText(ContentUtil.getPrettyTime(App.getInstance(), listItem.getPubtime()));
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView title;
        TextView time;
    }
}
