/**
 * This Source-code are based on the https://github.com/jeremynealbrown/makemachine
 * */
package br.inova.mobile.form.generator;

import android.content.Context;
import android.widget.Button;

public class FormButton extends FormWidget {
        protected Button _button;
        protected int    _priority;
        
        public FormButton(Context context, String property) {
                super(context, property);
                
                _button = new Button(context);
                _button.setText(property);
                
                _layout.addView(_button);
        }
        
        /**
         * Returns the instance of the button.
         * */
        public Button getButton() {
                return _button;
        }
}
