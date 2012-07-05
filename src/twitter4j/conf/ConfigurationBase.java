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

package twitter4j.conf;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import twitter4j.TwitterConstants;
import twitter4j.Version;

/**
 * Configuration base class with default settings.
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
class ConfigurationBase implements TwitterConstants, Configuration, Serializable {

	private static final boolean DEFAULT_USE_SSL = true;

	private boolean debug;
	private String userAgent;
	private String user;
	private String password;
	private boolean useSSL;
	private boolean ignoreSSLError;
	private boolean prettyDebug;
	private boolean gzipEnabled;
	private String httpProxyHost;
	private String httpProxyUser;
	private String httpProxyPassword;
	private int httpProxyPort;
	private int httpConnectionTimeout;
	private int httpReadTimeout;

	private int httpStreamingReadTimeout;
	private int httpRetryCount;
	private int httpRetryIntervalSeconds;
	private int maxTotalConnections;
	private int defaultMaxPerRoute;
	private String oAuthConsumerKey;
	private String oAuthConsumerSecret;
	private String oAuthAccessToken;
	private String oAuthAccessTokenSecret;

	private String oAuthRequestTokenURL;
	private String oAuthAuthorizationURL;
	private String oAuthAccessTokenURL;
	private String oAuthAuthenticationURL;
	
	private String signingOAuthRequestTokenURL;
	private String signingOAuthAuthorizationURL;
	private String signingOAuthAccessTokenURL;
	private String signingOAuthAuthenticationURL;

	private String oAuthBaseURL = DEFAULT_OAUTH_BASE_URL;

	private String signingOAuthBaseURL;
	private String signingRestBaseURL;

	private String restBaseURL;
	private String searchBaseURL;
	private String streamBaseURL;
	private String userStreamBaseURL;
	private String siteStreamBaseURL;
	private String uploadBaseURL;

	private String dispatcherImpl;

	private int asyncNumThreads;

	private boolean includeRTsEnabled;

	private boolean includeEntitiesEnabled;

	private boolean jsonStoreEnabled;

	private boolean userStreamRepliesAllEnabled;

	private String mediaProvider;

	private String mediaProviderAPIKey;

	private Properties mediaProviderParameters;
	// hidden portion
	private String clientVersion;
	private String clientURL;

	private static final long serialVersionUID = -6610497517837844232L;

	// method for HttpRequestFactoryConfiguration
	Map<String, String> requestHeaders;

	private static final List<ConfigurationBase> instances = new ArrayList<ConfigurationBase>();

	protected ConfigurationBase() {
		setDebug(false);
		setUser(null);
		setPassword(null);
		setUseSSL(false);
		setPrettyDebugEnabled(false);
		setGZIPEnabled(true);
		setHttpProxyHost(null);
		setHttpProxyUser(null);
		setHttpProxyPassword(null);
		setHttpProxyPort(-1);
		setHttpConnectionTimeout(20000);
		setHttpReadTimeout(120000);
		setHttpStreamingReadTimeout(40 * 1000);
		setHttpRetryCount(0);
		setHttpRetryIntervalSeconds(5);
		setHttpMaxTotalConnections(20);
		setHttpDefaultMaxPerRoute(2);
		setOAuthConsumerKey(null);
		setOAuthConsumerSecret(null);
		setOAuthAccessToken(null);
		setOAuthAccessTokenSecret(null);
		setAsyncNumThreads(1);
		setClientVersion(Version.getVersion());
		setClientURL("http://twitter4j.org/en/twitter4j-" + Version.getVersion() + ".xml");
		setUserAgent("twitter4j http://twitter4j.org/ /" + Version.getVersion());

		setIncludeRTsEnbled(true);

		setIncludeEntitiesEnbled(true);

		setJSONStoreEnabled(false);

		setOAuthBaseURL(DEFAULT_OAUTH_BASE_URL);
		setSigningOAuthBaseURL(DEFAULT_SIGNING_OAUTH_BASE_URL);

		setOAuthRequestTokenURL(DEFAULT_OAUTH_REQUEST_TOKEN_URL);
		setOAuthAuthorizationURL(DEFAULT_OAUTH_AUTHORIZATION_URL);
		setOAuthAccessTokenURL(DEFAULT_OAUTH_ACCESS_TOKEN_URL);
		setOAuthAuthenticationURL(DEFAULT_OAUTH_AUTHENTICATION_URL);

		setRestBaseURL(DEFAULT_REST_BASE_URL);
		setSigningRestBaseURL(DEFAULT_SIGNING_REST_BASE_URL);
		// search api tends to fail with SSL as of 12/31/2009
		// setSearchBaseURL(fixURL(useSSL, "http://search.twitter.com/"));
		setSearchBaseURL(DEFAULT_SEARCH_BASE_URL);
		// streaming api doesn't support SSL as of 12/30/2009
		// setStreamBaseURL(fixURL(useSSL, "http://stream.twitter.com/1/"));
		setStreamBaseURL(DEFAULT_STREAM_BASE_URL);
		setUserStreamBaseURL(DEFAULT_USER_STREAM_BASE_URL);
		setSiteStreamBaseURL(DEFAULT_SITE_STREAM_BASE_URL);
		setUploadBaseURL(DEFAULT_UPLOAD_BASE_URL);

		setDispatcherImpl("twitter4j.internal.async.DispatcherImpl");

		setIncludeRTsEnbled(true);
		setUserStreamRepliesAllEnabled(true);

		setMediaProvider("TWITTER");
		setMediaProviderAPIKey(null);
		setMediaProviderParameters(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ConfigurationBase)) return false;

		final ConfigurationBase that = (ConfigurationBase) o;

		if (asyncNumThreads != that.asyncNumThreads) return false;
		if (debug != that.debug) return false;
		if (defaultMaxPerRoute != that.defaultMaxPerRoute) return false;
		if (gzipEnabled != that.gzipEnabled) return false;
		if (httpConnectionTimeout != that.httpConnectionTimeout) return false;
		if (httpProxyPort != that.httpProxyPort) return false;
		if (httpReadTimeout != that.httpReadTimeout) return false;
		if (httpRetryCount != that.httpRetryCount) return false;
		if (httpRetryIntervalSeconds != that.httpRetryIntervalSeconds) return false;
		if (httpStreamingReadTimeout != that.httpStreamingReadTimeout) return false;
		if (includeEntitiesEnabled != that.includeEntitiesEnabled) return false;
		if (includeRTsEnabled != that.includeRTsEnabled) return false;
		if (jsonStoreEnabled != that.jsonStoreEnabled) return false;
		if (maxTotalConnections != that.maxTotalConnections) return false;
		if (prettyDebug != that.prettyDebug) return false;
		if (useSSL != that.useSSL) return false;
		if (ignoreSSLError != that.ignoreSSLError) return false;
		if (userStreamRepliesAllEnabled != that.userStreamRepliesAllEnabled) return false;
		if (clientURL != null ? !clientURL.equals(that.clientURL) : that.clientURL != null) return false;
		if (clientVersion != null ? !clientVersion.equals(that.clientVersion) : that.clientVersion != null)
			return false;
		if (dispatcherImpl != null ? !dispatcherImpl.equals(that.dispatcherImpl) : that.dispatcherImpl != null)
			return false;
		if (httpProxyHost != null ? !httpProxyHost.equals(that.httpProxyHost) : that.httpProxyHost != null)
			return false;
		if (httpProxyPassword != null ? !httpProxyPassword.equals(that.httpProxyPassword)
				: that.httpProxyPassword != null) return false;
		if (httpProxyUser != null ? !httpProxyUser.equals(that.httpProxyUser) : that.httpProxyUser != null)
			return false;
		if (mediaProvider != null ? !mediaProvider.equals(that.mediaProvider) : that.mediaProvider != null)
			return false;
		if (mediaProviderAPIKey != null ? !mediaProviderAPIKey.equals(that.mediaProviderAPIKey)
				: that.mediaProviderAPIKey != null) return false;
		if (mediaProviderParameters != null ? !mediaProviderParameters.equals(that.mediaProviderParameters)
				: that.mediaProviderParameters != null) return false;
		if (oAuthAccessToken != null ? !oAuthAccessToken.equals(that.oAuthAccessToken) : that.oAuthAccessToken != null)
			return false;
		if (oAuthAccessTokenSecret != null ? !oAuthAccessTokenSecret.equals(that.oAuthAccessTokenSecret)
				: that.oAuthAccessTokenSecret != null) return false;
		if (oAuthBaseURL != null ? !oAuthBaseURL.equals(that.oAuthBaseURL) : that.oAuthBaseURL != null) return false;
		if (signingOAuthBaseURL != null ? !signingOAuthBaseURL.equals(that.signingOAuthBaseURL)
				: that.signingOAuthBaseURL != null) return false;
		if (oAuthAccessTokenURL != null ? !oAuthAccessTokenURL.equals(that.oAuthAccessTokenURL)
				: that.oAuthAccessTokenURL != null) return false;
		if (oAuthAuthenticationURL != null ? !oAuthAuthenticationURL.equals(that.oAuthAuthenticationURL)
				: that.oAuthAuthenticationURL != null) return false;
		if (oAuthAuthorizationURL != null ? !oAuthAuthorizationURL.equals(that.oAuthAuthorizationURL)
				: that.oAuthAuthorizationURL != null) return false;
		if (signingOAuthAccessTokenURL != null ? !signingOAuthAccessTokenURL.equals(that.signingOAuthAccessTokenURL)
				: that.signingOAuthAccessTokenURL != null) return false;
		if (signingOAuthAuthenticationURL != null ? !signingOAuthAuthenticationURL.equals(that.signingOAuthAuthenticationURL)
				: that.signingOAuthAuthenticationURL != null) return false;
		if (signingOAuthAuthorizationURL != null ? !signingOAuthAuthorizationURL.equals(that.signingOAuthAuthorizationURL)
				: that.signingOAuthAuthorizationURL != null) return false;
		if (oAuthConsumerKey != null ? !oAuthConsumerKey.equals(that.oAuthConsumerKey) : that.oAuthConsumerKey != null)
			return false;
		if (oAuthConsumerSecret != null ? !oAuthConsumerSecret.equals(that.oAuthConsumerSecret)
				: that.oAuthConsumerSecret != null) return false;
		if (oAuthRequestTokenURL != null ? !oAuthRequestTokenURL.equals(that.oAuthRequestTokenURL)
				: that.oAuthRequestTokenURL != null) return false;
		if (signingOAuthRequestTokenURL != null ? !signingOAuthRequestTokenURL.equals(that.signingOAuthRequestTokenURL)
				: that.signingOAuthRequestTokenURL != null) return false;
		if (password != null ? !password.equals(that.password) : that.password != null) return false;
		if (requestHeaders != null ? !requestHeaders.equals(that.requestHeaders) : that.requestHeaders != null)
			return false;
		if (restBaseURL != null ? !restBaseURL.equals(that.restBaseURL) : that.restBaseURL != null) return false;
		if (signingRestBaseURL != null ? !signingRestBaseURL.equals(that.signingRestBaseURL)
				: that.signingRestBaseURL != null) return false;
		if (searchBaseURL != null ? !searchBaseURL.equals(that.searchBaseURL) : that.searchBaseURL != null)
			return false;
		if (siteStreamBaseURL != null ? !siteStreamBaseURL.equals(that.siteStreamBaseURL)
				: that.siteStreamBaseURL != null) return false;
		if (streamBaseURL != null ? !streamBaseURL.equals(that.streamBaseURL) : that.streamBaseURL != null)
			return false;
		if (uploadBaseURL != null ? !uploadBaseURL.equals(that.uploadBaseURL) : that.uploadBaseURL != null)
			return false;
		if (user != null ? !user.equals(that.user) : that.user != null) return false;
		if (userAgent != null ? !userAgent.equals(that.userAgent) : that.userAgent != null) return false;
		if (userStreamBaseURL != null ? !userStreamBaseURL.equals(that.userStreamBaseURL)
				: that.userStreamBaseURL != null) return false;

		return true;
	}

	@Override
	public final int getAsyncNumThreads() {
		return asyncNumThreads;
	}

	@Override
	public final String getClientURL() {
		return clientURL;
	}

	@Override
	public final String getClientVersion() {
		return clientVersion;
	}

	@Override
	public String getDispatcherImpl() {
		return dispatcherImpl;
	}

	@Override
	public final int getHttpConnectionTimeout() {
		return httpConnectionTimeout;
	}

	@Override
	public final int getHttpDefaultMaxPerRoute() {
		return defaultMaxPerRoute;
	}

	@Override
	public final int getHttpMaxTotalConnections() {
		return maxTotalConnections;
	}

	@Override
	public final String getHttpProxyHost() {
		return httpProxyHost;
	}

	@Override
	public final String getHttpProxyPassword() {
		return httpProxyPassword;
	}

	@Override
	public final int getHttpProxyPort() {
		return httpProxyPort;
	}

	@Override
	public final String getHttpProxyUser() {
		return httpProxyUser;
	}

	@Override
	public final int getHttpReadTimeout() {
		return httpReadTimeout;
	}

	@Override
	public final int getHttpRetryCount() {
		return httpRetryCount;
	}

	@Override
	public final int getHttpRetryIntervalSeconds() {
		return httpRetryIntervalSeconds;
	}

	// methods for HttpClientConfiguration

	@Override
	public int getHttpStreamingReadTimeout() {
		return httpStreamingReadTimeout;
	}

	@Override
	public final boolean isSSLErrorIgnored() {
		return ignoreSSLError;
	}

	@Override
	public String getMediaProvider() {
		return mediaProvider;
	}

	@Override
	public String getMediaProviderAPIKey() {
		return mediaProviderAPIKey;
	}

	@Override
	public Properties getMediaProviderParameters() {
		return mediaProviderParameters;
	}

	@Override
	public String getOAuthAccessToken() {
		return oAuthAccessToken;
	}

	@Override
	public String getOAuthAccessTokenSecret() {
		return oAuthAccessTokenSecret;
	}

	@Override
	public String getOAuthAccessTokenURL() {
		return oAuthAccessTokenURL;
	}

	@Override
	public String getOAuthAuthenticationURL() {
		return oAuthAuthenticationURL;
	}

	@Override
	public String getOAuthAuthorizationURL() {
		return oAuthAuthorizationURL;
	}

	@Override
	public String getOAuthBaseURL() {
		return oAuthBaseURL;
	}

	@Override
	public final String getOAuthConsumerKey() {
		return oAuthConsumerKey;
	}

	@Override
	public final String getOAuthConsumerSecret() {
		return oAuthConsumerSecret;
	}

	@Override
	public String getOAuthRequestTokenURL() {
		return oAuthRequestTokenURL;
	}

	@Override
	public final String getPassword() {
		return password;
	}

	@Override
	public Map<String, String> getRequestHeaders() {
		return requestHeaders;
	}

	@Override
	public String getRestBaseURL() {
		return restBaseURL;
	}

	@Override
	public String getSearchBaseURL() {
		return searchBaseURL;
	}

	@Override
	public String getSigningOAuthBaseURL() {
		return signingOAuthBaseURL;
	}

	@Override
	public String getSigningRestBaseURL() {
		return signingRestBaseURL;
	}

	@Override
	public String getSiteStreamBaseURL() {
		return siteStreamBaseURL;
	}

	@Override
	public String getStreamBaseURL() {
		return streamBaseURL;
	}

	// oauth related setter/getters

	@Override
	public String getUploadBaseURL() {
		return uploadBaseURL;
	}

	@Override
	public final String getUser() {
		return user;
	}

	@Override
	public final String getUserAgent() {
		return userAgent;
	}

	@Override
	public String getUserStreamBaseURL() {
		return userStreamBaseURL;
	}

	@Override
	public int hashCode() {
		int result = debug ? 1 : 0;
		result = 31 * result + (userAgent != null ? userAgent.hashCode() : 0);
		result = 31 * result + (user != null ? user.hashCode() : 0);
		result = 31 * result + (password != null ? password.hashCode() : 0);
		result = 31 * result + (useSSL ? 1 : 0);
		result = 31 * result + (ignoreSSLError ? 1 : 0);
		result = 31 * result + (prettyDebug ? 1 : 0);
		result = 31 * result + (gzipEnabled ? 1 : 0);
		result = 31 * result + (httpProxyHost != null ? httpProxyHost.hashCode() : 0);
		result = 31 * result + (httpProxyUser != null ? httpProxyUser.hashCode() : 0);
		result = 31 * result + (httpProxyPassword != null ? httpProxyPassword.hashCode() : 0);
		result = 31 * result + httpProxyPort;
		result = 31 * result + httpConnectionTimeout;
		result = 31 * result + httpReadTimeout;
		result = 31 * result + httpStreamingReadTimeout;
		result = 31 * result + httpRetryCount;
		result = 31 * result + httpRetryIntervalSeconds;
		result = 31 * result + maxTotalConnections;
		result = 31 * result + defaultMaxPerRoute;
		result = 31 * result + (oAuthConsumerKey != null ? oAuthConsumerKey.hashCode() : 0);
		result = 31 * result + (oAuthConsumerSecret != null ? oAuthConsumerSecret.hashCode() : 0);
		result = 31 * result + (oAuthAccessToken != null ? oAuthAccessToken.hashCode() : 0);
		result = 31 * result + (oAuthAccessTokenSecret != null ? oAuthAccessTokenSecret.hashCode() : 0);
		result = 31 * result + (oAuthRequestTokenURL != null ? oAuthRequestTokenURL.hashCode() : 0);
		result = 31 * result + (oAuthAuthorizationURL != null ? oAuthAuthorizationURL.hashCode() : 0);
		result = 31 * result + (oAuthAccessTokenURL != null ? oAuthAccessTokenURL.hashCode() : 0);
		result = 31 * result + (oAuthAuthenticationURL != null ? oAuthAuthenticationURL.hashCode() : 0);
		result = 31 * result + (signingOAuthRequestTokenURL != null ? signingOAuthRequestTokenURL.hashCode() : 0);
		result = 31 * result + (signingOAuthAuthorizationURL != null ? signingOAuthAuthorizationURL.hashCode() : 0);
		result = 31 * result + (signingOAuthAccessTokenURL != null ? signingOAuthAccessTokenURL.hashCode() : 0);
		result = 31 * result + (signingOAuthAuthenticationURL != null ? signingOAuthAuthenticationURL.hashCode() : 0);
		result = 31 * result + (restBaseURL != null ? restBaseURL.hashCode() : 0);
		result = 31 * result + (signingRestBaseURL != null ? signingRestBaseURL.hashCode() : 0);
		result = 31 * result + (oAuthBaseURL != null ? oAuthBaseURL.hashCode() : 0);
		result = 31 * result + (signingOAuthBaseURL != null ? signingOAuthBaseURL.hashCode() : 0);
		result = 31 * result + (searchBaseURL != null ? searchBaseURL.hashCode() : 0);
		result = 31 * result + (streamBaseURL != null ? streamBaseURL.hashCode() : 0);
		result = 31 * result + (userStreamBaseURL != null ? userStreamBaseURL.hashCode() : 0);
		result = 31 * result + (siteStreamBaseURL != null ? siteStreamBaseURL.hashCode() : 0);
		result = 31 * result + (uploadBaseURL != null ? uploadBaseURL.hashCode() : 0);
		result = 31 * result + (dispatcherImpl != null ? dispatcherImpl.hashCode() : 0);
		result = 31 * result + asyncNumThreads;
		result = 31 * result + (includeRTsEnabled ? 1 : 0);
		result = 31 * result + (includeEntitiesEnabled ? 1 : 0);
		result = 31 * result + (jsonStoreEnabled ? 1 : 0);
		result = 31 * result + (userStreamRepliesAllEnabled ? 1 : 0);
		result = 31 * result + (mediaProvider != null ? mediaProvider.hashCode() : 0);
		result = 31 * result + (mediaProviderAPIKey != null ? mediaProviderAPIKey.hashCode() : 0);
		result = 31 * result + (mediaProviderParameters != null ? mediaProviderParameters.hashCode() : 0);
		result = 31 * result + (clientVersion != null ? clientVersion.hashCode() : 0);
		result = 31 * result + (clientURL != null ? clientURL.hashCode() : 0);
		result = 31 * result + (requestHeaders != null ? requestHeaders.hashCode() : 0);
		return result;
	}

	@Override
	public final boolean isDebugEnabled() {
		return debug;
	}

	@Override
	public boolean isGZIPEnabled() {
		return gzipEnabled;
	}

	@Override
	public boolean isIncludeEntitiesEnabled() {
		return includeEntitiesEnabled;
	}

	@Override
	public boolean isIncludeRTsEnabled() {
		return includeRTsEnabled;
	}

	@Override
	public boolean isJSONStoreEnabled() {
		return jsonStoreEnabled;
	}

	@Override
	public boolean isPrettyDebugEnabled() {
		return prettyDebug;
	}

	@Override
	public boolean isUserStreamRepliesAllEnabled() {
		return userStreamRepliesAllEnabled;
	}

	@Override
	public String toString() {
		return "ConfigurationBase{" + "debug=" + debug + ", userAgent='" + userAgent + '\'' + ", user='" + user + '\''
				+ ", password='" + password + '\'' + ", useSSL=" + useSSL + ", ignoreSSLError=" + ignoreSSLError
				+ ", prettyDebug=" + prettyDebug + ", gzipEnabled=" + gzipEnabled + ", httpProxyHost='" + httpProxyHost
				+ '\'' + ", httpProxyUser='" + httpProxyUser + '\'' + ", httpProxyPassword='" + httpProxyPassword
				+ '\'' + ", httpProxyPort=" + httpProxyPort + ", httpConnectionTimeout=" + httpConnectionTimeout
				+ ", httpReadTimeout=" + httpReadTimeout + ", httpStreamingReadTimeout=" + httpStreamingReadTimeout
				+ ", httpRetryCount=" + httpRetryCount + ", httpRetryIntervalSeconds=" + httpRetryIntervalSeconds
				+ ", maxTotalConnections=" + maxTotalConnections + ", defaultMaxPerRoute=" + defaultMaxPerRoute
				+ ", oAuthConsumerKey='" + oAuthConsumerKey + '\'' + ", oAuthConsumerSecret='" + oAuthConsumerSecret
				+ '\'' + ", oAuthAccessToken='" + oAuthAccessToken + '\'' + ", oAuthAccessTokenSecret='"
				+ oAuthAccessTokenSecret + '\'' + ", oAuthRequestTokenURL='" + oAuthRequestTokenURL + '\''
				+ ", oAuthAuthorizationURL='" + oAuthAuthorizationURL + '\'' + ", oAuthAccessTokenURL='"
				+ oAuthAccessTokenURL + '\'' + ", oAuthAuthenticationURL='" + oAuthAuthenticationURL + '\''
				+ ", restBaseURL='" + restBaseURL + '\'' + ", searchBaseURL='" + searchBaseURL + '\''
				+ ", streamBaseURL='" + streamBaseURL + '\'' + ", userStreamBaseURL='" + userStreamBaseURL + '\''
				+ ", siteStreamBaseURL='" + siteStreamBaseURL + '\'' + ", uploadBaseURL='" + uploadBaseURL + '\''
				+ ", dispatcherImpl='" + dispatcherImpl + '\'' + ", asyncNumThreads=" + asyncNumThreads
				+ ", includeRTsEnabled=" + includeRTsEnabled + ", includeEntitiesEnabled=" + includeEntitiesEnabled
				+ ", jsonStoreEnabled=" + jsonStoreEnabled + ", userStreamRepliesAllEnabled="
				+ userStreamRepliesAllEnabled + ", mediaProvider='" + mediaProvider + '\'' + ", mediaProviderAPIKey='"
				+ mediaProviderAPIKey + '\'' + ", mediaProviderParameters=" + mediaProviderParameters
				+ ", clientVersion='" + clientVersion + '\'' + ", clientURL='" + clientURL + '\'' + ", requestHeaders="
				+ requestHeaders + '}';
	}

	protected void cacheInstance() {
		cacheInstance(this);
	}

	// assures equality after deserializedation
	protected Object readResolve() throws ObjectStreamException {
		return getInstance(this);
	}

	protected final void setAsyncNumThreads(int asyncNumThreads) {
		this.asyncNumThreads = asyncNumThreads;
	}

	protected final void setClientURL(String clientURL) {
		this.clientURL = clientURL;
		initRequestHeaders();
	}

	protected final void setClientVersion(String clientVersion) {
		this.clientVersion = clientVersion;
		initRequestHeaders();
	}

	protected final void setDebug(boolean debug) {
		this.debug = debug;
	}

	protected final void setDispatcherImpl(String dispatcherImpl) {
		this.dispatcherImpl = dispatcherImpl;
	}

	protected final void setGZIPEnabled(boolean gzipEnabled) {
		this.gzipEnabled = gzipEnabled;
		initRequestHeaders();
	}

	protected final void setHttpConnectionTimeout(int connectionTimeout) {
		httpConnectionTimeout = connectionTimeout;
	}

	protected final void setHttpDefaultMaxPerRoute(int defaultMaxPerRoute) {
		this.defaultMaxPerRoute = defaultMaxPerRoute;
	}

	protected final void setHttpMaxTotalConnections(int maxTotalConnections) {
		this.maxTotalConnections = maxTotalConnections;
	}

	protected final void setHttpProxyHost(String proxyHost) {
		httpProxyHost = proxyHost;
	}

	protected final void setHttpProxyPassword(String proxyPassword) {
		httpProxyPassword = proxyPassword;
	}

	protected final void setHttpProxyPort(int proxyPort) {
		httpProxyPort = proxyPort;
	}

	protected final void setHttpProxyUser(String proxyUser) {
		httpProxyUser = proxyUser;
	}

	protected final void setHttpReadTimeout(int readTimeout) {
		httpReadTimeout = readTimeout;
	}

	protected final void setHttpRetryCount(int retryCount) {
		httpRetryCount = retryCount;
	}

	protected final void setHttpRetryIntervalSeconds(int retryIntervalSeconds) {
		httpRetryIntervalSeconds = retryIntervalSeconds;
	}

	protected final void setHttpStreamingReadTimeout(int httpStreamingReadTimeout) {
		this.httpStreamingReadTimeout = httpStreamingReadTimeout;
	}

	protected final void setIgnoreSSLError(boolean ignoreSSLError) {
		this.ignoreSSLError = ignoreSSLError;
	}

	protected final void setIncludeEntitiesEnbled(boolean enabled) {
		includeEntitiesEnabled = enabled;
	}

	protected final void setIncludeRTsEnbled(boolean enabled) {
		includeRTsEnabled = enabled;
	}

	protected final void setJSONStoreEnabled(boolean enabled) {
		jsonStoreEnabled = enabled;
	}

	protected final void setMediaProvider(String mediaProvider) {
		this.mediaProvider = mediaProvider;
	}

	protected final void setMediaProviderAPIKey(String mediaProviderAPIKey) {
		this.mediaProviderAPIKey = mediaProviderAPIKey;
	}

	protected final void setMediaProviderParameters(Properties props) {
		mediaProviderParameters = props;
	}

	protected final void setOAuthAccessToken(String oAuthAccessToken) {
		this.oAuthAccessToken = oAuthAccessToken;
	}

	protected final void setOAuthAccessTokenSecret(String oAuthAccessTokenSecret) {
		this.oAuthAccessTokenSecret = oAuthAccessTokenSecret;
	}

	@Deprecated
	protected final void setOAuthAccessTokenURL(String oAuthAccessTokenURL) {
		if (isNullOrEmpty(oAuthAccessTokenURL)) oAuthAccessTokenURL = DEFAULT_OAUTH_ACCESS_TOKEN_URL;
		this.oAuthAccessTokenURL = oAuthAccessTokenURL;
		fixRestBaseURL();
	}

	@Deprecated
	protected final void setOAuthAuthenticationURL(String oAuthAuthenticationURL) {
		if (isNullOrEmpty(oAuthAuthenticationURL)) oAuthAuthenticationURL = DEFAULT_OAUTH_AUTHENTICATION_URL;
		this.oAuthAuthenticationURL = oAuthAuthenticationURL;
		fixRestBaseURL();
	}

	@Deprecated
	protected final void setOAuthAuthorizationURL(String oAuthAuthorizationURL) {
		if (isNullOrEmpty(oAuthAuthorizationURL)) oAuthAuthorizationURL = DEFAULT_OAUTH_AUTHORIZATION_URL;
		this.oAuthAuthorizationURL = oAuthAuthorizationURL;
		fixRestBaseURL();
	}
	
	@Deprecated
	protected final void setOAuthRequestTokenURL(String oAuthRequestTokenURL) {
		if (isNullOrEmpty(oAuthRequestTokenURL)) oAuthRequestTokenURL = DEFAULT_OAUTH_REQUEST_TOKEN_URL;
		this.oAuthRequestTokenURL = oAuthRequestTokenURL;
		fixRestBaseURL();
	}
	
	@Deprecated
	protected final void setSigningOAuthAccessTokenURL(String signingOAuthAccessTokenURL) {
		if (isNullOrEmpty(signingOAuthAccessTokenURL)) signingOAuthAccessTokenURL = DEFAULT_SIGNING_OAUTH_ACCESS_TOKEN_URL;
		this.signingOAuthAccessTokenURL = signingOAuthAccessTokenURL;
		fixRestBaseURL();
	}

	@Deprecated
	protected final void setSigningOAuthAuthenticationURL(String signingOAuthAuthenticationURL) {
		if (isNullOrEmpty(signingOAuthAuthenticationURL)) signingOAuthAuthenticationURL = DEFAULT_SIGNING_OAUTH_AUTHENTICATION_URL;
		this.signingOAuthAuthenticationURL = signingOAuthAuthenticationURL;
		fixRestBaseURL();
	}

	@Deprecated
	protected final void setSigningOAuthAuthorizationURL(String signingOAuthAuthorizationURL) {
		if (isNullOrEmpty(signingOAuthAuthorizationURL)) signingOAuthAuthorizationURL = DEFAULT_SIGNING_OAUTH_AUTHORIZATION_URL;
		this.signingOAuthAuthorizationURL = signingOAuthAuthorizationURL;
		fixRestBaseURL();
	}
	
	@Deprecated
	protected final void setSigningOAuthRequestTokenURL(String signingOAuthRequestTokenURL) {
		if (isNullOrEmpty(signingOAuthRequestTokenURL)) signingOAuthRequestTokenURL = DEFAULT_SIGNING_OAUTH_REQUEST_TOKEN_URL;
		this.signingOAuthRequestTokenURL = signingOAuthRequestTokenURL;
		fixRestBaseURL();
	}

	protected final void setOAuthBaseURL(String oAuthBaseURL) {
		if (isNullOrEmpty(oAuthBaseURL)) oAuthBaseURL = DEFAULT_OAUTH_BASE_URL;
		this.oAuthBaseURL = fixURLSlash(oAuthBaseURL);

		oAuthAccessTokenURL = oAuthBaseURL + PATH_SEGMENT_ACCESS_TOKEN;
		oAuthAuthenticationURL = oAuthBaseURL + PATH_SEGMENT_AUTHENTICATION;
		oAuthAuthorizationURL = oAuthBaseURL + PATH_SEGMENT_AUTHORIZATION;
		oAuthRequestTokenURL = oAuthBaseURL + PATH_SEGMENT_REQUEST_TOKEN;
	
		setSigningOAuthBaseURL(oAuthBaseURL);
		fixRestBaseURL();
	}

	protected final void setOAuthConsumerKey(String oAuthConsumerKey) {
		this.oAuthConsumerKey = oAuthConsumerKey;
		fixRestBaseURL();
	}

	protected final void setOAuthConsumerSecret(String oAuthConsumerSecret) {
		this.oAuthConsumerSecret = oAuthConsumerSecret;
		fixRestBaseURL();
	}


	protected final void setPassword(String password) {
		this.password = password;
	}

	protected final void setPrettyDebugEnabled(boolean prettyDebug) {
		this.prettyDebug = prettyDebug;
	}

	protected final void setRestBaseURL(String restBaseURL) {
		if (isNullOrEmpty(restBaseURL)) restBaseURL = DEFAULT_REST_BASE_URL;
		this.restBaseURL = fixURLSlash(restBaseURL);
		fixRestBaseURL();
	}

	protected final void setSearchBaseURL(String searchBaseURL) {
		if (isNullOrEmpty(searchBaseURL)) searchBaseURL = DEFAULT_SEARCH_BASE_URL;
		this.searchBaseURL = fixURLSlash(searchBaseURL);
	}

	protected final void setSigningOAuthBaseURL(String signingOAuthBaseURL) {
		if (isNullOrEmpty(signingOAuthBaseURL)) signingOAuthBaseURL = DEFAULT_SIGNING_OAUTH_BASE_URL;
		this.signingOAuthBaseURL = fixURLSlash(signingOAuthBaseURL);
		
		signingOAuthAccessTokenURL = signingOAuthBaseURL + PATH_SEGMENT_ACCESS_TOKEN;
		signingOAuthAuthenticationURL = signingOAuthBaseURL + PATH_SEGMENT_AUTHENTICATION;
		signingOAuthAuthorizationURL = signingOAuthBaseURL + PATH_SEGMENT_AUTHORIZATION;
		signingOAuthRequestTokenURL = signingOAuthBaseURL + PATH_SEGMENT_REQUEST_TOKEN;
		
		fixRestBaseURL();
	}

	protected final void setSigningRestBaseURL(String signingRestBaseURL) {
		if (isNullOrEmpty(signingRestBaseURL)) signingRestBaseURL = DEFAULT_SIGNING_REST_BASE_URL;
		this.signingRestBaseURL = fixURLSlash(signingRestBaseURL);
		fixRestBaseURL();
	}

	protected final void setSiteStreamBaseURL(String siteStreamBaseURL) {
		if (isNullOrEmpty(siteStreamBaseURL)) siteStreamBaseURL = DEFAULT_SITE_STREAM_BASE_URL;
		this.siteStreamBaseURL = siteStreamBaseURL;
	}

	protected final void setStreamBaseURL(String streamBaseURL) {
		if (isNullOrEmpty(streamBaseURL)) streamBaseURL = DEFAULT_STREAM_BASE_URL;
		this.streamBaseURL = streamBaseURL;
	}

	protected final void setUploadBaseURL(String uploadBaseURL) {
		if (isNullOrEmpty(uploadBaseURL)) uploadBaseURL = DEFAULT_UPLOAD_BASE_URL;
		this.uploadBaseURL = fixURLSlash(uploadBaseURL);
		fixUploadBaseURL();
	}

	protected final void setUser(String user) {
		this.user = user;
	}

	protected final void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
		initRequestHeaders();
	}

	protected final void setUserStreamBaseURL(String siteStreamBaseURL) {
		userStreamBaseURL = siteStreamBaseURL;
	}

	protected final void setUserStreamRepliesAllEnabled(boolean enabled) {
		userStreamRepliesAllEnabled = enabled;
	}

	protected final void setUseSSL(boolean useSSL) {
		this.useSSL = useSSL;
		fixRestBaseURL();
	}

	private void fixRestBaseURL() {
		if (DEFAULT_REST_BASE_URL.equals(fixURL(DEFAULT_USE_SSL, restBaseURL))) {
			restBaseURL = fixURL(useSSL, restBaseURL);
		}
		if (restBaseURL != null && restBaseURL.equals(fixURL(DEFAULT_USE_SSL, signingRestBaseURL))) {
			signingRestBaseURL = fixURL(useSSL, signingRestBaseURL);
		}
		//TODO
		if (DEFAULT_OAUTH_BASE_URL.equals(fixURL(DEFAULT_USE_SSL, oAuthBaseURL))) {
			oAuthBaseURL = fixURL(useSSL, oAuthBaseURL);
		}
		//TODO
		if (oAuthBaseURL != null && oAuthBaseURL.equals(fixURL(DEFAULT_USE_SSL, signingOAuthBaseURL))) {
			signingOAuthBaseURL = fixURL(useSSL, signingOAuthBaseURL);
		}
		if (oAuthBaseURL != null && (oAuthBaseURL+PATH_SEGMENT_ACCESS_TOKEN).equals(fixURL(DEFAULT_USE_SSL, oAuthAccessTokenURL))) {
			oAuthAccessTokenURL = fixURL(useSSL, oAuthAccessTokenURL);
		}
		if (oAuthBaseURL != null && (oAuthBaseURL+PATH_SEGMENT_AUTHENTICATION).equals(fixURL(DEFAULT_USE_SSL, oAuthAuthenticationURL))) {
			oAuthAuthenticationURL = fixURL(useSSL, oAuthAuthenticationURL);
		}
		if (oAuthBaseURL != null && (oAuthBaseURL+PATH_SEGMENT_AUTHORIZATION).equals(fixURL(DEFAULT_USE_SSL, oAuthAuthorizationURL))) {
			oAuthAuthorizationURL = fixURL(useSSL, oAuthAuthorizationURL);
		}
		if (oAuthBaseURL != null && (oAuthBaseURL+PATH_SEGMENT_REQUEST_TOKEN).equals(fixURL(DEFAULT_USE_SSL, oAuthRequestTokenURL))) {
			oAuthRequestTokenURL = fixURL(useSSL, oAuthRequestTokenURL);
		}
		if (signingOAuthBaseURL != null && (signingOAuthBaseURL+PATH_SEGMENT_ACCESS_TOKEN).equals(fixURL(DEFAULT_USE_SSL, signingOAuthAccessTokenURL))) {
			signingOAuthAccessTokenURL = fixURL(useSSL, signingOAuthAccessTokenURL);
		}
		if (signingOAuthBaseURL != null && (signingOAuthBaseURL+PATH_SEGMENT_ACCESS_TOKEN).equals(fixURL(DEFAULT_USE_SSL, signingOAuthAuthenticationURL))) {
			signingOAuthAuthenticationURL = fixURL(useSSL, signingOAuthAuthenticationURL);
		}
		if (signingOAuthBaseURL != null && (signingOAuthBaseURL+PATH_SEGMENT_ACCESS_TOKEN).equals(fixURL(DEFAULT_USE_SSL, signingOAuthAuthorizationURL))) {
			signingOAuthAuthorizationURL = fixURL(useSSL, signingOAuthAuthorizationURL);
		}
		if (signingOAuthBaseURL != null && (signingOAuthBaseURL+PATH_SEGMENT_ACCESS_TOKEN).equals(fixURL(DEFAULT_USE_SSL, signingOAuthRequestTokenURL))) {
			signingOAuthRequestTokenURL = fixURL(useSSL, signingOAuthRequestTokenURL);
		}
		if (DEFAULT_SEARCH_BASE_URL.equals(fixURL(DEFAULT_USE_SSL, searchBaseURL))) {
			searchBaseURL = fixURL(useSSL, searchBaseURL);
		}
	}

	private void fixUploadBaseURL() {
		if (DEFAULT_UPLOAD_BASE_URL.equals(fixURL(DEFAULT_USE_SSL, uploadBaseURL))) {
			uploadBaseURL = fixURL(useSSL, uploadBaseURL);
		}
	}

	static String fixURLSlash(String urlOrig) {
		if (urlOrig == null) return null;
		if (!urlOrig.endsWith("/")) return urlOrig + "/";
		return urlOrig;
	}

	private void initRequestHeaders() {
		requestHeaders = new HashMap<String, String>();
		requestHeaders.put("X-Twitter-Client-Version", getClientVersion());
		requestHeaders.put("X-Twitter-Client-URL", getClientURL());
		requestHeaders.put("X-Twitter-Client", "Twitter4J");

		requestHeaders.put("User-Agent", getUserAgent());
		if (gzipEnabled) {
			requestHeaders.put("Accept-Encoding", "gzip");
		}
		requestHeaders.put("Connection", "close");
	}

	private static void cacheInstance(ConfigurationBase conf) {
		if (!instances.contains(conf)) {
			instances.add(conf);
		}
	}

	private static ConfigurationBase getInstance(ConfigurationBase configurationBase) {
		int index;
		if ((index = instances.indexOf(configurationBase)) == -1) {
			instances.add(configurationBase);
			return configurationBase;
		} else
			return instances.get(index);
	}

	static boolean isNullOrEmpty(String string) {
		if (string == null) return true;
		if (string.length() == 0) return true;
		return false;
	}
	
	static String fixURL(boolean useSSL, String url) {
		if (null == url) return null;
		if (!url.startsWith("http://") || !url.startsWith("https://")) {
			url = "https://" + url;
		}
		final int index = url.indexOf("://");
		if (-1 == index) throw new IllegalArgumentException("url should contain '://'");
		final String hostAndLater = url.substring(index + 3);
		if (useSSL)
			return "https://" + hostAndLater;
		else
			return "http://" + hostAndLater;
	}

	@Override
	public String getSigningOAuthAccessTokenURL() {
		return signingOAuthAccessTokenURL;
	}

	@Override
	public String getSigningOAuthAuthenticationURL() {
		return signingOAuthAuthenticationURL;
	}

	@Override
	public String getSigningOAuthAuthorizationURL() {
		return signingOAuthAuthorizationURL;
	}

	@Override
	public String getSigningOAuthRequestTokenURL() {
		return signingOAuthRequestTokenURL;
	}

}
