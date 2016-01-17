package francis.loup_garou.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import francis.loup_garou.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentMaitre extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maitre_du_jeu, container, false);

    }
}
