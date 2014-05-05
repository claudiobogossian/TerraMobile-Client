package br.inova.mobile.form.generator;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class FormGeneratorExample extends FormActivity {
        public static final int OPTION_SAVE     = 0;
        public static final int OPTION_POPULATE = 1;
        public static final int OPTION_CANCEL   = 2;
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                //generateForm(FormActivity.parseFileToString(this, "schemas.json"));
                generateForm(FormActivity.parseFileToString(this, "bauru.json"));
                save();
        }
        
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
                menu.add(0, OPTION_SAVE, 0, "Save");
                menu.add(0, OPTION_POPULATE, 0, "Populate");
                menu.add(0, OPTION_CANCEL, 0, "Cancel");
                return true;
        }
        
        @Override
        public boolean onMenuItemSelected(int id, MenuItem item) {
                
                switch (item.getItemId()) {
                        case OPTION_SAVE:
                                save();
                                break;
                        
                        case OPTION_POPULATE:
                                populate(FormActivity.parseFileToString(this, "data.json"));
                                break;
                        
                        case OPTION_CANCEL:
                                
                                break;
                }
                
                return super.onMenuItemSelected(id, item);
        }
}