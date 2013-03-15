/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.preference;

import static android.text.format.DateUtils.getRelativeTimeSpanString;
import static org.mariotaku.twidere.util.HtmlEscapeHelper.toPlainText;
import static org.mariotaku.twidere.util.Utils.formatSameDayTime;
import static org.mariotaku.twidere.util.Utils.getInlineImagePreviewDisplayOptionInt;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.view.holder.StatusViewHolder;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.preference.Preference;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;

public class StatusPreviewPreference extends Preference implements Constants, OnSharedPreferenceChangeListener {

	private static final String TEXT_HTML = "Twidere is an open source twitter client for Android, see <a href='https://github.com/mariotaku/twidere'>github.com/mariotak&#8230;<a/>";

	private final LayoutInflater mInflater;
	private final SharedPreferences mPreferences;
	private StatusViewHolder mHolder;

	public StatusPreviewPreference(final Context context) {
		this(context, null);
	}

	public StatusPreviewPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public StatusPreviewPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mInflater = LayoutInflater.from(context);
		mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
		if (mHolder == null) return;
		if (PREFERENCE_KEY_TEXT_SIZE.equals(key)) {
			setTextSize();
		} else if (PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE.equals(key)) {
			setProfileImage();
		} else if (PREFERENCE_KEY_INLINE_IMAGE_PREVIEW_DISPLAY_OPTION.equals(key)) {
			setImagePreview();
		} else if (PREFERENCE_KEY_SHOW_ABSOLUTE_TIME.equals(key)) {
			setTime();
		} else if (PREFERENCE_KEY_NAME_DISPLAY_OPTION.equals(key)) {
			setName();
		} else if (PREFERENCE_KEY_LINK_HIGHLIGHTING.equals(key)) {
			setText();
		}
	}

	@Override
	protected void onBindView(final View view) {
		mHolder = new StatusViewHolder(view);
		mHolder.profile_image.setImageResource(R.drawable.ic_launcher);
		mHolder.reply_retweet_status.setVisibility(View.GONE);
		mHolder.setShowAsGap(false);
		mHolder.setIsMyStatus(false);
		setText();
		setName();
		setImagePreview();
		setProfileImage();
		setTextSize();
		setTime();
		setDetailsAndMedia();
		super.onBindView(view);
	}

	@Override
	protected View onCreateView(final ViewGroup parent) {
		return mInflater.inflate(R.layout.status_list_item, null);
	}

	private void setDetailsAndMedia() {
		mHolder.image_preview.setImageResource(R.drawable.twidere_promotional_graphic);
		final boolean fast_timeline_processing = mPreferences
				.getBoolean(PREFERENCE_KEY_FAST_TIMELINE_PROCESSING, false);
		mHolder.time.setCompoundDrawablesWithIntrinsicBounds(0, 0, fast_timeline_processing ? 0
				: R.drawable.ic_indicator_has_media, 0);
	}

	private void setImagePreview() {
		if (mHolder == null) return;
		final String option_string = mPreferences.getString(PREFERENCE_KEY_INLINE_IMAGE_PREVIEW_DISPLAY_OPTION,
				INLINE_IMAGE_PREVIEW_DISPLAY_OPTION_NONE);
		final boolean fast_timeline_processing = mPreferences
				.getBoolean(PREFERENCE_KEY_FAST_TIMELINE_PROCESSING, false);
		final int option = getInlineImagePreviewDisplayOptionInt(option_string);

		mHolder.image_preview_container.setVisibility(!fast_timeline_processing
				&& option != INLINE_IMAGE_PREVIEW_DISPLAY_OPTION_CODE_NONE ? View.VISIBLE : View.GONE);
		final MarginLayoutParams lp = (MarginLayoutParams) mHolder.image_preview_frame.getLayoutParams();
		if (option == INLINE_IMAGE_PREVIEW_DISPLAY_OPTION_CODE_LARGE) {
			lp.width = LayoutParams.MATCH_PARENT;
			lp.leftMargin = 0;
			mHolder.image_preview_frame.setLayoutParams(lp);
		} else if (option == INLINE_IMAGE_PREVIEW_DISPLAY_OPTION_CODE_SMALL) {
			final Resources res = getContext().getResources();
			lp.width = res.getDimensionPixelSize(R.dimen.image_preview_width);
			lp.leftMargin = (int) (res.getDisplayMetrics().density * 16);
			mHolder.image_preview_frame.setLayoutParams(lp);
		}
	}

	private void setName() {
		final String option = mPreferences.getString(PREFERENCE_KEY_NAME_DISPLAY_OPTION, NAME_DISPLAY_OPTION_BOTH);
		if (NAME_DISPLAY_OPTION_NAME.equals(option)) {
			mHolder.name.setText("Twidere Project");
			mHolder.screen_name.setText(null);
			mHolder.screen_name.setVisibility(View.GONE);
		} else if (NAME_DISPLAY_OPTION_SCREEN_NAME.equals(option)) {
			mHolder.name.setText("@twidere_project");
			mHolder.screen_name.setText(null);
			mHolder.screen_name.setVisibility(View.GONE);
		} else {
			mHolder.name.setText("Twidere Project");
			mHolder.screen_name.setText("@twidere_project");
			mHolder.screen_name.setVisibility(View.VISIBLE);
		}

	}

	private void setProfileImage() {
		if (mHolder == null) return;
		mHolder.profile_image
				.setVisibility(mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true) ? View.VISIBLE
						: View.GONE);
	}

	private void setText() {
		if (mPreferences == null) return;
		final boolean fast_timeline_processing = mPreferences
				.getBoolean(PREFERENCE_KEY_FAST_TIMELINE_PROCESSING, false);
		if (mPreferences.getBoolean(PREFERENCE_KEY_LINK_HIGHLIGHTING, false) && !fast_timeline_processing) {
			mHolder.text.setText(Html.fromHtml(TEXT_HTML));
			final TwidereLinkify linkify = new TwidereLinkify(null);
			linkify.applyAllLinks(mHolder.text, 0, false);
			mHolder.text.setMovementMethod(null);
		} else {
			mHolder.text.setText(toPlainText(TEXT_HTML));
		}

	}

	private void setTextSize() {
		if (mHolder == null) return;
		mHolder.setTextSize(mPreferences.getInt(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE));
	}

	private void setTime() {
		if (mHolder == null) return;
		if (mPreferences.getBoolean(PREFERENCE_KEY_SHOW_ABSOLUTE_TIME, false)) {
			mHolder.time.setText(formatSameDayTime(getContext(), System.currentTimeMillis() - 360000));
		} else {
			mHolder.time.setText(getRelativeTimeSpanString(System.currentTimeMillis() - 360000));
		}
	}

}
