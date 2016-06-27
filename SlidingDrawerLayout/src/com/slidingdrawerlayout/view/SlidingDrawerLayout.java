package com.slidingdrawerlayout.view;

import android.content.Context;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;

/**
 * SlidingDrawerLayout for top and bottom menu.
 * 
 * @author xu(QQ:1396847724)
 * @since 2016.6.30
 * @version 1.0
 * 
 */
public class SlidingDrawerLayout extends ViewGroup {

	// The slidingDrawerLayout child views.
	private View mTopView, mContentView, mBottomView;
	// The tab height.
	private int mTopTabHeight, mBottomTabHeight;
	// Record last x and y points for intercept.
	private ArrayMap<Integer, Float> mLastXForIntercept = new ArrayMap<Integer, Float>();
	private ArrayMap<Integer, Float> mLastYForIntercept = new ArrayMap<Integer, Float>();
	// Whether tab selected.
	private boolean mSelectedTop, mSelectedBottom;
	// Record last y points.
	private ArrayMap<Integer, Float> mLastY = new ArrayMap<Integer, Float>();
	// Whether in smoothing.
	private boolean mIsSmoothing;
	// Whether in event back.
	private boolean mIsInBackEvent;
	// Use for getting speed.
	private VelocityTracker mVelocityTracker;
	// Prevent two menu operating at the same time.
	private boolean mIsInOperating;
	// Whether top last in bottom.
	private boolean mTopLastInBottom;
	// Whether bottom last in top.
	private boolean mBottomLastInTop;

	public SlidingDrawerLayout(Context context) {
		this(context, null);
	}

	public SlidingDrawerLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlidingDrawerLayout(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	// Initialise the slidingDrawerLayout layout

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		// Find child.
		View topView = findView("topView");
		View contentView = findView("contentView");
		View bottomView = findView("bottomView");
		if (topView != null) {
			topView.setClickable(true);
			// Default size.
			setTopTabHeight(50, false);
			mTopView = topView;
		}
		if (contentView != null) {
			mContentView = contentView;
		}
		if (bottomView != null) {
			bottomView.setClickable(true);
			setBottomTabHeight(50, false);
			mBottomView = bottomView;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int specHeight = MeasureSpec.getSize(heightMeasureSpec);
		// Initialise top height.
		if (mTopView != null) {
			int topHeightMeasureSpec = MeasureSpec.makeMeasureSpec(specHeight
					- mBottomTabHeight, MeasureSpec.EXACTLY);
			measureChild(mTopView, widthMeasureSpec, topHeightMeasureSpec);
		}
		// Initialise content height.
		if (mContentView != null) {
			int contentHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
					specHeight - mTopTabHeight - mBottomTabHeight,
					MeasureSpec.EXACTLY);
			measureChild(mContentView, widthMeasureSpec,
					contentHeightMeasureSpec);
		}
		// Initialise bottom height
		if (mBottomView != null) {
			int bottomHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
					specHeight - mTopTabHeight, MeasureSpec.EXACTLY);
			measureChild(mBottomView, widthMeasureSpec, bottomHeightMeasureSpec);
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// Initialise top position.
		if (mTopView != null) {
			int t = -(getMeasuredHeight() - mTopTabHeight - mBottomTabHeight);
			int b = mTopTabHeight;
			mTopView.layout(0, t, getMeasuredWidth(), b);
		}
		// Initialise content position.
		if (mContentView != null) {
			int t = mTopTabHeight;
			int b = getMeasuredHeight() - mBottomTabHeight;
			mContentView.layout(0, t, getMeasuredWidth(), b);
		}
		// Initialise bottom position.
		if (mBottomView != null) {
			int t = getMeasuredHeight() - mBottomTabHeight;
			int b = getMeasuredHeight() + (getMeasuredHeight() - mTopTabHeight);
			mBottomView.layout(0, t, getMeasuredWidth(), b);
		}
	}

	// Handler the slidingDrawerLayout event.

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		int action = event.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mLastXForIntercept.clear();
			mLastYForIntercept.clear();
			// Record the first point.
			mLastXForIntercept.put(event.getPointerId(0), event.getX());
			mLastYForIntercept.put(event.getPointerId(0), event.getY());
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			mLastXForIntercept.clear();
			mLastYForIntercept.clear();
			// Record all points.
			for (int i = 0; i < event.getPointerCount(); i++) {
				int id = event.getPointerId(i);
				float x = event.getX(i);
				float y = event.getY(i);
				mLastXForIntercept.put(id, x);
				mLastYForIntercept.put(id, y);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			// Check all points.
			for (int i = 0; i < event.getPointerCount(); i++) {
				int id = event.getPointerId(i);
				float x = event.getX(i);
				float y = event.getY(i);
				float lastX = mLastXForIntercept.get(id);
				float lastY = mLastYForIntercept.get(id);
				float dx = Math.abs(x - lastX);
				float dy = Math.abs(y - lastY);
				// Check top view.
				if (mTopView != null) {
					float topY = mTopView.getY();
					float topTabY = topY + mTopView.getHeight() - mTopTabHeight;
					if (lastY >= topTabY && lastY <= topTabY + mTopTabHeight) {
						if (dx < dy - 5) {
							mLastY.clear();
							mLastY.put(id, y);
							mSelectedTop = true;
							// Judge again.
							if (!shouldIntercept(false, true)) {
								return false;
							}
							return true;
						}
					}
				}
				// Check Bottom view.
				if (mBottomView != null) {
					float bottomY = mBottomView.getY();
					if (lastY >= bottomY && lastY <= bottomY + mBottomTabHeight) {
						if (dx < dy - 5) {
							mLastY.clear();
							mLastY.put(id, y);
							mSelectedBottom = true;
							// Judge again.
							if (!shouldIntercept(true, false)) {
								return false;
							}
							return true;
						}
					}
				}
				// Record last values.
				mLastXForIntercept.put(id, x);
				mLastYForIntercept.put(id, y);
			}
			break;
		case MotionEvent.ACTION_UP:
			mIsInBackEvent = false;
			// Reset
			mSelectedTop = false;
			mSelectedBottom = false;
			break;
		}
		return super.onInterceptTouchEvent(event);
	}

	private boolean shouldIntercept(boolean top, boolean bottom) {
		// Close tab.
		if (bottom && isBottomOpened()) {
			closeBottom();
			mIsInBackEvent = true;
		}
		if (top && isTopOpened()) {
			closeTop();
			mIsInBackEvent = true;
		}
		if (mIsSmoothing) {
			return false;
		}
		if (mIsInBackEvent) {
			return false;
		}
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		addVelocityTracker(event);
		int action = event.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_MOVE:
			boolean move = true;
			for (int i = 0; i < event.getPointerCount(); i++) {
				int id = event.getPointerId(i);
				float y = event.getY(i);
				if (mLastY.containsKey(id) && move) {
					float lastY = mLastY.get(id);
					float distance = y - lastY;
					// Slide tab.
					if (mSelectedTop) {
						slideTop((int) distance);
					}
					if (mSelectedBottom) {
						slideBottom((int) distance);
					}
					// If slided this time.
					if (distance != 0) {
						move = false;
					}
				}
				mLastY.put(id, y);
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			for (int i = 0; i <= event.getActionIndex(); i++) {
				int id = event.getPointerId(i);
				if (mLastY.containsKey(id)) {
					mLastY.remove(id);
				}
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			mLastY.clear();
			smoothSlide();
			// Reset
			mSelectedTop = false;
			mSelectedBottom = false;
			break;
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
		}
	}

	private void addVelocityTracker(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		} else {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				mVelocityTracker.clear();
			}
		}
		mVelocityTracker.addMovement(event);
	}

	private void slideTop(int distance) {
		if (mTopView != null) {
			float b = mTopView.getY() + mTopView.getHeight() + distance;
			if (b < mTopTabHeight || b + mBottomTabHeight > getHeight()) {
				return;
			}
			float t = b - mTopView.getHeight();
			mTopView.layout(0, (int) t, getWidth(), (int) b);
		}
	}

	private void slideBottom(int distance) {
		if (mBottomView != null) {
			float t = mBottomView.getY() + distance;
			if (t < mTopTabHeight || t + mBottomTabHeight > getHeight()) {
				return;
			}
			float b = t + mBottomView.getHeight();
			mBottomView.layout(0, (int) t, getWidth(), (int) b);
		}
	}

	private void smoothSlide() {
		if (mSelectedTop) {
			smoothSlideTop();
		}
		if (mSelectedBottom) {
			smoothSlideBottom();
		}
	}

	private void smoothSlideTop() {
		if (mTopView != null) {
			final float upY = mTopView.getY();
			float start = mTopTabHeight;
			float end = getHeight() - mBottomTabHeight;
			float bottom = upY + mTopView.getHeight();
			boolean isShow = (bottom - start) / (end - start) >= 0.1f;
			if (mTopLastInBottom) {
				mTopLastInBottom = false;
				isShow = !((bottom - start) / (end - start) <= 0.9f);
			}
			openCloseTop(upY, isShow);
		}
	}

	private void openCloseTop(final float startY, final boolean isShow) {
		mIsSmoothing = true;
		final int timeUnit = 5;
		float minValue = 35;
		final float speedY = getVelocity(timeUnit, minValue);
		post(new Runnable() {
			float y = startY;
			float speed = speedY;

			@Override
			public void run() {
				if (isShow) {
					y += speed;
				} else {
					y -= speed;
				}
				speed += 5;

				float b = y + mTopView.getHeight();
				boolean out = false;
				if (b < mTopTabHeight) {
					b = mTopTabHeight;
					out = true;
					mTopLastInBottom = false;
				}
				if (b + mBottomTabHeight > getHeight()) {
					b = getHeight() - mBottomTabHeight;
					out = true;
					mTopLastInBottom = true;
				}
				float t = b - mTopView.getHeight();
				mTopView.layout(0, (int) t, getWidth(), (int) b);

				if (!out) {
					postDelayed(this, timeUnit);
				} else {
					mIsSmoothing = false;
					mIsInOperating = false;
				}
			}
		});
	}

	private int getVelocity(int timeUnit, float minValue) {
		if (mVelocityTracker == null) {
			return 0;
		}
		mVelocityTracker.computeCurrentVelocity(timeUnit);
		float speed = Math.abs(mVelocityTracker.getYVelocity());
		return (int) (speed <= minValue ? minValue : speed);// Must be int.
	}

	private void smoothSlideBottom() {
		if (mBottomView != null) {
			float upY = mBottomView.getY();
			float start = mTopTabHeight;
			float end = getHeight() - mBottomTabHeight;
			float top = upY;
			boolean isShow = (top - start) / (end - start) <= 0.9f;
			if (mBottomLastInTop) {
				mBottomLastInTop = false;
				isShow = !((top - start) / (end - start) >= 0.1f);
			}
			openCloseBottom(upY, isShow);
		}
	}

	private void openCloseBottom(final float startY, final boolean isShow) {
		mIsSmoothing = true;
		final int timeUnit = 5;
		float minValue = 35;
		final float speedY = getVelocity(timeUnit, minValue);
		post(new Runnable() {
			float y = startY;
			float speed = (int) speedY;

			@Override
			public void run() {
				if (isShow) {
					y -= speed;
				} else {
					y += speed;
				}
				speed += 5;

				float t = y;
				boolean out = false;
				if (t < mTopTabHeight) {
					t = mTopTabHeight;
					out = true;
					mBottomLastInTop = true;
				}
				if (t > getHeight() - mBottomTabHeight) {
					t = getHeight() - mBottomTabHeight;
					out = true;
					mBottomLastInTop = false;
				}
				float b = t + mBottomView.getHeight();
				mBottomView.layout(0, (int) t, getWidth(), (int) b);

				if (!out) {
					postDelayed(this, timeUnit);
				} else {
					mIsSmoothing = false;
					mIsInOperating = false;
				}
			}
		});
	}

	// Prevent two opening at the same time.
	private void open(boolean top, boolean bottom) {
		if (top && bottom) {
			return;
		}
		synchronized (this) {
			if (mIsInOperating) {
				return;
			}
			mIsInOperating = true;
			if (top) {
				openTop();
				return;
			}
			if (bottom) {
				openBottom();
			}
		}
	}

	// Public method for user.

	/**
	 * Close the top view.
	 */
	public void openTop() {
		if (mTopView != null) {
			float startY = mTopView.getY();
			openCloseTop(startY, true);
		}
	}

	/**
	 * Close the top view by sync.
	 */
	public void openTopSync() {
		open(true, false);
	}

	/**
	 * Close the top view.
	 */
	public void closeTop() {
		if (mTopView != null) {
			float startY = mTopView.getY();
			openCloseTop(startY, false);
		}
	}

	/**
	 * Whether top view opened.
	 * 
	 * @return
	 */
	public boolean isTopOpened() {
		if (mTopView != null) {
			float startY = mTopView.getY();
			return startY == 0;
		}
		return false;
	}

	/**
	 * Open the bottom view.
	 */
	public void openBottom() {
		if (mBottomView != null) {
			float startY = mBottomView.getY();
			openCloseBottom(startY, true);
		}
	}

	/**
	 * Open the bottom view by sync.
	 */
	public void openBottomSync() {
		open(false, true);
	}

	/**
	 * Close the bottom view.
	 */
	public void closeBottom() {
		if (mBottomView != null) {
			float startY = mBottomView.getY();
			openCloseBottom(startY, false);
		}
	}

	/**
	 * Whether bottom view opened.
	 * 
	 * @return
	 */
	public boolean isBottomOpened() {
		if (mBottomView != null) {
			float startY = mBottomView.getY();
			return startY == mTopTabHeight;
		}
		return false;
	}

	// Initialise the slidingDrawerLayout params.

	private View findView(String name) {
		String defType = "id";
		String defPackage = getContext().getPackageName();
		int identifier = getResources()
				.getIdentifier(name, defType, defPackage);
		return findViewById(identifier);
	}

	/**
	 * Set the top tab height.
	 * 
	 * @param value
	 * @param isPx
	 *            Whether using pixel for unit.
	 */
	public void setTopTabHeight(int value, boolean isPx) {
		if (isPx) {
			mTopTabHeight = value;
		} else {
			mTopTabHeight = getTabHeight(value);
		}
	}

	/**
	 * Set the bottom tab height.
	 * 
	 * @param value
	 * @param isPx
	 *            Whether using pixel for unit.
	 */
	public void setBottomTabHeight(int value, boolean isPx) {
		if (isPx) {
			mBottomTabHeight = value;
		} else {
			mBottomTabHeight = getTabHeight(value);
		}
	}

	private int getTabHeight(int value) {
		int unit = TypedValue.COMPLEX_UNIT_DIP;
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int height = (int) TypedValue.applyDimension(unit, value, metrics);
		return height;
	}
}
