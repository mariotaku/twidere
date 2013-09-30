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

import static org.mariotaku.twidere.util.HtmlEscapeHelper.toPlainText;
import static org.mariotaku.twidere.util.Utils.getDefaultTextSize;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.view.holder.StatusViewHolder;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.Preference;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class StatusPreviewPreference extends Preference implements Constants, OnSharedPreferenceChangeListener {

	private static final String NAME = "Twidere Project";
	private static final String SCREEN_NAME = "@TwidereProject";
	private static final String TEXT_HTML = "Twidere is an open source twitter client for Android, see <a href='https://github.com/mariotaku/twidere'>github.com/mariotak&#8230;<a/>";

	private final LayoutInflater mInflater;
	private final SharedPreferences mPreferences;
	private final TwidereLinkify mLinkify;
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
		mLinkify = new TwidereLinkify(null);
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
		} else if (PREFERENCE_KEY_DISPLAY_IMAGE_PREVIEW.equals(key)) {
			setImagePreview();
		} else if (PREFERENCE_KEY_SHOW_ABSOLUTE_TIME.equals(key)) {
			setTime();
		} else if (PREFERENCE_KEY_NAME_DISPLAY_OPTION.equals(key)) {
			setName();
		} else if (PREFERENCE_KEY_LINK_HIGHLIGHTING.equals(key)) {
			setText();
		} else if (PREFERENCE_KEY_LINK_UNDERLINE_ONLY.equals(key)) {
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
		mHolder.image_preview.setImageResource(R.drawable.twidere_feature_graphic);
		mHolder.time.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_indicator_has_media, 0);
	}

	private void setImagePreview() {
		if (mHolder == null) return;
		final boolean display = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_IMAGE_PREVIEW, false);
		mHolder.image_preview_container.setVisibility(display ? View.VISIBLE : View.GONE);
		mHolder.image_preview_progress.setVisibility(View.GONE);
	}

	private void setName() {
		final String option = mPreferences.getString(PREFERENCE_KEY_NAME_DISPLAY_OPTION, NAME_DISPLAY_OPTION_BOTH);
		if (NAME_DISPLAY_OPTION_NAME.equals(option)) {
			mHolder.name.setText(NAME);
			mHolder.screen_name.setText(null);
			mHolder.screen_name.setVisibility(View.GONE);
		} else if (NAME_DISPLAY_OPTION_SCREEN_NAME.equals(option)) {
			mHolder.name.setText(SCREEN_NAME);
			mHolder.screen_name.setText(null);
			mHolder.screen_name.setVisibility(View.GONE);
		} else {
			mHolder.name.setText(NAME);
			mHolder.screen_name.setText(SCREEN_NAME);
			mHolder.screen_name.setVisibility(View.VISIBLE);
		}
	}

	private void setProfileImage() {
		if (mHolder == null) return;
		final boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		mHolder.profile_image.setVisibility(display_profile_image ? View.VISIBLE : View.GONE);
	}

	private void setText() {
		if (mPreferences == null) return;
		final boolean underline_only = mPreferences.getBoolean(PREFERENCE_KEY_LINK_UNDERLINE_ONLY, false);
		if (underline_only) {
			mLinkify.setHighlightStyle(TwidereLinkify.HIGHLIGHT_STYLE_UNDERLINE);
		} else {
			mLinkify.setHighlightStyle(TwidereLinkify.HIGHLIGHT_STYLE_COLOR);
		}
		if (mPreferences.getBoolean(PREFERENCE_KEY_LINK_HIGHLIGHTING, false)) {
			mHolder.text.setText(Html.fromHtml(TEXT_HTML));
			// TODO
			// mLinkify.applyAllLinks(mHolder.text, 0, false);
			mLinkify.applyUserProfileLink(mHolder.name, 0, 0, SCREEN_NAME);
			mLinkify.applyUserProfileLink(mHolder.screen_name, 0, 0, SCREEN_NAME);
			// mHolder.text.setMovementMethod(null);
			mHolder.name.setMovementMethod(null);
			mHolder.screen_name.setMovementMethod(null);
		} else {
			mHolder.text.setText(toPlainText(TEXT_HTML));
		}
	}

	private void setTextSize() {
		if (mHolder == null) return;
		mHolder.setTextSize(mPreferences.getInt(PREFERENCE_KEY_TEXT_SIZE, getDefaultTextSize(getContext())));
	}

	private void setTime() {
		if (mHolder == null) return;
		mHolder.time.setTime(System.currentTimeMillis() - 360000);
	}

}
