package com.muchen.tweetstormandroid.views;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.muchen.tweetstormandroid.R;
import com.muchen.tweetstormandroid.constants.Constants;
import com.muchen.tweetstormandroid.models.Draft;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Comparator;
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

    public class DraftViewHolder extends RecyclerView.ViewHolder{
        public TextView title, body, lastModified;

        public DraftViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.draft_title_text_view);
            body = itemView.findViewById(R.id.draft_body_text_view);
            lastModified = itemView.findViewById(R.id.draft_last_modified_text_view);
            itemView.setOnClickListener((view)->{
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
        CardView view = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.draft, parent, false);
        return new DraftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DraftViewHolder draftViewHolder, int position) {
        draftViewHolder.title.setText(drafts.get(position).getTitle());

        Date lastModified = new Date(drafts.get(position).getLastModified());
        // sets lastModified to a date/time string formatted according to the default locale
        draftViewHolder.lastModified.setText(DateFormat.getDateTimeInstance().format(lastModified));

        // trims body down if its length exceeds MAX_CHARACTERS
        String body = drafts.get(position).getBody();
        if (body.length() > Constants.MAX_DRAFT_BODY_SNIPPET_SIZE){
            body = body.substring(0, body.offsetByCodePoints(Constants.MAX_DRAFT_BODY_SNIPPET_SIZE - 1, 1))
                    + "...";
        }
        draftViewHolder.body.setText(body);
    }

    @Override
    public int getItemCount() { return drafts == null ? 0 : drafts.size(); }

    public Draft getDraft(int index) { return drafts.get(index); }

    public void setDrafts(List<Draft> drafts) { this.drafts = drafts; }

    public void sortDraftsByLastModified(final int order){
        Comparator<Draft> comp = (o1, o2) -> {
            long diff = 0;
            if (order == Constants.MOST_RECENT_FIRST) diff = o2.getLastModified() - o1.getLastModified();
            if (order == Constants.MOST_RECENT_LAST) diff = o1.getLastModified() - o2.getLastModified();
            if (diff < 0) return -1;
            if (diff > 0) return 1;
            return 0;
        };
        Collections.sort(drafts, comp);
    }

    public void sortDraftsByTitle(final int order){
        Comparator<Draft> comp = (o1, o2) -> {
            int diff = 0;
            if (order == Constants.ASC_ORDER) diff = o1.getTitle().compareTo(o2.getTitle());
            if (order == Constants.DESC_ORDER) diff = o2.getTitle().compareTo(o1.getTitle());
            if (diff < 0) return -1;
            if (diff > 0) return 1;
            return 0;
        };
        Collections.sort(drafts, comp);
    }
}