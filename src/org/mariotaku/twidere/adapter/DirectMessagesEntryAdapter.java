package org.mariotaku.twidere.adapter;

import static android.text.format.DateUtils.formatSameDayTime;
import static android.text.format.DateUtils.getRelativeTimeSpanString;
import static org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry.IDX_ACCOUNT_ID;
import static org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry.IDX_CONVERSATION_ID;
import static org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry.IDX_NAME;
import static org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry.IDX_PROFILE_IMAGE_URL;
import static org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry.IDX_SCREEN_NAME;
import static org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry.IDX_TEXT;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getBiggerTwitterProfileImage;
import static org.mariotaku.twidere.util.Utils.getNormalTwitterProfileImage;
import static org.mariotaku.twidere.util.Utils.parseURL;

import java.text.DateFormat;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.DMConversationsEntryViewHolder;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry;
import org.mariotaku.twidere.util.BaseAdapterInterface;
import org.mariotaku.twidere.util.LazyImageLoader;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;

public class DirectMessagesEntryAdapter extends SimpleCursorAdapter implements BaseAdapterInterface {

	private boolean mDisplayProfileImage, mDisplayHiResProfileImage, mDisplayName, mShowAccountColor,
			mShowAbsoluteTime;
	private final LazyImageLoader mProfileImageLoader;
	private float mTextSize;

	public DirectMessagesEntryAdapter(Context context, LazyImageLoader loader) {
		super(context, R.layout.direct_messages_entry_item, null, new String[0], new int[0], 0);
		mProfileImageLoader = loader;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final DMConversationsEntryViewHolder holder = (DMConversationsEntryViewHolder) view.getTag();

		final long account_id = cursor.getLong(ConversationsEntry.IDX_ACCOUNT_ID);
		final long message_timestamp = cursor.getLong(ConversationsEntry.IDX_MESSAGE_TIMESTAMP);
		final boolean is_outgoing = cursor.getInt(ConversationsEntry.IDX_IS_OUTGOING) == 1;

		final String name = mDisplayName ? cursor.getString(IDX_NAME) : cursor.getString(IDX_SCREEN_NAME);

		holder.setAccountColorEnabled(mShowAccountColor);

		if (mShowAccountColor) {
			holder.setAccountColor(getAccountColor(mContext, account_id));
		}

		holder.setTextSize(mTextSize);
		holder.name.setText(name);
		holder.text.setText(cursor.getString(IDX_TEXT));
		if (mShowAbsoluteTime) {
			holder.time.setText(formatSameDayTime(message_timestamp, System.currentTimeMillis(), DateFormat.MEDIUM,
					DateFormat.SHORT));
		} else {
			holder.time.setText(getRelativeTimeSpanString(message_timestamp));
		}
		holder.time.setCompoundDrawablesWithIntrinsicBounds(0, 0, is_outgoing ? R.drawable.ic_indicator_outgoing
				: R.drawable.ic_indicator_incoming, 0);
		holder.profile_image.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
		if (mDisplayProfileImage) {
			final String profile_image_url_string = cursor.getString(IDX_PROFILE_IMAGE_URL);
			if (mDisplayHiResProfileImage) {
				mProfileImageLoader.displayImage(parseURL(getBiggerTwitterProfileImage(profile_image_url_string)),
						holder.profile_image);
			} else {
				mProfileImageLoader.displayImage(parseURL(getNormalTwitterProfileImage(profile_image_url_string)),
						holder.profile_image);
			}
		}

		super.bindView(view, context, cursor);
	}

	public long findAccountId(long id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			if (getItemId(i) == id) return ((Cursor) getItem(i)).getLong(IDX_ACCOUNT_ID);
		}
		return -1;
	}

	public long findConversationId(long id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			if (getItemId(i) == id) return ((Cursor) getItem(i)).getLong(IDX_CONVERSATION_ID);
		}
		return -1;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View view = super.newView(context, cursor, parent);
		final Object tag = view.getTag();
		if (!(tag instanceof DMConversationsEntryViewHolder)) {
			view.setTag(new DMConversationsEntryViewHolder(view, context));
		}
		return view;
	}

	@Override
	public void setDisplayHiResProfileImage(boolean display) {
		if (display != mDisplayHiResProfileImage) {
			mDisplayHiResProfileImage = display;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setDisplayName(boolean display) {
		if (display != mDisplayName) {
			mDisplayName = display;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setDisplayProfileImage(boolean display) {
		if (display != mDisplayProfileImage) {
			mDisplayProfileImage = display;
			notifyDataSetChanged();
		}
	}

	public void setShowAbsoluteTime(boolean show) {
		if (show != mShowAbsoluteTime) {
			mShowAbsoluteTime = show;
			notifyDataSetChanged();
		}
	}

	public void setShowAccountColor(boolean show) {
		if (show != mShowAccountColor) {
			mShowAccountColor = show;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setTextSize(float text_size) {
		if (text_size != mTextSize) {
			mTextSize = text_size;
			notifyDataSetChanged();
		}
	}
}
