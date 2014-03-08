package im.tox.antox;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;


/**
 * Created by ollie on 28/02/14.
 */
public class ContactsFragment extends Fragment {
    /**
     * List View for displaying all the friends in a scrollable list
     */
    private ListView leftPaneListView;
    /**
     * Adapter for the friendListView
     */
    private LeftPaneAdapter leftPaneAdapter;


    public ContactsFragment() {
        main_act = (MainActivity) getActivity();

    }

    public void onChangeFriendRequest(int position, String key, String message) {
        Fragment newFragment = new FriendRequestFragment(key, message);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.right_pane, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        main_act.activeFriendRequestKey = key;
    }

    public void onChangeContact(int position, String name) {
        Fragment newFragment = new ChatFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.right_pane, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        main_act.activeFriendRequestKey = null;
    }

    public void updateLeftPane() {
        leftPaneAdapter = main_act.leftPaneAdapter;
        leftPaneListView.setAdapter(leftPaneAdapter);
        System.out.println("updated left pane");
    }

    private MainActivity main_act;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /**
         * Stores a 2 dimensional string array holding friend details. Will be populated
         * by a tox function once implemented
         */

        main_act = (MainActivity) getActivity();



        View rootView = inflater.inflate(R.layout.fragment_leftpane, container, false);
        leftPaneListView = (ListView) rootView.findViewById(R.id.left_pane_list);

        updateLeftPane();

        leftPaneListView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position,
                                            long id) {
                        LeftPaneItem item = (LeftPaneItem) parent.getAdapter().getItem(position);
                        int type = item.viewType();
                        if (type == Constants.TYPE_CONTACT) {
                            onChangeContact(position, item.first());
                            main_act.activeTitle = item.first();
                            main_act.pane.closePane();
                        } else if (type == Constants.TYPE_FRIEND_REQUEST) {

                            String key = item.first();
                            String message = item.second();
                            onChangeFriendRequest(position, key, message);
                            main_act.activeTitle = "Friend Request";
                            main_act.pane.closePane();
                        }
                    }
                });

        leftPaneListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View itemView, int index, long id) {
                final LeftPaneItem item = (LeftPaneItem) parent.getAdapter().getItem(index);
                AlertDialog.Builder builder = new AlertDialog.Builder(main_act);
                boolean isGroupChat=false;
                final boolean isFriendRequest = item.viewType()==Constants.TYPE_FRIEND_REQUEST;
                final CharSequence items[];
                if(isFriendRequest){
                    items= new CharSequence[]{getResources().getString(R.string.friendrequest_accept),
                            getResources().getString(R.string.friendrequest_reject)
                    };
                }else{
                    items= new CharSequence[]{
                            getResources().getString(R.string.friend_action_sendfile),
                            isGroupChat ? getResources().getString(R.string.group_action_leave) : getResources().getString(R.string.friend_action_delete),
                            getResources().getString(R.string.friend_action_block)
                    };
                }
                builder.setTitle("Actions on " + item.first)
                        .setCancelable(true)
                        .setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int index) {
                                Log.d("picked", "" + items[index]);
                                //item.first equals the key
                                if(isFriendRequest){
                                    switch (index){
                                        case 0:
                                            Intent acceptRequestIntent = new Intent(getActivity(), ToxService.class);
                                            acceptRequestIntent.setAction(Constants.ACCEPT_FRIEND_REQUEST);
                                            acceptRequestIntent.putExtra("key", item.first);
                                            main_act.startService(acceptRequestIntent);
                                            main_act.updateLeftPane();
                                            break;
                                        case 1:
                                            Intent rejectRequestIntent = new Intent(main_act, ToxService.class);
                                            rejectRequestIntent.setAction(Constants.REJECT_FRIEND_REQUEST);
                                            rejectRequestIntent.putExtra("key", item.first);
                                            main_act.startService(rejectRequestIntent);
                                            main_act.updateLeftPane();
                                            //rejectRequest(item.first);
                                            break;
                                    }
                                }else{
                                    switch (index){
                                        case 0:
                                            Log.v("To implement", "" + items[0]);
                                            break;
                                        case 1:
                                            Log.v("To implement", "" + items[1]);
                                            break;
                                        case 2:
                                            Log.v("To implement", "" + items[2]);
                                            break;
                                    }
                                }
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                if(item.viewType()!=Constants.TYPE_HEADER){
                    alert.show();
                }
                return true;
            }
        });

        return rootView;
    }
}
