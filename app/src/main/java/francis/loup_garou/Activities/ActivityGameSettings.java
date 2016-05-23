package francis.loup_garou.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import francis.loup_garou.R;

public class ActivityGameSettings extends AppCompatActivity {
    SeekBar seekBarLoup, seekBarVoyante, seekBarSorciere, seekBarChasseur, seekBarPetiteFille, seekBarVoleur;
    Switch switchCupid, switchCustomSettings;
    TextView nbLoupTxt, nbVoyanteTxt, nbSorciereTxt, nbChasseurTxt, nbPetiteFilleTxt, nbVoleurTxt;
    int maxPlayer = 0, nbCupid = 0;

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

        maxPlayer = getIntent().getIntExtra("nbPlayer", 0);
        TextView textView = (TextView) findViewById(R.id.nbJoueurTxt);
        textView.setText("" + maxPlayer + " " + R.string.player);

        setSwitches();
        changesMaxs();


    }

    private void changesMaxs() {
        int currentSum = seekBarChasseur.getProgress() + seekBarLoup.getProgress() + seekBarSorciere.getProgress() + seekBarVoyante.getProgress() + seekBarPetiteFille.getProgress() + seekBarVoleur.getProgress() + nbCupid;
        seekBarLoup.setMax(maxPlayer - currentSum + seekBarLoup.getProgress());
        seekBarChasseur.setMax(maxPlayer - currentSum + seekBarChasseur.getProgress());
        seekBarSorciere.setMax(maxPlayer - currentSum + seekBarSorciere.getProgress());
        seekBarVoyante.setMax(maxPlayer - currentSum + seekBarVoyante.getProgress());
        seekBarPetiteFille.setMax(maxPlayer - currentSum + seekBarPetiteFille.getProgress());
        seekBarVoleur.setMax(maxPlayer - currentSum + seekBarVoleur.getProgress());

        if (currentSum >= maxPlayer) {
            if (!switchCupid.isChecked())
                switchCupid.setClickable(false);
            else {
                switchCupid.setClickable(true);
            }
        }

        TextView txtView = (TextView) (findViewById(R.id.nbVillagerTxt));
        txtView.setText("" + (maxPlayer - currentSum) + " " + R.string.villager);
    }

    public void setSwitches() {
        switchCustomSettings = (Switch) findViewById(R.id.switchEnableCustomSettings);
        switchCupid = (Switch) findViewById(R.id.switchCupid);

        switchCustomSettings.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    intent.putExtra("useCustom", true);
                    setResult(RESULT_OK, intent);
                    enableModifications(true);
                } else {
                    intent.putExtra("useCustom", false);
                    setResult(RESULT_OK, intent);
                    enableModifications(false);
                }
            }
        });
        switchCustomSettings.setChecked(getIntent().getBooleanExtra("useCustom", false));

        switchCupid.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    nbCupid = 1;
                    intent.putExtra("nbCupid", 1);
                    setResult(RESULT_OK, intent);
                    changesMaxs();
                } else {
                    nbCupid = 0;
                    intent.putExtra("nbCupid", 0);
                    setResult(RESULT_OK, intent);
                    changesMaxs();
                }
            }
        });
        switchCupid.setChecked(getIntent().getBooleanExtra("haveCupid", false));

        enableModifications(getIntent().getBooleanExtra("useCustom", false));
    }

    private void enableModifications(boolean b) {
        switchCupid.setEnabled(b);
        seekBarLoup.setEnabled(b);
        seekBarChasseur.setEnabled(b);
        seekBarVoyante.setEnabled(b);
        seekBarSorciere.setEnabled(b);
        seekBarPetiteFille.setEnabled(b);
        seekBarVoleur.setEnabled(b);
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
                changesMaxs();
            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        MenuItem mnu = menu.findItem(R.id.btnSave);
        mnu.isVisible();
        return true;
    }

    public void save(MenuItem item) {
        finish();
    }
}