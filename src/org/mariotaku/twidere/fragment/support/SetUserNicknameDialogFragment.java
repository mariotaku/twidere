package org.mariotaku.twidere.fragment.support;

import static org.mariotaku.twidere.util.UserColorNicknameUtils.clearUserNickname;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.setUserNickname;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.FrameLayout;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.ThemeUtils;

public class SetUserNicknameDialogFragment extends BaseSupportDialogFragment implements OnClickListener {

	private static final String FRAGMENT_TAG_SET_USER_NICKNAME = "set_user_nickname";
	private EditText mEditText;

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		final Bundle args = getArguments();
		final String text = ParseUtils.parseString(mEditText.getText());
		final long userId = args != null ? args.getLong(EXTRA_USER_ID, -1) : -1;
		if (userId == -1) return;
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE: {
				if (TextUtils.isEmpty(text)) {
					clearUserNickname(getActivity(), userId);
				} else {
					setUserNickname(getActivity(), userId, text);
				}
				break;
			}
			case DialogInterface.BUTTON_NEUTRAL: {
				clearUserNickname(getActivity(), userId);
				break;
			}
		}

	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final Bundle args = getArguments();
		final String nick = args.getString(EXTRA_NAME);
		final Context wrapped = ThemeUtils.getDialogThemedContext(getActivity());
		final AlertDialog.Builder builder = new AlertDialog.Builder(wrapped);
		builder.setTitle(R.string.set_nickname);
		builder.setPositiveButton(android.R.string.ok, this);
		if (!TextUtils.isEmpty(nick)) {
			builder.setNeutralButton(R.string.clear, this);
		}
		builder.setNegativeButton(android.R.string.cancel, null);
		final FrameLayout view = new FrameLayout(wrapped);
		mEditText = new EditText(wrapped);
		final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.WRAP_CONTENT);
		lp.leftMargin = lp.topMargin = lp.bottomMargin = lp.rightMargin = getResources().getDimensionPixelSize(
				R.dimen.element_spacing_default);
		view.addView(mEditText, lp);
		builder.setView(view);
		mEditText.setText(nick);
		return builder.create();
	}

	public static SetUserNicknameDialogFragment show(final FragmentManager fm, final long user_id, final String nickname) {
		final SetUserNicknameDialogFragment f = new SetUserNicknameDialogFragment();
		final Bundle args = new Bundle();
		args.putLong(EXTRA_USER_ID, user_id);
		args.putString(EXTRA_NAME, nickname);
		f.setArguments(args);
		f.show(fm, FRAGMENT_TAG_SET_USER_NICKNAME);
		return f;
	}

}
