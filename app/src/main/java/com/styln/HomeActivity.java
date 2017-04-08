package com.styln;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.user.signin.FacebookSignInProvider;
import com.amazonaws.mobile.user.signin.GoogleSignInProvider;
import com.amazonaws.mobile.util.ThreadUtils;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.models.nosql.UsersDO;
import com.bumptech.glide.Glide;
import com.styln.demo.nosql.DemoNoSQLOperationListItem;
import com.styln.demo.nosql.DemoNoSQLTableBase;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class HomeActivity extends AppCompatActivity {

    private static final String LOG_TAG = HomeActivity.class.getSimpleName();
    private ImageView imageLike;
    private TextView textLikes;
    private ImageView profilePic;
    private Button follow,followMe;
    private String userName;

    private AddToUsersTable addToUsersTable;
    private AddClothesTable addClothesTable;
    private DummyPostUser dummy;
    private AddPostsTable addPost;
    DemoNoSQLTableBase table;

    ListView operationsListView;
    private ArrayAdapter<DemoNoSQLOperationListItem> operationsListAdapter;
    private boolean a = false;

    private List<Item> itemList = new ArrayList<>();
    private RecyclerView recyclerView;
    private PostItemsAdapter iAdapter;

    static boolean liked = false;
    static int numLikes = 0;
    static boolean followed = false;
    static boolean followedMe = false;



    private void getUserTable() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //addToUsersTable.getUser();
                } catch (final AmazonClientException ex) {
                    Log.e(LOG_TAG, "failed to retrieve");
                    return;
                }
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getApplicationContext());
//                        dialogBuilder.setTitle(R.string.nosql_dialog_title_added_sample_data_text);
//                        dialogBuilder.setMessage("Add successful");
//                        dialogBuilder.setNegativeButton(R.string.nosql_dialog_ok_text, null);
//                        dialogBuilder.show();
                    }
                });
            }
        }).start();
    }

    private void addItemTable() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    addToUsersTable.addItem();
                } catch (final AmazonClientException ex) {
                    Log.e(LOG_TAG, "failed to add");
                    return;
                }
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getApplicationContext());
//                        dialogBuilder.setTitle(R.string.nosql_dialog_title_added_sample_data_text);
//                        dialogBuilder.setMessage("Add successful");
//                        dialogBuilder.setNegativeButton(R.string.nosql_dialog_ok_text, null);
//                        dialogBuilder.show();
                    }
                });
            }
        }).start();
    }

    private void addDummyItemTable() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    dummy.addDummyItem();
                } catch (final AmazonClientException ex) {
                    Log.e(LOG_TAG, "failed to add");
                    return;
                }
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getApplicationContext());
//                        dialogBuilder.setTitle(R.string.nosql_dialog_title_added_sample_data_text);
//                        dialogBuilder.setMessage("Add successful");
//                        dialogBuilder.setNegativeButton(R.string.nosql_dialog_ok_text, null);
//                        dialogBuilder.show();
                    }
                });
            }
        }).start();
    }

    private void addPostTable() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    addPost.addPost();
                } catch (final AmazonClientException ex) {
                    Log.e(LOG_TAG, "failed to add");
                    return;
                }
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getApplicationContext());
//                        dialogBuilder.setTitle(R.string.nosql_dialog_title_added_sample_data_text);
//                        dialogBuilder.setMessage("Add successful");
//                        dialogBuilder.setNegativeButton(R.string.nosql_dialog_ok_text, null);
//                        dialogBuilder.show();
                    }
                });
            }
        }).start();
    }

    private void addClothesTable() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    addClothesTable.addClothes();
                } catch (final AmazonClientException ex) {
                    Log.e(LOG_TAG, "failed to add");
                    return;
                }
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        }).start();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textLikes = (TextView)findViewById(R.id.numLikes);
        follow = (Button)findViewById(R.id.home_follow);
        followMe = (Button)findViewById(R.id.home_followMe);


        if(followed){
            follow.setText("UNFOLLOW");
            follow.setTextSize(10);
    } else {
            follow.setText("FOLLOW");
            follow.setTextSize(10);
    }
    //TODO get our servers username
        if (Application.getSign_opt() == 'f') {
            profilePic = (ImageView)findViewById(R.id.profilePicture);
            String address = FacebookSignInProvider.userImageUrl;
            Glide.with(this).load(address).bitmapTransform(new CropCircleTransformation(getBaseContext())).
                    thumbnail(0.1f).into(profilePic);
            userName = FacebookSignInProvider.userName;
            Log.i(LOG_TAG,FacebookSignInProvider.userName);
        }
        else {
            profilePic = (ImageView)findViewById(R.id.profilePicture);
            String address = GoogleSignInProvider.userImageUrl;
            Glide.with(this).load(address).bitmapTransform(new CropCircleTransformation(getBaseContext())).
                    thumbnail(0.1f).into(profilePic);
            userName = GoogleSignInProvider.userName;
            Log.i(LOG_TAG,GoogleSignInProvider.userName);
        }

        addToUsersTable = new AddToUsersTable(userName);
        addItemTable();
        addPost = new AddPostsTable();
        addPostTable();
        addClothesTable = new AddClothesTable();
        addClothesTable();
        getUserTable();



        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        iAdapter = new PostItemsAdapter(this,itemList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(iAdapter);

        prepareCollectionData();

    }

    private void prepareCollectionData() {
        //TODO item load
        Item item = new Item("Tshirt 1", "Adidas",1,R.drawable.item_1);
        itemList.add(item);
        iAdapter.notifyDataSetChanged();
    }

    public void checkLogin()
    {
        DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
        UsersDO user = mapper.load(UsersDO.class, AWSMobileClient.defaultMobileClient().getIdentityManager().getCachedUserID());
        if(user == null)
            a =  true;
    }
    public void checkLoginHelper()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    checkLogin();
                } catch (final AmazonClientException ex) {
                    Log.e(LOG_TAG, "failed to add");
                    return;
                }
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        }).start();
    }

    public void openHome(View view) {
    }

    public void openTrend(View view) {
        Log.d(LOG_TAG, "Launching Trend Activity...");
        startActivity(new Intent(HomeActivity.this, TrendActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        // finish should always be called on the main thread.
        finish();
    }

    public void openPost(View view) {
    }

    public void openBrowse(View view) {
        Log.d(LOG_TAG, "Launching Browse Activity...");
        startActivity(new Intent(HomeActivity.this, BrowseActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        // finish should always be called on the main thread.
        finish();
    }

    public void openProfile(View view) {
        Log.d(LOG_TAG, "Launching Profile Activity...");
        startActivity(new Intent(HomeActivity.this, ProfileActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        // finish should always be called on the main thread.
        finish();
    }

    public void openSettings(View view) {
            Log.d(LOG_TAG, "Launching Settings Activity...");
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            // finish should always be called on the main thread.
            finish();
    }



    public void addTo(View view) {
        showPopupMenu(view);
    }

    private void showPopupMenu(View view) {
        // inflate menu
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.item_action_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new HomeActivity.MyMenuItemClickListener());
        popup.show();
    }

    /**
     * Click listener for popup menu items
     */
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        public MyMenuItemClickListener() {
        }
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.menu_like:
                    Toast.makeText(getApplicationContext(), "Liked", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.menu_add_to_wardrobe:
                    Toast.makeText(getApplicationContext(), "Added to Wardrobe", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.menu_add_to_wishlist:
                    Toast.makeText(getApplicationContext(), "Added to Wishlist", Toast.LENGTH_SHORT).show();
                    return true;
                default:
            }
            return false;
        }
    }

    public void follow(View view) {

        if(followed){
            followed = false;
            follow.setText("FOLLOW");
            follow.setTextSize(10);
            FollowAction fl = new FollowAction();
            fl.followSomeone("us-east-1:3254e0fa-3613-45b2-aa81-6ad73c765f0e");
            Log.d("d","Follow");

        } else {
            followed = true;
            follow.setText("UNFOLLOW");
            follow.setTextSize(10);
            FollowAction fl = new FollowAction();
            fl.unfollowSomeone("us-east-1:3254e0fa-3613-45b2-aa81-6ad73c765f0e");
            Log.d("d","UNFollow");
        }
    }


    public void followMe(View view) {
        if(followedMe){
            followedMe = false;
            follow.setText("FOLLOW ME");
            follow.setTextSize(10);
            FollowAction fl = new FollowAction();
            fl.followSomeone("us-east-1:66c73c49-93fc-49f1-8212-accbcb213056");
            Log.d("d","Follow");

        } else {
            followedMe = true;
            follow.setText("UNFOLLOW ME");
            follow.setTextSize(10);
            FollowAction fl = new FollowAction();
            fl.unfollowSomeone("us-east-1:66c73c49-93fc-49f1-8212-accbcb213056");
            Log.d("d","UNFollow");
        }
    }
}
