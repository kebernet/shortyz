package com.totsp.crossword;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

import com.totsp.crossword.io.IO;
import com.totsp.crossword.puz.Playboard;
import com.totsp.crossword.puz.Playboard.Position;
import com.totsp.crossword.puz.Playboard.Word;
import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.shortyz.R;
import com.totsp.crossword.shortyz.ShortyzApplication;
import com.totsp.crossword.view.PlayboardRenderer;
import com.totsp.crossword.view.ScrollingImageView;
import com.totsp.crossword.view.ScrollingImageView.ClickListener;
import com.totsp.crossword.view.ScrollingImageView.Point;

import java.io.File;
import java.io.IOException;

public class ClueListActivity extends ShortyzActivity {
	private Configuration configuration;
	private File baseFile;
	private ImaginaryTimer timer;
	private KeyboardView keyboardView = null;
	private ListView across;
	private ListView down;
	private Puzzle puz;
	private ScrollingImageView imageView;
	private TabHost tabHost;
	private boolean useNativeKeyboard = false;
	private PlayboardRenderer renderer;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		this.configuration = newConfig;
		try {
			if (this.prefs.getBoolean("forceKeyboard", false)
					|| (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES)
					|| (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_UNDEFINED)) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

				if (imm != null)
					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
							InputMethodManager.HIDE_NOT_ALWAYS);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item == null || item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		utils.holographic(this);
		utils.finishOnHomeButton(this);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		this.renderer = new PlayboardRenderer(ShortyzApplication.getInstance().getBoard(), metrics.densityDpi, metrics.widthPixels,
				!prefs.getBoolean("supressHints", false), prefs.getBoolean("showErrorsIndicator", false),
				ContextCompat.getColor(this, R.color.boxColor), ContextCompat.getColor(this, R.color.blankColor),
				ContextCompat.getColor(this, R.color.errorColor));

		try {
			this.configuration = getBaseContext().getResources()
					.getConfiguration();
		} catch (Exception e) {
			Toast.makeText(this, "Unable to read device configuration.",
					Toast.LENGTH_LONG).show();
			finish();
		}
        if(ShortyzApplication.getInstance().getBoard() == null || ShortyzApplication.getInstance().getBoard().getPuzzle() == null){
            finish();
        }
		this.timer = new ImaginaryTimer(
				ShortyzApplication.getInstance().getBoard().getPuzzle().getTime());

		Uri u = this.getIntent().getData();

		if (u != null) {
			if (u.getScheme().equals("file")) {
				baseFile = new File(u.getPath());
			}
		}

		puz = ShortyzApplication.getInstance().getBoard().getPuzzle();
		timer.start();
		setContentView(R.layout.clue_list);

		int keyboardType = "CONDENSED_ARROWS".equals(prefs.getString(
				"keyboardType", "")) ? R.xml.keyboard_dpad : R.xml.keyboard;
		Keyboard keyboard = new Keyboard(this, keyboardType);
		keyboardView = this.findViewById(R.id.clueKeyboard);
		keyboardView.setKeyboard(keyboard);
		this.useNativeKeyboard = "NATIVE".equals(prefs.getString(
				"keyboardType", ""));

		if (this.useNativeKeyboard) {
			keyboardView.setVisibility(View.GONE);
		}

		keyboardView
				.setOnKeyboardActionListener(new OnKeyboardActionListener() {
					private long lastSwipe = 0;

					public void onKey(int primaryCode, int[] keyCodes) {
						long eventTime = System.currentTimeMillis();

						if ((eventTime - lastSwipe) < 500) {
							return;
						}

						KeyEvent event = new KeyEvent(eventTime, eventTime,
								KeyEvent.ACTION_DOWN, primaryCode, 0, 0, 0, 0,
								KeyEvent.FLAG_SOFT_KEYBOARD
										| KeyEvent.FLAG_KEEP_TOUCH_MODE);
						ClueListActivity.this.onKeyDown(primaryCode, event);
					}

					public void onPress(int primaryCode) {}

					public void onRelease(int primaryCode){}

					public void onText(CharSequence text) {}

					public void swipeDown() {}

					public void swipeLeft() {
						long eventTime = System.currentTimeMillis();
						lastSwipe = eventTime;

						KeyEvent event = new KeyEvent(eventTime, eventTime,
								KeyEvent.ACTION_DOWN,
								KeyEvent.KEYCODE_DPAD_LEFT, 0, 0, 0, 0,
								KeyEvent.FLAG_SOFT_KEYBOARD
										| KeyEvent.FLAG_KEEP_TOUCH_MODE);
						ClueListActivity.this.onKeyDown(
								KeyEvent.KEYCODE_DPAD_LEFT, event);
					}

					public void swipeRight() {
						long eventTime = System.currentTimeMillis();
						lastSwipe = eventTime;

						KeyEvent event = new KeyEvent(eventTime, eventTime,
								KeyEvent.ACTION_DOWN,
								KeyEvent.KEYCODE_DPAD_RIGHT, 0, 0, 0, 0,
								KeyEvent.FLAG_SOFT_KEYBOARD
										| KeyEvent.FLAG_KEEP_TOUCH_MODE);
						ClueListActivity.this.onKeyDown(
								KeyEvent.KEYCODE_DPAD_RIGHT, event);
					}

					public void swipeUp() {
						// TODO Auto-generated method stub
					}
				});

		this.imageView = (ScrollingImageView) this.findViewById(R.id.miniboard);

		this.imageView.setContextMenuListener(new ClickListener() {
			public void onContextMenu(Point e) {
				// TODO Auto-generated method stub
			}

			public void onTap(Point e) {
				Word current = ShortyzApplication.getInstance().getBoard().getCurrentWord();
				int newAcross = current.start.across;
				int newDown = current.start.down;
				int box = ShortyzApplication.getInstance().getRenderer().findBoxNoScale(e);

				if (box < current.length) {
					if (tabHost.getCurrentTab() == 0) {
						newAcross += box;
					} else {
						newDown += box;
					}
				}

				Position newPos = new Position(newAcross, newDown);

				if (!newPos.equals(ShortyzApplication.getInstance().getBoard()
						.getHighlightLetter())) {
					ShortyzApplication.getInstance().getBoard().setHighlightLetter(newPos);
					ClueListActivity.this.render();
				}
			}
		});

		this.tabHost = this.findViewById(R.id.tabhost);
		this.tabHost.setup();

		TabSpec ts = tabHost.newTabSpec("TAB1");

		ts.setIndicator("Across",
				ContextCompat.getDrawable(this, R.drawable.across));

		ts.setContent(R.id.acrossList);

		this.tabHost.addTab(ts);

		ts = this.tabHost.newTabSpec("TAB2");

		ts.setIndicator("Down", ContextCompat.getDrawable(this, R.drawable.down));

		ts.setContent(R.id.downList);
		this.tabHost.addTab(ts);

		this.tabHost.setCurrentTab(ShortyzApplication.getInstance().getBoard().isAcross() ? 0 : 1);

		this.across = (ListView) this.findViewById(R.id.acrossList);
		this.down = (ListView) this.findViewById(R.id.downList);

		across.setAdapter(new ArrayAdapter<>(this,
				android.R.layout.simple_list_item_1, ShortyzApplication.getInstance().getBoard()
						.getAcrossClues()));
		across.setFocusableInTouchMode(true);
		down.setAdapter(new ArrayAdapter<>(this,
				android.R.layout.simple_list_item_1, ShortyzApplication.getInstance().getBoard()
						.getDownClues()));
		across.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				arg0.setSelected(true);
				ShortyzApplication.getInstance().getBoard().jumpTo(arg2, true);
				imageView.scrollTo(0, 0);
				render();

				if (prefs.getBoolean("snapClue", false)) {
					across.setSelectionFromTop(arg2, 5);
					across.setSelection(arg2);
				}
			}
		});
		across.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				Playboard board = ShortyzApplication.getInstance().getBoard();
				if (!board.isAcross()
						|| (board.getCurrentClueIndex() != arg2)) {
					board.jumpTo(arg2, true);
					imageView.scrollTo(0, 0);
					render();

					if (prefs.getBoolean("snapClue", false)) {
						across.setSelectionFromTop(arg2, 5);
						across.setSelection(arg2);
					}
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		down.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1,
					final int arg2, long arg3) {
				ShortyzApplication.getInstance().getBoard().jumpTo(arg2, false);
				imageView.scrollTo(0, 0);
				render();

				if (prefs.getBoolean("snapClue", false)) {
					down.setSelectionFromTop(arg2, 5);
					down.setSelection(arg2);
				}
			}
		});

		down.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (ShortyzApplication.getInstance().getBoard().isAcross()
						|| (ShortyzApplication.getInstance().getBoard().getCurrentClueIndex() != arg2)) {
					ShortyzApplication.getInstance().getBoard().jumpTo(arg2, false);
					imageView.scrollTo(0, 0);
					render();

					if (prefs.getBoolean("snapClue", false)) {
						down.setSelectionFromTop(arg2, 5);
						down.setSelection(arg2);
					}
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		this.render();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Playboard board = ShortyzApplication.getInstance().getBoard();
		Word w = board.getCurrentWord();
		Position last = new Position(w.start.across
				+ (w.across ? (w.length - 1) : 0), w.start.down
				+ ((!w.across) ? (w.length - 1) : 0));

		switch (keyCode) {
		case KeyEvent.KEYCODE_MENU:
			return false;

		case KeyEvent.KEYCODE_BACK:
			System.out.println("BACK!!!");
			this.setResult(0);

			return true;

		case KeyEvent.KEYCODE_DPAD_LEFT:

			if (!board.getHighlightLetter().equals(
					board.getCurrentWord().start)) {
				board.previousLetter();

				this.render();
			}

			return true;

		case KeyEvent.KEYCODE_DPAD_RIGHT:

			if (!board.getHighlightLetter().equals(last)) {
				board.nextLetter();
				this.render();
			}

			return true;

		case KeyEvent.KEYCODE_DEL:
			w = board.getCurrentWord();
			board.deleteLetter();

			Position p = board.getHighlightLetter();

			if (!w.checkInWord(p.across, p.down)) {
				board.setHighlightLetter(w.start);
			}

			this.render();

			return true;

		case KeyEvent.KEYCODE_SPACE:

			if (!prefs.getBoolean("spaceChangesDirection", true)) {
				board.playLetter(' ');

				Position curr = board.getHighlightLetter();

				if (!board.getCurrentWord().equals(w)
						|| (board.getBoxes()[curr.across][curr.down] == null)) {
					board.setHighlightLetter(last);
				}

				this.render();

				return true;
			}
		}

		char c = Character
				.toUpperCase(((this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) || this.useNativeKeyboard) ? event
						.getDisplayLabel() : ((char) keyCode));

		if (PlayActivity.ALPHA.indexOf(c) != -1) {
			board.playLetter(c);

			Position p = board.getHighlightLetter();

			if (!board.getCurrentWord().equals(w)
					|| (board.getBoxes()[p.across][p.down] == null)) {
				board.setHighlightLetter(last);
			}

			this.render();
			
			if ((puz.getPercentComplete() == 100) && (timer != null)) {
	            timer.stop();
	            puz.setTime(timer.getElapsed());
	            this.timer = null;
	            Intent i = new Intent(ClueListActivity.this, PuzzleFinishedActivity.class);
	            this.startActivity(i);
	            
	        }

			return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			this.finish();

			return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	protected void onPause() {
		super.onPause();

		try {
			if ((puz != null) && (baseFile != null)) {
				if ((timer != null) && (puz.getPercentComplete() != 100)) {
					this.timer.stop();
					puz.setTime(timer.getElapsed());
					this.timer = null;
				}

				IO.save(puz, baseFile);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		if (this.prefs.getBoolean("forceKeyboard", false)
				|| (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES)
				|| (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_UNDEFINED)) {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(this.imageView.getWindowToken(), 0);
		}
	}

	private void render() {
		if (this.prefs.getBoolean("forceKeyboard", false)
				|| (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES)
				|| (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_UNDEFINED)) {
			if (this.useNativeKeyboard) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

				imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
						InputMethodManager.HIDE_IMPLICIT_ONLY);
			} else {
				this.keyboardView.setVisibility(View.VISIBLE);
			}
		} else {
			this.keyboardView.setVisibility(View.GONE);
		}

		this.imageView.setBitmap(renderer.drawWord());
	}
}
