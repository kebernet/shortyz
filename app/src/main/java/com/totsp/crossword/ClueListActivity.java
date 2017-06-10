package com.totsp.crossword;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
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
import com.totsp.crossword.puz.MovementStrategy;
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
	private static String MENU_JUMP_FORWARD  = "Jump Forward";
	private static String MENU_JUMP_BACKWARD = "Jump Backward";

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

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem mi;

		mi = menu.add(MENU_JUMP_BACKWARD).setIcon(android.R.drawable.arrow_up_float);
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			mi.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
		mi = menu.add(MENU_JUMP_FORWARD).setIcon(android.R.drawable.arrow_down_float);
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			mi.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item == null || item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getTitle().toString().equals(MENU_JUMP_FORWARD)) {
					jumpToNextUnsolved(true);
					render();
					return true;
				} else if (item.getTitle().toString().equals(MENU_JUMP_BACKWARD)) {
					jumpToNextUnsolved(false);
					render();
					return true;
				}
        else return super.onOptionsItemSelected(item);
    }

    @Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		utils.holographic(this);
		utils.finishOnHomeButton(this);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		this.renderer = new PlayboardRenderer(ShortyzApplication.BOARD, metrics.densityDpi, metrics.widthPixels,
				!prefs.getBoolean("supressHints", false),
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
        if(ShortyzApplication.BOARD == null || ShortyzApplication.BOARD.getPuzzle() == null){
            finish();
        }
		this.timer = new ImaginaryTimer(
				ShortyzApplication.BOARD.getPuzzle().getTime());

		Uri u = this.getIntent().getData();

		if (u != null) {
			if (u.getScheme().equals("file")) {
				baseFile = new File(u.getPath());
			}
		}

		puz = ShortyzApplication.BOARD.getPuzzle();
		timer.start();
		setContentView(R.layout.clue_list);

		int keyboardType = "CONDENSED_ARROWS".equals(prefs.getString(
				"keyboardType", "")) ? R.xml.keyboard_dpad : R.xml.keyboard;
		Keyboard keyboard = new Keyboard(this, keyboardType);
		keyboardView = (KeyboardView) this.findViewById(R.id.clueKeyboard);
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
				Word current = ShortyzApplication.BOARD.getCurrentWord();
				int newAcross = current.start.across;
				int newDown = current.start.down;
				int box = ShortyzApplication.RENDERER.findBoxNoScale(e);

				if (box < current.length) {
					if (tabHost.getCurrentTab() == 0) {
						newAcross += box;
					} else {
						newDown += box;
					}
				}

				Position newPos = new Position(newAcross, newDown);

				if (!newPos.equals(ShortyzApplication.BOARD
						.getHighlightLetter())) {
					ShortyzApplication.BOARD.setHighlightLetter(newPos);
					ClueListActivity.this.render();
				}
			}
		});

		this.tabHost = (TabHost) this.findViewById(R.id.tabhost);
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

		this.tabHost.setCurrentTab(ShortyzApplication.BOARD.isAcross() ? 0 : 1);

		this.across = (ListView) this.findViewById(R.id.acrossList);
		this.down = (ListView) this.findViewById(R.id.downList);

		across.setAdapter(new ArrayAdapter<>(this,
				android.R.layout.simple_list_item_1, ShortyzApplication.BOARD
						.getAcrossClues()));
		across.setFocusableInTouchMode(true);
		down.setAdapter(new ArrayAdapter<>(this,
				android.R.layout.simple_list_item_1, ShortyzApplication.BOARD
						.getDownClues()));
		across.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				arg0.setSelected(true);
				ShortyzApplication.BOARD.jumpTo(arg2, true);
				imageView.scrollTo(0, 0);
				skipFilled();
				render();

				if (prefs.getBoolean("snapClue", false)) {
					across.setSelection(arg2);
				}
			}
		});
		across.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (!ShortyzApplication.BOARD.isAcross()
						|| (ShortyzApplication.BOARD.getCurrentClueIndex() != arg2)) {
					ShortyzApplication.BOARD.jumpTo(arg2, true);
					imageView.scrollTo(0, 0);
					skipFilled();
					render();

					if (prefs.getBoolean("snapClue", false)) {
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
				ShortyzApplication.BOARD.jumpTo(arg2, false);
				imageView.scrollTo(0, 0);
				skipFilled();
				render();

				if (prefs.getBoolean("snapClue", false)) {
					down.setSelection(arg2);
				}
			}
		});

		down.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (ShortyzApplication.BOARD.isAcross()
						|| (ShortyzApplication.BOARD.getCurrentClueIndex() != arg2)) {
					ShortyzApplication.BOARD.jumpTo(arg2, false);
					imageView.scrollTo(0, 0);
					skipFilled();
					render();

					if (prefs.getBoolean("snapClue", false)) {
						down.setSelection(arg2);
					}
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

			// Initial position to the clue that is current on the play board
			(ShortyzApplication.BOARD.isAcross() ? across : down)
				.setSelection(ShortyzApplication.BOARD.getCurrentClueIndex());

      ShortyzApplication.BOARD.jumpTo(ShortyzApplication.BOARD.getCurrentClueIndex(),
        ShortyzApplication.BOARD.isAcross());

      skipFilled();
      this.render();
	}

	private void skipFilled()
	{
		// Position on the first blank in the current word, or on the first
		// character if there is no blank in the word.
		Position p = ShortyzApplication.BOARD.getNextBlankOrError();
		if (p != null) {
			ShortyzApplication.BOARD.setHighlightLetter(p, false);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Word w = ShortyzApplication.BOARD.getCurrentWord();
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

			MovementStrategy.STOP_ON_END.back(ShortyzApplication.BOARD);
			this.render();
			return true;

		case KeyEvent.KEYCODE_DPAD_RIGHT:

			MovementStrategy.STOP_ON_END.move(ShortyzApplication.BOARD,
				ShortyzApplication.BOARD.isSkipCompletedLetters());
			this.render();
			return true;

		case KeyEvent.KEYCODE_DEL:
			w = ShortyzApplication.BOARD.getCurrentWord();
			ShortyzApplication.BOARD.deleteLetter();

			Position p = ShortyzApplication.BOARD.getHighlightLetter();

			if (!w.checkInWord(p.across, p.down)) {
				ShortyzApplication.BOARD.setHighlightLetter(w.start);
			}

			this.render();

			return true;

		case KeyEvent.KEYCODE_SPACE:

			if (!prefs.getBoolean("spaceChangesDirection", true)) {
				ShortyzApplication.BOARD.playLetter(' ');

				Position curr = ShortyzApplication.BOARD.getHighlightLetter();

				if (!ShortyzApplication.BOARD.getCurrentWord().equals(w)
						|| (ShortyzApplication.BOARD.getBoxes()[curr.across][curr.down] == null)) {
					ShortyzApplication.BOARD.setHighlightLetter(last);
				}

				this.render();

				return true;
			}
		}

		char c = Character
				.toUpperCase(((this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) || this.useNativeKeyboard) ? event
						.getDisplayLabel() : ((char) keyCode));

		if (PlayActivity.ALPHA.indexOf(c) != -1) {
			ShortyzApplication.BOARD.playLetter(c);

			Position p = ShortyzApplication.BOARD.getHighlightLetter();

			if (!ShortyzApplication.BOARD.getCurrentWord().equals(w)
					|| (ShortyzApplication.BOARD.getBoxes()[p.across][p.down] == null)) {
				ShortyzApplication.BOARD.setHighlightLetter(last);
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

	public void jumpToNextUnsolved(boolean forward) {
		Playboard board = ShortyzApplication.BOARD;

		int clue = board.getCurrentClueIndex();
		int original_clue = clue;

		boolean acr = board.isAcross();
		boolean original_acr = acr;

		while (true)
		{
			if (forward) {
				if (clue == (acr ? this.across : this.down).getAdapter().getCount() - 1) {
					clue = 0;
					acr = !acr;
				}
				else {
					clue++;
				}
			} else {
				if (clue == 0) {
					acr = !acr;
					clue = (acr ? this.across : this.down).getAdapter().getCount() - 1;
				} else {
					clue--;
				}
			}
			board.jumpTo(clue, acr);
			Position p = board.getNextBlankOrError();
			if (p != null) {
				board.setHighlightLetter(p, false);
				imageView.scrollTo(0, 0);
				this.tabHost.setCurrentTab(acr ? 0 : 1);
				(acr ? across : down).setSelection(clue);
				break;
			}
			if (clue == original_clue && acr == original_acr) {
				imageView.scrollTo(0, 0);
				break;
			}
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
