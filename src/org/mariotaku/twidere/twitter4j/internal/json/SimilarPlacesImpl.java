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

package org.mariotaku.twidere.twitter4j.internal.json;

import org.json.JSONException;
import org.json.JSONObject;
import org.mariotaku.twidere.twitter4j.Place;
import org.mariotaku.twidere.twitter4j.ResponseList;
import org.mariotaku.twidere.twitter4j.SimilarPlaces;
import org.mariotaku.twidere.twitter4j.TwitterException;
import org.mariotaku.twidere.twitter4j.conf.Configuration;
import org.mariotaku.twidere.twitter4j.http.HttpResponse;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.7
 */
public class SimilarPlacesImpl extends ResponseListImpl<Place> implements SimilarPlaces {

	private static final long serialVersionUID = -5530218831645947540L;
	private final String token;

	SimilarPlacesImpl(final ResponseList<Place> places, final HttpResponse res, final String token) {
		super(places.size(), res);
		this.addAll(places);
		this.token = token;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getToken() {
		return token;
	}

	/* package */
	static SimilarPlaces createSimilarPlaces(final HttpResponse res, final Configuration conf) throws TwitterException {
		JSONObject json = null;
		try {
			json = res.asJSONObject();
			final JSONObject result = json.getJSONObject("result");
			return new SimilarPlacesImpl(PlaceJSONImpl.createPlaceList(result.getJSONArray("places"), res, conf), res,
					result.getString("token"));
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone.getMessage() + ":" + json.toString(), jsone);
		}
	}

}
