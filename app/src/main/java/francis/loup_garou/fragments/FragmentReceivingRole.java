package francis.loup_garou.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import francis.loup_garou.R;
import francis.loup_garou.Roles;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentReceivingRole extends Fragment {

    View view;
    TextView txtviewRole;
    ImageView imgRole;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_receiving_role, container, false);

        return view;
    }

    public void changeTextRole(Roles role){
        int id = R.id.imgRole;
        txtviewRole = (TextView) view.findViewById(R.id.txtviewRole);
        imgRole = (ImageView) view.findViewById(id);


        Log.d("I got", "" + role);
        switch (role){
            case LoupGarou:
                Log.d("writting", "Loup-Garou");
                txtviewRole.setText(R.string.loup_garou);
                imgRole.setImageResource(R.drawable.loup_garou);
                break;
            case Voyante:
                Log.d("writting", "Voyante");
                txtviewRole.setText(R.string.voyante);
                imgRole.setImageResource(R.drawable.voyante);
                break;
            case Chasseur:
                Log.d("writting", "Chasseur");
                txtviewRole.setText(R.string.chasseur);
                imgRole.setImageResource(R.drawable.chasseur);
                break;
            case Cupidon:
                Log.d("writting", "Cupidon");
                txtviewRole.setText(R.string.cupidon);
                imgRole.setImageResource(R.drawable.cupidon);
                break;
            case Sorciere:
                Log.d("writting", "Sorciere");
                txtviewRole.setText(R.string.sorciere);
                imgRole.setImageResource(R.drawable.sorciere);
                break;
            case PetiteFille:
                Log.d("writting", "Petite fille");
                txtviewRole.setText(R.string.petite_fille);
                imgRole.setImageResource(R.drawable.petite_fille);
                break;
            case Voleur:
                Log.d("writting", "Voleur");
                txtviewRole.setText(R.string.voleur);
                imgRole.setImageResource(R.drawable.voleur);
                break;
            case Villageois:
                Log.d("writting", "Villageois");
                txtviewRole.setText(R.string.villageois);
                imgRole.setImageResource(R.drawable.villageois);
                break;
            case Maitre:
                Log.d("writting", "Maitre");
                txtviewRole.setText(R.string.maitre);
                break;
        }

    }
}
