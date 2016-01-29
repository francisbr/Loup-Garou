package francis.loup_garou.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;

import francis.loup_garou.Game;
import francis.loup_garou.MainActivity;
import francis.loup_garou.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentLoupGarou extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_loup_garou, container, false);
    }

    public void updateList() {

        ListView listVote = (ListView) getView().findViewById(R.id.listPlayersToKill);

        for (int i = 0; i < Game.playersAliveIDs.size(); i++) {
            if (Game.playersAliveIDs.get(i).equals(Game.loupIDs.get(i))) {
                Log.d("removing wolf","" + Game.playersAliveNames.get(i) + Game.playersAliveIDs.get(i));
                Game.playersAliveNames.remove(i);
                Game.playersAliveIDs.remove(i);

            }
        }

        listVote.setAdapter(MainActivity.adapterAliveNames);
        MainActivity.adapterAliveNames.notifyDataSetChanged();
    }
}
