package francis.loup_garou.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import francis.loup_garou.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentMaitre extends Fragment {
    public static TextView mDebugInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_maitre_du_jeu, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Debug text view
        mDebugInfo = (TextView) getView().findViewById(R.id.logTxtView);
        mDebugInfo.setMovementMethod(new ScrollingMovementMethod());


        mDebugInfo.append("\n" + "Start");
    }


}
