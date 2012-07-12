package org.mariotaku.twidere.adapter;

import static org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry.IDX_CONVERSATION_ID;
import static org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry.IDX_PROFILE_IMAGE_URL;
import static org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry.IDX_SCREEN_NAME;
import static org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry.IDX_TEXT;
import static org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry.IDX_NAME;
import static org.mariotaku.twidere.util.Utils.formatToShortTimeString;
import static org.mariotaku.twidere.util.Utils.parseURL;

import java.net.URL;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.DMConversationsEntryViewHolder;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry;
import org.mariotaku.twidere.util.LazyImageLoader;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;

public class DirectMessagesEntryAdapter extends SimpleCursorAdapter {

	private boolean mDisplayProfileImage, mDisplayName;
	private final LazyImageLoader mImageLoader;
	private float mTextSize;

	public DirectMessagesEntryAdapter(Context context, LazyImageLoader loader) {
		super(context, R.layout.direct_messages_entry_item, null, new String[0], new int[0], 0);
		mImageLoader = loader;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final DMConversationsEntryViewHolder holder = (DMConversationsEntryViewHolder) view.getTag();

		final long message_timestamp = cursor.getLong(ConversationsEntry.IDX_MESSAGE_TIMESTAMP);
		final boolean is_outgoing = cursor.getInt(ConversationsEntry.IDX_IS_OUTGOING) == 1;

		final String name = mDisplayName ? cursor.getString(IDX_NAME) : cursor.getString(IDX_SCREEN_NAME);
		final URL profile_image_url = parseURL(cursor.getString(IDX_PROFILE_IMAGE_URL));

		holder.setTextSize(mTextSize);
		holder.name.setText(name);
		holder.text.setText(cursor.getString(IDX_TEXT));
		holder.time.setText(formatToShortTimeString(mContext, message_timestamp));
		holder.time.setCompoundDrawablesWithIntrinsicBounds(0, 0, is_outgoing ? R.drawable.ic_indicator_outgoing
				: R.drawable.ic_indicator_incoming, 0);
		holder.profile_image.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
		if (mDisplayProfileImage) {
			mImageLoader.displayImage(profile_image_url, holder.profile_image);
		}

		super.bindView(view, context, cursor);
	}

	public long findConversationId(long id) {
		for (int i = 0; i < getCount(); i++) {
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

	public void setDisplayName(boolean display) {
		if (display != mDisplayName) {
			mDisplayName = display;
			notifyDataSetChanged();
		}
	}

	public void setDisplayProfileImage(boolean display) {
		if (display != mDisplayProfileImage) {
			mDisplayProfileImage = display;
			notifyDataSetChanged();
		}
	}

	public void setTextSize(float text_size) {
		if (text_size != mTextSize) {
			mTextSize = text_size;
			notifyDataSetChanged();
		}
	}
}
