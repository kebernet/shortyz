package com.totsp.crossword.puz;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class Playboard implements Serializable {
    private HashMap<Integer, Position> acrossWordStarts = new HashMap<Integer, Position>();
    private HashMap<Integer, Position> downWordStarts = new HashMap<Integer, Position>();
    private MovementStrategy movementStrategy = MovementStrategy.MOVE_NEXT_ON_AXIS;
    private Position highlightLetter = new Position(0, 0);
    private Puzzle puzzle;
    private String responder;
    private Box[][] boxes;
    private boolean across = true;
    private boolean showErrors;
    private boolean skipCompletedLetters;

    public Playboard(Puzzle puzzle, MovementStrategy movementStrategy) {
        this(puzzle);
        this.movementStrategy = movementStrategy;
    }

    public Playboard(Puzzle puzzle) {
        this.puzzle = puzzle;
        this.highlightLetter = new Position(0, 0);
        this.boxes = new Box[puzzle.getBoxes()[0].length][puzzle.getBoxes().length];

        for (int x = 0; x < puzzle.getBoxes().length; x++) {
            for (int y = 0; y < puzzle.getBoxes()[x].length; y++) {
                boxes[y][x] = puzzle.getBoxes()[x][y];

                if ((boxes[y][x] != null) && boxes[y][x].isAcross()) {
                    acrossWordStarts.put(boxes[y][x].getClueNumber(), new Position(y, x));
                }

                if ((boxes[y][x] != null) && boxes[y][x].isDown()) {
                    downWordStarts.put(boxes[y][x].getClueNumber(), new Position(y, x));
                }
            }
        }

        if (this.boxes[0][0] == null) {
            this.moveRight(false);
        }
    }

    public void setAcross(boolean across) {
        this.across = across;
    }

    public boolean isAcross() {
        return across;
    }

    public Clue[] getAcrossClues() {
        Clue[] clues = new Clue[puzzle.getAcrossClues().length];

        for (int i = 0; i < clues.length; i++) {
            clues[i] = new Clue();
            clues[i].hint = puzzle.getAcrossClues()[i];
            clues[i].number = puzzle.getAcrossCluesLookup()[i];
        }

        return clues;
    }

    public Box[][] getBoxes() {
        return this.boxes;
    }

    public Clue getClue() {
        Clue c = new Clue();

        try {
            Position start = this.getCurrentWordStart();
            c.number = this.getBoxes()[start.across][start.down].getClueNumber();
            c.hint = this.across ? this.puzzle.findAcrossClue(c.number) : this.puzzle.findDownClue(c.number);
        } catch (Exception e) {
        }

        return c;
    }

    public Box getCurrentBox() {
        return this.boxes[this.highlightLetter.across][this.highlightLetter.down];
    }

    /** Returns the 0 based index of the current clue based on the current across or down state
     *
     * @return index of the across or down clue based on the current state
     */
    public int getCurrentClueIndex() {
        Clue c = this.getClue();

        if (across) {
            return Arrays.binarySearch(this.puzzle.getAcrossCluesLookup(), c.number);
        } else {
            return Arrays.binarySearch(this.puzzle.getDownCluesLookup(), c.number);
        }
    }

    public Word getCurrentWord() {
        Word w = new Word();
        w.start = this.getCurrentWordStart();
        w.across = this.across;
        w.length = this.getWordRange();

        return w;
    }

    public Box[] getCurrentWordBoxes() {
        Word currentWord = this.getCurrentWord();
        Box[] result = new Box[currentWord.length];

        int across = currentWord.start.across;
        int down = currentWord.start.down;

        for (int i = 0; i < result.length; i++) {
            int newAcross = across;
            int newDown = down;

            if (currentWord.across) {
                newAcross += i;
            } else {
                newDown += i;
            }

            result[i] = this.boxes[newAcross][newDown];
        }

        return result;
    }

    public Position[] getCurrentWordPositions() {
        Word currentWord = this.getCurrentWord();
        Position[] result = new Position[currentWord.length];
        int across = currentWord.start.across;
        int down = currentWord.start.down;

        for (int i = 0; i < result.length; i++) {
            int newAcross = across;
            int newDown = down;

            if (currentWord.across) {
                newAcross += i;
            } else {
                newDown += i;
            }

            result[i] = new Position(newAcross, newDown);
        }

        return result;
    }

    public Position getCurrentWordStart() {
        if (this.isAcross()) {
            int col = this.highlightLetter.across;
            Box b = null;

            while (b == null) {
                try {
                    if ((boxes[col][this.highlightLetter.down] != null) &&
                            boxes[col][this.highlightLetter.down].isAcross()) {
                        b = boxes[col][this.highlightLetter.down];
                    } else {
                        col--;
                    }
                } catch (Exception e) {
                    break;
                }
            }

            return new Position(col, this.highlightLetter.down);
        } else {
            int row = this.highlightLetter.down;
            Box b = null;

            while (b == null) {
                try {
                    if ((boxes[this.highlightLetter.across][row] != null) &&
                            boxes[this.highlightLetter.across][row].isDown()) {
                        b = boxes[this.highlightLetter.across][row];
                    } else {
                        row--;
                    }
                } catch (Exception e) {
                    break;
                }
            }

            return new Position(this.highlightLetter.across, row);
        }
    }

    public Clue[] getDownClues() {
        Clue[] clues = new Clue[puzzle.getDownClues().length];

        for (int i = 0; i < clues.length; i++) {
            clues[i] = new Clue();
            clues[i].hint = puzzle.getDownClues()[i];
            clues[i].number = puzzle.getDownCluesLookup()[i];
        }

        return clues;
    }

    public Word setHighlightLetter(Position highlightLetter) {
        Word w = this.getCurrentWord();

        if (highlightLetter.equals(this.highlightLetter)) {
            this.toggleDirection();
        } else {
            if ((this.boxes.length > highlightLetter.across) && (highlightLetter.across >= 0) &&
                    (this.boxes[highlightLetter.across].length > highlightLetter.down) && (highlightLetter.down >= 0) &&
                    (this.boxes[highlightLetter.across][highlightLetter.down] != null)) {
                this.highlightLetter = highlightLetter;
            }
        }

        return w;
    }

    public Position getHighlightLetter() {
        return highlightLetter;
    }

    public void setMovementStrategy(MovementStrategy movementStrategy) {
        this.movementStrategy = movementStrategy;
    }

    public Puzzle getPuzzle() {
        return this.puzzle;
    }

    /**
     * @param responder the responder to set
     */
    public void setResponder(String responder) {
        this.responder = responder;
    }

    /**
     * @return the responder
     */
    public String getResponder() {
        return responder;
    }

    public void setShowErrors(boolean showErrors) {
        this.showErrors = showErrors;
    }

    public boolean isShowErrors() {
        return this.showErrors;
    }

    public void setSkipCompletedLetters(boolean skipCompletedLetters) {
        this.skipCompletedLetters = skipCompletedLetters;
    }

    public boolean isSkipCompletedLetters() {
        return skipCompletedLetters;
    }

    public Box[] getWordBoxes(int number, boolean isAcross) {
        Position start = isAcross ? this.acrossWordStarts.get(number) : this.downWordStarts.get(number);
        int range = this.getWordRange(start, isAcross);
        int across = start.across;
        int down = start.down;
        Box[] result = new Box[range];

        for (int i = 0; i < result.length; i++) {
            int newAcross = across;
            int newDown = down;

            if (isAcross) {
                newAcross += i;
            } else {
                newDown += i;
            }

            result[i] = this.boxes[newAcross][newDown];
        }

        return result;
    }

    public int getWordRange(Position start, boolean across) {
        if (across) {
            int col = start.across;
            Box b = null;

            do {
                b = null;

                int checkCol = col + 1;

                try {
                    col++;
                    b = this.getBoxes()[checkCol][start.down];
                } catch (RuntimeException e) {
                }
            } while (b != null);

            return col - start.across;
        } else {
            int row = start.down;
            Box b = null;

            do {
                b = null;

                int checkRow = row + 1;

                try {
                    row++;
                    b = this.getBoxes()[start.across][checkRow];
                } catch (RuntimeException e) {
                }
            } while (b != null);

            return row - start.down;
        }
    }

    public int getWordRange() {
        return getWordRange(this.getCurrentWordStart(), this.isAcross());
    }

    /**
     * Handler for the backspace key.  Uses the following algorithm:
     * -If current box is empty, move back one character.  If not, stay still.
     * -Delete the letter in the current box.
     */
    public Word deleteLetter() {
        Box currentBox = this.boxes[this.highlightLetter.across][this.highlightLetter.down];
        Word wordToReturn = this.getCurrentWord();

        if (currentBox.getResponse() == ' ') {
            wordToReturn = this.previousLetter();
            currentBox = this.boxes[this.highlightLetter.across][this.highlightLetter.down];
        }

        currentBox.setResponse(' ');

        return wordToReturn;
    }

    public void jumpTo(int clueIndex, boolean across) {
        try {
            if (across) {
                this.highlightLetter = (this.acrossWordStarts.get(this.puzzle.getAcrossCluesLookup()[clueIndex]));
            } else {
                this.highlightLetter = (this.downWordStarts.get(this.puzzle.getDownCluesLookup()[clueIndex]));
            }

            this.across = across;
        } catch (Exception e) {
        }
    }

    public Word moveDown() {
        return this.moveDown(false);
    }

    public Position moveDown(Position original, boolean skipCompleted) {
        Position next = new Position(original.across, original.down + 1);
        Box value = this.getBoxes()[next.across][next.down];

        if ((value == null) || skipCurrentBox(value, skipCompleted)) {
            try {
                next = moveDown(next, skipCompleted);
            } catch (ArrayIndexOutOfBoundsException e) {
            }
        }

        return next;
    }

    public Word moveDown(boolean skipCompleted) {
        Word w = this.getCurrentWord();

        try {
            Position newPos = this.moveDown(this.getHighlightLetter(), skipCompleted);
            this.setHighlightLetter(newPos);
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        return w;
    }

    public Position moveLeft(Position original, boolean skipCompleted) {
        Position next = new Position(original.across - 1, original.down);
        Box value = this.getBoxes()[next.across][next.down];

        if ((value == null) || skipCurrentBox(value, skipCompleted)) {
            try {
                next = moveLeft(next, skipCompleted);
            } catch (ArrayIndexOutOfBoundsException e) {
            }
        }

        return next;
    }

    public Word moveLeft(boolean skipCompleted) {
        Word w = this.getCurrentWord();

        try {
            Position newPos = this.moveLeft(this.getHighlightLetter(), skipCompleted);
            this.setHighlightLetter(newPos);
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        return w;
    }

    public Word moveLeft() {
        return moveLeft(false);
    }

    public Word moveRight() {
        return moveRight(false);
    }

    public Position moveRight(Position original, boolean skipCompleted) {
        Position next = new Position(original.across + 1, original.down);
        Box value = this.getBoxes()[next.across][next.down];

        if ((value == null) || skipCurrentBox(value, skipCompleted)) {
            try {
                next = moveRight(next, skipCompleted);
            } catch (ArrayIndexOutOfBoundsException e) {
            }
        }

        return next;
    }

    public Word moveRight(boolean skipCompleted) {
        Word w = this.getCurrentWord();

        try {
            Position newPos = this.moveRight(this.getHighlightLetter(), skipCompleted);
            this.setHighlightLetter(newPos);
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        return w;
    }

    public Position moveUp(Position original, boolean skipCompleted) {
        Position next = new Position(original.across, original.down - 1);
        Box value = this.getBoxes()[next.across][next.down];

        if ((value == null) || skipCurrentBox(value, skipCompleted)) {
            try {
                next = moveUp(next, skipCompleted);
            } catch (ArrayIndexOutOfBoundsException e) {
            }
        }

        return next;
    }

    public Word moveUp() {
        return moveUp(false);
    }

    public Word moveUp(boolean skipCompleted) {
        Word w = this.getCurrentWord();

        try {
            Position newPos = this.moveUp(this.getHighlightLetter(), skipCompleted);
            this.setHighlightLetter(newPos);
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        return w;
    }

    public Word nextLetter(boolean skipCompletedLetters) {
        return this.movementStrategy.move(this, skipCompletedLetters);
    }

    public Word nextLetter() {
        return nextLetter(this.skipCompletedLetters);
    }

    public Word nextWord() {
        Word previous = this.getCurrentWord();

        Position p = this.getHighlightLetter();

        int newAcross = p.across;
        int newDown = p.down;

        if (previous.across) {
            newAcross = (previous.start.across + previous.length) - 1;
        } else {
            newDown = (previous.start.down + previous.length) - 1;
        }

        Position newPos = new Position(newAcross, newDown);

        if (!newPos.equals(p)) {
            this.setHighlightLetter(newPos);
        }

        this.nextLetter();

        return previous;
    }

    public Word playLetter(char letter) {
        Box b = this.boxes[this.highlightLetter.across][this.highlightLetter.down];

        if (b == null) {
            return null;
        }

        b.setResponse(letter);
        b.setResponder(this.responder);

        return this.nextLetter();
    }

    public Word previousLetter() {
        return this.movementStrategy.back(this);
    }

    public Word previousWord() {
        Word previous = this.getCurrentWord();

        Position p = this.getHighlightLetter();

        int newAcross = p.across;
        int newDown = p.down;

        if (previous.across) {
            newAcross = previous.start.across - 1;
        } else {
            newDown = previous.start.down - 1;
        }

        this.setHighlightLetter(new Position(newAcross, newDown));
        this.previousLetter();

        Word current = this.getCurrentWord();
        this.setHighlightLetter(new Position(current.start.across, current.start.down));

        return previous;
    }

    public Position revealLetter() {
        Box b = this.boxes[this.highlightLetter.across][this.highlightLetter.down];

        if ((b != null) && (b.getSolution() != b.getResponse())) {
            b.setCheated(true);
            b.setResponse(b.getSolution());

            return this.highlightLetter;
        }

        return null;
    }

    public List<Position> revealPuzzle() {
        ArrayList<Position> changes = new ArrayList<Position>();

        for (int across = 0; across < this.boxes.length; across++) {
            for (int down = 0; down < this.boxes[across].length; down++) {
                Box b = this.boxes[across][down];

                if ((b != null) && (b.getSolution() != b.getResponse())) {
                    b.setCheated(true);
                    b.setResponse(b.getSolution());
                    changes.add(new Position(across, down));
                }
            }
        }

        return changes;
    }

    public List<Position> revealWord() {
        ArrayList<Position> changes = new ArrayList<Position>();
        Position oldHighlight = this.highlightLetter;
        Word w = this.getCurrentWord();
        this.highlightLetter = w.start;

        for (int i = 0; i < w.length; i++) {
            Position p = revealLetter();

            if (p != null) {
                changes.add(p);
            }

            nextLetter(false);
        }

        this.highlightLetter = oldHighlight;

        return changes;
    }

    public boolean skipCurrentBox(Box b, boolean skipCompleted) {
        return skipCompleted && (b.getResponse() != ' ') &&
        (!this.isShowErrors() || (b.getResponse() == b.getSolution()));
    }

    public Word toggleDirection() {
        Word w = this.getCurrentWord();
        this.across = !across;

        return w;
    }

    public void toggleShowErrors() {
        this.showErrors = !this.showErrors;
    }

    public static class Clue implements Serializable {
        public String hint;
        public int number;

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            final Clue other = (Clue) obj;

            if ((this.hint == null) ? (other.hint != null) : (!this.hint.equals(other.hint))) {
                return false;
            }

            if (this.number != other.number) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return this.number;
        }

        @Override
        public String toString() {
            return number + ". " + hint;
        }
    }

    public static class Position implements Serializable {
        public int across;
        public int down;

        protected Position(){

        }

        public Position(int across, int down) {
            this.down = down;
            this.across = across;
        }

        @Override
        public boolean equals(Object o) {
            if ((o == null) || (o.getClass() != this.getClass())) {
                return false;
            }

            Position p = (Position) o;

            return ((p.down == this.down) && (p.across == this.across));
        }

        @Override
        public int hashCode() {
            return this.across ^ this.down;
        }

        @Override
        public String toString() {
            return "[" + this.across + " x " + this.down + "]";
        }
    }

    public static class Word implements Serializable {
        public Position start;
        public boolean across;
        public int length;

        public boolean checkInWord(int across, int down) {
            int ranging = this.across ? across : down;
            boolean offRanging = this.across ? (down == start.down) : (across == start.across);

            int startPos = this.across ? start.across : start.down;

            return (offRanging && (startPos <= ranging) && ((startPos + length) > ranging));
        }

        @Override
        public boolean equals(Object o) {
            if (o.getClass() != Word.class) {
                return false;
            }

            Word check = (Word) o;

            return check.start.equals(this.start) && (check.across == this.across) && (check.length == this.length);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = (29 * hash) + ((this.start != null) ? this.start.hashCode() : 0);
            hash = (29 * hash) + (this.across ? 1 : 0);
            hash = (29 * hash) + this.length;

            return hash;
        }
    }
}
