package com.quintus.labs.datingapp.Main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.quintus.labs.datingapp.R;
import com.quintus.labs.datingapp.viewPager2.SlideItem;
import com.quintus.labs.datingapp.viewPager2.SliderAdapter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class ProfileCheckinMain extends AppCompatActivity {

    private Context mContext;
    private Cards card;
    private TextView name, status, school, job, company, hobbies;
    private ImageButton back;
    private String favorite;
    ImageView imageView1, imageView2, imageView3;
    private ViewPager2 viewPager2;
    String profileImageUrl;
    List<SlideItem> slideItemList = new ArrayList<>();
    private Handler slideHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_checkin_main);

        mContext = ProfileCheckinMain.this;



//       /* ImageButton back = findViewById(R.id.back);

//*/
        ;
        name =  findViewById(R.id.nameText);
        status =  findViewById(R.id.statusText);
        job =  findViewById(R.id.jobText);
        school =  findViewById(R.id.schoolText);
        company =  findViewById(R.id.companyText);
        imageView1 = findViewById(R.id.image_view_1);
        imageView2 = findViewById(R.id.image_view_2);
        imageView3 = findViewById(R.id.image_view_3);
        hobbies = findViewById(R.id.hobbieslText);
        back = findViewById(R.id.back);
        //slide image
        viewPager2 = findViewById(R.id.viewpager2);


        Intent intent = getIntent();
        card = (Cards) intent.getSerializableExtra("card");
        String cardName = card.getName();
        String jobTxt = card.getJob();
        String companyTxt = card.getCompany();
        String schoolTxt = card.getSchool();
        String statusTxt = card.getStatus();
        String image1 = card.getProfileImageUrl();
        Log.d("Image",image1);
        String image2 = card.getImage2();
        String image3 = card.getImage3();

//        profileDistance.setText(distance + " " + append);
        name.setText(cardName);
        company.setText(companyTxt);
        school.setText(schoolTxt);
        status.setText(statusTxt);
        job.setText(jobTxt);
        favorite = "";
        if(card.isFishing())
            favorite += "Câu cá";
        if(card.isSport())
            favorite += "\nThể thao";
        if(card.isMovie())
            favorite += "\nXem phim";
        if(card.isTravel())
            favorite += "\nDu lịch";
        if(card.isMusic())
            favorite += "\nNghe nhạc";
        hobbies.setText(favorite);

        //viewpager hiển thị slide ảnh
        slideItemList.add(new SlideItem(image1));
        slideItemList.add(new SlideItem(image2));
        slideItemList.add(new SlideItem(image3));
        viewPager2.setAdapter(new SliderAdapter(slideItemList,viewPager2));
        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r = 1 - Math.abs(position);
                page.setScaleX(0.75f + r * 0.25f);
                page.setScaleY(0.75f + r * 0.25f);
            }
        });
        viewPager2.setPageTransformer(compositePageTransformer);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                slideHandler.removeCallbacks(slideRunnable);
                slideHandler.postDelayed(slideRunnable,1500);
            }
        });
    back.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    });


    }
    private Runnable slideRunnable = new Runnable() {
        @Override
        public void run() {
            viewPager2.setCurrentItem(viewPager2.getCurrentItem()+1);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        slideHandler.removeCallbacks(slideRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        slideHandler.postDelayed(slideRunnable,1500);
    }
}
