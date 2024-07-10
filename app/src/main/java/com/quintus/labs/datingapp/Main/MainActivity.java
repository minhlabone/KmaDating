package com.quintus.labs.datingapp.Main;

import static android.app.PendingIntent.getActivity;
import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.quintus.labs.datingapp.Introduction.IntroductionMain;
import com.quintus.labs.datingapp.R;
import com.quintus.labs.datingapp.Utils.PulsatorLayout;
import com.quintus.labs.datingapp.Utils.TopNavigationViewHelper;
import com.quintus.labs.datingapp.Utils.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final int ACTIVITY_NUM = 1;
    final private int MY_PERMISSIONS_REQUEST_LOCATION = 123;
    ListView listView;
    List<Cards> rowItems;
    FrameLayout cardFrame, moreFrame;
    private Context mContext = MainActivity.this;
    private NotificationHelper mNotificationHelper;
    private Cards cards_data[];
    private PhotoAdapter arrayAdapter;
    private FirebaseAuth mAuth;
    public static boolean isFinished;
    private ArrayList<User> userArrayList;
    private DatabaseReference mDatabase;
    private String currentUserId;
    private String sex;
    private FirebaseUser currentUser;
    private ArrayList<String> selectedUserId;
    private DatabaseReference selectedDatabase;
    private DatabaseReference matchDatabase;
    private User myUser;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
//    private SwipeFlingAdapterView flingContainer;
    LottieAnimationView animationView,animationView2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        setupTopNavigationView();
        cardFrame = findViewById(R.id.card_frame);
        moreFrame = findViewById(R.id.more_frame);
        animationView = findViewById(R.id.animationView);
        animationView2 = findViewById(R.id.animationView2);
        // start pulsator( hiệu ứng dung nhị tròn tròn)
        PulsatorLayout mPulsator = findViewById(R.id.pulsator);
        mPulsator.start();
        mNotificationHelper = new NotificationHelper(this);
        // lấy người dùng hiện tại
        currentUser = mAuth.getCurrentUser();

        if(currentUser==null){
            //nếu không có thì chuyển qua màn hình Intro ( giới thiệu)
            Intent intent = new Intent(MainActivity.this, IntroductionMain.class);
            startActivity(intent);
            finish();
        }
        else{
//            flingContainer = findViewById(R.id.frame);
            // lấy UID của user hiện tại
            currentUserId = currentUser.getUid();

           //Cấp quyền  truy cập vị trí
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }else{
                //lấy location từ hệ thống
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                //nếu GPS được bật thì  gọi update cập nhật vị trí  swipcard
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    updateLocation();
                    swipeCard();
                }else{
                    // yêu cầu bật GPS
                  enableGPS();
                }
                }

//            updateLocation();
//            swipeCard();
            //lấy ra uid của người dùng và set giá trị cho onile và seen
            String CurrentUID = currentUser.getUid();
            FirebaseDatabase.getInstance().getReference().child("users").child(CurrentUID).child("Online").setValue("true");
            FirebaseDatabase.getInstance().getReference().child("users").child(CurrentUID).child("Seen").setValue("online");


        }
    }
    @Override
    protected void onStop() {
        super.onStop();
     // set trạng thái thành offline
            FirebaseAuth Auth= FirebaseAuth.getInstance();
            FirebaseUser currentUser = Auth.getCurrentUser();
            if (currentUser != null) {
                String CurrentUID = currentUser.getUid();

                FirebaseDatabase.getInstance().getReference().child("users").child(CurrentUID).child("Online").setValue(ServerValue.TIMESTAMP);
                FirebaseDatabase.getInstance().getReference().child("users").child(CurrentUID).child("Seen").setValue("offline");
            }


    }


    private void swipeCard(){
//        updateLocation();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        matchDatabase = FirebaseDatabase.getInstance().getReference("Match").child(currentUserId);
        selectedDatabase = FirebaseDatabase.getInstance().getReference().child("Selected").child(currentUserId);
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    userArrayList = new ArrayList<>();
                    for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                        //nếu id của người dùng  không phải là người dùng hiện tại
                        if(!snapshot.getKey().equals(currentUserId)) {
                            User user = new User();
                            user.setUser_id(snapshot.getKey());
                            user.setName(snapshot.child("Name").getValue().toString());
                            user.setAge(snapshot.child("Age").getValue(Integer.class));
                             user.setImage2(snapshot.child("Image2").getValue().toString());
                            user.setImage1(snapshot.child("Image1").getValue().toString());
                            user.setImage3(snapshot.child("Image3").getValue().toString());
                            user.setStatus(snapshot.child("Status").getValue().toString());
                            user.setJob(snapshot.child("Job").getValue().toString());
                            user.setSchool(snapshot.child("School").getValue().toString());
                            user.setSex(snapshot.child("sex").getValue().toString());
                            user.setCompany(snapshot.child("Company").getValue().toString());
                            user.setFishing(Boolean.parseBoolean(snapshot.child("Fishing").getValue().toString()));
                            user.setMovie(Boolean.parseBoolean(snapshot.child("Movie").getValue().toString()));
                            user.setMusic(Boolean.parseBoolean(snapshot.child("Music").getValue().toString()));
                            user.setSports(Boolean.parseBoolean(snapshot.child("Sports").getValue().toString()));
                            user.setGaming(Boolean.parseBoolean(snapshot.child("Gaming").getValue().toString()));
                            user.setTravel(Boolean.parseBoolean(snapshot.child("Travel").getValue().toString()));
                            user.setLatitude(snapshot.child("latitude").getValue(Double.class));
                            user.setLongtitude(snapshot.child("longitude").getValue(Double.class));
//                            user.setLatitude(20.979185);
//                            user.setLongtitude(105.7993016);
                            userArrayList.add(user);
                        }
                        else{
                            myUser = new User();
                            myUser.setSex(snapshot.child("sex").getValue().toString());
                            myUser.setAge(snapshot.child("Age").getValue(Integer.class));
                            myUser.setFishing(Boolean.parseBoolean(snapshot.child("Fishing").getValue().toString()));
                            myUser.setMovie(Boolean.parseBoolean(snapshot.child("Movie").getValue().toString()));
                            myUser.setMusic(Boolean.parseBoolean(snapshot.child("Music").getValue().toString()));
                            myUser.setSports(Boolean.parseBoolean(snapshot.child("Sports").getValue().toString()));
                            myUser.setGaming(Boolean.parseBoolean(snapshot.child("Gaming").getValue().toString()));
                            myUser.setTravel(Boolean.parseBoolean(snapshot.child("Travel").getValue().toString()));
                            myUser.setAgeFrom(snapshot.child("AgeFrom").getValue(Integer.class));
                            myUser.setAgeTo(snapshot.child("AgeTo").getValue(Integer.class));
                            myUser.setLongtitude(snapshot.child("longitude").getValue(Double.class));
                            myUser.setLatitude(snapshot.child("latitude").getValue(Double.class));
                            myUser.setDistance(snapshot.child("Distance").getValue(Integer.class));
                        }

                    }
                    getCards();
                    arrayAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        selectedDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                selectedUserId = new ArrayList<>();
                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                        selectedUserId.add(dataSnapshot.getKey());
                    }
                    mDatabase.addValueEventListener(eventListener);
                }else{
                    mDatabase.addValueEventListener(eventListener);
                }
//
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

// Thuật toán ghép cặp tinder :vvvv
    private void getCards(){
        // Xóa nguười dùng trung với người dùng cùng id khỏi danh sách
        for(String id: selectedUserId){
            userArrayList.removeIf(s -> s.getUser_id().equals(id));
        }
        // khởi tạo 1 rowItem là ArrayList mới  để lưu các thẻ
        rowItems = new ArrayList<>();
        arrayAdapter = new PhotoAdapter(this, R.layout.item, rowItems);

        // lấy ra vị tr hiện tại của myUser  ( người dùng hiện tại)
        Location currentLocation = new Location("currentLocation");
        currentLocation.setLatitude(myUser.getLatitude());
        currentLocation.setLongitude(myUser.getLongtitude());

        // sau khi lấy vị trí rồi duệt qua danh sách người dùng và tính khoảng cách
        for(User user: userArrayList){
            Location location2 = new Location("location2");
            location2.setLongitude(user.getLongtitude());
            location2.setLatitude(user.getLatitude());
           // tính khoảng cách giữa myUser với người trong danh sách theo km
            float distance = currentLocation.distanceTo(location2)/1000 ;
            distance = Math.round(distance*100)/100;

            //sau khi tính xong thì kiểm tra tiêu chí để ghép
             // 1 : giới tính khác nhau +  tuổi của user phải nằm trong khoảng set của myUser + cos 1 sở thích chung
            if((!user.getSex().equals(myUser.getSex())
                && user.getAge() <= myUser.getAgeTo()
                && user.getAge() >= myUser.getAgeFrom())
                && (
                        ((user.isMusic() && myUser.isMusic())
                      || (user.isSports() && myUser.isSports())
                      || (user.isGaming() && myUser.isGaming())
                      || (user.isMovie() && myUser.isMovie())
                      || (user.isTravel() && myUser.isTravel())
                      || (user.isFishing() && myUser.isFishing())
                    )
            ) && distance <= myUser.getDistance()) // không vượt quá khoảng cách tối đa mà myUser đã setting
              {
                  // nếu oke thì lưu user đó vào list card để hiển thị lên cho myUser ghép cặp
                Cards cards = new Cards(user.getUser_id(),
                        user.getName(),
                        user.getAge(),
                        user.getImage1(),
                        user.getImage2(),
                        user.getImage3(),
                        user.getStatus(),
                        user.getCompany(),
                        user.getSchool(),
                        user.getJob(),
                        user.isMovie(),
                        user.isFishing(),
                        user.isTravel(),
                        user.isSports(),
                        user.isMusic(),
                        distance);
                        rowItems.add(cards);

            }
        }
        checkRowItem(); // check list card để hiển thị
        updateSwipeCard();

    }


    private void checkRowItem() {
        // nếu row trống thì hiển thị moreFrame
        if (rowItems.isEmpty()) {
            moreFrame.setVisibility(View.VISIBLE);
            cardFrame.setVisibility(View.GONE);
        }
    }

    private void updateLocation() {
     // Kiểm tra quyền
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        } else {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Location myLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            //nếu không lấy được từ GPS thì check  thụ động
            if (myLocation == null)
            {
                myLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            }
//            FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("longitude").setValue(myLocation.getLongitude());
//            FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("latitude").setValue(myLocation.getLatitude());

            // code mới update location
            //nếu nhận được GPS thì cập nhật kinh độ vĩ độ lên FireBase
            if (myLocation != null) {
                double longitude = myLocation.getLongitude();
                double latitude = myLocation.getLatitude();

                // Cập nhật longtiute lên Firebase
                FirebaseDatabase.getInstance().getReference()
                        .child("users")
                        .child(currentUserId)
                        .child("longitude")
                        .setValue(longitude);

                FirebaseDatabase.getInstance().getReference()
                        .child("users")
                        .child(currentUserId)
                        .child("latitude")
                        .setValue(latitude);
            } else {
                // Xử lý trường hợp không thể lấy vị trí
                Log.e("LocationError", "Unable to retrieve location.");
                // Bạn có thể hiển thị thông báo cho người dùng hoặc thực hiện các hành động khác
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

//                        updateLocation();
//                        swipeCard();
                        enableGPS();
                    } else {
                        Toast.makeText(MainActivity.this, "Chưa cấp quyền truy cấp địa chỉ, bạn phải cấp quyền truy cập địa chỉ để sử dụng lọc khoảng cách", Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            }

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void updateSwipeCard() {
        // library SwipeFlingAdapter
        final SwipeFlingAdapterView flingContainer = findViewById(R.id.frame);
        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // khi thẻ đầu tiên bị loại bỏ
                Log.d("LIST", "removed object!");
                rowItems.remove(0);
//                selectedDatabase.child(rowItems.get(0).getUserId()).setValue(rowItems.get(0).getUserId());
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                //lottie animation
                animationView2.setVisibility(View.VISIBLE);
                animationView2.setAnimation(R.raw.breakhear2);
                animationView2.playAnimation();
                cardFrame.setVisibility(View.GONE);
                moreFrame.setVisibility(View.GONE);
                // vuốt sang trái và lấy ra đối tượng card rồi checkRow hiển thị lên màn hình
                Cards obj = (Cards) dataObject;
                selectedDatabase.child(obj.getUserId()).setValue(obj.getUserId());
                animationView2.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animationView2.cancelAnimation();
                        animationView2.setVisibility(View.GONE);
                        cardFrame.setVisibility(View.VISIBLE);
                        checkRowItem();
                        // Hoặc dùng lottieAnimationView.pauseAnimation();
                    }
                }, 2500);
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                animationView.setVisibility(View.VISIBLE);
                animationView.setAnimation(R.raw.love);
                animationView.playAnimation();
                cardFrame.setVisibility(View.GONE);
                moreFrame.setVisibility(View.GONE);
                // vuốt sang phải thì insert vào match rồi cập nhật card mới
                Cards obj = (Cards) dataObject;
                matchDatabase.child(obj.getUserId()).setValue(obj.getUserId());
                selectedDatabase.child(obj.getUserId()).setValue(obj.getUserId());
                //check matches
//                checkRowItem();
                animationView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animationView.cancelAnimation();
                        animationView.setVisibility(View.GONE);
                        cardFrame.setVisibility(View.VISIBLE);
                        checkRowItem();
                        // Hoặc dùng lottieAnimationView.pauseAnimation();
                    }
                }, 2500);
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here


            }

            @Override
            public void onScroll(float scrollProgressPercent) {
                // trả về đối tượng view thẻ hiện tại người dùng đang tương tác
                View view = flingContainer.getSelectedView();
                // set độ mờ và dịch chuyển khi vuốt
                view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
            }
        });

        //Profile của user cards khi click
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                Cards card_item = (Cards)dataObject;
//                Toast.makeText(getApplicationContext(), "Clicked", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, ProfileCheckinMain.class);
                intent.putExtra("card", card_item);
                mContext.startActivity(intent);
            }
        });


    }


    public void sendNotification() {
        NotificationCompat.Builder nb = mNotificationHelper.getChannel1Notification(mContext.getString(R.string.app_name), mContext.getString(R.string.match_notification));

        mNotificationHelper.getManager().notify(1, nb.build());
    }
// khi ấn vào dislike
    public void DislikeBtn(View v) {
        //ktra danh sách có rỗng ko
        if (rowItems.size() != 0) {
            //lấy ra card đầu tiên
            Cards card_item = rowItems.get(0);

            String userId = card_item.getUserId();
           // remove đối tượng card này
            rowItems.remove(0);
            arrayAdapter.notifyDataSetChanged();
            selectedDatabase.child(card_item.getUserId()).setValue(card_item.getUserId());
            Intent btnClick = new Intent(mContext, BtnDislikeActivity.class);
            btnClick.putExtra("url", card_item.getProfileImageUrl());
            startActivity(btnClick);
        }
    }
// like thì lưu vào trong match
    public void LikeBtn(View v) {
        if (rowItems.size() != 0) {
            Cards card_item = rowItems.get(0);

            String userId = card_item.getUserId();

            //check matches

            rowItems.remove(0);
            arrayAdapter.notifyDataSetChanged();
            matchDatabase.child(card_item.getUserId()).setValue(card_item.getUserId());
            selectedDatabase.child(card_item.getUserId()).setValue(card_item.getUserId());
            Intent btnClick = new Intent(mContext, BtnLikeActivity.class);
            btnClick.putExtra("url", card_item.getProfileImageUrl());
            startActivity(btnClick);
        }
    }



    /**
     * setup top tool bar
     */
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
    public void onBackPressed() {

    }
  // kiểm tra và yêu cầu bat GPS
    public void enableGPS(){
        //tạo yêu cầu vị trí mới
        LocationRequest locationRequest = LocationRequest.create();
        // đặt ưu tiên cao cho việc lấy vị trí chính xác nhất
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // yêu cầu kiểm tra cài đặt vị trí và thêm vào builder
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        //kiểm tra các cài đặt vị trí hiện tại và trả về một đối tượng Task<LocationSettingsResponse>
        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(MainActivity.this).checkLocationSettings(builder.build());
        //sử lí kết quả kiểm tra cài đặt
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        //nghĩa là cài đặt vị trí chưa thỏa mãn, nhưng có thể được sửa chữa bằng cách hiển thị một hộp thoại cho người dùng.
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                //hiển thị hộp thoại yêu cầu người dùng bật cài đặt vị trí.
                                resolvable.startResolutionForResult(
                                        MainActivity.this,
                                        LocationRequest.PRIORITY_HIGH_ACCURACY);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                            //ghĩa là cài đặt vị trí không thỏa mãn và không có cách nào để sửa chữa chúng, nên không hiển thị hộp thoại
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LocationRequest.PRIORITY_HIGH_ACCURACY:
                switch (resultCode) {
                    case Activity.RESULT_OK:

                        // thành công thì cập nhật vị trí mỗi 1s
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                // Actions to do after 10 seconds
                                updateLocation();

                            }
                        }, 1000);
                        swipeCard();
                        Log.i(TAG, "onActivityResult: GPS Enabled by user");
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(this, "Chưa bật vị trí rồi bạn", Toast.LENGTH_SHORT).show();
                        enableGPS();
                        // The user was asked to change settings, but chose not to
                        Log.i(TAG, "onActivityResult: User rejected GPS request");
                        break;
                    default:
                        break;
                }
                break;
        }
    }
}
