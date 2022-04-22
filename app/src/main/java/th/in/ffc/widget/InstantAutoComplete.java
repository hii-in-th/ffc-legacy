package th.in.ffc.widget;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.text.Editable;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filterable;
import android.widget.ListAdapter;
import th.in.ffc.R;

public class InstantAutoComplete extends androidx.appcompat.widget.AppCompatAutoCompleteTextView {

    public InstantAutoComplete(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
        this.setEnabled(false);
    }


    @Override
    public boolean enoughToFilter() {
        return true;
    }


    @Override
    public <T extends ListAdapter & Filterable> void setAdapter(final T adapter) {
        super.setAdapter(adapter);
//        this.setEnabled(adapter != null);
        boolean post = this.post(new Runnable() {
            @Override
            public void run() {
                setEnabled(adapter != null);
            }

        });
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction,
                                  Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused && this.isEnabled()) {
            performFiltering(getText(), 0);
            showDropDown();
        }
    }

    public static ArrayAdapter<String> getAdapter(Context context, Cursor c, int index) {
        String[] array;
        if (c.moveToFirst()) {
            array = new String[c.getCount()];
            int count = 0;
            do {
                array[count++] = c.getString(index);

            } while (c.moveToNext());
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    context, R.layout.default_spinner_item, array);
            return adapter;
        }
        return null;
    }

}
