package com.chateasy.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

import com.chateasy.android.R;
import com.chateasy.android.fragment.ImageViewFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2022/1/26.
 */

public class ImageViewActivity extends FragmentActivity {
    private ArrayList<String> imageList;
    private List<Fragment> fragList;
    private ViewPager imageVp;
    private TextView currentTv;
    private TextView totalTv;
    private int currentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            if(bundle.containsKey("images")){
                imageList = bundle.getStringArrayList("images");
            }
            if(bundle.containsKey("clickedIndex")){
                currentPage = bundle.getInt("clickedIndex");
            }
        }
        setContentView(R.layout.activity_images_view);
        findView();
        init();
    }

    private void init(){
        totalTv.setText("/" + imageList.size());
        fragList = new ArrayList<Fragment>();
        for(int i = 0;i < imageList.size();i++){
            ImageViewFragment imageVF = new ImageViewFragment();
            imageVF.setImageUrl(imageList.get(i));
            fragList.add(imageVF);
        }
        //类似缓存
        imageVp.setOffscreenPageLimit(imageList.size());
        imageVp.setAdapter(new ImageViewFPAdapter(getSupportFragmentManager()));
        imageVp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int index) {
                currentPage = index;
                currentTv.setText((index + 1) + "");
                fragList.get(currentPage).onPause();
                if(fragList.get(index).isAdded()){
                    fragList.get(index).onResume();
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        imageVp.setCurrentItem(currentPage);
        currentTv.setText((currentPage + 1) + "");
    }

    protected void findView(){
        imageVp = (ViewPager) findViewById(R.id.images_vp);
        currentTv = (TextView) findViewById(R.id.imageView_current_tv);
        totalTv = (TextView) findViewById(R.id.imageView_total_tv);
    }

    class ImageViewFPAdapter extends FragmentPagerAdapter{
        protected FragmentManager fm;

        public ImageViewFPAdapter(FragmentManager fm){
            super(fm);
            this.fm = fm;
        }

        @Override
        public Fragment getItem(int arg0){
            return fragList.get(arg0);
        }

        @Override
        public int getCount(){
            return fragList.size();
        }
    }

    @Override
    protected void onDestroy(){
        if(fragList.size() > 0){
            for(Fragment fragment : fragList){
                fragment.onDestroy();
            }
            super.onDestroy();
        }
    }

}
