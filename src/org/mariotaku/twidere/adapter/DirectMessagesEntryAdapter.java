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
import static org.mariotaku.twidere.util.Utils.getUserColor;
import static org.mariotaku.twidere.util.Utils.parseURL;

import java.text.DateFormat;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.DirectMessageEntryViewHolder;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry;
import org.mariotaku.twidere.util.BaseAdapterInterface;
import org.mariotaku.twidere.util.LazyImageLoader;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;

public class DirectMessagesEntryAdapter extends SimpleCursorAdapter implements BaseAdapterInterface {

	private boolean mDisplayProfileImage, mShowAccountColor, mShowAbsoluteTime, mFastProcessingEnabled;
	private final LazyImageLoader mProfileImageLoader;
	private float mTextSize;
	private final boolean mDisplayHiResProfileImage;

	public DirectMessagesEntryAdapter(final Context context, final LazyImageLoader loader) {
		super(context, R.layout.direct_messages_entry_item, null, new String[0], new int[0], 0);
		mProfileImageLoader = loader;
		mDisplayHiResProfileImage = context.getResources().getBoolean(R.bool.hires_profile_image);
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		final DirectMessageEntryViewHolder holder = (DirectMessageEntryViewHolder) view.getTag();

		final long account_id = cursor.getLong(ConversationsEntry.IDX_ACCOUNT_ID);
		final long conversation_id = cursor.getLong(ConversationsEntry.IDX_CONVERSATION_ID);
		final long message_timestamp = cursor.getLong(ConversationsEntry.IDX_MESSAGE_TIMESTAMP);
		final boolean is_outgoing = cursor.getInt(ConversationsEntry.IDX_IS_OUTGOING) == 1;

		final String name = cursor.getString(IDX_NAME);
		final String screen_name = cursor.getString(IDX_SCREEN_NAME);

		holder.setAccountColorEnabled(mShowAccountColor);

		if (mShowAccountColor) {
			holder.setAccountColor(getAccountColor(mContext, account_id));
		}

		if (!mFastProcessingEnabled) {
			holder.setUserColor(getUserColor(mContext, conversation_id));
		} else {
			holder.setUserColor(Color.TRANSPARENT);
		}

		holder.setTextSize(mTextSize);
		holder.name.setText(name);
		holder.screen_name.setText("@" + screen_name);
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
				mProfileImageLoader.displayImage(parseURL(profile_image_url_string), holder.profile_image);
			}
		}

		super.bindView(view, context, cursor);
	}

	public long findAccountId(final long id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			if (getItemId(i) == id) return ((Cursor) getItem(i)).getLong(IDX_ACCOUNT_ID);
		}
		return -1;
	}

	public long findConversationId(final long id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			if (getItemId(i) == id) return ((Cursor) getItem(i)).getLong(IDX_CONVERSATION_ID);
		}
		return -1;
	}

	@Override
	public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
		final View view = super.newView(context, cursor, parent);
		final Object tag = view.getTag();
		if (!(tag instanceof DirectMessageEntryViewHolder)) {
			view.setTag(new DirectMessageEntryViewHolder(view));
		}
		return view;
	}

	@Override
	public void setDisplayProfileImage(final boolean display) {
		if (display != mDisplayProfileImage) {
			mDisplayProfileImage = display;
			notifyDataSetChanged();
		}
	}

	public void setFastProcessingEnabled(final boolean enabled) {
		if (enabled != mFastProcessingEnabled) {
			mFastProcessingEnabled = enabled;
			notifyDataSetChanged();
		}
	}

	public void setShowAbsoluteTime(final boolean show) {
		if (show != mShowAbsoluteTime) {
			mShowAbsoluteTime = show;
			notifyDataSetChanged();
		}
	}

	public void setShowAccountColor(final boolean show) {
		if (show != mShowAccountColor) {
			mShowAccountColor = show;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setTextSize(final float text_size) {
		if (text_size != mTextSize) {
			mTextSize = text_size;
			notifyDataSetChanged();
		}
	}
}
