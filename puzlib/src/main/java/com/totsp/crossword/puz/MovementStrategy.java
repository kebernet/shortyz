package com.totsp.crossword.puz;

import com.totsp.crossword.puz.Playboard.Position;
import com.totsp.crossword.puz.Playboard.Word;

import java.io.Serializable;
import java.util.Arrays;

public interface MovementStrategy extends Serializable {

	public static final MovementStrategy MOVE_NEXT_ON_AXIS = new MovementStrategy() {

		public Word move(Playboard board, boolean skipCompletedLetters) {
			if (board.isAcross()) {
				return board.moveRight(skipCompletedLetters);
			} else {
				return board.moveDown(skipCompletedLetters);
			}
		}

		public Word back(Playboard board) {
	        if (board.isAcross()) {
	            return board.moveLeft();
	        } else {
	            return board.moveUp(false);
	        }
		}
	};

	public static final MovementStrategy STOP_ON_END = new MovementStrategy() {

		public Word move(Playboard board, boolean skipCompletedLetters) {
			// This is overly complex, but I am trying to save calls to heavy
			// methods on the board.

			Position p = board.getHighlightLetter();
			Word w = board.getCurrentWord();
			if ((w.across && p.across == w.start.across + w.length - 1)
					|| (!w.across && p.down == w.start.down + w.length - 1)) {
				return w;
			} else {
				MOVE_NEXT_ON_AXIS.move(board,skipCompletedLetters);
				Word newWord = board.getCurrentWord();
				if (newWord.equals(w)) {
					return w;
				} else {
					board.setHighlightLetter(p);
					return w;
				}
			}
		}

		public Word back(Playboard board) {
			Word w = board.getCurrentWord();
			Position p = board.getHighlightLetter();
			if(!p.equals(w.start)){
				MOVE_NEXT_ON_AXIS.back(board);
			}
			return w;
		}

	};

	public static final MovementStrategy MOVE_NEXT_CLUE = new MovementStrategy() {
		
		/**
		 * Moves to the word corresponding to the next clue.  If the current word is the last
		 * across word, then this moves to the first down word, and if it is the last down word,
		 * it moves to the first across word.  Returns true if the first letter of the new clue
		 * is blank.
		 */
		private boolean moveToNextWord(Playboard board, boolean skipCompletedLetters) {
			Word w = board.getCurrentWord();	
			int currentClueNumber = board.getBoxes()[w.start.across][w.start.down].getClueNumber();
			int nextClueIndex;
			boolean nextClueAcross;
			Puzzle puz = board.getPuzzle();
			Integer[] cluesLookup = w.across ? puz.getAcrossCluesLookup() : puz.getDownCluesLookup();
			if(currentClueNumber == cluesLookup[cluesLookup.length - 1]) {
				// At end of clues - move to first clue of other type.
				nextClueIndex = 0;
				nextClueAcross = !w.across;
			} else {
				nextClueIndex = Arrays.binarySearch(cluesLookup, currentClueNumber) + 1;
				nextClueAcross = w.across;
			}
			board.jumpTo(nextClueIndex, nextClueAcross);
			return !board.skipCurrentBox(board.getCurrentBox(), skipCompletedLetters);
		}
		
		/**
		 * Moves to the last letter of the word corresponding to the previous clue.  Does nothing
		 * if the current word is the first across or first down clue.
		 */
		private void moveToPreviousWord(Playboard board) {
			Word w = board.getCurrentWord();
			int currentClueNumber = board.getBoxes()[w.start.across][w.start.down].getClueNumber();
			int previousClueIndex;
			Puzzle puz = board.getPuzzle();
			Integer[] cluesLookup = w.across ? puz.getAcrossCluesLookup() : puz.getDownCluesLookup();
			if (currentClueNumber == cluesLookup[0]) {
				// At beginning of grid - do nothing.
				return;
			} else {
				previousClueIndex = Arrays.binarySearch(cluesLookup, currentClueNumber) - 1;
			}
			board.jumpTo(previousClueIndex, w.across);
			
			// Move to last letter.
			w = board.getCurrentWord();
			Position newPos;
			if (w.across) {
				newPos = new Position(w.start.across + w.length - 1, w.start.down);
			} else {
				newPos = new Position(w.start.across, w.start.down + w.length - 1);
			}
			board.setHighlightLetter(newPos);
		}
		
		/**
		 * Moves to the next blank letter in this clue, starting at position p.  Returns true if 
		 * such a letter was found; returns false if the clue has already been filled.
		 */
		private boolean moveToNextBlank(Playboard board, Position p, boolean skipCompletedLetters) {
			Word w = board.getCurrentWord();
			Box[] wordBoxes = board.getCurrentWordBoxes();
			
			if(w.across) {
				for(int x = p.across; x < w.start.across + w.length; x++) {
					if(!board.skipCurrentBox(wordBoxes[x - w.start.across], skipCompletedLetters)) {
						board.setHighlightLetter(new Position(x, p.down));
						return true;
					}
				}
			} else {
				for(int y = p.down; y < w.start.down + w.length; y++) {
					if(!board.skipCurrentBox(wordBoxes[y - w.start.down], skipCompletedLetters)) {
						board.setHighlightLetter(new Position(p.across, y));
						return true;
					}
				}
			}
			return false;
		}
		
		public Word move(Playboard board, boolean skipCompletedLetters) {
			Position p = board.getHighlightLetter();
			Word w = board.getCurrentWord();
			
			if((!board.isShowErrors() && board.getPuzzle().getPercentFilled() == 100) || 
					board.getPuzzle().getPercentComplete() == 100) {
				// Puzzle complete - don't move.
				return w;
			}
			
			Position nextPos;
			if ((w.across && p.across == w.start.across + w.length - 1)
					|| (!w.across && p.down == w.start.down + w.length - 1)) {
				// At end of a word - move to the next one and continue.
				if(moveToNextWord(board, skipCompletedLetters)) {
					return w;
				}
				nextPos = board.getHighlightLetter();
			} else {
				// In middle of word - move to the next unfilled letter.
				nextPos = w.across ? new Position(p.across + 1, p.down) : new Position(p.across, p.down + 1);
			}
			while(!(moveToNextBlank(board, nextPos, skipCompletedLetters))) {
				if(moveToNextWord(board, skipCompletedLetters)) {
					break;
				}
				nextPos = board.getHighlightLetter();
			}
			return w;
		}

		public Word back(Playboard board) {
			Position p = board.getHighlightLetter();
			Word w = board.getCurrentWord();
			if ((w.across && p.across == w.start.across)
					|| (!w.across && p.down == w.start.down)) {
				// At beginning of word - move to previous clue.
				moveToPreviousWord(board);
			} else {
				// In middle of word - just move back one character.
				MOVE_NEXT_ON_AXIS.back(board);
			}
			return w;
		}
		
	};

	public static final MovementStrategy MOVE_PARALLEL_WORD = new MovementStrategy() {

		public Word move(Playboard board, boolean skipCompletedLetters) {
			Word w = board.getCurrentWord();
			Position p = board.getHighlightLetter();
			

			if ((w.across && p.across == w.start.across + w.length - 1)
					|| (!w.across && p.down == w.start.down + w.length - 1)) {
				//board.setHighlightLetter(w.start);
				Word newWord;
				if (w.across) {
					board.moveDown();
					while(board.getClue().hint == null && board.getHighlightLetter().down < board.getBoxes()[0].length){
						board.moveDown();
					}
					if(board.getClue().hint == null){
						board.toggleDirection();
					}
					newWord = board.getCurrentWord();
				} else {
					board.moveRight();
					while(board.getClue().hint == null && board.getHighlightLetter().across < board.getBoxes().length){
						board.moveRight();
					}
					if(board.getClue().hint == null){
						board.toggleDirection();
					}
					newWord = board.getCurrentWord();
					
				}
				if (!newWord.equals(w)) {
					board.setHighlightLetter(newWord.start);
					board.setAcross(w.across);
				}
			
			} else {
				MOVE_NEXT_ON_AXIS.move(board,
						skipCompletedLetters);
				Word newWord = board.getCurrentWord();
				if (!newWord.equals(w)) {
					Position end = new Position(w.start.across + (w.across ? w.length -1: 0),
							w.start.down + (w.across? 0 : w.length -1));
					board.setHighlightLetter(end);
					this.move(board, skipCompletedLetters);
				}
			}

			return w;
		}

		public Word back(Playboard board) {
			Word w = board.getCurrentWord();
			Position p = board.getHighlightLetter();
			if ((w.across && p.across == w.start.across)
					|| (!w.across && p.down == w.start.down)) {
				//board.setHighlightLetter(w.start);
				Word newWord;
				Position lastPos = null;
				if (w.across) {
					board.moveUp();
					while(!board.getHighlightLetter().equals(lastPos) && board.getClue().hint == null && board.getHighlightLetter().down < board.getBoxes()[0].length){
						lastPos = board.getHighlightLetter();
						board.moveUp();
						
					}
					if(board.getClue().hint == null){
						board.toggleDirection();
					}
					newWord = board.getCurrentWord();
				} else {
					board.moveLeft();
					while(!board.getHighlightLetter().equals(lastPos) && board.getClue().hint == null && board.getHighlightLetter().across < board.getBoxes().length){
						lastPos = board.getHighlightLetter();
						board.moveLeft();
					}
					if(board.getClue().hint == null){
						board.toggleDirection();
					}
					newWord = board.getCurrentWord();
					
				}
				if (!newWord.equals(w)) {
					
					Position newPos = new Position(newWord.start.across + (newWord.across ? newWord.length -1 : 0), newWord.start.down + (newWord.across ? 0 : newWord.length -1));
					
					board.setHighlightLetter(newPos);
					board.setAcross(w.across);
				}
			
			} else {
				Word newWord = MOVE_NEXT_ON_AXIS.back(board);
				if (!newWord.equals(w)) {
					board.setHighlightLetter(newWord.start);
				}
			}

			return w;
		}

	};

	Word move(Playboard board, boolean skipCompletedLetters);
	
	Word back(Playboard board);

}
