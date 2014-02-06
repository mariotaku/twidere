package org.mariotaku.twidere.activity.support;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;

import org.mariotaku.twidere.fragment.support.CharactersGridFragment;

public class EmojiPickerActivity extends BaseSupportDialogActivity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		final FragmentManager fm = getSupportFragmentManager();
		final FragmentTransaction ft = fm.beginTransaction();
		ft.replace(android.R.id.content, new CharactersGridFragment());
		ft.commit();
	}

}
