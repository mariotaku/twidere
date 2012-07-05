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

import java.util.Map;
import java.util.Properties;

import twitter4j.auth.AuthorizationConfiguration;
import twitter4j.internal.http.HttpClientConfiguration;
import twitter4j.internal.http.HttpClientWrapperConfiguration;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public interface Configuration extends HttpClientConfiguration, HttpClientWrapperConfiguration,
		AuthorizationConfiguration, java.io.Serializable {

	int getAsyncNumThreads();

	String getClientURL();

	String getClientVersion();

	String getDispatcherImpl();

	@Override
	int getHttpConnectionTimeout();

	// methods for HttpClientConfiguration

	@Override
	int getHttpDefaultMaxPerRoute();

	@Override
	int getHttpMaxTotalConnections();

	@Override
	String getHttpProxyHost();

	@Override
	String getHttpProxyPassword();

	@Override
	int getHttpProxyPort();

	@Override
	String getHttpProxyUser();

	@Override
	int getHttpReadTimeout();

	@Override
	int getHttpRetryCount();

	@Override
	int getHttpRetryIntervalSeconds();

	int getHttpStreamingReadTimeout();

	@Override
	boolean isSSLErrorIgnored();

	// oauth related setter/getters

	String getMediaProvider();

	String getMediaProviderAPIKey();

	Properties getMediaProviderParameters();

	@Override
	String getOAuthAccessToken();

	@Override
	String getOAuthAccessTokenSecret();

	String getOAuthAccessTokenURL();
	String getOAuthAuthenticationURL();
	String getOAuthAuthorizationURL();
	String getOAuthRequestTokenURL();

	String getSigningOAuthAccessTokenURL();
	String getSigningOAuthAuthenticationURL();
	String getSigningOAuthAuthorizationURL();
	String getSigningOAuthRequestTokenURL();
	
	String getOAuthBaseURL();

	@Override
	String getOAuthConsumerKey();

	@Override
	String getOAuthConsumerSecret();


	@Override
	String getPassword();

	@Override
	Map<String, String> getRequestHeaders();

	String getRestBaseURL();

	String getSearchBaseURL();

	String getSigningOAuthBaseURL();

	String getSigningRestBaseURL();

	String getSiteStreamBaseURL();

	String getStreamBaseURL();

	String getUploadBaseURL();

	@Override
	String getUser();

	String getUserAgent();

	String getUserStreamBaseURL();

	boolean isDebugEnabled();

	boolean isIncludeEntitiesEnabled();

	boolean isIncludeRTsEnabled();

	boolean isJSONStoreEnabled();

	boolean isUserStreamRepliesAllEnabled();
}
