package dbxprts.terminaltrak;

import android.content.Context;
import android.content.res.Resources;
import android.widget.Filter;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Diego on 30/01/2017.
 */

public class CustomAdapter extends SimpleAdapter {

    int resource;
    int[] to;

    List<HashMap<String, String>> data, mUnfilteredData;
    String[] from;

    Context context;
    Resources res;
    Filter filter;

    /*
     * A custom adapter which implements custom filtering
     */
    public CustomAdapter(Context context,
                         List<HashMap<String, String>> data, int resource, String[] from, int[] to) {

        super(context, data, resource, from, to);
        this.context = context;
        this.resource = resource;
        this.data = data;
        this.from = from;
        this.to = to;
        this.res = context.getResources();

    }

    @Override
    public Filter getFilter() {
        if(filter != null) return filter;
        else return filter = new CustomFilter();
    }

    /*
     * A custom filter which is copied from the simplefilter in simpleadapter.
     * Changed word check from .startswith to .contains
     */
    private class CustomFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mUnfilteredData == null) {
                mUnfilteredData = new ArrayList<HashMap<String, String>>(data);
            }

            if (prefix == null || prefix.length() == 0) {
                List<HashMap<String, String>> list = mUnfilteredData;
                results.values = list;
                results.count = list.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();

                List<HashMap<String, String>> unfilteredValues = mUnfilteredData;
                int count = unfilteredValues.size();

                ArrayList<Map<String, ?>> newValues = new ArrayList<Map<String, ?>>(count);

                for (int i = 0; i < count; i++) {
                    Map<String, ?> h = unfilteredValues.get(i);
                    if (h != null) {

                        int len = to.length;

                        for (int j=0; j<len; j++) {
                            String str =  (String)h.get(from[j]);

                            String[] words = str.split(" ");
                            int wordCount = words.length;

                            for (int k = 0; k < wordCount; k++) {
                                String word = words[k];

                                if (word.toLowerCase().contains(prefixString)) {
                                    newValues.add(h);
                                    break;
                                }
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            data.clear();
            data.addAll((List<HashMap<String, String>>) results.values);
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
