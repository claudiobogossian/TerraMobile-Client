package br.inova.mobile.task;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import br.inpe.mobile.R;

public class ListTasksAdapter extends ArrayAdapter<String> {
        private final Context  context;
        private final String[] values;
        
        public ListTasksAdapter(Context context, String[] values) {
                super(context, R.layout.row_task_list, values);
                this.context = context;
                this.values = values;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                
                View rowView = inflater.inflate(R.layout.row_task_list, parent, false);
                TextView title = (TextView) rowView.findViewById(R.id.rowTaskTitle);
                ImageView imageView = (ImageView) rowView.findViewById(R.id.rowTaskImage);
                
                title.setText(values[position]);
                String s = values[position];
                
                if (s.startsWith("Windows7") || s.startsWith("iPhone") || s.startsWith("Solaris")) {
                        imageView.setImageResource(R.drawable.ico_cancelar);
                }
                else {
                        imageView.setImageResource(R.drawable.ico_ok);
                }
                
                return rowView;
        }
        
}
