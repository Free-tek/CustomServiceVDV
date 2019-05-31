package com.example.customservicechasisnumbercheck.filebrowser;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IconifiedTextView extends LinearLayout {

    private TextView mText = null;
    private ImageView mIcon = null;

    public IconifiedTextView(Context context, IconifiedText aIconifiedText) {
        super(context);
        this.setOrientation(HORIZONTAL);
        mIcon = new ImageView(context);
        mIcon.setImageDrawable(aIconifiedText.getIcon());

        mIcon.setPadding(6, 14, 6, 14);
        addView(mIcon, new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        mText = new TextView(context);
        mText.setText(aIconifiedText.getText());
        mText.setPadding(4, 6, 4, 6);
        mText.setTextSize(24);
        mText.setWidth(LayoutParams.WRAP_CONTENT);
        mText.setHeight(LayoutParams.WRAP_CONTENT);
        addView(mText, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
    }

    public void setText(String words) {
        mText.setText(words);
    }

    public void setIcon(Drawable bullet) {
        mIcon.setImageDrawable(bullet);
    }
}
