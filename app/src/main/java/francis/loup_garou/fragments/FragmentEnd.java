package francis.loup_garou.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import francis.loup_garou.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentEnd extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_end, container, false);

    }

    public void loupWin(){
        getView().findViewById(R.id.loupWin).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.villageWin).setVisibility(View.GONE);
    }
    public void villageWin(){
        getView().findViewById(R.id.loupWin).setVisibility(View.GONE);
        getView().findViewById(R.id.villageWin).setVisibility(View.VISIBLE);
    }
}
