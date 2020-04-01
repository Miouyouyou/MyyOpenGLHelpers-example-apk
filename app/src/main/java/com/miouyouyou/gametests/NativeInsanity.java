/*
	Copyright (c) 2016 Miouyouyou <Myy>

	Permission is hereby granted, free of charge, to any person obtaining
	a copy of this software and associated documentation files 
	(the "Software"), to deal in the Software without restriction, 
	including without limitation the rights to use, copy, modify, merge, 
	publish, distribute, sublicense, and/or sell copies of the Software, 
	and to permit persons to whom the Software is furnished to do so, 
	subject to the following conditions:

	The above copyright notice and this permission notice shall be 
	included in all copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
	EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
	MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
	IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY 
	CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
	TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
	SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.miouyouyou.gametests;

import android.app.NativeActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.inputmethod.InputMethodManager;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;



import java.lang.ref.WeakReference;


public class NativeInsanity extends NativeActivity {

	static {
		System.loadLibrary("main");
		try {
			System.loadLibrary("AGA");
		}
		catch (UnsatisfiedLinkError e) {
			Log.print("Calling the police ! AGA is not loaded !");
		}
	}

	public static native void myyTextInputStopped(byte[] data, long pointer);
	public static class Log {
		public static final String LOG_TAG = "Java_native-insanity";
		public static void print(final String message, Object... args) {
			android.util.Log.e(LOG_TAG, String.format(message, args));
		}
	}

	class OnTextChange implements TextWatcher {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			Log.print("Text changed : %s\n", s.toString());
		}
	}

	private long myy_states_pointer = 0;
	/* TODO Try
	 * - One time TextEdit use. Use SHOW_IMPLICIT and every time
	 *   the input init function is called, just remove all views from Layout
	 *   and add a new one again.
	 * - An activity dedicated to text input, that will return the text
	 *   provided.
	 */
	class OnTextInserted implements TextView.OnEditorActionListener {
		private long user_state_pointer = 0;
		public void set_state_pointer(final long pointer) {
			user_state_pointer = pointer;
		}
		public OnTextInserted() {}
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			Log.print("%d - %d",
				actionId,
				(event != null) ? event.getKeyCode() : -1);
			if (actionId != 0)
				((LinearLayout) v.getParent()).removeAllViews();
			Log.print("TextView : %s\n", v.getText().toString());
			try {
				NativeInsanity.myyTextInputStopped(
						v.getText().toString().getBytes("UTF-8"),
						user_state_pointer);
			}
			catch (Exception e) {}

			NativeInsanity activity =
				(NativeInsanity) v.getContext();
			final InputMethodManager imm =
				(InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(0,0);
			return false;
		}
	}

	private LinearLayout layout;
	private OnTextInserted cb_text_edit_on_insert;
	private OnTextChange cb_text_edit_on_change;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		cb_text_edit_on_insert = new OnTextInserted();
		cb_text_edit_on_change = new OnTextChange();
		layout = new LinearLayout(this);
		layout.setLayoutParams(new LinearLayout.LayoutParams(1, 1));

		setContentView(layout);

	}
	/**
	 * Open a website URL provided in a byte String.
	 *
	 * The main purpose of this method is to open a website, using the
	 * default browser of the Android terminal.
	 *
	 * If you're using Myy's OpenGL helpers, this method is actually
	 * called by the myy_open_website(char const * const) method.
	 *
	 * E.g. : Calling myy_open_website("https://github.com/Miouyouyou") 
	 * will generate a byte array containing https://github.com/Miouyouyou
	 * and pass it to openWebsite(), which will open a browser at this URL
	 *
	 * @param url_as_bytes A byte Array representing the URL
	 *
	 */
	public void openWebsite(final byte[] url_as_bytes) {
		final String url_as_string = new String(url_as_bytes);
		try {			
			final Uri url = Uri.parse(url_as_string);
			startActivity(new Intent(Intent.ACTION_VIEW, url));
			Log.print("Website opened !\n");
		}
		catch(Exception e) {
			Log.print(
				"Trying to open %s triggered : \n%s\n",
				url_as_string, e.getMessage());
		}
	}

	private final int MyyInputTypeText    = 0;
	private final int MyyInputTypeNumeric = 1;

	public void startInput(final long user_pointer, final int input_type)
	{
		Log.print("[Java] pointer address : %08x\n", user_pointer);
		final NativeInsanity insanity = this;
		this.myy_states_pointer = user_pointer;
		this.cb_text_edit_on_insert.set_state_pointer(user_pointer);

		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				final InputMethodManager imm =
					(InputMethodManager)
					getSystemService(Context.INPUT_METHOD_SERVICE);
				EditText invisible_text_edit = new EditText(insanity);

				invisible_text_edit.setOnEditorActionListener(
					cb_text_edit_on_insert);
				invisible_text_edit.addTextChangedListener(
					cb_text_edit_on_change);
				invisible_text_edit.setFocusable(true);
				invisible_text_edit.setHeight(1);
				invisible_text_edit.setWidth(1);
				invisible_text_edit.setVisibility(0);

				switch(input_type) {
				case MyyInputTypeText:
					invisible_text_edit.setInputType(
						InputType.TYPE_CLASS_NUMBER
						| InputType.TYPE_NUMBER_VARIATION_NORMAL);
					break;
				case MyyInputTypeNumeric:
					invisible_text_edit.setInputType(
						InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_VARIATION_NORMAL
						| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
					break;
				default: break;
				}

				invisible_text_edit.setInputType(
					InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_NORMAL
					| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
				layout.removeAllViews();
				layout.addView(invisible_text_edit);

				invisible_text_edit.requestFocus();

				final StringBuilder sb = new StringBuilder();
				sb.append(invisible_text_edit);
				sb.append(",focus=" + invisible_text_edit.hasFocus());
				sb.append(",windowFocus=" + invisible_text_edit.hasWindowFocus());
				sb.append(",activityFocus=" + hasWindowFocus());
				sb.append(",window=" + invisible_text_edit.getWindowToken());
				sb.append(",temporaryDetach=" + invisible_text_edit.isTemporarilyDetached());
				Log.print(" CURRENT VIEW INFOS --------------------------------------- %s\n", sb.toString());
				boolean ret = imm.showSoftInput(invisible_text_edit, InputMethodManager.SHOW_FORCED);
				Log.print("showSoftInput : %b\n", ret);
			}
		});
		//imm.showSoftInput(invisible_text_edit, InputMethodManager.SHOW_IMPLICIT);
	}
	/* This avoid destroying and recreating the activity on screenChange,
	 * which helps avoiding EGL_BAD_DISPLAY issues when locking the
	 * screen orientation */
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
	}

}
