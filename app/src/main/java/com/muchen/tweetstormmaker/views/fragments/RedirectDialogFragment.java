package com.muchen.tweetstormmaker.views.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.muchen.tweetstormmaker.R;
import com.muchen.tweetstormmaker.constants.Constants;

import org.jetbrains.annotations.NotNull;

public class RedirectDialogFragment extends DialogFragment {
    private RedirectDialogListenerInterface listener;

    public interface RedirectDialogListenerInterface {
        void onRedirectPositiveButtonClick();
    }

    @NotNull
    public static RedirectDialogFragment newInstance(String url){
        RedirectDialogFragment frag = new RedirectDialogFragment();
        Bundle args = new Bundle();
        args.putString(Constants.BUNDLE_KEY_REDIRECT_URL, url);
        frag.setArguments(args);
        return frag;
    }

    public void setOnRedirectDialogListener(RedirectDialogListenerInterface listener){
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        String redirectURL = getArguments().getString(Constants.BUNDLE_KEY_REDIRECT_URL);
        String explanation = "You will be redirected to the following URL to authorize this app to" +
                " tweet using your account:\n\n";
        // gets AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(explanation + redirectURL)
                .setPositiveButton(R.string.dialog_positive_button_text, (dialog, id)->{
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(redirectURL));
                    if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                        listener.onRedirectPositiveButtonClick();
                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(), "Cannot find any app to open URL", Toast.LENGTH_LONG).show();
                    }
                    dismiss();
                })
                .setNegativeButton(R.string.dialog_negative_button_text, (dialog, id)-> dismiss());
        return builder.create();
    }
}
