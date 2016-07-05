package com.viewpagerindicator;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.viewpagerindicator.view.ViewPagerIndicator;
import com.viewpagerindicator.view.VpSimpleFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends FragmentActivity {

	private ViewPager mViewPager;
	private ViewPagerIndicator mIndicator;

	private List<String> mTiles = Arrays.asList("导航1", "导航2", "导航3","导航4",
			"导航5", "导航6","导航7", "导航8", "导航9");
	private List<VpSimpleFragment> mContents = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main2);

		for (String title : mTiles) {
			VpSimpleFragment fragment = VpSimpleFragment.newInstance(title);
			mContents.add(fragment);
		}

		mViewPager = (ViewPager) findViewById(R.id.id_viewpager);
		mIndicator = (ViewPagerIndicator) findViewById(R.id.id_indicator);

		//mIndicator.setVisibleTabCount(3);
		mIndicator.setTabItemTitles(mTiles);
		mIndicator.setViewPager(mViewPager, 0);

		mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
			@Override
			public Fragment getItem(int position) {
				return mContents.get(position);
			}

			@Override
			public int getCount() {
				return mContents.size();
			}
		});
	}
}


