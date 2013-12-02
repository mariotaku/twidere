package org.mariotaku.twidere.view.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.view.iface.IColorLabelView;

public class DraftViewHolder extends ViewHolder {

	public final IColorLabelView content;
	public final TextView text;
	public final TextView time;
	public ImageView image_preview;
	public View image_preview_container;

	public DraftViewHolder(final View view) {
		super(view);
		content = (IColorLabelView) findViewById(R.id.content);
		text = (TextView) findViewById(R.id.text);
		time = (TextView) findViewById(R.id.time);
		image_preview = (ImageView) findViewById(R.id.image_preview);
		image_preview_container = findViewById(R.id.image_preview_container);
	}

	public void setTextSize(final float textSize) {
		text.setTextSize(textSize);
		time.setTextSize(textSize * 0.75f);
	}

}
