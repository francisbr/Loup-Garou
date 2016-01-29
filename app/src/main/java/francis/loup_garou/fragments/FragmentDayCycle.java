package francis.loup_garou.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import francis.loup_garou.Game;
import francis.loup_garou.MainActivity;
import francis.loup_garou.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentDayCycle extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_day_cycle, container, false);
    }

    public void showDay(String nbLoupAlive){

        Log.d("showDay", "");

        //NB LOUP
        TextView tx = (TextView) getView().findViewById(R.id.nbLoupAliveTxtview);
        tx.setText(nbLoupAlive);


        //LISTE ALIVE
        ListView listViewAlive = (ListView) getView().findViewById(R.id.listPlayersAlive);

        Log.d("playersAliveNames.size", "" + Game.playersAliveNames.size());
        for (int i = 0 ; i < Game.playersAliveNames.size() ; i++){
            Log.d("playersAliveNames " + i, Game.playersAliveNames.get(i));
        }

        listViewAlive.setAdapter(MainActivity.adapterAliveNames);
        MainActivity.adapterAliveNames.notifyDataSetChanged();



        //AFFICHE LE LAYOUT
        getView().findViewById(R.id.layoutDay).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.layoutNight).setVisibility(View.GONE);

    }

    public void showNight(){

        getView().findViewById(R.id.layoutNight).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.layoutDay).setVisibility(View.GONE);

    }
}
