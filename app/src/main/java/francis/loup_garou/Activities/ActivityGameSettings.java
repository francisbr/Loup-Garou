package francis.loup_garou.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import francis.loup_garou.R;
import francis.loup_garou.Roles;

public class ActivityGameSettings extends Activity {
    SeekBar seekBarLoup, seekBarVoyante, seekBarSorciere, seekBarChasseur, seekBarPetiteFille, seekBarVoleur;
    TextView nbLoupTxt, nbVoyanteTxt, nbSorciereTxt, nbChasseurTxt, nbPetiteFilleTxt, nbVoleurTxt;
    int maxPlayer = 0;

    Intent intent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_game_settings);

        intent.putExtra("MyData", "Ric suce");
        setResult(RESULT_OK, intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        seekBarLoup = (SeekBar) findViewById(R.id.seekBarLoup);
        nbLoupTxt = (TextView) findViewById(R.id.nbLoupTxt);
        setSeekBar(seekBarLoup, nbLoupTxt, getIntent().getExtras().getInt("nbLoup"));

        seekBarVoyante = (SeekBar) findViewById(R.id.seekBarVoyante);
        nbVoyanteTxt = (TextView) findViewById(R.id.nbVoyanteTxt);
        setSeekBar(seekBarVoyante, nbVoyanteTxt, getIntent().getExtras().getInt("nbVoyante"));

        seekBarSorciere = (SeekBar) findViewById(R.id.seekBarSorciere);
        nbSorciereTxt = (TextView) findViewById(R.id.nbSorciereTxt);
        setSeekBar(seekBarSorciere, nbSorciereTxt, getIntent().getExtras().getInt("nbSorciere"));

        seekBarVoleur = (SeekBar) findViewById(R.id.seekBarVoleur);
        nbVoleurTxt = (TextView) findViewById(R.id.nbVoleurTxt);
        setSeekBar(seekBarVoleur, nbVoleurTxt, getIntent().getExtras().getInt("nbVoleur"));

        seekBarChasseur = (SeekBar) findViewById(R.id.seekBarChasseur);
        nbChasseurTxt = (TextView) findViewById(R.id.nbChasseurTxt);
        setSeekBar(seekBarChasseur, nbChasseurTxt, getIntent().getExtras().getInt("nbChasseur"));

        seekBarPetiteFille = (SeekBar) findViewById(R.id.seekBarPetiteFille);
        nbPetiteFilleTxt = (TextView) findViewById(R.id.nbPetiteFilleTxt);
        setSeekBar(seekBarPetiteFille, nbPetiteFilleTxt, getIntent().getExtras().getInt("nbPetiteFille"));


        intent.putExtra("nbLoup", seekBarLoup.getProgress());
        setResult(RESULT_OK, intent);
        intent.putExtra("nbSorciere", seekBarSorciere.getProgress());
        setResult(RESULT_OK, intent);
        intent.putExtra("nbVoyante", seekBarVoyante.getProgress());
        setResult(RESULT_OK, intent);
        intent.putExtra("nbChasseur", seekBarChasseur.getProgress());
        setResult(RESULT_OK, intent);
        intent.putExtra("nbPetiteFille", seekBarPetiteFille.getProgress());
        setResult(RESULT_OK, intent);
        intent.putExtra("nbVoleur", seekBarVoleur.getProgress());
        setResult(RESULT_OK, intent);


        Switch switchCustomSettings = (Switch) findViewById(R.id.switchEnableCustomSettings);
        switchCustomSettings.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    intent.putExtra("useCustom", true);
                    setResult(RESULT_OK, intent);
                } else {
                    intent.putExtra("useCustom", false);
                    setResult(RESULT_OK, intent);
                }
            }
        });
        switchCustomSettings.setChecked(getIntent().getBooleanExtra("useCustom", false));
        maxPlayer = getIntent().getIntExtra("nbPlayer", 0);

        changeSeekBarsMax();
    }

    private void changeSeekBarsMax() {
        int currentSum = seekBarChasseur.getProgress() + seekBarLoup.getProgress() + seekBarSorciere.getProgress() + seekBarVoyante.getProgress() + seekBarPetiteFille.getProgress() + seekBarVoleur.getProgress();
        Log.d("maxPlayer", "" + maxPlayer);
        Log.d("CurrentSum", "" + currentSum);
        seekBarLoup.setMax(maxPlayer - currentSum);
        seekBarChasseur.setMax(maxPlayer - currentSum);
        seekBarSorciere.setMax(maxPlayer - currentSum);
        seekBarVoyante.setMax(maxPlayer - currentSum);
        seekBarPetiteFille.setMax(maxPlayer - currentSum);
        seekBarVoleur.setMax(maxPlayer - currentSum);
    }

    public void setSeekBar(SeekBar seekBar, final TextView textView, int nb) {
        seekBar.setProgress(nb);
        textView.setText("" + seekBar.getProgress());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText("" + seekBar.getProgress());
                if (seekBar == seekBarLoup) {
                    intent.putExtra("nbLoup", seekBar.getProgress());
                } else if (seekBar == seekBarChasseur) {
                    intent.putExtra("nbChasseur", seekBar.getProgress());
                } else if (seekBar == seekBarSorciere) {
                    intent.putExtra("nbSorciere", seekBar.getProgress());
                } else if (seekBar == seekBarVoyante) {
                    intent.putExtra("nbVoyante", seekBar.getProgress());
                } else if (seekBar == seekBarPetiteFille) {
                    intent.putExtra("nbPetiteFille", seekBar.getProgress());
                } else if (seekBar == seekBarVoleur) {
                    intent.putExtra("nbVoleur", seekBar.getProgress());
                }

                setResult(RESULT_OK, intent);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                changeSeekBarsMax();
            }
        });

    }
}