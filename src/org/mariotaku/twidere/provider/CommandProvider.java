package org.mariotaku.twidere.provider;

import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.PermissionManager;
import org.mariotaku.twidere.util.TwitterWrapper;

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
	private TwitterWrapper mTwitterWrapper;

	public boolean onCreate() {
		mContext = getContext();
		final TwidereApplication app = TwidereApplication.getInstance(mContext);
		mPermissionManager = new PermissionManager(mContext);
		mPackageManager = mContext.getPackageManager();
		mTwitterWrapper = app.getTwitterWrapper();
		return true;
	}

	public Cursor query(Uri uri, String[] projection, String where, String[] whereArgs, String sortOrder) {
		// TODO: Implement this method
		return null;
	}

	public String getType(Uri uri) {
		// TODO: Implement this method
		return null;
	}

	public Uri insert(Uri uri, ContentValues values) {
		// TODO: Implement this method
		return null;
	}

	public int delete(Uri uri, String where, String[] whereArgs) {
		// TODO: Implement this method
		return 0;
	}

	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		// TODO: Implement this method
		return 0;
	}
	
}
