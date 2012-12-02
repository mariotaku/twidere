package org.mariotaku.twidere.activity;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.Utils.getDefaultAccountScreenName;
import static org.mariotaku.twidere.util.Utils.parseInt;
import static org.mariotaku.twidere.util.Utils.parseString;

import org.mariotaku.twidere.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class DonateActivity extends BaseDialogWhenLargeActivity implements OnClickListener, TextWatcher {

	private EditText mEditName, mEditAmount;
	private Button mDonateButton;

	@Override
	public void afterTextChanged(final Editable s) {

	}

	@Override
	public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

	}

	@Override
	public void onClick(final View view) {
		switch (view.getId()) {
			case R.id.donate: {
				final String amount = parseString(mEditAmount.getText());
				if (isEmpty(amount)) return;
				final String name = parseString(mEditName.getText());
				final Uri.Builder builder = Uri.parse("https://www.paypal.com/cgi-bin/webscr").buildUpon();
				builder.appendQueryParameter("cmd", "_xclick");
				builder.appendQueryParameter("business", "mariotaku.lee@gmail.com");
				builder.appendQueryParameter("amount", amount);
				builder.appendQueryParameter("item_name", isEmpty(name) ? "Twidere donation" : "Twidere donation by "
						+ name);
				startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
				finish();
				break;
			}
		}

	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mEditName = (EditText) findViewById(R.id.name);
		mEditAmount = (EditText) findViewById(R.id.amount);
		mDonateButton = (Button) findViewById(R.id.donate);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.donate);
		mEditAmount.setText("5.00");
		mEditName.setText(getDefaultAccountScreenName(this));
		mEditAmount.addTextChangedListener(this);
		mDonateButton.setOnClickListener(this);
	}

	@Override
	public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
		mDonateButton.setEnabled(count > 0 && parseInt(parseString(s)) > 0);
	}
}
