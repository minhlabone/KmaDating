package com.quintus.labs.datingapp.Matched;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.quintus.labs.datingapp.R;
import com.quintus.labs.datingapp.Utils.TopNavigationViewHelper;
import com.quintus.labs.datingapp.Utils.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;



public class Matched_Activity extends AppCompatActivity implements SelectListener {

    private static final String TAG = "Matched_Activity";
    private static final int ACTIVITY_NUM = 2;
    List<Users> matchList = new ArrayList<>();
    List<User> copyList = new ArrayList<>();
    private Context mContext = Matched_Activity.this;
    private String userId, userSex, lookforSex;
    private double latitude = 37.349642;
    private double longtitude = -121.938987;
    private EditText search;
    private List<Users> usersList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ListView mRecyclerView;
    private ActiveUserAdapter adapter;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private View mMainView;
    private String mParam1;
    private String mParam2;

    //my variables
    private ArrayList<String> UsersId;
    private ArrayList <Friends>UsersArrayList;
    private ArrayList<Friends> UsersArrayList2;
    private ListView UserChatsListView;
    private ImageButton btnCheckInfoBeforeMatched;
    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String CurrentUId;
    private String FinalMessage="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matched);

        setupTopNavigationView();
        searchFunc();


        recyclerView = findViewById(R.id.active_recycler_view);
        UserChatsListView= findViewById(R.id.UserChats_ListView_id);


        prepareMatchData();


    }

    private void prepareActiveData() {
    }
 // Matched trong messenger
    private void prepareMatchData() {
        //firebase lấy thông tin người dùng hiện tại
        mAuth= FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        CurrentUId=currentUser.getUid();


        //lưu trữ thông tin người dùng và bạn bè
        UsersId=new ArrayList<>();
        UsersArrayList=new ArrayList<>();
        //click user chat để sang màn hình chat
        UserChatsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Friends friend= UsersArrayList.get(i);
                Intent intent = new Intent(Matched_Activity.this,FriendsChattingActivity.class);
                intent.putExtra("User Id",friend.getFriendId());
                intent.putExtra("User Name",friend.getFriendName());
                intent.putExtra("User Image",friend.getFriendImage());
                startActivity(intent);
            }
        });


        //kiểm tra người dùng có bạn bè hay không
        DatabaseReference root= FirebaseDatabase.getInstance().getReference();
        DatabaseReference m=root.child("Match").child(CurrentUId);
        ValueEventListener eventListener= new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // nếu có
                if(dataSnapshot.exists()){
                    //clear dữ liệu cũ để tránh bị lặp
                    UsersId.clear();
                    UsersArrayList.clear();
                    // hiển thị lên listview
                    final FriendsAdapter adapter=new FriendsAdapter(Matched_Activity.this,UsersArrayList);
                    UserChatsListView.setAdapter(adapter);
                    // kiểm tra và lấy dữ liệu bạn bè từ firebase

                    checkTheFriends();

                }
                else{
                    //không hiển thị vì ko có bạn bè nào
                    UsersId.clear();
                    UsersArrayList.clear();
                    final FriendsAdapter adapter=new FriendsAdapter(Matched_Activity.this,UsersArrayList);
                    UserChatsListView.setAdapter(adapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        m.addListenerForSingleValueEvent(eventListener);
//        mAdapter.notifyDataSetChanged();
    }

    private void searchFunc() {
        search = findViewById(R.id.searchBar);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchText();
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchText();
            }
        });
    }
 // tìm kiếm bạn bè
    private void searchText() {
        final FriendsAdapter adapter=new FriendsAdapter(Matched_Activity.this,UsersArrayList);
        UserChatsListView.setAdapter(adapter);
//        UsersArrayList2 = UsersArrayList;
         String text = search.getText().toString().toLowerCase(Locale.getDefault());
         if (text.length() != 0) {
             UsersArrayList = new ArrayList<>(UsersArrayList2);
//             UsersArrayList.addAll(UsersArrayList2);
//             UsersArrayList.stream().filter(s -> s.getFriendName().contains(text)).collect(Collectors.toList());
//             UserChatsListView.clearTextFilter();
             UsersArrayList.removeIf(s -> !s.getFriendName().toLowerCase().contains(text));

         } else {
             UsersArrayList = new ArrayList<>(UsersArrayList2);
//             UsersArrayList.stream().;
//             UserChatsListView.setFilterText(text);
         }

        adapter.notifyDataSetChanged();
     }

   // kiểm tra user hiện tại có bạn bè hay không. nếu có thì cập nhật giao diện người dùng
    private void checkTheFriends(){
        UsersId.clear();

        DatabaseReference root=FirebaseDatabase.getInstance().getReference();
        DatabaseReference m=root.child("Match").child(CurrentUId);
        ValueEventListener eventListener= new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for( DataSnapshot Snapshot: dataSnapshot.getChildren()){
                        //Thêm ID của người dùng bạn bè vào danh sách UsersId.
                        //Snapshot.getKey() trả về ID của người dùng tại vị trí của nút con trong cơ sở dữ liệu
                        UsersId.add(Snapshot.getKey().toString());
                    }
                    sentUserDataToArrayAdapter();

                    //xóa r
                    prepareActiveData();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        m.addListenerForSingleValueEvent(eventListener);
    }
// câp nhật dữ liệu lên adapter
    private void sentUserDataToArrayAdapter(){
        UsersArrayList.clear();
        //hiển thị friend
        final FriendsAdapter adapter=new FriendsAdapter(Matched_Activity.this,UsersArrayList);
        final ActiveUserAdapter activeAdapter = new ActiveUserAdapter(usersList, getApplicationContext(), this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(activeAdapter);
      // lặp qua danh sách UserId
        for(int i=0;i<UsersId.size();i++){
            getFinalMessage(UsersId.get(i));
            DatabaseReference root=FirebaseDatabase.getInstance().getReference();
            DatabaseReference m=root.child("users").child(UsersId.get(i));
            ValueEventListener eventListener= new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //ktra dữ liệu có tồn tại tại vị trí sử lí không
                    if(dataSnapshot.exists()){
                        //lấy ra tên, hình ảnh , status của người dùng từ snapshot
                        String name = dataSnapshot.child("Name").getValue().toString();
                        String image = dataSnapshot.child("Image1").getValue().toString();
                        String OnLine = dataSnapshot.child("Online").getValue().toString();
                        //nếu online thì thêm người dùng vào UserArrayList rồi hiển thị lên recyclerview
                        if(OnLine.equals("true")){
                            UsersArrayList.add(new Friends(name,FinalMessage,image,dataSnapshot.getKey(),true));
                            Users user = new Users(dataSnapshot.getKey().toString(), name, image);
                            usersList.add(user);
                            activeAdapter.notifyDataSetChanged();
                        }
                        // nếu không online thì thêm người dùng vào useraraylist với trang thái không trực tuyến và copy sang userarrraylist2
                        else UsersArrayList.add(new Friends(name,FinalMessage,image,dataSnapshot.getKey(),false));
                        FinalMessage="";
                        UsersArrayList2 = new ArrayList<>();
                        UsersArrayList2.addAll(UsersArrayList);
                        searchFunc();
                        adapter.notifyDataSetChanged();
//                        searchFunc();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            };
            m.addListenerForSingleValueEvent(eventListener);

        }

        UserChatsListView.setAdapter(adapter);


    }
 //lấy tin nhắn cuối cùng từ cơ sở dữ liệu Firebase Realtime Database giữa người dùng hiện tại và một người dùng khác
    private void getFinalMessage(String UserId){
        FinalMessage="";
        DatabaseReference root= FirebaseDatabase.getInstance().getReference();
        DatabaseReference m=root.child("chats").child(CurrentUId).child(UserId);
        ValueEventListener eventListener= new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot Snapshot : dataSnapshot.getChildren()){
                        // nếu là hình ảnh gán hình ảnh cho FinalMessage
                        if(Snapshot.child("Message Type").getValue().equals("Image")) FinalMessage="Image";
                        else if(Snapshot.child("Message Type").getValue().equals("Record")) FinalMessage="Audio";
                        else FinalMessage=Snapshot.child("Message").getValue().toString().substring(1, Snapshot.child("Message").getValue().toString().length());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        m.addListenerForSingleValueEvent(eventListener);

    }

    private void setupTopNavigationView() {
        Log.d(TAG, "setupTopNavigationView: setting up TopNavigationView");
        BottomNavigationViewEx tvEx = findViewById(R.id.topNavViewBar);
        TopNavigationViewHelper.setupTopNavigationView(tvEx);
        TopNavigationViewHelper.enableNavigation(mContext, tvEx);
        Menu menu = tvEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }


    @Override
    public void onItemClicked(Users user) {
        Intent intent = new Intent(Matched_Activity.this,FriendsChattingActivity.class);
        intent.putExtra("User Id",user.getUserId());
        intent.putExtra("User Name",user.getName());
        intent.putExtra("User Image",user.getProfileImageUrl());
        startActivity(intent);
    }
}
