package br.inova.mobile.task;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class ListTasks extends ListActivity {
        
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                initializeListView();
        }
        
        public void initializeListView() {
                String[] values = new String[] { "Android", "iPhone", "WindowsMobile", "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2" };
                ListTasksAdapter adapter = new ListTasksAdapter(this, values);
                setListAdapter(adapter);
        }
        
        @Override
        protected void onListItemClick(ListView l, View v, int position, long id) {
                String item = (String) getListAdapter().getItem(position);
                Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
        }
}
