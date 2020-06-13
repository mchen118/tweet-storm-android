package com.muchen.tweetstormandroid.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.muchen.tweetstormandroid.R;

public class PinDialogFragment extends DialogFragment {
    private PinDialogListenerInterface listener;

    public interface PinDialogListenerInterface{
        void onPinPositiveButtonClick(String pin);
    }

    public void setOnPinDialogListener(PinDialogListenerInterface listener){
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.pin_dialog_fragment, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.pin_dialog_title_text)
                .setView(view)
                .setPositiveButton((R.string.dialog_positive_button_text), (dialog, id)->{
                    EditText editText = (EditText) view.findViewById(R.id.pin_dialog_edit_text);
                    String input = editText.getText().toString();
                    if (input.isEmpty()){
                        Toast.makeText(getActivity(), "Pin can not be empty!", Toast.LENGTH_LONG).show();
                    } else {
                        listener.onPinPositiveButtonClick(input);
                    }
                    dismiss();
                })
                .setNegativeButton(R.string.dialog_negative_button_text, (dialog, id)-> dismiss());
        return builder.create();
    }
}
