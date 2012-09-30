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

/**
 * A data interface that has detailed information about a relationship between
 * two users
 * 
 * @author Perry Sakkaris - psakkaris at gmail.com
 * @see <a href="https://dev.twitter.com/docs/api/1/get/friendships/show">GET
 *      friendships/show | Twitter Developers</a>
 * @since Twitter4J 2.1.0
 */
public interface Relationship extends TwitterResponse {
	/**
	 * Returns the source user id
	 * 
	 * @return the source user id
	 */
	long getSourceUserId();

	/**
	 * Returns the source user screen name
	 * 
	 * @return returns the source user screen name
	 */
	String getSourceUserScreenName();

	/**
	 * Returns the target user id
	 * 
	 * @return target user id
	 */
	long getTargetUserId();

	/**
	 * Returns the target user screen name
	 * 
	 * @return the target user screen name
	 */
	String getTargetUserScreenName();

	/**
	 * Returns if the source user is blocking the target user
	 * 
	 * @return if the source is blocking the target
	 */
	boolean isSourceBlockingTarget();

	/**
	 * Checks if source user is being followed by target user
	 * 
	 * @return true if source user is being followed by target user
	 */
	boolean isSourceFollowedByTarget();

	/**
	 * Checks if source user is following target user
	 * 
	 * @return true if source user is following target user
	 */
	boolean isSourceFollowingTarget();

	/**
	 * Checks if the source user has enabled notifications for updates of the
	 * target user
	 * 
	 * @return true if source user enabled notifications for target user
	 */
	boolean isSourceNotificationsEnabled();

	/**
	 * Checks if target user is being followed by source user.<br>
	 * This method is equivalent to isSourceFollowingTarget().
	 * 
	 * @return true if target user is being followed by source user
	 */
	boolean isTargetFollowedBySource();

	/**
	 * Checks if target user is following source user.<br>
	 * This method is equivalent to isSourceFollowedByTarget().
	 * 
	 * @return true if target user is following source user
	 */
	boolean isTargetFollowingSource();

}
