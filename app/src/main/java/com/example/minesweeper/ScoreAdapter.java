package com.example.minesweeper;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * ScoreAdapter
 * -------------
 * This adapter connects the list of Score objects to the RecyclerView in
 * ScoreboardActivity. It inflates each row layout (item_score.xml) and binds
 * the score data (difficulty, time, and date) to the corresponding TextViews.
 */
public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder> {

    private static final String TAG = "ScoreAdapter";

    /** The application context, used for inflating layouts */
    private Context context;

    /** The list of Score objects to display */
    private List<Score> scores;

    /**
     * Constructor for the adapter
     *
     * @param context The current context
     * @param scores  The list of Score objects to display
     */
    public ScoreAdapter(Context context, List<Score> scores) {
        this.context = context;
        this.scores = scores;
        Log.d(TAG, "Adapter created with " + scores.size() + " scores.");
    }

    /**
     * Called when RecyclerView needs a new ViewHolder to represent an item.
     * Inflates the layout for a single score row (item_score.xml).
     */
    @Override
    public ScoreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder called");
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_score, parent, false);
        return new ScoreViewHolder(view);
    }

    /**
     * Binds data from a Score object to the corresponding ViewHolder.
     * Called automatically as the user scrolls.
     */
    @Override
    public void onBindViewHolder(ScoreViewHolder holder, int position) {
        Score score = scores.get(position);

        Log.d(TAG, "Binding score at position " + position +
                ": " + score.getDifficulty() + ", " + score.getTime() + "s");

        holder.tvDifficulty.setText(score.getDifficulty());
        holder.tvTime.setText("Time: " + score.getTime() + "s");
        holder.tvDate.setText(score.getDate());
    }

    /**
     * Returns the total number of items in the list.
     */
    @Override
    public int getItemCount() {
        return scores != null ? scores.size() : 0;
    }

    /**
     * A ViewHolder describes a single score item view.
     * Holds references to the TextViews in item_score.xml.
     */
    static class ScoreViewHolder extends RecyclerView.ViewHolder {

        /** TextViews showing the score data */
        TextView tvDifficulty, tvTime, tvDate;

        /**
         * Constructor — binds the layout views to variables
         *
         * @param itemView The root view of the score item layout
         */
        public ScoreViewHolder(View itemView) {
            super(itemView);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDate = itemView.findViewById(R.id.tvDate);
            Log.d(TAG, "ScoreViewHolder created");
        }
    }
}
