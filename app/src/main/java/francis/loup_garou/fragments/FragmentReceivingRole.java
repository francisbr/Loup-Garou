package francis.loup_garou.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import francis.loup_garou.Game;
import francis.loup_garou.MainActivity;
import francis.loup_garou.R;
import francis.loup_garou.Roles;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentReceivingRole extends Fragment {

    View view;
    TextView txtviewRole, descrRole;
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
        descrRole = (TextView) view.findViewById(R.id.descriptionRole);


        switch (role){
            case LoupGarou:
                txtviewRole.setText(R.string.loup_garou);
                imgRole.setImageResource(R.drawable.loup_garou);
                descrRole.setText(R.string.descriptionLoup);
                break;
            case Voyante:
                txtviewRole.setText(R.string.voyante);
                imgRole.setImageResource(R.drawable.voyante);
                descrRole.setText(R.string.descriptionVoyante);
                break;
            case Chasseur:
                txtviewRole.setText(R.string.chasseur);
                imgRole.setImageResource(R.drawable.chasseur);
                descrRole.setText(R.string.descriptionChasseur);
                break;
            case Cupidon:
                txtviewRole.setText(R.string.cupidon);
                imgRole.setImageResource(R.drawable.cupidon);
                descrRole.setText(R.string.descriptionCupidon);
                break;
            case Sorciere:
                txtviewRole.setText(R.string.sorciere);
                imgRole.setImageResource(R.drawable.sorciere);
                descrRole.setText(R.string.descriptionSorciere);
                break;
            case PetiteFille:
                txtviewRole.setText(R.string.petite_fille);
                imgRole.setImageResource(R.drawable.petite_fille);
                descrRole.setText(R.string.descriptionPetiteFille);
                break;
            case Voleur:
                txtviewRole.setText(R.string.voleur);
                imgRole.setImageResource(R.drawable.voleur);
                descrRole.setText(R.string.descriptionVoleur);
                break;
            case Villageois:
                txtviewRole.setText(R.string.villageois);
                imgRole.setImageResource(R.drawable.villageois);
                descrRole.setText(R.string.descriptionVillageois);
                break;
            case Maitre:
                txtviewRole.setText(R.string.maitre);
                break;
            default:
                txtviewRole.setText(R.string.villageois);
                imgRole.setImageResource(R.drawable.villageois);
                descrRole.setText(R.string.descriptionVillageois);
                break;
        }

    }
}
