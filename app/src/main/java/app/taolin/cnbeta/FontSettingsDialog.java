package app.taolin.cnbeta;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import app.taolin.cnbeta.utils.SharedPreferenceUtil;

/**
 * @author taolin
 * @version v1.0
 * @date Jun 28, 2016.
 * @description
 */

public class FontSettingsDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.settings_font)
                .setCancelable(true)
                .setSingleChoiceItems(R.array.font_size_list, SharedPreferenceUtil.read(SharedPreferenceUtil.KEY_FONT_SIZE, 1),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferenceUtil.write(SharedPreferenceUtil.KEY_FONT_SIZE, which);
                        dismiss();
                    }
                })
                .create();
    }
}
