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

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.internal.http.HttpRequest;
import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.http.HttpResponseCode;
import twitter4j.internal.json.z_T4JInternalJSONImplFactory;
import twitter4j.internal.util.z_T4JInternalParseUtil;

/**
 * An exception class that will be thrown when TwitterAPI calls are failed.<br>
 * In case the Twitter server returned HTTP error code, you can get the HTTP
 * status code using getStatusCode() method.
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class TwitterException extends Exception implements TwitterResponse, HttpResponseCode {
	private static final long serialVersionUID = 5876311940770455282L;

	private int statusCode = -1;

	private ExceptionDiagnosis exceptionDiagnosis = null;
	private HttpResponse response;
	private HttpRequest request;
	private String errorMessage = null;
	private String requestPath = null;

	private final static String[] FILTER = new String[] { "twitter4j" };

	boolean nested = false;

	public TwitterException(final Exception cause) {
		this(cause.getMessage(), cause);
		if (cause instanceof TwitterException) {
			((TwitterException) cause).setNested();
		}
	}

	public TwitterException(final String message) {
		this(message, (Throwable) null);
	}

	public TwitterException(final String message, final Exception cause, final int statusCode) {
		this(message, cause);
		this.statusCode = statusCode;
	}

	public TwitterException(final String message, final HttpRequest req, final HttpResponse res) {
		this(message);
		request = req;
		response = res;
		statusCode = res != null ? res.getStatusCode() : -1;
	}

	public TwitterException(final String message, final Throwable cause) {
		super(message, cause);
		decode(message);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof TwitterException)) return false;

		final TwitterException that = (TwitterException) o;

		if (nested != that.nested) return false;
		if (statusCode != that.statusCode) return false;
		if (errorMessage != null ? !errorMessage.equals(that.errorMessage) : that.errorMessage != null) return false;
		if (exceptionDiagnosis != null ? !exceptionDiagnosis.equals(that.exceptionDiagnosis)
				: that.exceptionDiagnosis != null) return false;
		if (requestPath != null ? !requestPath.equals(that.requestPath) : that.requestPath != null) return false;
		if (response != null ? !response.equals(that.response) : that.response != null) return false;
		if (request != null ? !request.equals(that.request) : that.request != null) return false;

		return true;
	}

	/**
	 * Tests if the exception is caused by rate limitation exceed
	 * 
	 * @return if the exception is caused by rate limitation exceed
	 * @see <a href="https://dev.twitter.com/docs/rate-limiting">Rate Limiting |
	 *      Twitter Developers</a>
	 * @since Twitter4J 2.1.2
	 */
	public boolean exceededRateLimitation() {
		return statusCode == 400 && getRateLimitStatus() != null // REST API
				|| statusCode == 420; // Search API
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getAccessLevel() {
		return z_T4JInternalParseUtil.toAccessLevel(response);
	}

	/**
	 * Returns error message from the API if available.
	 * 
	 * @return error message from the API
	 * @since Twitter4J 2.2.3
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Returns a hexadecimal representation of this exception stacktrace.<br>
	 * An exception code is a hexadecimal representation of the stacktrace which
	 * enables it easier to Google known issues.<br>
	 * Format : XXXXXXXX:YYYYYYYY[ XX:YY]<br>
	 * Where XX is a hash code of stacktrace without line number<br>
	 * YY is a hash code of stacktrace excluding line number<br>
	 * [-XX:YY] will appear when this instance a root cause
	 * 
	 * @return a hexadecimal representation of this exception stacktrace
	 */
	public String getExceptionCode() {
		return getExceptionDiagnosis().asHexString();
	}

	/**
	 * Returns the current feature-specific rate limit status if available.<br>
	 * This method is available in conjunction with Twitter#searchUsers()<br>
	 * 
	 * @return current rate limit status
	 * @see <a href="https://dev.twitter.com/docs/rate-limiting">Rate Limiting |
	 *      Twitter Developers</a>
	 * @since Twitter4J 2.1.2
	 */
	public RateLimitStatus getFeatureSpecificRateLimitStatus() {
		if (null == response) return null;
		return z_T4JInternalJSONImplFactory.createFeatureSpecificRateLimitStatusFromResponseHeader(response);
	}

	public HttpRequest getHttpRequest() {
		return request;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMessage() {
		final StringBuffer value = new StringBuffer();
		if (errorMessage != null && request != null) {
			value.append("error - ").append(errorMessage).append("\n");
			value.append("request - ").append(requestPath).append("\n");
		} else {
			value.append(super.getMessage());
		}
		if (statusCode != -1)
			return errorMessage != null ? errorMessage : getCause(statusCode) + "\n" + value.toString();
		else
			return value.toString();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @since Twitter4J 2.1.2
	 */
	@Override
	public RateLimitStatus getRateLimitStatus() {
		if (null == response) return null;
		return z_T4JInternalJSONImplFactory.createRateLimitStatusFromResponseHeader(response);
	}

	/**
	 * Returns the request path returned by the API.
	 * 
	 * @return the request path returned by the API
	 * @since Twitter4J 2.2.3
	 */
	public String getRequestPath() {
		return requestPath;
	}

	public String getResponseHeader(final String name) {
		String value = null;
		if (response != null) {
			final List<String> header = response.getResponseHeaderFields().get(name);
			if (header.size() > 0) {
				value = header.get(0);
			}
		}
		return value;
	}

	/**
	 * Returns int value of "Retry-After" response header (Search API) or
	 * seconds_until_reset (REST API). An application that exceeds the rate
	 * limitations of the Search API will receive HTTP 420 response codes to
	 * requests. It is a best practice to watch for this error condition and
	 * honor the Retry-After header that instructs the application when it is
	 * safe to continue. The Retry-After header's value is the number of seconds
	 * your application should wait before submitting another query (for
	 * example: Retry-After: 67).<br>
	 * Check if getStatusCode() == 503 before calling this method to ensure that
	 * you are actually exceeding rate limitation with query apis.<br>
	 * 
	 * @return instructs the application when it is safe to continue in seconds
	 * @see <a href="https://dev.twitter.com/docs/rate-limiting">Rate Limiting |
	 *      Twitter Developers</a>
	 * @since Twitter4J 2.1.0
	 */
	public int getRetryAfter() {
		int retryAfter = -1;
		if (statusCode == 400) {
			final RateLimitStatus rateLimitStatus = getRateLimitStatus();
			if (rateLimitStatus != null) {
				retryAfter = rateLimitStatus.getSecondsUntilReset();
			}
		} else if (statusCode == ENHANCE_YOUR_CLAIM) {
			try {
				final String retryAfterStr = response.getResponseHeader("Retry-After");
				if (retryAfterStr != null) {
					retryAfter = Integer.valueOf(retryAfterStr);
				}
			} catch (final NumberFormatException ignore) {
			}
		}
		return retryAfter;
	}

	public int getStatusCode() {
		return statusCode;
	}

	@Override
	public int hashCode() {
		int result = statusCode;
		result = 31 * result + (exceptionDiagnosis != null ? exceptionDiagnosis.hashCode() : 0);
		result = 31 * result + (request != null ? request.hashCode() : 0);
		result = 31 * result + (response != null ? response.hashCode() : 0);
		result = 31 * result + (errorMessage != null ? errorMessage.hashCode() : 0);
		result = 31 * result + (requestPath != null ? requestPath.hashCode() : 0);
		result = 31 * result + (nested ? 1 : 0);
		return result;
	}

	/**
	 * Tests if the exception is caused by network issue
	 * 
	 * @return if the exception is caused by network issue
	 * @since Twitter4J 2.1.2
	 */
	public boolean isCausedByNetworkIssue() {
		return getCause() instanceof java.io.IOException;
	}

	/**
	 * Tests if error message from the API is available
	 * 
	 * @return true if error message from the API is available
	 * @since Twitter4J 2.2.3
	 */
	public boolean isErrorMessageAvailable() {
		return errorMessage != null;
	}

	/**
	 * Tests if the exception is caused by non-existing resource
	 * 
	 * @return if the exception is caused by non-existing resource
	 * @since Twitter4J 2.1.2
	 */
	public boolean resourceNotFound() {
		return statusCode == 404;
	}

	@Override
	public String toString() {
		return getMessage();
	}

	private void decode(final String str) {
		if (str != null && str.startsWith("{")) {
			try {
				final JSONObject json = new JSONObject(str);
				if (!json.isNull("error")) {
					errorMessage = json.getString("error");
				}
				if (!json.isNull("request")) {
					requestPath = json.getString("request");
				}
			} catch (final JSONException ignore) {
			}
		}
	}

	private ExceptionDiagnosis getExceptionDiagnosis() {
		if (null == exceptionDiagnosis) {
			exceptionDiagnosis = new ExceptionDiagnosis(this, FILTER);
		}
		return exceptionDiagnosis;
	}

	void setNested() {
		nested = true;
	}

	private static String getCause(final int statusCode) {
		String cause;
		// https://dev.twitter.com/docs/error-codes-responses
		switch (statusCode) {
			case NOT_MODIFIED:
				cause = "There was no new data to return.";
				break;
			case BAD_REQUEST:
				cause = "The request was invalid. An accompanying error message will explain why. This is the status code that will be returned during rate limiting (https://dev.twitter.com/pages/rate-limiting).";
				break;
			case UNAUTHORIZED:
				cause = "Authentication credentials (https://dev.twitter.com/docs/auth) were missing or incorrect. Ensure that you have set valid consumer key/secret, access token/secret, and the system clock is in sync.";
				break;
			case FORBIDDEN:
				cause = "The request is understood, but it has been refused. An accompanying error message will explain why. This code is used when requests are being denied due to update limits (https://support.twitter.com/articles/15364-about-twitter-limits-update-api-dm-and-following).";
				break;
			case NOT_FOUND:
				cause = "The URI requested is invalid or the resource requested, such as a user, does not exist.";
				break;
			case NOT_ACCEPTABLE:
				cause = "Returned by the Search API when an invalid format is specified in the request.\n"
						+ "Returned by the Streaming API when one or more of the parameters are not suitable for the resource. The track parameter, for example, would throw this error if:\n"
						+ " The track keyword is too long or too short.\n"
						+ " The bounding box specified is invalid.\n"
						+ " No predicates defined for filtered resource, for example, neither track nor follow parameter defined.\n"
						+ " Follow userid cannot be read.";
				break;
			case TOO_LONG:
				cause = "A parameter list is too long. The track parameter, for example, would throw this error if:\n"
						+ " Too many track tokens specified for role; contact API team for increased access.\n"
						+ " Too many bounding boxes specified for role; contact API team for increased access.\n"
						+ " Too many follow userids specified for role; contact API team for increased access.";
				break;
			case ENHANCE_YOUR_CLAIM:
				cause = "Returned by the Search and Trends API when you are being rate limited (https://dev.twitter.com/docs/rate-limiting).\n"
						+ "Returned by the Streaming API:\n Too many login attempts in a short period of time.\n"
						+ " Running too many copies of the same application authenticating with the same account name.";
				break;
			case INTERNAL_SERVER_ERROR:
				cause = "Something is broken. Please post to the group (https://dev.twitter.com/docs/support) so the Twitter team can investigate.";
				break;
			case BAD_GATEWAY:
				cause = "Twitter is down or being upgraded.";
				break;
			case SERVICE_UNAVAILABLE:
				cause = "The Twitter servers are up, but overloaded with requests. Try again later.";
				break;
			default:
				cause = "";
		}
		return statusCode + ":" + cause;
	}
}
