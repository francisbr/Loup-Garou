package francis.loup_garou.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import francis.loup_garou.R;
import francis.loup_garou.Roles;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentReceivingRole extends Fragment {
    View view;
    TextView txtviewRole;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_receiving_role, container, false);

        return view;
    }

    public void changeTextRole(Roles role){
        int id= R.id.txtviewRole;
        txtviewRole = (TextView) view.findViewById(id);


        Log.d("I got", "" + role);
        switch (role){
            case LoupGarou:
                Log.d("writting", "Loup-Garou");
                txtviewRole.setText(R.string.loup_garou);
                break;
            case Voyante:
                Log.d("writting", "Voyante");
                txtviewRole.setText(R.string.voyante);
                break;
            case Chasseur:
                Log.d("writting", "Chasseur");
                txtviewRole.setText(R.string.chasseur);
                break;
            case Cupidon:
                Log.d("writting", "Cupidon");
                txtviewRole.setText(R.string.cupidon);
                break;
            case Sorciere:
                Log.d("writting", "Sorciere");
                txtviewRole.setText(R.string.sorciere);
                break;
            case PetiteFille:
                Log.d("writting", "Petite fille");
                txtviewRole.setText(R.string.petite_fille);
                break;
            case Voleur:
                Log.d("writting", "Voleur");
                txtviewRole.setText(R.string.voleur);
                break;
            case Villageois:
                Log.d("writting", "Villageois");
                txtviewRole.setText(R.string.villageois);
                break;
            case Maitre:
                Log.d("writting", "Maitre");
                txtviewRole.setText(R.string.maitre);
                break;
        }

    }
}
