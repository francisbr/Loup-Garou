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
public class FragmentDayCycle extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_day_cycle, container, false);
    }

    public void showDay(){

        getView().findViewById(R.id.layoutDay).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.layoutNight).setVisibility(View.GONE);

    }

    public void showNight(){

        getView().findViewById(R.id.layoutNight).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.layoutDay).setVisibility(View.GONE);

    }
}
