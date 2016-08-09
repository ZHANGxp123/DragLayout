package com.heim.dragdrawlayout;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.heim.dragdrawlayout.view.DragLayout;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ListView   mLvLeft;
    ImageView  mIvImage;
    ListView   mLvMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLvLeft = (ListView) findViewById(R.id.lv_left);
        mLvMain= (ListView) findViewById(R.id.lv_main);
        mIvImage= (ImageView) findViewById(R.id.iv_image);

        mLvLeft.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Cheeses.sCheeseStrings){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView)view).setTextColor(Color.WHITE);
                return view;
            }
        });

        mLvMain.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,Cheeses.NAMES));

        DragLayout mDl = (DragLayout) findViewById(R.id.dl);
        mDl.setOnStatusChangeListener(new DragLayout.OnStatusChangeListener() {
            @Override
            public void onOpen() {
                Util.showToast(MainActivity.this,"open");
                mLvLeft.smoothScrollToPosition(new Random().nextInt(50));
            }

            @Override
            public void onClose() {
                Util.showToast(MainActivity.this,"close");
                ObjectAnimator animator = ObjectAnimator.ofFloat(mIvImage, "translationX", 0.0f, 15f);
                animator.setInterpolator(new CycleInterpolator(4));
                animator.setDuration(500);
                animator.start();
            }

            @Override
            public void onDraging(float percent) {
                ViewHelper.setAlpha(mIvImage,1-percent);
            }
        });
    }
}
