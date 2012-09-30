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

package twitter4j.internal.http;

import twitter4j.TwitterException;

/**
 * @author Andrew Hedges - andrew.hedges at gmail.com
 */
public final class HttpResponseEvent {

	private final HttpRequest request;

	private final HttpResponse response;

	private final TwitterException twitterException;

	HttpResponseEvent(final HttpRequest request, final HttpResponse response, final TwitterException te) {
		this.request = request;
		this.response = response;
		twitterException = te;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final HttpResponseEvent that = (HttpResponseEvent) o;

		if (request != null ? !request.equals(that.request) : that.request != null) return false;
		if (response != null ? !response.equals(that.response) : that.response != null) return false;

		return true;
	}

	/**
	 * returns the request associated with the event
	 * 
	 * @return the request associated with the event
	 */
	public HttpRequest getRequest() {
		return request;
	}

	/**
	 * returns the response associated with the event
	 * 
	 * @return the response associated with the event
	 */
	public HttpResponse getResponse() {
		return response;
	}

	/**
	 * returns the TwitterException associated with the event
	 * 
	 * @return the TwitterException associated with the event
	 */
	public TwitterException getTwitterException() {
		return twitterException;
	}

	@Override
	public int hashCode() {
		int result = request != null ? request.hashCode() : 0;
		result = 31 * result + (response != null ? response.hashCode() : 0);
		return result;
	}

	public boolean isAuthenticated() {
		return request.getAuthorization().isEnabled();
	}

	@Override
	public String toString() {
		return "HttpResponseEvent{" + "request=" + request + ", response=" + response + '}';
	}
}
