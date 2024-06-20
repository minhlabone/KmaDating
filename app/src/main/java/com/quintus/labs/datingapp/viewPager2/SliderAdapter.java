package com.quintus.labs.datingapp.viewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.makeramen.roundedimageview.RoundedImageView;
import com.quintus.labs.datingapp.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SlideDerViewHoler>{
    private List<SlideItem> slideItems;
    private ViewPager2 viewPager2;

    public SliderAdapter(List<SlideItem> slideItems, ViewPager2 viewPager2) {
        this.slideItems = slideItems;
        this.viewPager2 = viewPager2;
    }

    @NonNull
    @Override
    public SlideDerViewHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SlideDerViewHoler(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.slide_item_container,parent,false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull SlideDerViewHoler holder, int position) {
         holder.setImage(slideItems.get(position));
         if(position == slideItems.size()-2){
             viewPager2.post(runnable);
         }
    }

    @Override
    public int getItemCount() {
        return slideItems.size();
    }

    class SlideDerViewHoler extends RecyclerView.ViewHolder {
         private RoundedImageView imageView;
         SlideDerViewHoler(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageSlide);
        }
        void setImage(SlideItem slideItem) {
            if (!slideItem.getImage().equals("")) {
                Picasso.get().load(slideItem.getImage()).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.monkey).into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(slideItem.getImage()).placeholder(R.drawable.monkey).into(imageView);
                            }
                        });
            }
        }
    }
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            slideItems.addAll(slideItems);
            notifyDataSetChanged();
        }
    };
}
