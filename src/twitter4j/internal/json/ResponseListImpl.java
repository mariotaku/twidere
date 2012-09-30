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

import java.util.ArrayList;

import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.util.z_T4JInternalParseUtil;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.3
 */
class ResponseListImpl<T> extends ArrayList<T> implements ResponseList<T> {
	private static final long serialVersionUID = -7789068763212377625L;
	private transient RateLimitStatus rateLimitStatus = null;
	private transient RateLimitStatus featureSpecificRateLimitStatus = null;
	private transient int accessLevel;

	ResponseListImpl(final HttpResponse res) {
		super();
		init(res);
	}

	ResponseListImpl(final int size, final HttpResponse res) {
		super(size);
		init(res);
	}

	ResponseListImpl(final RateLimitStatus rateLimitStatus, final RateLimitStatus featureSpecificRateLimitStatus,
			final int accessLevel) {
		super();
		this.rateLimitStatus = rateLimitStatus;
		this.featureSpecificRateLimitStatus = featureSpecificRateLimitStatus;
		this.accessLevel = accessLevel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getAccessLevel() {
		return accessLevel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RateLimitStatus getFeatureSpecificRateLimitStatus() {
		return featureSpecificRateLimitStatus;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RateLimitStatus getRateLimitStatus() {
		return rateLimitStatus;
	}

	private void init(final HttpResponse res) {
		this.rateLimitStatus = RateLimitStatusJSONImpl.createFromResponseHeader(res);
		this.featureSpecificRateLimitStatus = RateLimitStatusJSONImpl
				.createFeatureSpecificRateLimitStatusFromResponseHeader(res);
		accessLevel = z_T4JInternalParseUtil.toAccessLevel(res);
	}
}
