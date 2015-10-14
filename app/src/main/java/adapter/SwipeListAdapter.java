package adapter;

/**
 * Created by nullnil on 10/12/15.
 */
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import com.example.nullnil.shoutout.R;
import com.github.curioustechizen.ago.RelativeTimeTextView;

import helper.Post;

public class SwipeListAdapter extends RecyclerView.Adapter<SwipeListAdapter.ViewHolder> {

    private List<Post> postList;
    private Context mContext;

    public SwipeListAdapter(Context context, List<Post> postList) {
        this.mContext = context;
        this.postList = postList;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_row, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.text.setText(post.text);
        holder.username.setText(post.username);
        holder.timestamp.setReferenceTime(post.timestamp);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView text;
        public TextView username;
        public RelativeTimeTextView timestamp;

        public ViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.text);
            username = (TextView) itemView.findViewById(R.id.username);
            timestamp = (RelativeTimeTextView) itemView.findViewById(R.id.timestamp);
        }
    }

}
