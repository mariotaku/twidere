package org.mariotaku.menubar;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.mariotaku.internal.menu.MenuImpl;
import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.twidere.util.ViewAccessor;

public class MenuBar extends LinearLayout implements MenuItem.OnMenuItemClickListener {

	private final Menu mMenu;
	private final Context mContext;
	private OnMenuItemClickListener mItemClickListener;

	private PopupMenu mPopupMenu;

	private boolean mStretchButtonsEnabled = true;

	public MenuBar(final Context context) {
		this(context, null);
	}

	public MenuBar(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		if (!hasBackground(attrs)) {
			final TypedArray a = context.obtainStyledAttributes(null, new int[] { android.R.attr.backgroundSplit },
					android.R.attr.actionBarStyle, android.R.style.Widget_Holo_ActionBar);
			ViewAccessor.setBackground(this, a.getDrawable(0));
			a.recycle();
		}
		mContext = context;
		mMenu = new MenuImpl(context);
		setOrientation(HORIZONTAL);
	}

	public Menu getMenu() {
		return mMenu;
	}

	public MenuInflater getMenuInflater() {
		return new MenuInflater(mContext);
	}

	public void inflate(final int menuRes) {
		mMenu.clear();
		new MenuInflater(mContext).inflate(menuRes, mMenu);
	}

	@Override
	public boolean onMenuItemClick(final MenuItem item) {
		if (mItemClickListener != null) return mItemClickListener.onMenuItemClick(item);
		return false;
	}

	/**
	 * Set listener for action item clicked.
	 * 
	 * @param listener Listener
	 */
	public void setOnMenuItemClickListener(final OnMenuItemClickListener listener) {
		mItemClickListener = listener;
	}

	public void setStretchButtonsEnabled(final boolean enabled) {
		if (mStretchButtonsEnabled == enabled) return;
		mStretchButtonsEnabled = enabled;
		show();
	}

	public void show() {
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		removeAllViews();
		for (final MenuItem item : ((MenuImpl) mMenu).getMenuItems()) {
			if (item.isVisible()) {
				addMenuButton(item);
			}
		}
		invalidate();
	}

	@Override
	protected void onDetachedFromWindow() {
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		super.onDetachedFromWindow();
	}

	private View addMenuButton(final MenuItem item) {
		final LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		params.weight = 1;

		final View actionView = item.getActionView(), view;
		if (actionView != null) {
			view = actionView;
		} else {
			final ImageButton actionButton = new ImageButton(mContext, null, android.R.attr.actionButtonStyle);
			actionButton.setImageDrawable(item.getIcon());
			actionButton.setScaleType(ScaleType.CENTER);
			actionButton.setContentDescription(item.getTitle());
			actionButton.setEnabled(item.isEnabled());
			actionButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(final View view) {
					if (!item.isEnabled()) return;
					if (item.hasSubMenu()) {
						mPopupMenu = PopupMenu.getInstance(mContext, view);
						mPopupMenu.setOnMenuItemClickListener(MenuBar.this);
						mPopupMenu.setMenu(item.getSubMenu());
						mPopupMenu.show();
					} else {
						if (mItemClickListener != null) {
							mItemClickListener.onMenuItemClick(item);
						}
					}
				}
			});
			actionButton.setOnLongClickListener(new View.OnLongClickListener() {

				@Override
				public boolean onLongClick(final View v) {
					final Toast t = Toast.makeText(mContext, item.getTitle(), Toast.LENGTH_SHORT);
					final int[] screenPos = new int[2];
					v.getLocationOnScreen(screenPos);

					final int height = v.getHeight();

					t.setGravity(Gravity.TOP | Gravity.LEFT, screenPos[0], (int) (screenPos[1] - height * 1.5));
					t.show();
					return true;
				}
			});
			view = actionButton;
		}
		addView(view, params);
		return view;
	}

	private static boolean hasBackground(final AttributeSet attrs) {
		final int count = attrs.getAttributeCount();
		for (int i = 0; i < count; i++) {
			if (attrs.getAttributeNameResource(i) == android.R.attr.background) return true;
		}
		return false;
	}

}
