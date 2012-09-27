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
package twitter4j;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.2.6
 */
public interface Twt extends Serializable {

	/**
	 * Returns the annotations of the tweet. At the moment this code is written
	 * (2010-08-18), Twitter Search API does not support annotations yet (so
	 * even annotated tweets are returned without the annotations). This method
	 * is included here for completeness and for future use.
	 * 
	 * @return the annotations
	 * @since Twitter4J 2.1.4
	 */
	@Deprecated
	Annotations getAnnotations();

	/**
	 * returns the created_at
	 * 
	 * @return the created_at
	 */
	Date getCreatedAt();

	/**
	 * Returns The location that this tweet refers to if available.
	 * 
	 * @return returns The location that this tweet refers to if available (can
	 *         be null)
	 */
	GeoLocation getGeoLocation();

	/**
	 * returns the status id of the tweet
	 * 
	 * @return the status id
	 */
	long getId();

	/**
	 * Returns the in_reply_tostatus_id
	 * 
	 * @return the in_reply_tostatus_id
	 */
	long getInReplyToStatusId();

	/**
	 * Returns the place associated with the Tweet.
	 * 
	 * @return The place associated with the Tweet
	 */
	Place getPlace();

	/**
	 * returns the raw text
	 * 
	 * @return the raw text
	 */
	String getRawText();

	/**
	 * returns the source of the tweet
	 * 
	 * @return the source of the tweet
	 */
	String getSource();

	/**
	 * returns the text
	 * 
	 * @return the text
	 */
	String getText();
}
