package org.mariotaku.twidere.backup;

import org.mariotaku.twidere.Constants;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

public class TwidereBackupAgentHelper extends BackupAgentHelper implements Constants {

	// A key to uniquely identify the set of backup data
	static final String PREFS_BACKUP_KEY = "preference_backup";

	@Override
	public void onCreate() {
		addHelper(PREFS_BACKUP_KEY, new SharedPreferencesBackupHelper(this, SHARED_PREFERENCES_NAME));
		addHelper(PREFS_BACKUP_KEY, new SharedPreferencesBackupHelper(this, HOST_MAPPING_PREFERENCES_NAME));
		addHelper(PREFS_BACKUP_KEY, new SharedPreferencesBackupHelper(this, USER_COLOR_PREFERENCES_NAME));
		addHelper(PREFS_BACKUP_KEY, new SharedPreferencesBackupHelper(this, PERMISSION_PREFERENCES_NAME));
	}
}
