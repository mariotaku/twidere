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

import static twitter4j.internal.http.RequestMethod.DELETE;
import static twitter4j.internal.http.RequestMethod.GET;
import static twitter4j.internal.http.RequestMethod.HEAD;
import static twitter4j.internal.http.RequestMethod.POST;
import static twitter4j.internal.http.RequestMethod.PUT;

import java.util.HashMap;
import java.util.Map;

import twitter4j.TwitterException;
import twitter4j.auth.Authorization;
import twitter4j.conf.ConfigurationContext;

/**
 * HTTP Client wrapper with handy request methods, ResponseListener mechanism
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class HttpClientWrapper implements java.io.Serializable {
	private final HttpClientWrapperConfiguration wrapperConf;
	private HttpClient http;

	private final Map<String, String> requestHeaders;
	private static final long serialVersionUID = -6511977105603119379L;
	private HttpResponseListener httpResponseListener;

	// never used with this project. Just for handiness for those using this
	// class.
	public HttpClientWrapper() {
		wrapperConf = ConfigurationContext.getInstance();
		requestHeaders = wrapperConf.getRequestHeaders();
		http = HttpClientFactory.getInstance(wrapperConf);
	}

	public HttpClientWrapper(HttpClientWrapperConfiguration wrapperConf) {
		this.wrapperConf = wrapperConf;
		requestHeaders = wrapperConf.getRequestHeaders();
		http = HttpClientFactory.getInstance(wrapperConf);
	}

	public HttpResponse delete(String url) throws TwitterException {
		return request(new HttpRequest(DELETE, url, null, null, requestHeaders));
	}

	public HttpResponse delete(String url, Authorization authorization) throws TwitterException {
		return request(new HttpRequest(DELETE, url, null, authorization, requestHeaders));
	}

	public HttpResponse delete(String url, HttpParameter[] parameters) throws TwitterException {
		return request(new HttpRequest(DELETE, url, parameters, null, requestHeaders));
	}

	public HttpResponse delete(String url, HttpParameter[] parameters, Authorization authorization)
			throws TwitterException {
		return request(new HttpRequest(DELETE, url, parameters, authorization, requestHeaders));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final HttpClientWrapper that = (HttpClientWrapper) o;

		if (!http.equals(that.http)) return false;
		if (!requestHeaders.equals(that.requestHeaders)) return false;
		if (!wrapperConf.equals(that.wrapperConf)) return false;

		return true;
	}

	public HttpResponse get(String url) throws TwitterException {
		return request(new HttpRequest(GET, url, null, null, requestHeaders));
	}

	public HttpResponse get(String url, Authorization authorization) throws TwitterException {
		return request(new HttpRequest(GET, url, null, authorization, requestHeaders));
	}

	public HttpResponse get(String url, HttpParameter[] parameters) throws TwitterException {
		return request(new HttpRequest(GET, url, parameters, null, requestHeaders));
	}

	public HttpResponse get(String url, HttpParameter[] parameters, Authorization authorization)
			throws TwitterException {
		return request(new HttpRequest(GET, url, parameters, authorization, requestHeaders));
	}

	@Override
	public int hashCode() {
		int result = wrapperConf.hashCode();
		result = 31 * result + http.hashCode();
		result = 31 * result + requestHeaders.hashCode();
		return result;
	}

	public HttpResponse head(String url) throws TwitterException {
		return request(new HttpRequest(HEAD, url, null, null, requestHeaders));
	}

	public HttpResponse head(String url, Authorization authorization) throws TwitterException {
		return request(new HttpRequest(HEAD, url, null, authorization, requestHeaders));
	}

	public HttpResponse head(String url, HttpParameter[] parameters) throws TwitterException {
		return request(new HttpRequest(HEAD, url, parameters, null, requestHeaders));
	}

	public HttpResponse head(String url, HttpParameter[] parameters, Authorization authorization)
			throws TwitterException {
		return request(new HttpRequest(HEAD, url, parameters, authorization, requestHeaders));
	}

	public HttpResponse post(String url) throws TwitterException {
		return request(new HttpRequest(POST, url, null, null, requestHeaders));
	}

	public HttpResponse post(String url, Authorization authorization) throws TwitterException {
		return request(new HttpRequest(POST, url, null, authorization, requestHeaders));
	}

	public HttpResponse post(String url, HttpParameter[] parameters) throws TwitterException {
		return request(new HttpRequest(POST, url, parameters, null, requestHeaders));
	}

	public HttpResponse post(String url, HttpParameter[] parameters, Authorization authorization)
			throws TwitterException {
		return request(new HttpRequest(POST, url, parameters, authorization, requestHeaders));
	}

	public HttpResponse post(String url, HttpParameter[] parameters, Map<String, String> requestHeaders)
			throws TwitterException {
		final Map<String, String> headers = new HashMap<String, String>(this.requestHeaders);
		if (requestHeaders != null) {
			headers.putAll(requestHeaders);
		}

		return request(new HttpRequest(POST, url, parameters, null, headers));
	}

	public HttpResponse put(String url) throws TwitterException {
		return request(new HttpRequest(PUT, url, null, null, requestHeaders));
	}

	public HttpResponse put(String url, Authorization authorization) throws TwitterException {
		return request(new HttpRequest(PUT, url, null, authorization, requestHeaders));
	}

	public HttpResponse put(String url, HttpParameter[] parameters) throws TwitterException {
		return request(new HttpRequest(PUT, url, parameters, null, requestHeaders));
	}

	public HttpResponse put(String url, HttpParameter[] parameters, Authorization authorization)
			throws TwitterException {
		return request(new HttpRequest(PUT, url, parameters, authorization, requestHeaders));
	}

	public void setHttpResponseListener(HttpResponseListener listener) {
		httpResponseListener = listener;
	}

	public void shutdown() {
		http.shutdown();
	}

	@Override
	public String toString() {
		return "HttpClientWrapper{" + "wrapperConf=" + wrapperConf + ", http=" + http + ", requestHeaders="
				+ requestHeaders + ", httpResponseListener=" + httpResponseListener + '}';
	}

	private HttpResponse request(HttpRequest req) throws TwitterException {
		HttpResponse res;
		try {
			res = http.request(req);
			// fire HttpResponseEvent
			if (httpResponseListener != null) {
				httpResponseListener.httpResponseReceived(new HttpResponseEvent(req, res, null));
			}
		} catch (final TwitterException te) {
			if (httpResponseListener != null) {
				httpResponseListener.httpResponseReceived(new HttpResponseEvent(req, null, te));
			}
			throw te;
		}
		return res;
	}
}
