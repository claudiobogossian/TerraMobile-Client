/**
 * This Source-code are based on the https://github.com/jeremynealbrown/makemachine
 * */

package br.inova.mobile.form.generator;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class FormCheckBox extends FormWidget {
        
        class ChangeHandler implements CompoundButton.OnCheckedChangeListener {
                protected FormWidget _widget;
                
                public ChangeHandler(FormWidget widget) {
                        _widget = widget;
                }
                
                public void onCheckedChanged(
                                             CompoundButton buttonView,
                                             boolean isChecked) {
                        if (_handler != null) {
                                _handler.toggle(_widget);
                        }
                }
                
        }
        
        protected int      _priority;
        
        protected CheckBox _checkbox;
        
        public FormCheckBox(Context context, String property) {
                super(context, property);
                
                _checkbox = new CheckBox(context);
                _checkbox.setText(this.getDisplayText());
                
                _layout.addView(_checkbox);
        }
        
        @Override
        public String getValue() {
                return String.valueOf(_checkbox.isChecked() ? "1" : "0");
        }
        
        @Override
        public void setToggleHandler(
                                     FormActivity.FormWidgetToggleHandler handler) {
                super.setToggleHandler(handler);
                _checkbox.setOnCheckedChangeListener(new ChangeHandler(this));
        }
        
        public void setValue(String value) {
                _checkbox.setChecked(value.equals("1"));
        }
}
