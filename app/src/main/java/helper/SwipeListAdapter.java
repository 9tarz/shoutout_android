package helper;

/**
 * Created by nullnil on 10/12/15.
 */
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import com.example.nullnil.shoutout.R;

public class SwipeListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<Post> postList;
    private String[] bgColors;

    public SwipeListAdapter(Activity activity, List<Post> movieList) {
        this.activity = activity;
        this.postList = movieList;
        bgColors = activity.getApplicationContext().getResources().getStringArray(R.array.posts_bg);
    }

    @Override
    public int getCount() {
        return postList.size();
    }

    @Override
    public Object getItem(int location) {
        return postList.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_row, null);

        TextView text = (TextView) convertView.findViewById(R.id.text);

        text.setText(postList.get(position).text);

        String color = bgColors[position % bgColors.length];

        return convertView;
    }

}
