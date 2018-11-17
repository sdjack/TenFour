package com.steve-jackson-studios.tenfour.Profile;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.AppResolver;
import com.steve-jackson-studios.tenfour.Misc.ResolverDialogFragment;
import com.steve-jackson-studios.tenfour.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by sjackson on 8/7/2017.
 * ProfileEditDialog
 */

public class ProfileEditDialog extends ResolverDialogFragment {
    private int selectedTownId = 1;
    private String selectedTownName = "";
    private String selectedState = "";
//    private String selectedFirstName = "";
//    private String selectedLastName = "";
    private Spinner hometownSpinner;
    private Spinner homestateSpinner;
    private HomestateTypeArrayAdapter stateAdapter;
//    private EditText firstName;
//    private EditText lastName;

    public static ProfileEditDialog newInstance(AppResolver appResolver) {

        ProfileEditDialog instance = new ProfileEditDialog();
        instance.setResolver(appResolver);

        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_edit_dialog, container, false);

//        this.firstName = (EditText) view.findViewById(R.id.edit_profile_firstname);
//        firstName.setText(UserData.FIRST_NAME);
//        firstName.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                selectedFirstName = s.toString();
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                String n = s.toString();
//                if (TextUtils.isEmpty(n)) {
//                    selectedFirstName = UserData.FIRST_NAME;
//                    firstName.setText(UserData.FIRST_NAME);
//                } else {
//                    selectedFirstName = n;
//                }
//            }
//        });
//        this.lastName = (EditText) view.findViewById(R.id.edit_profile_lastname);
//        lastName.setText(UserData.LAST_NAME);
//        lastName.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                selectedLastName = s.toString();
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                String n = s.toString();
//                if (TextUtils.isEmpty(n)) {
//                    selectedLastName = UserData.LAST_NAME;
//                    lastName.setText(UserData.LAST_NAME);
//                } else {
//                    selectedLastName = n;
//                }
//            }
//        });

        final HometownTypeArrayAdapter townAdapter = new HometownTypeArrayAdapter(getActivity());

        this.hometownSpinner = (Spinner) view.findViewById(R.id.hometown_spinner);
        this.hometownSpinner.setAdapter(townAdapter);
        this.hometownSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int pos, long arg3) {
                HometownType item = (HometownType) hometownSpinner.getItemAtPosition(pos);
                selectedTownId = item.id;
                selectedTownName = item.name;
                hideKeyboard();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
                hideKeyboard();
            }
        });

        final HomestateTypeArrayAdapter stateAdapter = new HomestateTypeArrayAdapter(getActivity());

        this.homestateSpinner = (Spinner) view.findViewById(R.id.homestate_spinner);
        this.homestateSpinner.setAdapter(stateAdapter);
        this.homestateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int pos, long arg3) {
                HomestateType item = (HomestateType) homestateSpinner.getItemAtPosition(pos);
                selectedState = item.name;
                townAdapter.updateData();
                hideKeyboard();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
                hideKeyboard();
            }
        });

        Button submitButton = (Button) view.findViewById(R.id.edit_profile_submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolver.saveProfileHometown(selectedTownName, selectedTownId);
                dismiss();
            }
        });

        Button cancel = (Button)view.findViewById(R.id.edit_profile_cancel_button);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

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

    private class HomestateType {
        final String name;

        public HomestateType(String name) {
            this.name = name;
        }
    }

    private class HometownType {
        final int id;
        final String name;

        public HometownType(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    private class HomestateTypeArrayAdapter extends ArrayAdapter<HomestateType> {

        public HomestateTypeArrayAdapter(Context context) {
            super(context, R.layout.spinner_item);
            for (Object o : AppConstants.GEOFENCE_NAMES.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                add(new HomestateType((String)entry.getKey()));
            }
            setDropDownViewResource(R.layout.spinner_dropdown_item);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            TextView label = (TextView) super.getView(position, convertView, parent);

            HomestateType item = getItem(position);
            if (item != null) {
                label.setText(item.name);
            }

            return label;
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            hideKeyboard();
            TextView label = (TextView) super.getDropDownView(position, convertView, parent);

            HomestateType item = getItem(position);
            if (item != null) {
                label.setText(item.name);
            }

            return label;
        }
    }

    private class HometownTypeArrayAdapter extends ArrayAdapter<HometownType> {

        public HometownTypeArrayAdapter(Context context) {
            super(context, R.layout.spinner_item);
            if (AppConstants.GEOFENCE_NAMES.get(selectedState) != null) {
                try {
                    TreeMap<String, JSONObject> source = AppConstants.GEOFENCE_NAMES.get(selectedState);
                    if (!source.isEmpty()) {
                        Set set = source.entrySet();
                        Iterator iterator = set.iterator();
                        while(iterator.hasNext()) {
                            Map.Entry mentry = (Map.Entry)iterator.next();
                            JSONObject data = (JSONObject)mentry.getValue();
                            add(new HometownType(data.getInt("ID"), data.getString("METRO")));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            setDropDownViewResource(R.layout.spinner_dropdown_item);
        }

        public void updateData() {
            clear();
            if (AppConstants.GEOFENCE_NAMES.get(selectedState) != null) {
                try {
                    TreeMap<String, JSONObject> source = AppConstants.GEOFENCE_NAMES.get(selectedState);
                    if (!source.isEmpty()) {
                        Set set = source.entrySet();
                        Iterator iterator = set.iterator();
                        while(iterator.hasNext()) {
                            Map.Entry mentry = (Map.Entry)iterator.next();
                            JSONObject data = (JSONObject)mentry.getValue();
                            add(new HometownType(data.getInt("ID"), data.getString("METRO")));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            TextView label = (TextView) super.getView(position, convertView, parent);

            HometownType item = getItem(position);
            if (item != null) {
                label.setText(item.name);
            }

            return label;
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            hideKeyboard();
            TextView label = (TextView) super.getDropDownView(position, convertView, parent);

            HometownType item = getItem(position);
            if (item != null) {
                label.setText(item.name);
            }

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
}
