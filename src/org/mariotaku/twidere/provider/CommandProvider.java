package org.mariotaku.twidere.provider;

import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.PermissionManager;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;

public class CommandProvider extends ContentProvider {

	private Context mContext;
	private PermissionManager mPermissionManager;
	private PackageManager mPackageManager;
	private AsyncTwitterWrapper mTwitterWrapper;

	@Override
	public int delete(final Uri uri, final String where, final String[] whereArgs) {
		// TODO: Implement this method
		return 0;
	}

	@Override
	public String getType(final Uri uri) {
		// TODO: Implement this method
		return null;
	}

	@Override
	public Uri insert(final Uri uri, final ContentValues values) {
		// TODO: Implement this method
		return null;
	}

	@Override
	public boolean onCreate() {
		mContext = getContext();
		final TwidereApplication app = TwidereApplication.getInstance(mContext);
		mPermissionManager = new PermissionManager(mContext);
		mPackageManager = mContext.getPackageManager();
		mTwitterWrapper = app.getTwitterWrapper();
		return true;
	}

	@Override
	public Cursor query(final Uri uri, final String[] projection, final String where, final String[] whereArgs,
			final String sortOrder) {
		// TODO: Implement this method
		return null;
	}

	@Override
	public int update(final Uri uri, final ContentValues values, final String where, final String[] whereArgs) {
		// TODO: Implement this method
		return 0;
	}

}
