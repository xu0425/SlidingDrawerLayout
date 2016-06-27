package com.slidingdrawerlayout;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.slidingdrawerlayout.view.SlidingDrawerLayout;

public class MainActivity extends Activity implements OnClickListener {

	private SlidingDrawerLayout mSlidingDrawer;
	private View mTopBtn, mBottomBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findView();
		initView();
	}

	private void findView() {
		mSlidingDrawer = (SlidingDrawerLayout) findViewById(R.id.slidingDrawer);
		mTopBtn = findViewById(R.id.topBtn);
		mBottomBtn = findViewById(R.id.bottomBtn);
	}

	private void initView() {
		Resources res = getResources();
		int topBarSize = (int) res.getDimension(R.dimen.topBarSize);
		int bottomBarSize = (int) res.getDimension(R.dimen.bottomBarSize);
		mSlidingDrawer.setTopTabHeight(topBarSize, true);
		mSlidingDrawer.setBottomTabHeight(bottomBarSize, true);

		mTopBtn.setOnClickListener(this);
		mBottomBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.topBtn) {
			if (mSlidingDrawer.isBottomOpened()) {
				mSlidingDrawer.closeBottom();
			} else {
				if (mSlidingDrawer.isTopOpened()) {
					mSlidingDrawer.closeTop();
				} else {
					mSlidingDrawer.openTopSync();
				}
			}
		} else if (v.getId() == R.id.bottomBtn) {
			if (mSlidingDrawer.isTopOpened()) {
				mSlidingDrawer.closeTop();
			} else {
				if (mSlidingDrawer.isBottomOpened()) {
					mSlidingDrawer.closeBottom();
				} else {
					mSlidingDrawer.openBottomSync();
				}
			}
		}
	}
}
