package machine.microspin.com.flyerConsole;

import android.widget.EditText;
import android.util.AttributeSet;
import android.content.Context;
import android.view.KeyEvent;


public class EditTextCustom extends EditText {

    public EditTextCustom(Context context){
        super(context);
    }

    public EditTextCustom(Context context, AttributeSet attrs){
        super(context,attrs);
    }

    public EditTextCustom(Context context,AttributeSet attrs, int defStyleAttrs){
        super(context,attrs,defStyleAttrs);
    }

    public void init() {
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode== KeyEvent.KEYCODE_BACK && event.getAction()== KeyEvent.ACTION_UP){

            this.clearFocus();
        }
        return super.onKeyPreIme(keyCode, event);
    }

}
