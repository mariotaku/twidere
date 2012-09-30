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

import twitter4j.ProfileImage;
import twitter4j.internal.http.HttpResponse;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.7
 */
class ProfileImageImpl extends TwitterResponseImpl implements ProfileImage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 519484877188098901L;
	private final String url;

	ProfileImageImpl(final HttpResponse res) {
		super(res);
		url = res.getResponseHeader("Location");
	}

	@Override
	public String getURL() {
		return url;
	}
}
