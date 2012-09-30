/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j.internal.json;

import static twitter4j.internal.util.z_T4JInternalParseUtil.getDate;
import static twitter4j.internal.util.z_T4JInternalParseUtil.getLong;
import static twitter4j.internal.util.z_T4JInternalParseUtil.getUnescapedString;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.DirectMessage;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.internal.http.HttpResponse;

/**
 * A data class representing sent/received direct message.
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
/* package */final class DirectMessageJSONImpl extends TwitterResponseImpl implements DirectMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8809144846145143089L;
	private long id;
	private String text;
	private long senderId;
	private long recipientId;
	private Date createdAt;
	private String senderScreenName;
	private String recipientScreenName;

	private User sender;

	private User recipient;

	/* package */DirectMessageJSONImpl(final HttpResponse res, final Configuration conf) throws TwitterException {
		super(res);
		final JSONObject json = res.asJSONObject();
		init(json);
	}

	/* package */DirectMessageJSONImpl(final JSONObject json) throws TwitterException {
		init(json);
	}

	@Override
	public boolean equals(final Object obj) {
		if (null == obj) return false;
		if (this == obj) return true;
		return obj instanceof DirectMessage && ((DirectMessage) obj).getId() == id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date getCreatedAt() {
		return createdAt;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User getRecipient() {
		return recipient;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getRecipientId() {
		return recipientId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getRecipientScreenName() {
		return recipientScreenName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User getSender() {
		return sender;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getSenderId() {
		return senderId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSenderScreenName() {
		return senderScreenName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getText() {
		return text;
	}

	@Override
	public int hashCode() {
		return (int) id;
	}

	@Override
	public String toString() {
		return "DirectMessageJSONImpl{" + "id=" + id + ", text='" + text + '\'' + ", sender_id=" + senderId
				+ ", recipient_id=" + recipientId + ", created_at=" + createdAt + ", sender_screen_name='"
				+ senderScreenName + '\'' + ", recipient_screen_name='" + recipientScreenName + '\'' + ", sender="
				+ sender + ", recipient=" + recipient + '}';
	}

	private void init(final JSONObject json) throws TwitterException {
		id = getLong("id", json);
		text = getUnescapedString("text", json);
		senderId = getLong("sender_id", json);
		recipientId = getLong("recipient_id", json);
		createdAt = getDate("created_at", json);
		senderScreenName = getUnescapedString("sender_screen_name", json);
		recipientScreenName = getUnescapedString("recipient_screen_name", json);
		try {
			sender = new UserJSONImpl(json.getJSONObject("sender"));
			recipient = new UserJSONImpl(json.getJSONObject("recipient"));
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		}
	}

	/* package */
	static ResponseList<DirectMessage> createDirectMessageList(final HttpResponse res, final Configuration conf)
			throws TwitterException {
		try {
			final JSONArray list = res.asJSONArray();
			final int size = list.length();
			final ResponseList<DirectMessage> directMessages = new ResponseListImpl<DirectMessage>(size, res);
			for (int i = 0; i < size; i++) {
				final JSONObject json = list.getJSONObject(i);
				final DirectMessage directMessage = new DirectMessageJSONImpl(json);
				directMessages.add(directMessage);
			}
			return directMessages;
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		} catch (final TwitterException te) {
			throw te;
		}
	}
}
