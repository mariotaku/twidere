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
public final class HttpClientWrapper {
	private final HttpClientWrapperConfiguration wrapperConf;
	private HttpClient http;

	private final Map<String, String> requestHeaders;

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

	public HttpResponse delete(String url, String sign_url) throws TwitterException {
		return delete(url, sign_url, null, null);
	}

	public HttpResponse delete(String url, String sign_url, Authorization authorization) throws TwitterException {
		return delete(url, sign_url, null, authorization);
	}

	public HttpResponse delete(String url, String sign_url, HttpParameter[] parameters) throws TwitterException {
		return delete(url, sign_url, parameters, null);
	}

	public HttpResponse delete(String url, String sign_url, HttpParameter[] parameters, Authorization authorization)
			throws TwitterException {
		return request(new HttpRequest(DELETE, url, sign_url, parameters, authorization, requestHeaders));
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

	public HttpResponse get(String url, String sign_url) throws TwitterException {
		return get(url, sign_url, null, null);
	}

	public HttpResponse get(String url, String sign_url, Authorization authorization) throws TwitterException {
		return get(url, sign_url, null, authorization);
	}

	public HttpResponse get(String url, String sign_url, HttpParameter[] parameters) throws TwitterException {
		return get(url, sign_url, parameters, null);
	}

	public HttpResponse get(String url, String sign_url, HttpParameter[] parameters, Authorization authorization)
			throws TwitterException {
		return request(new HttpRequest(GET, url, sign_url, parameters, authorization, requestHeaders));
	}

	@Override
	public int hashCode() {
		int result = wrapperConf.hashCode();
		result = 31 * result + http.hashCode();
		result = 31 * result + requestHeaders.hashCode();
		return result;
	}

	public HttpResponse head(String url, String sign_url) throws TwitterException {
		return head(url, sign_url, null, null);
	}

	public HttpResponse head(String url, String sign_url, Authorization authorization) throws TwitterException {
		return head(url, sign_url, null, authorization);
	}

	public HttpResponse head(String url, String sign_url, HttpParameter[] parameters) throws TwitterException {
		return head(url, sign_url, parameters, null);
	}

	public HttpResponse head(String url, String sign_url, HttpParameter[] parameters, Authorization authorization)
			throws TwitterException {
		return request(new HttpRequest(HEAD, url, sign_url, parameters, authorization, requestHeaders));
	}

	public HttpResponse post(String url, String sign_url) throws TwitterException {
		return post(url, sign_url, null, null, null);
	}

	public HttpResponse post(String url, String sign_url, Authorization authorization) throws TwitterException {
		return post(url, sign_url, null, authorization, null);
	}

	public HttpResponse post(String url, String sign_url, HttpParameter[] parameters) throws TwitterException {
		return post(url, sign_url, parameters, null, null);
	}

	public HttpResponse post(String url, String sign_url, HttpParameter[] parameters, Authorization authorization)
			throws TwitterException {
		return post(url, sign_url, parameters, authorization, null);
	}

	public HttpResponse post(String url, String sign_url, HttpParameter[] parameters, Authorization authorization,
			Map<String, String> requestHeaders) throws TwitterException {
		final Map<String, String> headers = new HashMap<String, String>(this.requestHeaders);
		if (requestHeaders != null) {
			headers.putAll(requestHeaders);
		}
		return request(new HttpRequest(POST, url, sign_url, parameters, authorization, headers));
	}

	public HttpResponse post(String url, String sign_url, HttpParameter[] parameters, Map<String, String> requestHeaders)
			throws TwitterException {
		return post(url, sign_url, parameters, null, requestHeaders);
	}

	public HttpResponse put(String url, String sign_url) throws TwitterException {
		return put(url, sign_url, null, null);
	}

	public HttpResponse put(String url, String sign_url, Authorization authorization) throws TwitterException {
		return put(url, sign_url, null, authorization);
	}

	public HttpResponse put(String url, String sign_url, HttpParameter[] parameters) throws TwitterException {
		return put(url, sign_url, parameters, null);
	}

	public HttpResponse put(String url, String sign_url, HttpParameter[] parameters, Authorization authorization)
			throws TwitterException {
		return request(new HttpRequest(PUT, url, sign_url, parameters, authorization, requestHeaders));
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
