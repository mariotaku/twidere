package org.mariotaku.twidere.activity;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.mariotaku.twidere.R;
import twitter4j.Twitter;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpResponse;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class RestTestActivity extends Activity implements TextWatcher, OnClickListener {

	private TextView mResultView;
	private EditText mEditUrl;
	private View mGoButton;

	private Twitter mTwitter;
	private RequestTask mTask;

	@Override
	public void afterTextChanged(final Editable s) {

	}

	@Override
	public void beforeTextChanged(final CharSequence s, final int length, final int start, final int end) {

	}

	@Override
	public void onClick(final View v) {
		final String url = mEditUrl.getText().toString();
		if (mTask != null) {
			mTask.cancel(true);
		}
		mTask = new RequestTask(url, mTwitter, mResultView);
		mTask.execute();
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
//		mEditUrl = (EditText) findViewById(R.id.url);
//		mResultView = (TextView) findViewById(R.id.result);
//		mGoButton = findViewById(R.id.go);
//		mTwitter = Utils.getTwitterInstance(this);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rest_test);
//		mEditUrl.addTextChangedListener(this);
//		mGoButton.setOnClickListener(this);
	}

	@Override
	public void onTextChanged(final CharSequence s, final int length, final int start, final int end) {
		final String string = s.toString();
		final Configuration conf = mTwitter.getConfiguration();
		mGoButton.setEnabled(string.startsWith(conf.getRestBaseURL()));
	}

	static final class RequestTask extends AsyncTask<Void, Void, Object> {

		private final String url;
		private final Twitter moefou;
		private final TextView view;

		RequestTask(final String url, final Twitter moefou, final TextView view) {
			this.url = url;
			this.moefou = moefou;
			this.view = view;
		}

		@Override
		protected Object doInBackground(final Void... args) {
			try {
				final HttpResponse resp = null;
				final StringBuilder builder = new StringBuilder("Response: " + resp.getStatusCode() + "\n");
				builder.append(resp.asJSONObject().toString(1));
				return builder;
			} catch (final Throwable t) {
				return t;
			}
		}

		@Override
		protected void onPostExecute(final Object result) {
			view.append("------------------------------------\n");
			if (result instanceof Throwable) {
				final StringWriter sw = new StringWriter();
				final PrintWriter pw = new PrintWriter(sw);
				((Throwable) result).printStackTrace(pw);
				view.append("Stack trace:\n");
				view.append(sw.toString());
			} else {
				view.append(String.valueOf(result));
			}
		}

		@Override
		protected void onPreExecute() {
			view.setText(null);
			view.setText("Making request to " + url + "\n");
		}

	}
}
