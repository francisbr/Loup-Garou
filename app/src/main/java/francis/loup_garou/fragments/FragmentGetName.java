package francis.loup_garou.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;

import francis.loup_garou.MainActivity;
import francis.loup_garou.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentGetName extends Fragment {
    View view;

    public EditText textNom;
    public CheckBox rememberMe;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_get_name, container, false);
        return view;
    }

    public void setSavedName(SharedPreferences loginPreferences) {
        textNom = (EditText) view.findViewById(R.id.txtUsername);
        rememberMe = (CheckBox) view.findViewById(R.id.rememberMeCheckBox);

        textNom.setText(loginPreferences.getString("username", ""));
        rememberMe.setChecked(true);
        textNom.setSelection(textNom.getText().length());
    }
}
