package francis.loup_garou.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import francis.loup_garou.MainActivity;
import francis.loup_garou.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentMaitre extends Fragment {
    public static TextView mDebugInfo;
    public static Button btnJour, btnNuit, btnVote, btnLoup, btnVoyante, btnVoleur, btnCupidon, btnSorciere, btnCapitain;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_maitre_du_jeu, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        //ALL buttons
        btnJour = (Button) getView().findViewById(R.id.btnJour);
        btnNuit = (Button) getView().findViewById(R.id.btnNuit);
        btnVote = (Button) getView().findViewById(R.id.btnVote);
        btnLoup = (Button) getView().findViewById(R.id.btnLoup);
        btnVoyante = (Button) getView().findViewById(R.id.btnVoyante);
        btnVoleur = (Button) getView().findViewById(R.id.btnVoleur);
        btnCupidon = (Button) getView().findViewById(R.id.btnCupidon);
        btnSorciere = (Button) getView().findViewById(R.id.btnSorciere);
        btnCapitain = (Button) getView().findViewById((R.id.btnCapitain));

        // Debug text view
        mDebugInfo = (TextView) getView().findViewById(R.id.logTxtView);
        mDebugInfo.setMovementMethod(new ScrollingMovementMethod());

        MainActivity.showLogs("-- START --");

        enableButtons(false);
    }


    public static void enableButtons(boolean var) {
        if(var){
            btnJour.setEnabled(true);
            btnNuit.setEnabled(true);
            btnVote.setEnabled(true);
            btnLoup.setEnabled(true);
            btnVoyante.setEnabled(true);
            btnVoleur.setEnabled(true);
            btnCupidon.setEnabled(true);
            btnSorciere.setEnabled(true);
            btnCapitain.setEnabled(true);
        } else {
            btnJour.setEnabled(false);
            btnNuit.setEnabled(false);
            btnVote.setEnabled(false);
            btnLoup.setEnabled(false);
            btnVoyante.setEnabled(false);
            btnVoleur.setEnabled(false);
            btnCupidon.setEnabled(false);
            btnSorciere.setEnabled(false);
            btnCapitain.setEnabled(false);
        }

    }
}
