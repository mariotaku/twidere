/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.nostra13.universalimageloader.core.download;

import android.content.Context;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;

import java.io.IOException;
import java.io.InputStream;

/**
 * Implementation of ImageDownloader which uses {@link HttpClient} for image
 * stream retrieving.
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.4.1
 */
public class HttpClientImageDownloader extends BaseImageDownloader {

    private final HttpClient httpClient;

    public HttpClientImageDownloader(final Context context, final HttpClient httpClient) {
        super(context);
        this.httpClient = httpClient;
    }

    @Override
    protected InputStream getStreamFromNetwork(final String imageUri, final Object extra)
            throws IOException {
        final HttpGet httpRequest = new HttpGet(imageUri);
        final HttpResponse response = httpClient.execute(httpRequest);
        final HttpEntity entity = response.getEntity();
        final BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
        return bufHttpEntity.getContent();
    }
}
