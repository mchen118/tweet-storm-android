package com.muchen.tweetstormmaker.views.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.muchen.tweetstormmaker.R;
import com.muchen.tweetstormmaker.databinding.PinDialogFragmentBinding;

import org.jetbrains.annotations.NotNull;

public class PinDialogFragment extends DialogFragment {
    private PinDialogListenerInterface listener;

    public interface PinDialogListenerInterface{
        void onPinPositiveButtonClick(String pin);
    }

    public void setOnPinDialogListener(PinDialogListenerInterface listener){
        this.listener = listener;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        // Because DialogFragment.getLayoutInflater() calls onCreateDialog(Bundle) inside, so
        // using that method to create a binding instance will cause an infinite loop.
        PinDialogFragmentBinding binding = PinDialogFragmentBinding.
                inflate(LayoutInflater.from(getContext()));
        Log.d("debug", "PinDialogFragment.onCreateDialog(...) called");
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.pin_dialog_title_text)
                .setView(binding.getRoot())
                .setPositiveButton((R.string.dialog_positive_button_text), (dialog, id)->{
                    String input = binding.pinDialogEditText.getText().toString();
                    if (input.isEmpty()){
                        Toast.makeText(getActivity(), "Pin cannot be empty!", Toast.LENGTH_LONG).show();
                    } else {
                        listener.onPinPositiveButtonClick(input);
                    }
                    dismiss();
                })
                .setNegativeButton(R.string.dialog_negative_button_text, (dialog, id)-> dismiss());
        return builder.create();
    }
}
