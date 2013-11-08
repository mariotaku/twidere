package org.mariotaku.twidere.util;

import org.mariotaku.querybuilder.Columns;
import org.mariotaku.querybuilder.Columns.Column;
import org.mariotaku.querybuilder.OrderBy;
import org.mariotaku.querybuilder.SQLQueryBuilder;
import org.mariotaku.querybuilder.Selectable;
import org.mariotaku.querybuilder.Tables;
import org.mariotaku.querybuilder.Where;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.Conversation;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.Inbox;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.Outbox;

public class TwidereQueryBuilder {

	public static final class ConversationQueryBuilder {

		public static final String buildByConversationId(final String[] projection, final long account_id,
				final long conversation_id, final String selection, final String sortOrder) {
			final SQLQueryBuilder qb = new SQLQueryBuilder();
			final Selectable select = Utils.getColumnsFromProjection(projection);
			qb.select(select);
			qb.from(new Tables(Inbox.TABLE_NAME));
			final Where account_id_where = new Where(String.format("%s = %d", DirectMessages.ACCOUNT_ID, account_id));
			final Where sender_where = new Where(String.format("%s = %d", DirectMessages.SENDER_ID, conversation_id))
					.and(account_id_where);
			final Where recipient_where = new Where(String.format("%s = %d", DirectMessages.RECIPIENT_ID,
					conversation_id)).and(account_id_where);
			if (selection != null) {
				qb.where(new Where(selection).and(sender_where));
			} else {
				qb.where(sender_where);
			}
			qb.union();
			qb.select(select);
			qb.from(new Tables(Outbox.TABLE_NAME));
			if (selection != null) {
				qb.where(new Where(selection).and(recipient_where));
			} else {
				qb.where(recipient_where);
			}
			qb.orderBy(new OrderBy(sortOrder != null ? sortOrder : Conversation.DEFAULT_SORT_ORDER));
			return qb.build().getSQL();
		}

		public static final String buildByScreenName(final String[] projection, final long account_id,
				final String screen_name, final String selection, final String sortOrder) {
			final SQLQueryBuilder qb = new SQLQueryBuilder();
			final Selectable select = Utils.getColumnsFromProjection(projection);
			qb.select(select);
			qb.from(new Tables(Inbox.TABLE_NAME));
			final Where account_id_where = new Where(String.format("%s = %d", Conversation.ACCOUNT_ID, account_id));
			final Where sender_where = new Where(String.format("%s = '%s'", Conversation.SENDER_SCREEN_NAME,
					screen_name)).and(account_id_where);
			final Where recipient_where = new Where(String.format("%s = '%s'", Conversation.RECIPIENT_SCREEN_NAME,
					screen_name)).and(account_id_where);
			if (selection != null) {
				qb.where(new Where(selection).and(sender_where));
			} else {
				qb.where(sender_where);
			}
			qb.union();
			qb.select(select);
			qb.from(new Tables(Outbox.TABLE_NAME));
			if (selection != null) {
				qb.where(new Where(selection).and(recipient_where));
			} else {
				qb.where(recipient_where);
			}
			qb.orderBy(new OrderBy(sortOrder != null ? sortOrder : Conversation.DEFAULT_SORT_ORDER));
			return qb.build().getSQL();
		}

	}

	public static class ConversationsEntryQueryBuilder {
		public static String build(final String where) {
			final SQLQueryBuilder qb = new SQLQueryBuilder();
			qb.select(new Columns(new Column(DirectMessages._ID), new Column(ConversationsEntry.MESSAGE_TIMESTAMP),
					new Column(DirectMessages.MESSAGE_ID), new Column(DirectMessages.ACCOUNT_ID), new Column(
							DirectMessages.IS_OUTGOING), new Column(ConversationsEntry.NAME), new Column(
							ConversationsEntry.SCREEN_NAME), new Column(ConversationsEntry.PROFILE_IMAGE_URL),
					new Column(ConversationsEntry.TEXT_HTML), new Column(ConversationsEntry.CONVERSATION_ID)));
			final SQLQueryBuilder entry_ids = new SQLQueryBuilder();
			entry_ids.select(new Columns(new Column(DirectMessages._ID), new Column(
					ConversationsEntry.MESSAGE_TIMESTAMP), new Column(DirectMessages.MESSAGE_ID), new Column(
					DirectMessages.ACCOUNT_ID), new Column("0", DirectMessages.IS_OUTGOING), new Column(
					DirectMessages.SENDER_NAME, ConversationsEntry.NAME), new Column(DirectMessages.SENDER_SCREEN_NAME,
					ConversationsEntry.SCREEN_NAME), new Column(DirectMessages.SENDER_PROFILE_IMAGE_URL,
					ConversationsEntry.PROFILE_IMAGE_URL), new Column(ConversationsEntry.TEXT_HTML), new Column(
					DirectMessages.SENDER_ID, ConversationsEntry.CONVERSATION_ID)));
			entry_ids.from(new Tables(Inbox.TABLE_NAME));
			entry_ids.union();
			entry_ids.select(new Columns(new Column(DirectMessages._ID), new Column(
					ConversationsEntry.MESSAGE_TIMESTAMP), new Column(DirectMessages.MESSAGE_ID), new Column(
					DirectMessages.ACCOUNT_ID), new Column("1", DirectMessages.IS_OUTGOING), new Column(
					DirectMessages.RECIPIENT_NAME, ConversationsEntry.NAME), new Column(
					DirectMessages.RECIPIENT_SCREEN_NAME, ConversationsEntry.SCREEN_NAME), new Column(
					DirectMessages.RECIPIENT_PROFILE_IMAGE_URL, ConversationsEntry.PROFILE_IMAGE_URL), new Column(
					ConversationsEntry.TEXT_HTML), new Column(DirectMessages.RECIPIENT_ID,
					ConversationsEntry.CONVERSATION_ID)));
			entry_ids.from(new Tables(Outbox.TABLE_NAME));
			qb.from(entry_ids.build());
			final SQLQueryBuilder recent_inbox_msg_ids = new SQLQueryBuilder()
					.select(new Column("MAX(" + DirectMessages.MESSAGE_ID + ")")).from(new Tables(Inbox.TABLE_NAME))
					.groupBy(new Column(DirectMessages.SENDER_ID));
			final SQLQueryBuilder recent_outbox_msg_ids = new SQLQueryBuilder()
					.select(new Column("MAX(" + DirectMessages.MESSAGE_ID + ")")).from(new Tables(Outbox.TABLE_NAME))
					.groupBy(new Column(DirectMessages.RECIPIENT_ID));
			final SQLQueryBuilder conversation_ids = new SQLQueryBuilder();
			conversation_ids.select(new Columns(new Column(DirectMessages.MESSAGE_ID), new Column(
					DirectMessages.SENDER_ID, ConversationsEntry.CONVERSATION_ID)));
			conversation_ids.from(new Tables(Inbox.TABLE_NAME));
			conversation_ids.where(Where.in(new Column(DirectMessages.MESSAGE_ID), recent_inbox_msg_ids.build()));
			conversation_ids.union();
			conversation_ids.select(new Columns(new Column(DirectMessages.MESSAGE_ID), new Column(
					DirectMessages.RECIPIENT_ID, ConversationsEntry.CONVERSATION_ID)));
			conversation_ids.from(new Tables(Outbox.TABLE_NAME));
			conversation_ids.where(Where.in(new Column(DirectMessages.MESSAGE_ID), recent_outbox_msg_ids.build()));
			final SQLQueryBuilder grouped_message_conversation_ids = new SQLQueryBuilder();
			grouped_message_conversation_ids.select(new Column(DirectMessages.MESSAGE_ID));
			grouped_message_conversation_ids.from(conversation_ids.build());
			grouped_message_conversation_ids.groupBy(new Column(ConversationsEntry.CONVERSATION_ID));
			final Where grouped_where = Where.in(new Column(DirectMessages.MESSAGE_ID),
					grouped_message_conversation_ids.build());
			qb.where(grouped_where);
			if (where != null) {
				grouped_where.and(new Where(where));
			}
			qb.groupBy(Utils.getColumnsFromProjection(ConversationsEntry.CONVERSATION_ID, DirectMessages.ACCOUNT_ID));
			qb.orderBy(new OrderBy(ConversationsEntry.MESSAGE_TIMESTAMP + " DESC"));
			return qb.build().getSQL();
		}
	}

	public static final class DirectMessagesQueryBuilder {
		public static final String build(final String[] projection, final String selection, final String sortOrder) {
			final SQLQueryBuilder qb = new SQLQueryBuilder();
			final Selectable select = Utils.getColumnsFromProjection(projection);
			qb.select(select).from(new Tables(DirectMessages.Inbox.TABLE_NAME));
			if (selection != null) {
				qb.where(new Where(selection));
			}
			qb.union();
			qb.select(select).from(new Tables(DirectMessages.Outbox.TABLE_NAME));
			if (selection != null) {
				qb.where(new Where(selection));
			}
			qb.orderBy(new OrderBy(sortOrder != null ? sortOrder : DirectMessages.DEFAULT_SORT_ORDER));
			return qb.build().getSQL();
		}
	}

}
