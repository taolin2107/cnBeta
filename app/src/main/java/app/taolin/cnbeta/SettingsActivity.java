package app.taolin.cnbeta;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

/**
 * @author taolin
 * @version v1.0
 * @date Jul 15, 2016.
 * @description Settings
 */

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.settings);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.font_settings:
                new FontSettingsDialog().show(getFragmentManager(), "font_settings");
                break;
            case R.id.favor:

                break;
            case R.id.about:

                break;
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
