package edu.ucdavis.earlybird;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.Calendar;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings.Secure;

public class UploadTask extends AsyncTask<Void, Void, Void> {

	private static final String LAST_UPLOAD_DATE = "last_upload_time";
	private static final double MILLSECS_HALF_DAY = 1000 * 60 * 60 * 12;

	private static final String FTP_PROFILE_SERVER = "earlybird_profile.metaisle.com";
	private static final String FTP_USERNAME = "profile";
	private static final String FTP_PASSWORD = "profile";

	private final String device_id;
	private final Context context;

	public UploadTask(final Context context) {
		this.context = context;
		device_id = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	}

	@Override
	protected Void doInBackground(final Void... params) {
		final SharedPreferences prefs = context.getSharedPreferences("ucd_data_profiling", Context.MODE_PRIVATE);

		if (prefs.contains(LAST_UPLOAD_DATE)) {
			final long lastUpload = prefs.getLong(LAST_UPLOAD_DATE, System.currentTimeMillis());
			final double deltaDays = (System.currentTimeMillis() - lastUpload) / (MILLSECS_HALF_DAY * 2);
			if (deltaDays < 1) {
				ProfilingUtil.log("Uploaded less than 1 day ago.");
				return null;
			}
		}

		final File root = context.getFileStreamPath("");
		final File[] files = root.listFiles();

		try {
			uploadToFTP(files);
			prefs.edit().putLong(LAST_UPLOAD_DATE, System.currentTimeMillis()).commit();
		} catch (final Exception ex) {
		}
		return null;
	}

	private boolean uploadToFTP(final File... files) {

		final FTPClient ftp = new FTPClient();

		ftp.setDefaultTimeout(30000);

		try {
			ftp.connect(FTP_PROFILE_SERVER, 21);
			int reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ProfilingUtil.log("FTP connect fail");
				ftp.disconnect();
			} else {
				if (ftp.login(FTP_USERNAME, FTP_PASSWORD)) {
					ftp.enterLocalPassiveMode();
					ProfilingUtil.log("FTP connect OK");
					ftp.setFileType(FTP.BINARY_FILE_TYPE);
					final Calendar now = Calendar.getInstance();
					for (final File file : files) {
						if (!file.isFile() || file.length() <= 0) {
							continue;
						}
						final FileInputStream fis = new FileInputStream(file);
						final String filename = file.getName();
						final String profile_type = filename.substring(0, filename.indexOf('.'));
						final String file_type = filename.substring(filename.indexOf('.'));
						final boolean working_dir_exists = ftp.changeWorkingDirectory("/profile/" + device_id + "/"
								+ profile_type);
						if (!working_dir_exists) {
							ProfilingUtil.log("create user folder : " + "/profile/" + device_id + "/" + profile_type);
							ftp.makeDirectory("/profile/" + device_id);
							ftp.makeDirectory("/profile/" + device_id + "/" + profile_type);
						}
						final String upload_file_name = "/profile/" + device_id + "/" + profile_type + "/"
								+ now.getTimeInMillis() + file_type;

						ftp.setFileType(FTP.BINARY_FILE_TYPE);
						ftp.storeFile(upload_file_name, fis);
						ProfilingUtil.log("Upload File : " + upload_file_name);
						reply = ftp.getReplyCode();
						ProfilingUtil.log("reply :" + reply);
						if (reply == FTPReply.CLOSING_DATA_CONNECTION) {
							ProfilingUtil.log("file upload success");
							file.delete();
						}
					}
					ftp.logout();
				}
			}
		} catch (final FTPConnectionClosedException e) {
			ProfilingUtil.log("ftp connect error!!");
			e.printStackTrace();
			return false;
		} catch (final SocketException e) {
			e.printStackTrace();
			return false;
		} catch (final IOException e) {
			e.printStackTrace();
			return false;
		}
		return false;

	}

}
