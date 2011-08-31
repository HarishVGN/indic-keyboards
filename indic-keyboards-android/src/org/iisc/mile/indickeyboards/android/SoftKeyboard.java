/*
 * Copyright (C) 2008-2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.iisc.mile.indickeyboards.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.method.MetaKeyKeyListener;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

/**
 * Example of writing an input method for a soft keyboard.  This code is
 * focused on simplicity over completeness, so it should in no way be considered
 * to be a complete soft keyboard implementation.  Its purpose is to provide
 * a basic example for how you would get started writing an input method, to
 * be fleshed out as appropriate.
 */
public class SoftKeyboard extends InputMethodService 
implements KeyboardView.OnKeyboardActionListener {
	static final boolean DEBUG = false;

	/**
	 * This boolean indicates the optional example code for performing
	 * processing of hard keys in addition to regular text generation
	 * from on-screen interaction.  It would be used for input methods that
	 * perform language translations (such as converting text entered on 
	 * a QWERTY keyboard to Chinese), but may not be used for input methods
	 * that are primarily intended to be used for on-screen text entry.
	 */
	static final boolean PROCESS_HARD_KEYS = true;

	private static final int CHANGE_LANGUAGE_LAYOUT_OPTION_KEYCODE = 8; // Keyboard.EDGE_BOTTOM
	private static final int KAGAPA_LETTERS_TO_SYMBOLS_KEYCODE = -6; // Keyboard.KEYCODE_ALT
	private static final int KANNADA_INSCRIPT_LETTERS_TO_SYMBOLS_KEYCODE = 1; // Keyboard.EDGE_LEFT
	private static final int KANNADA_3X4_LETTERS_TO_NUMBERS_KEYCODE = 4; // Keyboard.EDGE_TOP
	private static final int KANNADA_3X4_LETTERS_TO_SYMBOLS_KEYCODE = -1; // Keyboard.KEYCODE_SHIFT
	private static final int PHONETIC_LETTERS_TO_SYMBOLS_KEYCODE = -2; // Keyboard.KEYCODE_MODE_CHANGE

	private KeyboardView mInputView;
	private CandidateView mCandidateView;
	private CompletionInfo[] mCompletions;

	private StringBuilder mComposing = new StringBuilder();
	private boolean mPredictionOn;
	private boolean mCompletionOn;
	private int mLastDisplayWidth;
	private boolean mCapsLock;
	private long mLastShiftTime;
	private long mMetaState;

	private LatinKeyboard mPhoneticSymbolsKeyboard;
	private LatinKeyboard mPhoneticSymbolsShiftedKeyboard;
	private LatinKeyboard mPhoneticKeyboard;
	private LatinKeyboard mPhoneticShiftedKeyboard;
	private LatinKeyboard mKaGaPaSymbolsKeyboard;
	private LatinKeyboard mKaGaPaSymbolsShiftedKeyboard;
	private LatinKeyboard mKaGaPaKeyboard;
	private LatinKeyboard mKaGaPaShiftedKeyboard;
	private LatinKeyboard mKannadaInScriptSymbolsKeyboard;
	private LatinKeyboard mKannadaInScriptSymbolsShiftedKeyboard;
	private LatinKeyboard mKannadaInScriptKeyboard;
	private LatinKeyboard mKannadaInScriptShiftedKeyboard;
	private LatinKeyboard mKannada3x4NumbersKeyboard;
	private LatinKeyboard mKannada3x4NumbersShiftedKeyboard;
	private LatinKeyboard mKannada3x4Keyboard;
	private LatinKeyboard mKannada3x4SymbolsKeyboard;

	static private LatinKeyboard mCurKeyboard;

	private String mWordSeparators;

	static private Set<Integer> mConsonants ;
	static private HashMap<Integer, Integer> mVowels;

	static {
		mConsonants = new HashSet<Integer>();
		for (int i = 'ಕ'; i <= 'ಹ'; i++) {
			mConsonants.add(i);
		}

		mVowels = new HashMap<Integer, Integer>();
		for (int i = 'ಆ', j = 'ಾ'; i <= 'ಔ'; i++, j++) {
			mVowels.put(i, j);
		}
	}

	/***
	 * Main initialization of the input method component.  Be sure to call
	 * to super class.
	 */
	@Override public void onCreate() {
		super.onCreate();
		mWordSeparators = getResources().getString(R.string.word_separators);
	}

	/**
	 * This is the point where you can do all of your UI initialization.  It
	 * is called after creation and any configuration change.
	 */
	@Override public void onInitializeInterface() {
		if (mPhoneticKeyboard != null) {
			// Configuration changes can happen after the keyboard gets recreated,
			// so we need to be able to re-build the keyboards if the available
			// space has changed.
			int displayWidth = getMaxWidth();
			if (displayWidth == mLastDisplayWidth) return;
			mLastDisplayWidth = displayWidth;
		}
		mPhoneticKeyboard = new LatinKeyboard(this, R.xml.phonetic);
		mPhoneticShiftedKeyboard = new LatinKeyboard(this, R.xml.phonetic_shift);
		mPhoneticSymbolsKeyboard = new LatinKeyboard(this, R.xml.phonetic_symbols);
		mPhoneticSymbolsShiftedKeyboard = new LatinKeyboard(this, R.xml.phonetic_symbols_shift);

		mKaGaPaKeyboard = new LatinKeyboard(this, R.xml.kagapa);
		mKaGaPaShiftedKeyboard = new LatinKeyboard(this, R.xml.kagapa_shift);
		mKaGaPaSymbolsKeyboard = new LatinKeyboard(this, R.xml.kagapa_symbols);
		mKaGaPaSymbolsShiftedKeyboard = new LatinKeyboard(this, R.xml.kagapa_symbols_shift);

		mKannadaInScriptKeyboard = new LatinKeyboard(this, R.xml.inscript);
		mKannadaInScriptShiftedKeyboard = new LatinKeyboard(this, R.xml.inscript_shift);
		mKannadaInScriptSymbolsKeyboard = new LatinKeyboard(this, R.xml.inscript_symbols);
		mKannadaInScriptSymbolsShiftedKeyboard = new LatinKeyboard(this, R.xml.inscript_symbols_shift);

		mKannada3x4Keyboard = new LatinKeyboard(this, R.xml.keyboard_3x4);
		mKannada3x4NumbersKeyboard = new LatinKeyboard(this, R.xml.keyboard_3x4_numbers);
		mKannada3x4NumbersShiftedKeyboard = new LatinKeyboard(this, R.xml.keyboard_3x4_numbers_shift);
		mKannada3x4SymbolsKeyboard = new LatinKeyboard(this, R.xml.keyboard_3x4_symbols);
	}

	/**
	 * Called by the framework when your view for creating input needs to
	 * be generated.  This will be called the first time your input method
	 * is displayed, and every time it needs to be re-created such as due to
	 * a configuration change.
	 */
	@Override public View onCreateInputView() {
		mInputView = (KeyboardView) getLayoutInflater().inflate(
				R.layout.input, null);
		mInputView.setOnKeyboardActionListener(this);
		if(mCurKeyboard == null)
			mInputView.setKeyboard(mPhoneticKeyboard);
		else
			mInputView.setKeyboard(mCurKeyboard);
		return mInputView;
	}

	/**
	 * Called by the framework when your view for showing candidates needs to
	 * be generated, like {@link #onCreateInputView}.
	 */
	@Override public View onCreateCandidatesView() {
		mCandidateView = new CandidateView(this);
		mCandidateView.setService(this);	
		return mCandidateView;
	}

	/**
	 * This is the main point where we do our initialization of the input method
	 * to begin operating on an application.  At this point we have been
	 * bound to the client, and are now receiving all of the detailed information
	 * about the target of our edits.
	 */
	@Override public void onStartInput(EditorInfo attribute, boolean restarting) {
		super.onStartInput(attribute, restarting);

		// Reset our state.  We want to do this even if restarting, because
		// the underlying state of the text editor could have changed in any way.
		mComposing.setLength(0);
		updateCandidates();

		if (!restarting) {
			// Clear shift states.
			mMetaState = 0;
		}

		mPredictionOn = false;
		mCompletionOn = false;
		mCompletions = null;

		// We are now going to initialize our state based on the type of
		// text being edited.
		switch (attribute.inputType&EditorInfo.TYPE_MASK_CLASS) {
		case EditorInfo.TYPE_CLASS_NUMBER:
		case EditorInfo.TYPE_CLASS_DATETIME:
			// Numbers and dates default to the symbols keyboard, with
			// no extra features.
			mCurKeyboard = mPhoneticSymbolsKeyboard;
			break;

		case EditorInfo.TYPE_CLASS_PHONE:
			// Phones will also default to the symbols keyboard, though
			// often you will want to have a dedicated phone keyboard.
			mCurKeyboard = mPhoneticSymbolsKeyboard;
			break;

		case EditorInfo.TYPE_CLASS_TEXT:
			// This is general text editing.  We will default to the
			// normal alphabetic keyboard, and assume that we should
			// be doing predictive text (showing candidates as the
			// user types).
			mCurKeyboard = mPhoneticKeyboard;
			mPredictionOn = false;

			// We now look for a few special variations of text that will
			// modify our behavior.
			int variation = attribute.inputType &  EditorInfo.TYPE_MASK_VARIATION;
			if (variation == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD ||
					variation == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
				// Do not display predictions / what the user is typing
				// when they are entering a password.
				mPredictionOn = false;
			}

			if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS 
					|| variation == EditorInfo.TYPE_TEXT_VARIATION_URI
					|| variation == EditorInfo.TYPE_TEXT_VARIATION_FILTER) {
				// Our predictions are not useful for e-mail addresses
				// or URIs.
				mPredictionOn = false;
			}

			if ((attribute.inputType&EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
				// If this is an auto-complete text view, then our predictions
				// will not be shown and instead we will allow the editor
				// to supply their own.  We only show the editor's
				// candidates when in fullscreen mode, otherwise relying
				// own it displaying its own UI.
				mPredictionOn = false;
				mCompletionOn = isFullscreenMode();
			}

			// We also want to look at the current state of the editor
			// to decide whether our alphabetic keyboard should start out
			// shifted.
			updateShiftKeyState(attribute);
			break;

		default:
			// For all unknown input types, default to the alphabetic
			// keyboard with no special features.
			mCurKeyboard = mPhoneticKeyboard;
			updateShiftKeyState(attribute);
		}

		// Update the label on the enter key, depending on what the application
		// says it will do.
		mCurKeyboard.setImeOptions(getResources(), attribute.imeOptions);
	}

	/**
	 * This is called when the user is done editing a field.  We can use
	 * this to reset our state.
	 */
	@Override public void onFinishInput() {
		super.onFinishInput();

		// Clear current composing text and candidates.
		mComposing.setLength(0);
		updateCandidates();

		// We only hide the candidates window when finishing input on
		// a particular editor, to avoid popping the underlying application
		// up and down if the user is entering text into the bottom of
		// its window.
		setCandidatesViewShown(false);

		mCurKeyboard = mPhoneticKeyboard;
		if (mInputView != null) {
			mInputView.closing();
		}
	}

	@Override public void onStartInputView(EditorInfo attribute, boolean restarting) {
		super.onStartInputView(attribute, restarting);
		// Apply the selected keyboard to the input view.
		mInputView.setKeyboard(mCurKeyboard);
		mInputView.closing();
	}

	/**
	 * Deal with the editor reporting movement of its cursor.
	 */
	@Override public void onUpdateSelection(int oldSelStart, int oldSelEnd,
			int newSelStart, int newSelEnd,
			int candidatesStart, int candidatesEnd) {
		super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
				candidatesStart, candidatesEnd);

		// If the current selection in the text view changes, we should
		// clear whatever candidate text we have.
		if (mComposing.length() > 0 && (newSelStart != candidatesEnd
				|| newSelEnd != candidatesEnd)) {
			mComposing.setLength(0);
			updateCandidates();
			InputConnection ic = getCurrentInputConnection();
			if (ic != null) {
				ic.finishComposingText();
			}
		}
	}

	/**
	 * This tells us about completions that the editor has determined based
	 * on the current text in it.  We want to use this in fullscreen mode
	 * to show the completions ourself, since the editor can not be seen
	 * in that situation.
	 */
	@Override public void onDisplayCompletions(CompletionInfo[] completions) {
		if (mCompletionOn) {
			mCompletions = completions;
			if (completions == null) {
				setSuggestions(null, false, false);
				return;
			}

			List<String> stringList = new ArrayList<String>();
			for (int i=0; i<(completions != null ? completions.length : 0); i++) {
				CompletionInfo ci = completions[i];
				if (ci != null) stringList.add(ci.getText().toString());
			}
			setSuggestions(stringList, true, true);
		}
	}

	/**
	 * This translates incoming hard key events in to edit operations on an
	 * InputConnection.  It is only needed when using the
	 * PROCESS_HARD_KEYS option.
	 */
	private boolean translateKeyDown(int keyCode, KeyEvent event) {
		mMetaState = MetaKeyKeyListener.handleKeyDown(mMetaState,
				keyCode, event);
		int c = event.getUnicodeChar(MetaKeyKeyListener.getMetaState(mMetaState));
		mMetaState = MetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
		InputConnection ic = getCurrentInputConnection();
		if (c == 0 || ic == null) {
			return false;
		}

		boolean dead = false;

		if ((c & KeyCharacterMap.COMBINING_ACCENT) != 0) {
			dead = true;
			c = c & KeyCharacterMap.COMBINING_ACCENT_MASK;
		}

		if (mComposing.length() > 0) {
			char accent = mComposing.charAt(mComposing.length() -1 );
			int composed = KeyEvent.getDeadChar(accent, c);

			if (composed != 0) {
				c = composed;
				mComposing.setLength(mComposing.length()-1);
			}
		}

		onKey(c, null);

		return true;
	}

	/**
	 * Use this to monitor key events being delivered to the application.
	 * We get first crack at them, and can either resume them or let them
	 * continue to the app.
	 */
	@Override public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			// The InputMethodService already takes care of the back
			// key for us, to dismiss the input method if it is shown.
			// However, our keyboard could be showing a pop-up window
			// that back should dismiss, so we first allow it to do that.
			if (event.getRepeatCount() == 0 && mInputView != null) {
				if (mInputView.handleBack()) {
					return true;
				}
			}
			break;

		case KeyEvent.KEYCODE_DEL:
			// Special handling of the delete key: if we currently are
			// composing text for the user, we want to modify that instead
			// of let the application to the delete itself.
			if (mComposing.length() > 0) {
				onKey(Keyboard.KEYCODE_DELETE, null);
				return true;
			}
			break;

		case KeyEvent.KEYCODE_ENTER:
			// Let the underlying text editor always handle these.
			return false;

		default:
			// For all other keys, if we want to do transformations on
			// text being entered with a hard keyboard, we need to process
			// it and do the appropriate action.
			if (PROCESS_HARD_KEYS) {
				if (keyCode == KeyEvent.KEYCODE_SPACE
						&& (event.getMetaState()&KeyEvent.META_ALT_ON) != 0) {
					// A silly example: in our input method, Alt+Space
					// is a shortcut for 'android' in lower case.
					InputConnection ic = getCurrentInputConnection();
					if (ic != null) {

						// First, tell the editor that it is no longer in the
						// shift state, since we are consuming this.
						ic.clearMetaKeyStates(KeyEvent.META_ALT_ON);
						keyDownUp(KeyEvent.KEYCODE_A);
						keyDownUp(KeyEvent.KEYCODE_N);
						keyDownUp(KeyEvent.KEYCODE_D);
						keyDownUp(KeyEvent.KEYCODE_R);
						keyDownUp(KeyEvent.KEYCODE_O);
						keyDownUp(KeyEvent.KEYCODE_I);
						keyDownUp(KeyEvent.KEYCODE_D);
						// And we consume this event.
						return true;
					}
				}
				if (mPredictionOn && translateKeyDown(keyCode, event)) {
					return true;
				}
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Use this to monitor key events being delivered to the application.
	 * We get first crack at them, and can either resume them or let them
	 * continue to the app.
	 */
	@Override public boolean onKeyUp(int keyCode, KeyEvent event) {
		// If we want to do transformations on text being entered with a hard
		// keyboard, we need to process the up events to update the meta key
		// state we are tracking.
		if (PROCESS_HARD_KEYS) {
			if (mPredictionOn) {
				mMetaState = MetaKeyKeyListener.handleKeyUp(mMetaState,
						keyCode, event);
			}
		}

		return super.onKeyUp(keyCode, event);
	}

	/**
	 * Helper function to commit any text being composed in to the editor.
	 */
	private void commitTyped(InputConnection inputConnection) {
		if (mComposing.length() > 0) {
			inputConnection.commitText(mComposing, mComposing.length());
			mComposing.setLength(0);
			updateCandidates();
		}
	}

	/**
	 * Helper to update the shift state of our keyboard based on the initial
	 * editor state.
	 */
	private void updateShiftKeyState(EditorInfo attr) {
		if (attr != null 
				&& mInputView != null && mPhoneticKeyboard == mInputView.getKeyboard()) {
			int caps = 0;
			EditorInfo ei = getCurrentInputEditorInfo();
			if (ei != null && ei.inputType != EditorInfo.TYPE_NULL) {
				//caps = getCurrentInputConnection().getCursorCapsMode(attr.inputType);
			}
			// mInputView.setShifted(mCapsLock || caps != 0);
		}
	}

	/**
	 * Helper to determine if a given character code is alphabetic.
	 */
	private boolean isAlphabet(int code) {
		if (Character.isLetter(code)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Helper to send a key down / key up pair to the current editor.
	 */
	private void keyDownUp(int keyEventCode) {
		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
	}

	/**
	 * Helper to send a character to the editor as raw key events.
	 */
	private void sendKey(int keyCode) {
		switch (keyCode) {
		case '\n':
			keyDownUp(KeyEvent.KEYCODE_ENTER);
			break;
		default:
			if (keyCode >= '0' && keyCode <= '9') {
				keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
			} else {
				getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
			}
			break;
		}
	}

	// Implementation of KeyboardViewListener
	public void onKey(int presentKeycode, int[] keyCodes) {
		InputConnection ic = getCurrentInputConnection();
		String lastChar;
		int mLastKey = -44;

		lastChar = ic.getTextBeforeCursor(1, 0).toString();
		if (lastChar.length() > 0) {
			mLastKey = lastChar.codePointAt(0);
		} else {
			mLastKey = -44; // dummy
		}

		if (isWordSeparator(presentKeycode)) {
			// Handle separator
			if (mComposing.length() > 0) {
				commitTyped(getCurrentInputConnection());
			}
			sendKey(presentKeycode);
			updateShiftKeyState(getCurrentInputEditorInfo());
		} else if (presentKeycode == Keyboard.KEYCODE_DELETE) {
			handleBackspace();
		} else if (presentKeycode == Keyboard.KEYCODE_SHIFT) {
			handleShift();
		} else if (presentKeycode == CHANGE_LANGUAGE_LAYOUT_OPTION_KEYCODE && mInputView != null) {
			showLanguageOptionsMenu();
		} else if (presentKeycode == Keyboard.KEYCODE_CANCEL) {
			handleClose();
			return;
		} else if (presentKeycode == Keyboard.KEYCODE_MODE_CHANGE && mInputView != null) {
			Keyboard current = mInputView.getKeyboard();
			if (current == mPhoneticKeyboard || current == mPhoneticShiftedKeyboard) {
				current = mPhoneticSymbolsKeyboard;
			} else {
				current = mPhoneticKeyboard;
			}
			mInputView.setKeyboard(current);
			if (current == mPhoneticSymbolsKeyboard) {
				current.setShifted(false);
			}
		} else if (presentKeycode == KAGAPA_LETTERS_TO_SYMBOLS_KEYCODE && mInputView != null) {
			Keyboard current = mInputView.getKeyboard();
			if (current == mKaGaPaKeyboard || current == mKaGaPaShiftedKeyboard) {
				current = mKaGaPaSymbolsKeyboard;
			} else {
				current = mKaGaPaKeyboard;
			}
			mInputView.setKeyboard(current);
			if (current == mKaGaPaSymbolsKeyboard) {
				current.setShifted(false);
			}
		} else if (presentKeycode == KANNADA_INSCRIPT_LETTERS_TO_SYMBOLS_KEYCODE && mInputView != null) {
			Keyboard current = mInputView.getKeyboard();
			if (current == mKannadaInScriptKeyboard || current == mKannadaInScriptShiftedKeyboard) {
				current = mKannadaInScriptSymbolsKeyboard;
			} else {
				current = mKannadaInScriptKeyboard;
			}
			mInputView.setKeyboard(current);
			if (current == mKannadaInScriptSymbolsKeyboard) {
				current.setShifted(false);
			}
		} else if (presentKeycode == KANNADA_3X4_LETTERS_TO_NUMBERS_KEYCODE && mInputView != null) {
			Keyboard current = mInputView.getKeyboard();
			if (current == mKannada3x4Keyboard || current == mKannada3x4SymbolsKeyboard) {
				current = mKannada3x4NumbersKeyboard;
			} else {
				current = mKannada3x4Keyboard;
			}
			mInputView.setKeyboard(current);
			if (current == mKannada3x4NumbersKeyboard) {
				current.setShifted(false);
			}
		} else if (mConsonants.contains(mLastKey) && mVowels.containsKey(presentKeycode)
				&& !checkInScriptKeyboard()) {
			handleCharacter(mVowels.get(presentKeycode), keyCodes);
		} else {
			handleCharacter(presentKeycode, keyCodes);
		}
	}

	private boolean check3x4Keyboard() {
		Keyboard current = mInputView.getKeyboard();
		if (current == mKannada3x4Keyboard || current == mKannada3x4SymbolsKeyboard) {
			return true;
		}
		return false;
	}

	private boolean checkInScriptKeyboard() {
		Keyboard current = mInputView.getKeyboard();
		if (current == mKannadaInScriptKeyboard || current == mKannadaInScriptShiftedKeyboard) {
			return true;
		}
		return false;
	}

	private void showLanguageOptionsMenu() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setIcon(R.drawable.icon);
		launchLanguageSettings();
	}

	private void showLayoutOptionsMenu() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setIcon(R.drawable.icon);
		launchLayoutSettings();
	}

	private AlertDialog mOptionsDialog;

	private void launchLayoutSettings() {
		AlertDialog.Builder lyBuilder = new AlertDialog.Builder(this);
		lyBuilder.setCancelable(true);
		lyBuilder.setTitle("Select Layout");
		lyBuilder.setIcon(R.drawable.icon);
		lyBuilder.setItems(new CharSequence[]{"KaGaPa", "InScript", "3x4 Keyboard", "Phonetic"}, new OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				Keyboard current = mInputView.getKeyboard();
				switch(which){
				case 0: //KaGaPa
					if (current == mKaGaPaKeyboard || current == mKaGaPaShiftedKeyboard) {
						current = mKaGaPaSymbolsKeyboard;
					} else {
						current = mKaGaPaKeyboard;
					}
					mInputView.setKeyboard(current);
					if (current == mKaGaPaSymbolsKeyboard) {
						current.setShifted(false);
					}
					break;
				case 1: //InScript Keyboard
					if (current == mKannadaInScriptKeyboard || current == mKannadaInScriptShiftedKeyboard) {
						current = mKannadaInScriptSymbolsKeyboard;
					} else {
						current = mKannadaInScriptKeyboard;
					}
					mInputView.setKeyboard(current);
					if (current == mKannadaInScriptSymbolsKeyboard) {
						current.setShifted(false);
					}
					break;
				case 2: //3x4 Keyboard
					if (current == mKannada3x4Keyboard || current == mKannada3x4SymbolsKeyboard) {
						current = mKannada3x4NumbersKeyboard;
					} else {
						current = mKannada3x4Keyboard;
					}
					mInputView.setKeyboard(current);
					if (current == mKannada3x4NumbersKeyboard) {
						current.setShifted(false);
					}
					break;
				case 3: //Phonetic
					if (current == mPhoneticKeyboard || current == mPhoneticShiftedKeyboard) {
						current = mPhoneticSymbolsKeyboard;
					} else {
						current = mPhoneticKeyboard;
					}
					mInputView.setKeyboard(current);
					if (current == mPhoneticSymbolsKeyboard) {
						current.setShifted(false);
					}
					break;
				}
			}
		});
		mOptionsDialog = lyBuilder.create();
		Window window = mOptionsDialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();

		lp.token = mInputView.getWindowToken();
		lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
		window.setAttributes(lp);
		window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		mOptionsDialog.show();
	}

	private void launchLanguageSettings() {
		AlertDialog.Builder lanBuilder = new AlertDialog.Builder(this);
		lanBuilder.setCancelable(true);
		lanBuilder.setTitle("Select Language");
		lanBuilder.setIcon(R.drawable.icon);
		lanBuilder.setItems(new CharSequence[]{"Hindi","Kannada","Tamil"}, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch(which){
				case 0: //Hindi
					Toast.makeText(getBaseContext(), "Hindi Keyboard selected", Toast.LENGTH_SHORT).show();
					break;
				case 1: //Kannada
					Toast.makeText(getBaseContext(), "Kannada Keyboard selected", Toast.LENGTH_SHORT).show();
					showLayoutOptionsMenu();
					break;
				default :
					Toast.makeText(getBaseContext(), "Tamil Keyboard selected", Toast.LENGTH_SHORT).show();
				}
			}
		});
		mOptionsDialog = lanBuilder.create();
		Window window = mOptionsDialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();

		lp.token = mInputView.getWindowToken();
		lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
		window.setAttributes(lp);
		window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		mOptionsDialog.show();

	}

	public void onText(CharSequence text) {
		InputConnection ic = getCurrentInputConnection();
		if (ic == null) return;
		ic.beginBatchEdit();
		if (mComposing.length() > 0) {
			commitTyped(ic);
		}
		ic.commitText(text, 0);
		ic.endBatchEdit();
		updateShiftKeyState(getCurrentInputEditorInfo());
	}

	/**
	 * Update the list of available candidates from the current composing
	 * text.  This will need to be filled in by however you are determining
	 * candidates.
	 */
	private void updateCandidates() {
		if (!mCompletionOn) {
			if (mComposing.length() > 0) {
				ArrayList<String> list = new ArrayList<String>();
				list.add(mComposing.toString());
				setSuggestions(list, true, true);
			} else {
				setSuggestions(null, false, false);
			}
		}
	}

	public void setSuggestions(List<String> suggestions, boolean completions,
			boolean typedWordValid) {
		if (suggestions != null && suggestions.size() > 0) {
			setCandidatesViewShown(true);
		} else if (isExtractViewShown()) {
			setCandidatesViewShown(true);
		}
		if (mCandidateView != null) {
			mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
		}
	}

	private void handleBackspace() {
		final int length = mComposing.length();
		if (length > 1) {
			mComposing.delete(length - 1, length);
			getCurrentInputConnection().setComposingText(mComposing, 1);
			updateCandidates();
		} else if (length > 0) {
			mComposing.setLength(0);
			getCurrentInputConnection().commitText("", 0);
			updateCandidates();
		} else {
			keyDownUp(KeyEvent.KEYCODE_DEL);
		}
		updateShiftKeyState(getCurrentInputEditorInfo());
	}

	private void handleShift() {
		if (mInputView == null) {
			return;
		}

		Keyboard currentKeyboard = mInputView.getKeyboard();
		if (currentKeyboard == mPhoneticKeyboard) {
			// Alphabet keyboard
			checkToggleCapsLock();
			mPhoneticKeyboard.setShifted(true);
			mInputView.setKeyboard(mPhoneticShiftedKeyboard);
			mPhoneticShiftedKeyboard.setShifted(true);
			// mInputView.setShifted(mCapsLock || !mInputView.isShifted());
		} else if (currentKeyboard == mPhoneticShiftedKeyboard) {
			mPhoneticShiftedKeyboard.setShifted(false);
			mInputView.setKeyboard(mPhoneticKeyboard);
			mPhoneticKeyboard.setShifted(false);
		} else if (currentKeyboard == mPhoneticSymbolsKeyboard) {
			mPhoneticSymbolsKeyboard.setShifted(true);
			mInputView.setKeyboard(mPhoneticSymbolsShiftedKeyboard);
			mPhoneticSymbolsShiftedKeyboard.setShifted(true);
		} else if (currentKeyboard == mPhoneticSymbolsShiftedKeyboard) {
			mPhoneticSymbolsShiftedKeyboard.setShifted(false);
			mInputView.setKeyboard(mPhoneticSymbolsKeyboard);
			mPhoneticSymbolsKeyboard.setShifted(false);
		} else if (currentKeyboard == mKaGaPaSymbolsKeyboard) {
			mKaGaPaSymbolsKeyboard.setShifted(true);
			mInputView.setKeyboard(mKaGaPaSymbolsShiftedKeyboard);
			mKaGaPaSymbolsShiftedKeyboard.setShifted(true);
		} else if (currentKeyboard == mKaGaPaSymbolsShiftedKeyboard) {
			mKaGaPaSymbolsShiftedKeyboard.setShifted(false);
			mInputView.setKeyboard(mKaGaPaSymbolsKeyboard);
			mKaGaPaSymbolsKeyboard.setShifted(false);
		} else if (currentKeyboard == mKaGaPaKeyboard) {
			mKaGaPaKeyboard.setShifted(true);
			mInputView.setKeyboard(mKaGaPaShiftedKeyboard);
			mKaGaPaShiftedKeyboard.setShifted(true);
		} else if (currentKeyboard == mKaGaPaShiftedKeyboard) {
			mKaGaPaShiftedKeyboard.setShifted(false);
			mInputView.setKeyboard(mKaGaPaKeyboard);
			mKaGaPaKeyboard.setShifted(false);
		} else if (currentKeyboard == mKannadaInScriptSymbolsKeyboard) {
			mKannadaInScriptSymbolsKeyboard.setShifted(true);
			mInputView.setKeyboard(mKannadaInScriptSymbolsShiftedKeyboard);
			mKannadaInScriptSymbolsShiftedKeyboard.setShifted(true);
		} else if (currentKeyboard == mKannadaInScriptSymbolsShiftedKeyboard) {
			mKannadaInScriptSymbolsShiftedKeyboard.setShifted(false);
			mInputView.setKeyboard(mKannadaInScriptSymbolsKeyboard);
			mKannadaInScriptSymbolsKeyboard.setShifted(false);
		} else if (currentKeyboard == mKannadaInScriptKeyboard) {
			mKannadaInScriptKeyboard.setShifted(true);
			mInputView.setKeyboard(mKannadaInScriptShiftedKeyboard);
			mKannadaInScriptShiftedKeyboard.setShifted(true);
		} else if (currentKeyboard == mKannadaInScriptShiftedKeyboard) {
			mKannadaInScriptShiftedKeyboard.setShifted(false);
			mInputView.setKeyboard(mKannadaInScriptKeyboard);
			mKannadaInScriptKeyboard.setShifted(false);
		} else if (currentKeyboard == mKannada3x4NumbersKeyboard) {
			mKannada3x4NumbersKeyboard.setShifted(true);
			mInputView.setKeyboard(mKannada3x4NumbersShiftedKeyboard);
			mKannada3x4NumbersShiftedKeyboard.setShifted(true);
		} else if (currentKeyboard == mKannada3x4NumbersShiftedKeyboard) {
			mKannada3x4NumbersShiftedKeyboard.setShifted(false);
			mInputView.setKeyboard(mKannada3x4NumbersKeyboard);
			mKannada3x4NumbersKeyboard.setShifted(false);
		} else if (currentKeyboard == mKannada3x4Keyboard) {
			mKannada3x4Keyboard.setShifted(true);
			mInputView.setKeyboard(mKannada3x4SymbolsKeyboard);
			mKannada3x4SymbolsKeyboard.setShifted(true);
		} else if (currentKeyboard == mKannada3x4SymbolsKeyboard) {
			mKannada3x4SymbolsKeyboard.setShifted(false);
			mInputView.setKeyboard(mKannada3x4Keyboard);
			mKannada3x4Keyboard.setShifted(false);
		}
	}

	private void handleCharacter(int primaryCode, int[] keyCodes) {
		if (isInputViewShown()) {
			if (mInputView.isShifted()) {
				//primaryCode = Character.toUpperCase(primaryCode);
			}
		}
		if (isAlphabet(primaryCode) && mPredictionOn) {
			mComposing.append((char) primaryCode);
			getCurrentInputConnection().setComposingText(mComposing, 1);
			updateShiftKeyState(getCurrentInputEditorInfo());
			updateCandidates();
		} else {
			getCurrentInputConnection().commitText(
					String.valueOf((char) primaryCode), 1);
		}
	}

	private void handleClose() {
		commitTyped(getCurrentInputConnection());
		requestHideSelf(0);
		mInputView.closing();
	}

	private void checkToggleCapsLock() {
		long now = System.currentTimeMillis();
		if (mLastShiftTime + 800 > now) {
			mCapsLock = !mCapsLock;
			mLastShiftTime = 0;
		} else {
			mLastShiftTime = now;
		}
	}

	private String getWordSeparators() {
		return mWordSeparators;
	}

	public boolean isWordSeparator(int code) {
		String separators = getWordSeparators();
		return separators.contains(String.valueOf((char)code));
	}

	public void pickDefaultCandidate() {
		pickSuggestionManually(0);
	}

	public void pickSuggestionManually(int index) {
		if (mCompletionOn && mCompletions != null && index >= 0
				&& index < mCompletions.length) {
			CompletionInfo ci = mCompletions[index];
			getCurrentInputConnection().commitCompletion(ci);
			if (mCandidateView != null) {
				mCandidateView.clear();
			}
			updateShiftKeyState(getCurrentInputEditorInfo());
		} else if (mComposing.length() > 0) {
			// If we were generating candidate suggestions for the current
			// text, we would commit one of them here.  But for this sample,
			// we will just commit the current text.
			commitTyped(getCurrentInputConnection());
		}
	}

	public void swipeRight() {
		if (mCompletionOn) {
			pickDefaultCandidate();
		}
	}

	public void swipeLeft() {
		handleBackspace();
	}

	public void swipeDown() {
		handleClose();
	}

	public void swipeUp() {
	}

	public void onPress(int primaryCode) {
	}

	public void onRelease(int primaryCode) {
	}
}