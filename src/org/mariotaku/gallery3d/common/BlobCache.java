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

// This is an on-disk cache which maps a 64-bits key to a byte array.
//
// It consists of three files: one index file and two data files. One of the
// data files is "active", and the other is "inactive". New entries are
// appended into the active region until it reaches the size limit. At that
// point the active file and the inactive file are swapped, and the new active
// file is truncated to empty (and the index for that file is also cleared).
// The index is a hash table with linear probing. When the load factor reaches
// 0.5, it does the same thing like when the size limit is reached.
//
// The index file format: (all numbers are stored in little-endian)
// [0]  Magic number: 0xB3273030
// [4]  MaxEntries: Max number of hash entries per region.
// [8]  MaxBytes: Max number of data bytes per region (including header).
// [12] ActiveRegion: The active growing region: 0 or 1.
// [16] ActiveEntries: The number of hash entries used in the active region.
// [20] ActiveBytes: The number of data bytes used in the active region.
// [24] Version number.
// [28] Checksum of [0..28).
// [32] Hash entries for region 0. The size is X = (12 * MaxEntries bytes).
// [32 + X] Hash entries for region 1. The size is also X.
//
// Each hash entry is 12 bytes: 8 bytes key and 4 bytes offset into the data
// file. The offset is 0 when the slot is free. Note that 0 is a valid value
// for key. The keys are used directly as index into a hash table, so they
// should be suitably distributed.
//
// Each data file stores data for one region. The data file is concatenated
// blobs followed by the magic number 0xBD248510.
//
// The blob format:
// [0]  Key of this blob
// [8]  Checksum of this blob
// [12] Offset of this blob
// [16] Length of this blob (not including header)
// [20] Blob
//
// Below are the interface for BlobCache. The instance of this class does not
// support concurrent use by multiple threads.
//
// public BlobCache(String path, int maxEntries, int maxBytes, boolean reset) throws IOException;
// public void insert(long key, byte[] data) throws IOException;
// public byte[] lookup(long key) throws IOException;
// public void lookup(LookupRequest req) throws IOException;
// public void close();
// public void syncIndex();
// public void syncAll();
// public static void deleteFiles(String path);
//
package org.mariotaku.gallery3d.common;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.zip.Adler32;

import android.util.Log;

public class BlobCache implements Closeable {
	private static final String TAG = "BlobCache";

	private static final int MAGIC_INDEX_FILE = 0xB3273030;
	private static final int MAGIC_DATA_FILE = 0xBD248510;

	// index header offset
	private static final int IH_MAGIC = 0;
	private static final int IH_MAX_ENTRIES = 4;
	private static final int IH_MAX_BYTES = 8;
	private static final int IH_ACTIVE_REGION = 12;
	private static final int IH_ACTIVE_ENTRIES = 16;
	private static final int IH_ACTIVE_BYTES = 20;
	private static final int IH_VERSION = 24;
	private static final int IH_CHECKSUM = 28;
	private static final int INDEX_HEADER_SIZE = 32;

	private static final int DATA_HEADER_SIZE = 4;

	private RandomAccessFile mIndexFile;
	private RandomAccessFile mDataFile0;
	private RandomAccessFile mDataFile1;
	private FileChannel mIndexChannel;
	private MappedByteBuffer mIndexBuffer;

	private int mMaxEntries;
	private int mMaxBytes;
	private int mActiveRegion;
	private int mActiveEntries;
	private int mActiveBytes;
	private int mVersion;

	private RandomAccessFile mActiveDataFile;
	private final byte[] mIndexHeader = new byte[INDEX_HEADER_SIZE];
	private final Adler32 mAdler32 = new Adler32();

	public BlobCache(final String path, final int maxEntries, final int maxBytes, final boolean reset, final int version)
			throws IOException {
		mIndexFile = new RandomAccessFile(path + ".idx", "rw");
		mDataFile0 = new RandomAccessFile(path + ".0", "rw");
		mDataFile1 = new RandomAccessFile(path + ".1", "rw");
		mVersion = version;

		if (!reset && loadIndex()) return;

		resetCache(maxEntries, maxBytes);

		if (!loadIndex()) {
			closeAll();
			throw new IOException("unable to load index");
		}
	}

	// Close the cache. All resources are released. No other method should be
	// called after this is called.
	@Override
	public void close() {
		syncAll();
		closeAll();
	}

	private int checkSum(final byte[] data, final int offset, final int nbytes) {
		mAdler32.reset();
		mAdler32.update(data, offset, nbytes);
		return (int) mAdler32.getValue();
	}

	private void closeAll() {
		closeSilently(mIndexChannel);
		closeSilently(mIndexFile);
		closeSilently(mDataFile0);
		closeSilently(mDataFile1);
	}

	// Returns true if loading index is successful. After this method is called,
	// mIndexHeader and index header in file should be kept sync.
	private boolean loadIndex() {
		try {
			mIndexFile.seek(0);
			mDataFile0.seek(0);
			mDataFile1.seek(0);

			final byte[] buf = mIndexHeader;
			if (mIndexFile.read(buf) != INDEX_HEADER_SIZE) {
				Log.w(TAG, "cannot read header");
				return false;
			}

			if (readInt(buf, IH_MAGIC) != MAGIC_INDEX_FILE) {
				Log.w(TAG, "cannot read header magic");
				return false;
			}

			if (readInt(buf, IH_VERSION) != mVersion) {
				Log.w(TAG, "version mismatch");
				return false;
			}

			mMaxEntries = readInt(buf, IH_MAX_ENTRIES);
			mMaxBytes = readInt(buf, IH_MAX_BYTES);
			mActiveRegion = readInt(buf, IH_ACTIVE_REGION);
			mActiveEntries = readInt(buf, IH_ACTIVE_ENTRIES);
			mActiveBytes = readInt(buf, IH_ACTIVE_BYTES);

			final int sum = readInt(buf, IH_CHECKSUM);
			if (checkSum(buf, 0, IH_CHECKSUM) != sum) {
				Log.w(TAG, "header checksum does not match");
				return false;
			}

			// Sanity check
			if (mMaxEntries <= 0) {
				Log.w(TAG, "invalid max entries");
				return false;
			}
			if (mMaxBytes <= 0) {
				Log.w(TAG, "invalid max bytes");
				return false;
			}
			if (mActiveRegion != 0 && mActiveRegion != 1) {
				Log.w(TAG, "invalid active region");
				return false;
			}
			if (mActiveEntries < 0 || mActiveEntries > mMaxEntries) {
				Log.w(TAG, "invalid active entries");
				return false;
			}
			if (mActiveBytes < DATA_HEADER_SIZE || mActiveBytes > mMaxBytes) {
				Log.w(TAG, "invalid active bytes");
				return false;
			}
			if (mIndexFile.length() != INDEX_HEADER_SIZE + mMaxEntries * 12 * 2) {
				Log.w(TAG, "invalid index file length");
				return false;
			}

			// Make sure data file has magic
			final byte[] magic = new byte[4];
			if (mDataFile0.read(magic) != 4) {
				Log.w(TAG, "cannot read data file magic");
				return false;
			}
			if (readInt(magic, 0) != MAGIC_DATA_FILE) {
				Log.w(TAG, "invalid data file magic");
				return false;
			}
			if (mDataFile1.read(magic) != 4) {
				Log.w(TAG, "cannot read data file magic");
				return false;
			}
			if (readInt(magic, 0) != MAGIC_DATA_FILE) {
				Log.w(TAG, "invalid data file magic");
				return false;
			}

			// Map index file to memory
			mIndexChannel = mIndexFile.getChannel();
			mIndexBuffer = mIndexChannel.map(FileChannel.MapMode.READ_WRITE, 0, mIndexFile.length());
			mIndexBuffer.order(ByteOrder.LITTLE_ENDIAN);

			setActiveVariables();
			return true;
		} catch (final IOException ex) {
			Log.e(TAG, "loadIndex failed.", ex);
			return false;
		}
	}

	private void resetCache(final int maxEntries, final int maxBytes) throws IOException {
		mIndexFile.setLength(0); // truncate to zero the index
		mIndexFile.setLength(INDEX_HEADER_SIZE + maxEntries * 12 * 2);
		mIndexFile.seek(0);
		final byte[] buf = mIndexHeader;
		writeInt(buf, IH_MAGIC, MAGIC_INDEX_FILE);
		writeInt(buf, IH_MAX_ENTRIES, maxEntries);
		writeInt(buf, IH_MAX_BYTES, maxBytes);
		writeInt(buf, IH_ACTIVE_REGION, 0);
		writeInt(buf, IH_ACTIVE_ENTRIES, 0);
		writeInt(buf, IH_ACTIVE_BYTES, DATA_HEADER_SIZE);
		writeInt(buf, IH_VERSION, mVersion);
		writeInt(buf, IH_CHECKSUM, checkSum(buf, 0, IH_CHECKSUM));
		mIndexFile.write(buf);
		// This is only needed if setLength does not zero the extended part.
		// writeZero(mIndexFile, maxEntries * 12 * 2);

		mDataFile0.setLength(0);
		mDataFile1.setLength(0);
		mDataFile0.seek(0);
		mDataFile1.seek(0);
		writeInt(buf, 0, MAGIC_DATA_FILE);
		mDataFile0.write(buf, 0, 4);
		mDataFile1.write(buf, 0, 4);
	}

	private void setActiveVariables() throws IOException {
		mActiveDataFile = mActiveRegion == 0 ? mDataFile0 : mDataFile1;
		mActiveDataFile.setLength(mActiveBytes);
		mActiveDataFile.seek(mActiveBytes);

		if (mActiveRegion == 0) {
		} else {
		}
	}

	private void syncAll() {
		syncIndex();
		try {
			mDataFile0.getFD().sync();
		} catch (final Throwable t) {
			Log.w(TAG, "sync data file 0 failed", t);
		}
		try {
			mDataFile1.getFD().sync();
		} catch (final Throwable t) {
			Log.w(TAG, "sync data file 1 failed", t);
		}
	}

	private void syncIndex() {
		try {
			mIndexBuffer.force();
		} catch (final Throwable t) {
			Log.w(TAG, "sync index failed", t);
		}
	}

	// Delete the files associated with the given path previously created
	// by the BlobCache constructor.
	public static void deleteFiles(final String path) {
		deleteFileSilently(path + ".idx");
		deleteFileSilently(path + ".0");
		deleteFileSilently(path + ".1");
	}

	private static void closeSilently(final Closeable c) {
		if (c == null) return;
		try {
			c.close();
		} catch (final Throwable t) {
			// do nothing
		}
	}

	private static void deleteFileSilently(final String path) {
		try {
			new File(path).delete();
		} catch (final Throwable t) {
			// ignore;
		}
	}

	private static int readInt(final byte[] buf, final int offset) {
		return buf[offset] & 0xff | (buf[offset + 1] & 0xff) << 8 | (buf[offset + 2] & 0xff) << 16
				| (buf[offset + 3] & 0xff) << 24;
	}

	private static void writeInt(final byte[] buf, final int offset, int value) {
		for (int i = 0; i < 4; i++) {
			buf[offset + i] = (byte) (value & 0xff);
			value >>= 8;
		}
	}

	public static class LookupRequest {
		public long key; // input: the key to find
		public byte[] buffer; // input/output: the buffer to store the blob
		public int length; // output: the length of the blob
	}
}
