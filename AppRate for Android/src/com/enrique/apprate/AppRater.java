package com.enrique.apprate;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AppRater {

	private static final String SHARED_PREFS_NAME = "apprate";
	private static final String PREF_DATE_FIRST_LAUNCH = "date_firstlaunch";
	private static final String PREF_LAUNCH_COUNT = "launch_count";
	private static final String PREF_DONT_SHOW_AGAIN = "dontshowagain";

	public static void init(Context context, long launchesUntilPrompt, int daysUntilPrompt) {

		SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, 0);
		if (prefs.getBoolean(PREF_DONT_SHOW_AGAIN, false)) {
			return;
		}

		SharedPreferences.Editor editor = prefs.edit();

		// Increment launch counter
		long launch_count = prefs.getLong(PREF_LAUNCH_COUNT, 0) + 1;
		editor.putLong(PREF_LAUNCH_COUNT, launch_count);

		// Get date of first launch
		Long date_firstLaunch = prefs.getLong(PREF_DATE_FIRST_LAUNCH, 0);
		if (date_firstLaunch == 0) {
			date_firstLaunch = System.currentTimeMillis();
			editor.putLong(PREF_DATE_FIRST_LAUNCH, date_firstLaunch);
		}

		// Wait at least n days before opening
		if (launch_count >= launchesUntilPrompt) {
			if (System.currentTimeMillis() >= date_firstLaunch + (daysUntilPrompt * DateUtils.DAY_IN_MILLIS)) {
				showRateDialog(context, editor);
			}
		}

		editor.commit();
	}

	private static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {

		final Dialog dialog = new Dialog(mContext);
		dialog.setTitle("Rate " + getApplicationName(mContext));

		LinearLayout linearLayout = new LinearLayout(mContext);
		linearLayout.setOrientation(LinearLayout.VERTICAL);

		TextView textView = new TextView(mContext);
		textView.setText("If you enjoy using " + getApplicationName(mContext)
				+ ", please take a moment to rate it. Thanks for your support!");
		textView.setWidth(240);
		textView.setPadding(4, 0, 4, 10);
		linearLayout.addView(textView);

		Button rateButton = new Button(mContext);
		rateButton.setText("Rate it!");
		rateButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("market://details?id=" + mContext.getPackageName())));
				dialog.dismiss();
			}
		});
		linearLayout.addView(rateButton);

		Button remindButton = new Button(mContext);
		remindButton.setText("Remind me later");
		remindButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		linearLayout.addView(remindButton);

		Button neverButton = new Button(mContext);
		neverButton.setText("No, thanks");
		neverButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (editor != null) {
					editor.putBoolean(PREF_DONT_SHOW_AGAIN, true);
					editor.commit();
				}
				dialog.dismiss();
			}
		});
		linearLayout.addView(neverButton);

		dialog.setContentView(linearLayout);
		dialog.show();
	}

	/**
	 * @param context A context of the current application.
	 * @return The application name of the current application.
	 */
	private static final String getApplicationName(Context context) {
		final PackageManager packageManager = context.getPackageManager();
		ApplicationInfo applicationInfo;
		try {
			applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
		} catch (final NameNotFoundException e) {
			applicationInfo = null;
		}
		return (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "(unknown)");
	}
}