package francis.loup_garou.Activities;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Connections;

import francis.loup_garou.Game;
import francis.loup_garou.MainActivity;
import francis.loup_garou.R;
import francis.loup_garou.Roles;

public class ActivityVoyante extends Activity {

    private String nom, roleString;
    private Roles role;
    //View view;
    private TextView txtviewRole, txtviewNom;
    private ImageView imgRole;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_voyante);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        nom = getIntent().getExtras().getString("name");
        roleString = getIntent().getExtras().getString("role");

        changeName();
        setRole();
        changeTextRole(role);
    }
    public void setRole(){
        switch (roleString){
            case "LoupGarou":
                role = Roles.LoupGarou;
                break;
            case "Voyante":
                role = Roles.Voyante;
                break;
            case "Chasseur":
                role = Roles.Chasseur;
                break;
            case "Cupidon":
                role = Roles.Cupidon;
                break;
            case "Sorciere":
                role = Roles.Sorciere;
                break;
            case "PetiteFille":
                role = Roles.PetiteFille;
                break;
            case "Voleur":
                role = Roles.Voleur;
                break;
            case "Villageois":
                role = Roles.Villageois;
                break;
            case "Maitre":
                role = Roles.Maitre;
                break;
            default:
                role = Roles.Villageois;
                break;
        }

    }
    public void changeName(){
        txtviewNom = (TextView) this.findViewById(R.id.txtviewNom);
        txtviewNom.setText(nom);
    }
    public void changeTextRole(Roles role){
        int id = R.id.imgRole;
        txtviewRole = (TextView) this.findViewById(R.id.txtviewRole);
        imgRole = (ImageView) this.findViewById(id);

        try{
            role.equals(Roles.Chasseur);
        } catch (NullPointerException e){
            role = Roles.Villageois;
        }

        switch (role){
            case LoupGarou:
                txtviewRole.setText(R.string.loup_garou);
                imgRole.setImageResource(R.drawable.loup_garou);
                break;
            case Voyante:
                txtviewRole.setText(R.string.voyante);
                imgRole.setImageResource(R.drawable.voyante);
                break;
            case Chasseur:
                txtviewRole.setText(R.string.chasseur);
                imgRole.setImageResource(R.drawable.chasseur);
                break;
            case Cupidon:
                txtviewRole.setText(R.string.cupidon);
                imgRole.setImageResource(R.drawable.cupidon);
                break;
            case Sorciere:
                txtviewRole.setText(R.string.sorciere);
                imgRole.setImageResource(R.drawable.sorciere);
                break;
            case PetiteFille:
                txtviewRole.setText(R.string.petite_fille);
                imgRole.setImageResource(R.drawable.petite_fille);
                break;
            case Voleur:
                txtviewRole.setText(R.string.voleur);
                imgRole.setImageResource(R.drawable.voleur);
                break;
            case Villageois:
                txtviewRole.setText(R.string.villageois);
                imgRole.setImageResource(R.drawable.villageois);
                break;
            case Maitre:
                txtviewRole.setText(R.string.maitre);
                break;
            default:
                txtviewRole.setText(R.string.villageois);
                imgRole.setImageResource(R.drawable.villageois);
                break;
        }
    }
    public void btnOk(View view){
        finish();
    }
}
