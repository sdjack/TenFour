package com.steve-jackson-studios.tenfour.Chat;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.R;

/**
 * Created by sjackson on 2/9/2017.
 * ChatCtreateDialog
 */

public class EventCreateDialog extends DialogFragment {

    private EditText inputField;
    private Spinner spinner;
    private int selectedCategory = 1;
    private CallbackListener callbackListener;

    /**
     * @param callbackListener the listener
     */
    public void setCallbackListener(CallbackListener callbackListener) {
        this.callbackListener = callbackListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.chat_create_layout, container, false);
        this.inputField = (EditText) view.findViewById(R.id.create_title_field);
        this.inputField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard();
                }
            }
        });

        this.spinner = (Spinner) view.findViewById(R.id.create_category_spinner);
        CategoryTypeArrayAdapter adapter = new CategoryTypeArrayAdapter(getActivity());
        this.spinner.setAdapter(adapter);
        this.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int pos, long arg3) {
                selectedCategory = spinner.getSelectedItemPosition()+1;
                hideKeyboard();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
                hideKeyboard();
            }
        });

        Button btn = (Button)view.findViewById(R.id.create_submit_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d("EventCreateDialog", "Submit button clicked");
                String title = inputField.getText().toString();
                callbackListener.onChatCreated(title, selectedCategory);
            }
        });
        Button cancel = (Button)view.findViewById(R.id.create_cancel_button);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        LinearLayout frame = (LinearLayout) view.findViewById(R.id.create_frame);
        frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }
        });

        this.inputField.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private class CategoryType {
        private int id;
        private String name;
        private int drawableId;

        public CategoryType(int id, String name, int drawableId) {
            super();
            this.id = id;
            this.name = name;
            this.drawableId = drawableId;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getDrawableId() {
            return drawableId;
        }
    }

    private class CategoryTypeArrayAdapter extends ArrayAdapter<CategoryType> {

        public CategoryTypeArrayAdapter(Context context) {
            super(context, R.layout.spinner_item);
            String[] categories = getResources().getStringArray(R.array.categories_array);
            for (int i=0; i < categories.length; i++) {
                String name = categories[i];
                //Log.d("ChatCtreateDialog", name);
                add(new CategoryType(i, name, AppConstants.CATEGORY_ICONS[i+1]));
            }
            setDropDownViewResource(R.layout.spinner_dropdown_item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView label = (TextView) super.getView(position, convertView, parent);

            CategoryType category = getItem(position);
            label.setText(category.getName());
            int size = (int) getActivity().getResources().getDimension(R.dimen.category_size_large);
            Drawable img = ContextCompat.getDrawable(getActivity(), category.getDrawableId());
            img.setBounds(0, 0, size, size);
            label.setCompoundDrawables(null, null, img, null);

            return label;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            hideKeyboard();
            TextView label = (TextView) super.getDropDownView(position, convertView, parent);

            CategoryType category = getItem(position);
            label.setText(category.getName());

            int size = (int) getActivity().getResources().getDimension(R.dimen.category_size_small);
            Drawable img = ContextCompat.getDrawable(getActivity(), category.getDrawableId());
            img.setBounds(0, 0, size, size);
            label.setCompoundDrawables(null, null, img, null);

            return label;
        }
    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Interface definition for callbacks
     */
    public interface CallbackListener {
        void onChatCreated(String title, int category);
    }
}
