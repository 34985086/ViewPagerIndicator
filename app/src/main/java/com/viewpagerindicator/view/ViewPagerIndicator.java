package com.viewpagerindicator.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.viewpagerindicator.R;

import java.util.List;

/**
 * Created by gavin on 2016-07-04.
 */
public class ViewPagerIndicator extends LinearLayout {
	private static final String TAG = "ViewPagerIndicator";
	private static final float RADIO_TRIANGLE_WIDTH = 1 / 6F;
	/**
	 * 三角形底边的最大宽度
	 */
	private final int DIMENSION_TRIANGLE_WIDTH_MAX = (int) (getScreenWidth() / 3 * RADIO_TRIANGLE_WIDTH);

	private static final int COLOR_TEXT_NORMAL = 0x77ffffff;
	private static final int COLOR_TEXT_HIGHLIGHT = 0xffffffff;

	private static final int COUNT_DEFAULT_TAB = 4;

	private int mTriangleWidth;
	private int mTriangleHeight;
	private int mInitTranslationX;
	private int mTranslationX = 0;
	private int mTabVisibleCount;

	private int mLineStartX;
	private int mLineStartY;

	private List<String> mTitles;
	private Paint mPaint;
	private Path mPath;
	private ViewPager mViewPager;

	private PageOnChangeListener mLister;

	public ViewPagerIndicator(Context context) {
		this(context, null);
	}

	public ViewPagerIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		//获取可见tab的数量
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ViewPagerIndicator);
		mTabVisibleCount = ta.getInt(R.styleable.ViewPagerIndicator_visible_tab_count, COUNT_DEFAULT_TAB);
		if (mTabVisibleCount < 0) {
			mTabVisibleCount = COUNT_DEFAULT_TAB;
		}
		ta.recycle();

		//初始化画笔
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.parseColor("#ffffffff"));
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setPathEffect(new CornerPathEffect(3));
	}

	public void setOnPageChangeListener(PageOnChangeListener listener) {
		this.mLister = listener;
	}

	/**
	 * 设置关联的ViewPager
	 *
	 * @param viewPager
	 * @param pos
	 */
	public void setViewPager(ViewPager viewPager, final int pos) {
		mViewPager = viewPager;
		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				scroll(position, positionOffset);
				if (mLister != null) {
					mLister.onPageScrolled(position, positionOffset, positionOffsetPixels);
				}
			}

			@Override
			public void onPageSelected(int position) {
				highLightTextView(position);
				if (mLister != null) {
					mLister.onPageSelected(position);
				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {
				if (mLister != null) {
					mLister.onPageScrollStateChanged(state);
				}
			}
		});

		mViewPager.setCurrentItem(pos);
		highLightTextView(pos);
	}

	/**
	 * 设置可见tab的数量
	 *
	 * @param count
	 */
	public void setVisibleTabCount(int count) {
		mTabVisibleCount = count;
	}

	public void setTabItemTitles(List<String> titles) {
		if (titles != null && titles.size() > 0) {
			removeAllViews();
			mTitles = titles;
			for (String title : mTitles) {
				addView(generateTextView(title));
			}

			setItemClickEvent();
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {

		canvas.save();

		canvas.translate(mInitTranslationX + mTranslationX, getHeight() + 2);
		canvas.drawPath(mPath, mPaint);

		canvas.restore();
		//draw seperator line
		int tabWidth = getWidth() / mTabVisibleCount;
		for (int i = 1; i < getChildCount(); i++) {
			canvas.drawLine(tabWidth * i, 30, tabWidth * i, 60, mPaint);
		}

		super.dispatchDraw(canvas);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		mTriangleWidth = (int) (w / mTabVisibleCount * RADIO_TRIANGLE_WIDTH);
		mTriangleWidth = Math.min(mTriangleWidth, DIMENSION_TRIANGLE_WIDTH_MAX);
		mInitTranslationX = w / mTabVisibleCount / 2 - mTriangleWidth / 2;

		initTriangle();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		int count = getChildCount();
		if (count == 0) return;
		for (int i = 0; i < count; i++) {
			View view = getChildAt(i);
			LinearLayout.LayoutParams lp = (LayoutParams) view.getLayoutParams();
			lp.weight = 0;
			lp.width = getScreenWidth() / mTabVisibleCount;
			view.setLayoutParams(lp);
		}

		setItemClickEvent();
	}

	/**
	 * 获取屏幕宽度
	 *
	 * @author gavin
	 * @time 2016-07-05 13:57
	 */
	private int getScreenWidth() {
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);

		return outMetrics.widthPixels;
	}

	private void initTriangle() {
		mTriangleHeight = mTriangleWidth / 2;
		mPath = new Path();
		mPath.moveTo(0, 0);
		mPath.lineTo(mTriangleWidth, 0);
		mPath.lineTo(mTriangleWidth / 2, -mTriangleHeight);
		mPath.close();
	}

	private void scroll(int position, float offset) {
		Log.d(TAG, "scroll() called with: " + "position = [" + position + "], offset = [" + offset + "]");
		int tabWidth = getWidth() / mTabVisibleCount;
		mTranslationX = (int) ((offset + position) * tabWidth);
		//容器移动,在tab处于移动至最后一个时
		int count = getChildCount();
		if (count > mTabVisibleCount &&
				position >= (mTabVisibleCount - 2) &&
				position < (count - 2)) {

			int x;
			if (mTabVisibleCount != 1) {
				x = (position - (mTabVisibleCount - 2)) * tabWidth + (int) (tabWidth * offset);
			} else {
				x = position * tabWidth + (int) (tabWidth * offset);
			}
			this.scrollTo(x, 0);
		}
		invalidate();
	}

	/**
	 * 根据title创建tab
	 *
	 * @param title
	 * @return
	 */
	private View generateTextView(String title) {
		TextView tv = new TextView(getContext());
		LinearLayout.LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		lp.width = getScreenWidth() / mTabVisibleCount;
		tv.setText(title);
		tv.setGravity(Gravity.CENTER);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv.setTextColor(COLOR_TEXT_NORMAL);
		tv.setLayoutParams(lp);

		return tv;
	}

	/**
	 * 高亮某个tab的文本
	 *
	 * @param pos
	 */
	private void highLightTextView(int pos) {
		for (int i = 0; i < getChildCount(); i++) {
			View view = getChildAt(i);
			if(view instanceof TextView){
				if(i == pos){
					((TextView) view).setTextColor(COLOR_TEXT_HIGHLIGHT);
				}else{
					((TextView) view).setTextColor(COLOR_TEXT_NORMAL);
				}
			}
		}
	}

	/**
	 * 设置tab的点击事件
	 */
	private void setItemClickEvent() {
		int cCount = getChildCount();
		for (int i = 0; i < cCount; i++) {
			final int item = i;
			View view = getChildAt(i);
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mViewPager.setCurrentItem(item);
				}
			});
		}
	}

	public interface PageOnChangeListener {

		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

		public void onPageSelected(int position);

		public void onPageScrollStateChanged(int state);

	}
}


