package jp.ac.dendai.im.cps.citywalkersmeter.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class OkCancelDialog extends DialogFragment {
    private static final String TAG = OkCancelDialog.class.getSimpleName();
    private DialogInterface.OnClickListener okClickListener = null;
    private DialogInterface.OnClickListener cancelClickListener = null;

    public static OkCancelDialog newInstance(int title, String message) {
        OkCancelDialog fragment = new OkCancelDialog();
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putString("message", message);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle safedInstanceState) {
        int title = getArguments().getInt("title");
        String message = getArguments().getString("message");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", this.okClickListener)
                .setNegativeButton("Cancel", this.cancelClickListener);

        return builder.create();
    }

    /**
     * OKクリックリスナーの登録
     */
    public void setOnOkClickListener(DialogInterface.OnClickListener listener) {
        this.okClickListener = listener;
    }

    /**
     * Cancelクリックリスナーの登録
     */
    public void setOnCancelClickListener(DialogInterface.OnClickListener listener) {
        this.cancelClickListener = listener;
    }
}
