package com.muchen.tweetstormmaker.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.muchen.tweetstormmaker.constants.Constants;
import com.muchen.tweetstormmaker.databinding.DraftBinding;
import com.muchen.tweetstormmaker.models.Draft;
import com.muchen.tweetstormmaker.views.activities.DraftEditActivity;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class DraftAdapter extends RecyclerView.Adapter<DraftAdapter.DraftViewHolder>{
    private List<Draft> drafts;
    // context is used in both the call to create intent and the call to start an activity with that
    // intent.
    private Context context;

    public DraftAdapter(@NonNull Context context){
        super();
        this.context = context;
    }

    class DraftViewHolder extends RecyclerView.ViewHolder{
        private DraftBinding binding;
        private DraftViewHolder(@NonNull DraftBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener((view)->{
                Intent intent = new Intent(context, DraftEditActivity.class);
                intent.putExtra(Constants.EXTRA_DRAFT_ID,
                        drafts.get(getAdapterPosition()).getDraftId());
                context.startActivity(intent);
            });
        }
    }

    @NonNull
    @Override
    public DraftAdapter.DraftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // By default, getItemViewType() returns 0, assuming there's only one type, so by default,
        // the value of viewType passed in is 0.
        DraftBinding binding = DraftBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false);
        return new DraftViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DraftViewHolder draftViewHolder, int position) {
        draftViewHolder.binding.draftTitleTextView.setText(drafts.get(position).getTitle());

        Date lastModified = new Date(drafts.get(position).getLastModified());
        // sets lastModified to a date/time string formatted according to the default locale
        draftViewHolder.binding.draftLastModifiedTextView.
                setText(DateFormat.getDateTimeInstance().format(lastModified));

        // trims body down if its length exceeds MAX_CHARACTERS
        String body = drafts.get(position).getBody();
        if (body.length() > Constants.MAX_DRAFT_BODY_SNIPPET_SIZE){
            body = body.substring(0, body.offsetByCodePoints(Constants.MAX_DRAFT_BODY_SNIPPET_SIZE - 1, 1))
                    + "...";
        }
        draftViewHolder.binding.draftBodyTextView.setText(body);
    }

    @Override
    public int getItemCount() { return drafts == null ? 0 : drafts.size(); }

    public Draft getDraft(int index) { return drafts.get(index); }

    public void setDrafts(List<Draft> drafts) { this.drafts = drafts; }

    // Methods to sort draft in different orders
//    public void sortDraftsByLastModified(final int order){
//        Comparator<Draft> comp = (o1, o2) -> {
//            long diff = 0;
//            if (order == Constants.MOST_RECENT_FIRST) diff = o2.getLastModified() - o1.getLastModified();
//            if (order == Constants.MOST_RECENT_LAST) diff = o1.getLastModified() - o2.getLastModified();
//            if (diff < 0) return -1;
//            if (diff > 0) return 1;
//            return 0;
//        };
//        Collections.sort(drafts, comp);
//    }
//
//    public void sortDraftsByTitle(final int order){
//        Comparator<Draft> comp = (o1, o2) -> {
//            int diff = 0;
//            if (order == Constants.ASC_ORDER) diff = o1.getTitle().compareTo(o2.getTitle());
//            if (order == Constants.DESC_ORDER) diff = o2.getTitle().compareTo(o1.getTitle());
//            if (diff < 0) return -1;
//            if (diff > 0) return 1;
//            return 0;
//        };
//        Collections.sort(drafts, comp);
//    }
}