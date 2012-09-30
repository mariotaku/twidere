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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import twitter4j.TwitterException;
import twitter4j.conf.ConfigurationContext;
import twitter4j.internal.logging.Logger;

/**
 * A data class representing HTTP Response
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public abstract class HttpResponse {
	private static final Logger logger = Logger.getLogger();
	protected final HttpClientConfiguration CONF;

	protected int statusCode;

	protected String responseAsString = null;

	protected InputStream is;
	private boolean streamConsumed = false;
	private JSONObject json = null;
	private JSONArray jsonArray = null;

	public HttpResponse(final HttpClientConfiguration conf) {
		CONF = conf;
	}

	HttpResponse() {
		CONF = ConfigurationContext.getInstance();
	}

	/**
	 * Returns the response body as {@link JSONArray}.<br>
	 * Disconnects the internal HttpURLConnection silently.
	 * 
	 * @return response body as {@link JSONArray}
	 * @throws TwitterException
	 */
	public JSONArray asJSONArray() throws TwitterException {
		if (jsonArray == null) {
			try {
				if (responseAsString == null) {
					jsonArray = new JSONArray(new JSONTokener(asString()));
				} else {
					jsonArray = new JSONArray(responseAsString);
				}
				if (CONF.isPrettyDebugEnabled()) {
					logger.debug(jsonArray.toString(1));
				}
			} catch (final JSONException jsone) {
				if (logger.isDebugEnabled())
					throw new TwitterException(jsone.getMessage() + ":" + responseAsString, jsone);
				else
					throw new TwitterException(jsone.getMessage(), jsone);
			} finally {
				disconnectForcibly();
			}
		}
		return jsonArray;
	}

	/**
	 * Returns the response body as {@link JSONObject}.<br>
	 * Disconnects the internal HttpURLConnection silently.
	 * 
	 * @return response body as {@link JSONObject}
	 * @throws TwitterException
	 */
	public JSONObject asJSONObject() throws TwitterException {
		if (json == null) {
			try {
				if (responseAsString == null) {
					json = new JSONObject(new JSONTokener(asString()));
				} else {
					json = new JSONObject(responseAsString);
				}
				if (CONF.isPrettyDebugEnabled()) {
					logger.debug(json.toString(1));
				}
			} catch (final JSONException jsone) {
				if (responseAsString == null)
					throw new TwitterException(jsone.getMessage(), jsone);
				else
					throw new TwitterException(jsone.getMessage() + ":" + responseAsString, jsone);
			} finally {
				disconnectForcibly();
			}
		}
		return json;
	}

	public Reader asReader() {
		try {
			return new BufferedReader(new InputStreamReader(is, "UTF-8"));
		} catch (final java.io.UnsupportedEncodingException uee) {
			return new InputStreamReader(is);
		}
	}

	/**
	 * Returns the response stream.<br>
	 * This method cannot be called after calling asString() or asDcoument()<br>
	 * It is suggested to call disconnect() after consuming the stream.
	 * <p/>
	 * Disconnects the internal HttpURLConnection silently.
	 * 
	 * @return response body stream
	 * @throws TwitterException
	 * @see #disconnect()
	 */
	public InputStream asStream() {
		if (streamConsumed) throw new IllegalStateException("Stream has already been consumed.");
		return is;
	}

	/**
	 * Returns the response body as string.<br>
	 * Disconnects the internal HttpURLConnection silently.
	 * 
	 * @return response body
	 * @throws TwitterException
	 */
	public String asString() throws TwitterException {
		if (null == responseAsString) {
			BufferedReader br = null;
			InputStream stream = null;
			try {
				stream = asStream();
				if (null == stream) return null;
				br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
				final StringBuffer buf = new StringBuffer();
				String line;
				while ((line = br.readLine()) != null) {
					buf.append(line).append("\n");
				}
				responseAsString = buf.toString();
				logger.debug(responseAsString);
				stream.close();
				streamConsumed = true;
			} catch (final IOException ioe) {
				throw new TwitterException(ioe.getMessage(), ioe);
			} catch (final OutOfMemoryError e) {
				throw new TwitterException(e.getMessage(), e);
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (final IOException ignore) {
					}
				}
				if (br != null) {
					try {
						br.close();
					} catch (final IOException ignore) {
					}
				}
				disconnectForcibly();
			}
		}
		return responseAsString;
	}

	public abstract void disconnect() throws IOException;

	public abstract String getResponseHeader(String name);

	public abstract Map<String, List<String>> getResponseHeaderFields();

	public int getStatusCode() {
		return statusCode;
	}

	@Override
	public String toString() {
		return "HttpResponse{" + "statusCode=" + statusCode + ", responseAsString='" + responseAsString + '\''
				+ ", is=" + is + ", streamConsumed=" + streamConsumed + '}';
	}

	private void disconnectForcibly() {
		try {
			disconnect();
		} catch (final Exception ignore) {
		}
	}
}
