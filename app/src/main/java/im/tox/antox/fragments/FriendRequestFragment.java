package im.tox.antox.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import im.tox.antox.R;
import im.tox.antox.activities.MainActivity;
import im.tox.antox.data.AntoxDB;
import im.tox.antox.tox.ToxService;
import im.tox.antox.tox.ToxSingleton;
import im.tox.antox.utils.Constants;

/**
 * Created by ollie on 28/02/14.
 */
public class FriendRequestFragment extends Fragment {

    private String key;
    private String message;

    ToxSingleton toxSingleton = ToxSingleton.getInstance();

    public FriendRequestFragment() {

    }

    public FriendRequestFragment(String key, String message) {
        this.key = key;
        this.message = message;
    }

    private String SplitKey(String key) {
        return key.substring(0, 38) + "\n" + key.substring(38);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friendrequest, container, false);
        TextView k = (TextView) rootView.findViewById(R.id.requestfragment_key);
        k.setText(SplitKey(key));
        TextView m = (TextView) rootView.findViewById(R.id.requestfragment_message);
        m.setText(message);

        Button accept = (Button) rootView.findViewById(R.id.acceptFriendRequest);
        Button reject = (Button) rootView.findViewById(R.id.rejectFriendRequest);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
                AntoxDB db = new AntoxDB(getActivity().getApplicationContext());
                db.addFriend(key, "Friend Accepted", "", "");
                db.close();
                ((MainActivity) getActivity()).updateLeftPane();
                ((MainActivity) getActivity()).pane.openPane();
                Intent acceptRequestIntent = new Intent(getActivity(), ToxService.class);
                acceptRequestIntent.setAction(Constants.ACCEPT_FRIEND_REQUEST);
                acceptRequestIntent.putExtra("key", toxSingleton.activeFriendRequestKey);
                getActivity().startService(acceptRequestIntent);
            }
        });

        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AntoxDB db = new AntoxDB(getActivity().getApplicationContext());
                db.deleteFriendRequest(key);
                db.close();
                getActivity().getSupportFragmentManager().popBackStack();
                ((MainActivity) getActivity()).pane.openPane();
                Intent rejectRequestIntent = new Intent(getActivity(), ToxService.class);
                rejectRequestIntent.setAction(Constants.REJECT_FRIEND_REQUEST);
                rejectRequestIntent.putExtra("key", toxSingleton.activeFriendRequestKey);
                getActivity().startService(rejectRequestIntent);
            }
        });

        return rootView;
    }
}
