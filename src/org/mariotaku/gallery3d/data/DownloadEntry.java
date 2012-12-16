/*
 * Copyright (C) 2010 The Android Open Source Project
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
package org.mariotaku.gallery3d.data;

import org.mariotaku.gallery3d.common.Entry;
import org.mariotaku.gallery3d.common.EntrySchema;

@Entry.Table("download")
public class DownloadEntry extends Entry {
	public static final EntrySchema SCHEMA = new EntrySchema(DownloadEntry.class);

	@Column(value = "hash_code", indexed = true)
	public long hashCode;

	@Column("content_url")
	public String contentUrl;

	@Column("_size")
	public long contentSize;

	@Column("etag")
	public String eTag;

	@Column(value = "last_access", indexed = true)
	public long lastAccessTime;

	@Column(value = "last_updated")
	public long lastUpdatedTime;

	@Column("_data")
	public String path;

	@Override
	public String toString() {
		// Note: THIS IS REQUIRED. We used all the fields here. Otherwise,
		// ProGuard will remove these UNUSED fields. However, these
		// fields are needed to generate database.
		return new StringBuilder().append("hash_code: ").append(hashCode).append(", ").append("content_url")
				.append(contentUrl).append(", ").append("_size").append(contentSize).append(", ").append("etag")
				.append(eTag).append(", ").append("last_access").append(lastAccessTime).append(", ")
				.append("last_updated").append(lastUpdatedTime).append(",").append("_data").append(path).toString();
	}

	public static interface Columns extends Entry.Columns {
		public static final String HASH_CODE = "hash_code";
		public static final String CONTENT_URL = "content_url";
		public static final String CONTENT_SIZE = "_size";
		public static final String ETAG = "etag";
		public static final String LAST_ACCESS = "last_access";
		public static final String LAST_UPDATED = "last_updated";
		public static final String DATA = "_data";
	}
}
