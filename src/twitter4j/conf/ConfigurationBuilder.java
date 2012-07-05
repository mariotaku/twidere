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

import java.util.Properties;

/**
 * A builder that can be used to construct a twitter4j configuration with
 * desired settings. This builder has sensible defaults such that
 * {@code new ConfigurationBuilder().build()} would create a usable
 * configuration. This configuration builder is useful for clients that wish to
 * configure twitter4j in unit tests or from command line flags for example.
 * 
 * @author John Sirois - john.sirois at gmail.com
 */
public final class ConfigurationBuilder {

	@SuppressWarnings("deprecation")
	private ConfigurationBase configuration = new PropertyConfiguration();

	public Configuration build() {
		checkNotBuilt();
		configuration.cacheInstance();
		try {
			return configuration;
		} finally {
			configuration = null;
		}
	}

	public ConfigurationBuilder setAsyncNumThreads(int asyncNumThreads) {
		checkNotBuilt();
		configuration.setAsyncNumThreads(asyncNumThreads);
		return this;
	}

	public ConfigurationBuilder setClientURL(String clientURL) {
		checkNotBuilt();
		configuration.setClientURL(clientURL);
		return this;
	}

	public ConfigurationBuilder setClientVersion(String clientVersion) {
		checkNotBuilt();
		configuration.setClientVersion(clientVersion);
		return this;
	}

	public ConfigurationBuilder setDebugEnabled(boolean debugEnabled) {
		checkNotBuilt();
		configuration.setDebug(debugEnabled);
		return this;
	}

	public ConfigurationBuilder setDispatcherImpl(String dispatcherImpl) {
		checkNotBuilt();
		configuration.setDispatcherImpl(dispatcherImpl);
		return this;
	}

	public ConfigurationBuilder setGZIPEnabled(boolean gzipEnabled) {
		checkNotBuilt();
		configuration.setGZIPEnabled(gzipEnabled);
		return this;
	}

	public ConfigurationBuilder setHttpConnectionTimeout(int httpConnectionTimeout) {
		checkNotBuilt();
		configuration.setHttpConnectionTimeout(httpConnectionTimeout);
		return this;
	}

	public ConfigurationBuilder setHttpDefaultMaxPerRoute(int httpDefaultMaxPerRoute) {
		checkNotBuilt();
		configuration.setHttpDefaultMaxPerRoute(httpDefaultMaxPerRoute);
		return this;
	}

	public ConfigurationBuilder setHttpMaxTotalConnections(int httpMaxConnections) {
		checkNotBuilt();
		configuration.setHttpMaxTotalConnections(httpMaxConnections);
		return this;
	}

	public ConfigurationBuilder setHttpProxyHost(String httpProxyHost) {
		checkNotBuilt();
		configuration.setHttpProxyHost(httpProxyHost);
		return this;
	}

	public ConfigurationBuilder setHttpProxyPassword(String httpProxyPassword) {
		checkNotBuilt();
		configuration.setHttpProxyPassword(httpProxyPassword);
		return this;
	}

	public ConfigurationBuilder setHttpProxyPort(int httpProxyPort) {
		checkNotBuilt();
		configuration.setHttpProxyPort(httpProxyPort);
		return this;
	}

	public ConfigurationBuilder setHttpProxyUser(String httpProxyUser) {
		checkNotBuilt();
		configuration.setHttpProxyUser(httpProxyUser);
		return this;
	}

	public ConfigurationBuilder setHttpReadTimeout(int httpReadTimeout) {
		checkNotBuilt();
		configuration.setHttpReadTimeout(httpReadTimeout);
		return this;
	}

	public ConfigurationBuilder setHttpRetryCount(int httpRetryCount) {
		checkNotBuilt();
		configuration.setHttpRetryCount(httpRetryCount);
		return this;
	}

	public ConfigurationBuilder setHttpRetryIntervalSeconds(int httpRetryIntervalSeconds) {
		checkNotBuilt();
		configuration.setHttpRetryIntervalSeconds(httpRetryIntervalSeconds);
		return this;
	}

	public ConfigurationBuilder setHttpStreamingReadTimeout(int httpStreamingReadTimeout) {
		checkNotBuilt();
		configuration.setHttpStreamingReadTimeout(httpStreamingReadTimeout);
		return this;
	}

	public ConfigurationBuilder setIgnoreSSLError(boolean ignoreSSLError) {
		checkNotBuilt();
		configuration.setIgnoreSSLError(ignoreSSLError);
		return this;
	}

	public ConfigurationBuilder setIncludeEntitiesEnabled(boolean enabled) {
		checkNotBuilt();
		configuration.setIncludeEntitiesEnbled(enabled);
		return this;
	}

	public ConfigurationBuilder setIncludeRTsEnabled(boolean enabled) {
		checkNotBuilt();
		configuration.setIncludeRTsEnbled(enabled);
		return this;
	}

	public ConfigurationBuilder setJSONStoreEnabled(boolean enabled) {
		checkNotBuilt();
		configuration.setJSONStoreEnabled(enabled);
		return this;
	}

	public ConfigurationBuilder setMediaProvider(String mediaProvider) {
		checkNotBuilt();
		configuration.setMediaProvider(mediaProvider);
		return this;
	}

	public ConfigurationBuilder setMediaProviderAPIKey(String mediaProviderAPIKey) {
		checkNotBuilt();
		configuration.setMediaProviderAPIKey(mediaProviderAPIKey);
		return this;
	}

	public ConfigurationBuilder setMediaProviderParameters(Properties props) {
		checkNotBuilt();
		configuration.setMediaProviderParameters(props);
		return this;
	}

	public ConfigurationBuilder setOAuthAccessToken(String oAuthAccessToken) {
		checkNotBuilt();
		configuration.setOAuthAccessToken(oAuthAccessToken);
		return this;
	}

	public ConfigurationBuilder setOAuthAccessTokenSecret(String oAuthAccessTokenSecret) {
		checkNotBuilt();
		configuration.setOAuthAccessTokenSecret(oAuthAccessTokenSecret);
		return this;
	}

	@Deprecated
	public ConfigurationBuilder setOAuthAccessTokenURL(String oAuthAccessTokenURL) {
		checkNotBuilt();
		configuration.setOAuthAccessTokenURL(oAuthAccessTokenURL);
		return this;
	}

	@Deprecated
	public ConfigurationBuilder setOAuthAuthenticationURL(String oAuthAuthenticationURL) {
		checkNotBuilt();
		configuration.setOAuthAuthenticationURL(oAuthAuthenticationURL);
		return this;
	}

	@Deprecated
	public ConfigurationBuilder setOAuthAuthorizationURL(String oAuthAuthorizationURL) {
		checkNotBuilt();
		configuration.setOAuthAuthorizationURL(oAuthAuthorizationURL);
		return this;
	}

	@Deprecated
	public ConfigurationBuilder setOAuthRequestTokenURL(String oAuthRequestTokenURL) {
		checkNotBuilt();
		configuration.setOAuthRequestTokenURL(oAuthRequestTokenURL);
		return this;
	}
	
	@Deprecated
	public ConfigurationBuilder setSigningOAuthAccessTokenURL(String oAuthAccessTokenURL) {
		checkNotBuilt();
		configuration.setSigningOAuthAccessTokenURL(oAuthAccessTokenURL);
		return this;
	}

	@Deprecated
	public ConfigurationBuilder setSigningOAuthAuthenticationURL(String oAuthAuthenticationURL) {
		checkNotBuilt();
		configuration.setSigningOAuthAuthenticationURL(oAuthAuthenticationURL);
		return this;
	}

	@Deprecated
	public ConfigurationBuilder setSigningOAuthAuthorizationURL(String oAuthAuthorizationURL) {
		checkNotBuilt();
		configuration.setSigningOAuthAuthorizationURL(oAuthAuthorizationURL);
		return this;
	}

	@Deprecated
	public ConfigurationBuilder setSigningOAuthRequestTokenURL(String oAuthRequestTokenURL) {
		checkNotBuilt();
		configuration.setSigningOAuthRequestTokenURL(oAuthRequestTokenURL);
		return this;
	}
	
	public ConfigurationBuilder setOAuthBaseURL(String oAuthBaseURL) {
		checkNotBuilt();
		configuration.setOAuthBaseURL(oAuthBaseURL);
		return this;
	}

	public ConfigurationBuilder setOAuthConsumerKey(String oAuthConsumerKey) {
		checkNotBuilt();
		configuration.setOAuthConsumerKey(oAuthConsumerKey);
		return this;
	}

	public ConfigurationBuilder setOAuthConsumerSecret(String oAuthConsumerSecret) {
		checkNotBuilt();
		configuration.setOAuthConsumerSecret(oAuthConsumerSecret);
		return this;
	}


	public ConfigurationBuilder setPassword(String password) {
		checkNotBuilt();
		configuration.setPassword(password);
		return this;
	}

	public ConfigurationBuilder setPrettyDebugEnabled(boolean prettyDebugEnabled) {
		checkNotBuilt();
		configuration.setPrettyDebugEnabled(prettyDebugEnabled);
		return this;
	}

	public ConfigurationBuilder setRestBaseURL(String restBaseURL) {
		checkNotBuilt();
		configuration.setRestBaseURL(restBaseURL);
		return this;
	}

	public ConfigurationBuilder setSearchBaseURL(String searchBaseURL) {
		checkNotBuilt();
		configuration.setSearchBaseURL(searchBaseURL);
		return this;
	}

	public ConfigurationBuilder setSigningOAuthBaseURL(String signingOAuthBaseURL) {
		checkNotBuilt();
		configuration.setSigningOAuthBaseURL(signingOAuthBaseURL);
		return this;
	}

	public ConfigurationBuilder setSigningRestBaseURL(String signingRestBaseURL) {
		checkNotBuilt();
		configuration.setSigningRestBaseURL(signingRestBaseURL);
		return this;
	}

	public ConfigurationBuilder setSiteStreamBaseURL(String siteStreamBaseURL) {
		checkNotBuilt();
		configuration.setSiteStreamBaseURL(siteStreamBaseURL);
		return this;
	}

	public ConfigurationBuilder setStreamBaseURL(String streamBaseURL) {
		checkNotBuilt();
		configuration.setStreamBaseURL(streamBaseURL);
		return this;
	}

	public ConfigurationBuilder setUploadBaseURL(String uploadURL) {
		checkNotBuilt();
		configuration.setUploadBaseURL(uploadURL);
		return this;
	}

	public ConfigurationBuilder setUser(String user) {
		checkNotBuilt();
		configuration.setUser(user);
		return this;
	}

	public ConfigurationBuilder setUserStreamBaseURL(String userStreamBaseURL) {
		checkNotBuilt();
		configuration.setUserStreamBaseURL(userStreamBaseURL);
		return this;
	}

	public ConfigurationBuilder setUserStreamRepliesAllEnabled(boolean enabled) {
		checkNotBuilt();
		configuration.setUserStreamRepliesAllEnabled(enabled);
		return this;
	}

	public ConfigurationBuilder setUseSSL(boolean useSSL) {
		checkNotBuilt();
		configuration.setUseSSL(useSSL);
		return this;
	}

	private void checkNotBuilt() {
		if (configuration == null)
			throw new IllegalStateException("Cannot use this builder any longer, build() has already been called");
	}
}
