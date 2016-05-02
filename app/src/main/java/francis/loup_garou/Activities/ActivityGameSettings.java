package francis.loup_garou.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import francis.loup_garou.R;
import francis.loup_garou.Roles;

public class ActivityGameSettings extends Activity {
    SeekBar seekBarLoup, seekBarVoyante, seekBarSorciere, seekBarChasseur, seekBarPetiteFille, seekBarVoleur;
    TextView nbLoupTxt, nbVoyanteTxt, nbSorciereTxt, nbChasseurTxt, nbPetiteFilleTxt, nbVoleurTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_settings);
    }

    @Override
    protected void onStart() {
        super.onStart();

        seekBarLoup = (SeekBar) findViewById(R.id.seekBarLoup);
        nbLoupTxt = (TextView) findViewById(R.id.nbLoupTxt);
        setSeekBar(seekBarLoup,nbLoupTxt,5);

        seekBarVoyante = (SeekBar) findViewById(R.id.seekBarVoyante);
        nbVoyanteTxt = (TextView) findViewById(R.id.nbVoyanteTxt);
        setSeekBar(seekBarVoyante,nbVoyanteTxt,5);

        seekBarSorciere = (SeekBar) findViewById(R.id.seekBarSorciere);
        nbSorciereTxt = (TextView) findViewById(R.id.nbSorciereTxt);
        setSeekBar(seekBarSorciere,nbSorciereTxt,5);

        seekBarVoleur = (SeekBar) findViewById(R.id.seekBarVoleur);
        nbVoleurTxt = (TextView) findViewById(R.id.nbVoleurTxt);
        setSeekBar(seekBarVoleur,nbVoleurTxt,5);

        seekBarChasseur = (SeekBar) findViewById(R.id.seekBarChasseur);
        nbChasseurTxt = (TextView) findViewById(R.id.nbChasseurTxt);
        setSeekBar(seekBarChasseur,nbChasseurTxt,5);

        seekBarPetiteFille = (SeekBar) findViewById(R.id.seekBarPetiteFille);
        nbPetiteFilleTxt = (TextView) findViewById(R.id.nbPetiteFilleTxt);
        setSeekBar(seekBarPetiteFille,nbPetiteFilleTxt,5);



    }

    public void setSeekBar(SeekBar seekBar, final TextView textView, int nb) {
        seekBar.setProgress(nb);
        textView.setText("" + seekBar.getProgress());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText("" + seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }
}