package org.mariotaku.twidere.receiver;

import org.mariotaku.twidere.activity.TestActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TestBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, final Intent intent) {
		final Intent test_intent = new Intent(context, TestActivity.class);
		test_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(test_intent);
	}

}
