package com.totsp.crossword.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.totsp.crossword.puz.Box;
import com.totsp.crossword.puz.Note;
import com.totsp.crossword.puz.Playboard;
import com.totsp.crossword.puz.Playboard.Position;
import com.totsp.crossword.puz.Playboard.Word;
import com.totsp.crossword.view.ScrollingImageView.Point;

import java.util.logging.Logger;


public class PlayboardRenderer {
    private static final float BASE_BOX_SIZE_INCHES = 0.25F;
    private static final Logger LOG = Logger.getLogger(PlayboardRenderer.class.getCanonicalName());
    private final Paint blackBox = new Paint();
    private final Paint blackCircle = new Paint();
    private final Paint blackLine = new Paint();
    private final Paint cheated = new Paint();
    private final Paint currentLetterBox = new Paint();
    private final Paint currentLetterHighlight = new Paint();
    private final Paint currentWordHighlight = new Paint();
    private final Paint letterText = new Paint();
    private final Paint numberText = new Paint();
    private final Paint noteText = new Paint();
    private final Paint red = new Paint();
    private final Paint white = new Paint();
    private Bitmap bitmap;
    private Playboard board;
    private float dpi;
    private float scale = 1.0F;
    private boolean hintHighlight;
    private int widthPixels;
    private final int boxColor;
    private final int blankColor;
    private final int errorColor;

    public PlayboardRenderer(Playboard board, float dpi, int widthPixels, boolean hintHighlight,
                             int boxColor, int blankColor, int errorColor) {
        this.boxColor = boxColor;
        this.blankColor = blankColor;
        this.errorColor = errorColor;

        this.dpi = dpi;
        this.widthPixels = widthPixels;
        this.board = board;
        this.hintHighlight = hintHighlight;
        blackLine.setColor(blankColor);
        blackLine.setStrokeWidth(2.0F);

        numberText.setTextAlign(Align.LEFT);
        numberText.setColor(blankColor);
        numberText.setAntiAlias(true);
        numberText.setTypeface(Typeface.MONOSPACE);

        noteText.setTextAlign(Align.LEFT);
        noteText.setColor(blankColor);
        noteText.setAntiAlias(true);
        noteText.setTypeface(Typeface.MONOSPACE);

        letterText.setTextAlign(Align.CENTER);
        letterText.setColor(blankColor);
        letterText.setAntiAlias(true);
        letterText.setTypeface(Typeface.SANS_SERIF);

        blackBox.setColor(blankColor);

        blackCircle.setColor(blankColor);
        blackCircle.setAntiAlias(true);
        blackCircle.setStyle(Style.STROKE);

        currentWordHighlight.setColor(Color.parseColor("#FFAE57"));
        currentLetterHighlight.setColor(Color.parseColor("#EB6000"));
        currentLetterBox.setColor(boxColor);
        currentLetterBox.setStrokeWidth(2.0F);

        white.setTextAlign(Align.CENTER);
        white.setColor(boxColor);
        white.setAntiAlias(true);
        white.setTypeface(Typeface.SANS_SERIF);

        red.setTextAlign(Align.CENTER);
        red.setColor(errorColor);
        red.setAntiAlias(true);
        red.setTypeface(Typeface.SANS_SERIF);

        this.cheated.setColor(Color.parseColor("#FFE0E0"));
    }

    public float getDeviceMaxScale(){
        float retValue;
        LOG.info("Board "+board.getBoxes().length+" widthPixels "+widthPixels);
        // inches * pixels per inch * units
        retValue = 2.2F;
        float puzzleBaseSizeInInches = board.getBoxes().length * BASE_BOX_SIZE_INCHES;
        //leave a 1/16th in gutter on the puzzle.
        float fitToScreen =  (dpi * (puzzleBaseSizeInInches + 0.0625F)) / dpi;

        if(retValue < fitToScreen){
            retValue = fitToScreen;
        }

        LOG.warning("getDeviceMaxScale "+retValue);
        return retValue;
    }

    public float getDeviceMinScale(){
        //inches * (pixels / pixels per inch);
        float retValue = 0.9F * ((dpi * BASE_BOX_SIZE_INCHES) / dpi);
        LOG.warning("getDeviceMinScale "+retValue);
        return retValue;
    }

    public void setScale(float scale) {
        if (scale > getDeviceMaxScale()) {
            scale = getDeviceMaxScale();
        } else if (scale < getDeviceMinScale()) {
            scale = getDeviceMinScale();
        } else if (String.valueOf(scale).equals("NaN")) {
            scale = 1.0f;
        }
        this.bitmap = null;
        this.scale = scale;
    }

    public float getScale()
    {
        return this.scale;
    }

    public Bitmap draw(Word reset,
                       boolean displayScratchAcross, boolean displayScratchDown) {
        try {
            Box[][] boxes = this.board.getBoxes();
            boolean renderAll = reset == null;

            if (scale > getDeviceMaxScale()) {
                scale = getDeviceMaxScale();
            } else if (scale < getDeviceMinScale()) {
                scale = getDeviceMinScale();
            } else if (Float.isNaN(scale)) {
                scale = 1.0F;
            }

            int boxSize = (int) (BASE_BOX_SIZE_INCHES * dpi * scale);

            if (bitmap == null) {
                LOG.warning("New bitmap box size "+boxSize);
                bitmap = Bitmap.createBitmap(boxes.length * boxSize,
                        boxes[0].length * boxSize, Bitmap.Config.RGB_565);
                bitmap.eraseColor(Color.BLACK);
                renderAll = true;
            }

            Canvas canvas = new Canvas(bitmap);

            // board data

            Word currentWord = this.board.getCurrentWord();

            for (int col = 0; col < boxes.length; col++) {
                for (int row = 0; row < boxes[col].length; row++) {
                    if (!renderAll) {
                        if (!currentWord.checkInWord(col, row) && !reset.checkInWord(col, row)) {
                            continue;
                        }
                    }

                    int x = col * boxSize;
                    int y = row * boxSize;
                    this.drawBox(canvas, x, y, row, col, boxSize, boxes[col][row], currentWord, this.board.getHighlightLetter(), displayScratchAcross, displayScratchDown);
                }
            }

            return bitmap;
        } catch (OutOfMemoryError e) {
            return bitmap;
        }
    }

    public Bitmap drawWord(boolean displayScratchAcross, boolean displayScratchDown) {
        Position[] word = this.board.getCurrentWordPositions();
        Box[] boxes = this.board.getCurrentWordBoxes();
        int boxSize = (int) (BASE_BOX_SIZE_INCHES * this.dpi * scale) ;
        Bitmap bitmap = Bitmap.createBitmap(word.length * boxSize, boxSize, Bitmap.Config.RGB_565);
        bitmap.eraseColor(Color.BLACK);

        Canvas canvas = new Canvas(bitmap);

        for (int i = 0; i < word.length; i++) {
            int x = i * boxSize;
            int y = 0;
            this.drawBox(canvas, x, y, word[i].down, word[i].across, boxSize, boxes[i], null, this.board.getHighlightLetter(), displayScratchAcross, displayScratchDown);
        }

        return bitmap;
    }

    public Bitmap drawBoxes(Box[] boxes,
                            Position highlight,
                            boolean displayScratchAcross,
                            boolean displayScratchDown) {
        if (boxes == null || boxes.length == 0) {
            return null;
        }

        int boxSize = (int) (BASE_BOX_SIZE_INCHES * this.dpi * scale);
        Bitmap bitmap = Bitmap.createBitmap((int) (boxes.length * boxSize),
                                            (int) (boxSize),
                                            Bitmap.Config.RGB_565);
        bitmap.eraseColor(Color.BLACK);

        Canvas canvas = new Canvas(bitmap);

        for (int i = 0; i < boxes.length; i++) {
            int x = (int) (i * boxSize);
            int y = 0;
            this.drawBox(canvas,
                         x, y,
                         0, i,
                         boxSize,
                         boxes[i],
                         null,
                         highlight,
                         displayScratchAcross, displayScratchDown);
        }

        return bitmap;
    }


    public Position findBox(Point p) {
        int boxSize = (int) (BASE_BOX_SIZE_INCHES * dpi * scale);

        if (boxSize == 0) {
            boxSize = (int) (BASE_BOX_SIZE_INCHES * dpi * 0.25F);
        }

        int col = p.x / boxSize;
        int row = p.y / boxSize;

        return new Position(col, row);
    }

    public int findBoxNoScale(Point p) {
        int boxSize =  (int) (BASE_BOX_SIZE_INCHES * dpi);
        LOG.info("DPI "+dpi+" scale "+ scale +" box size "+boxSize);
        return p.x / boxSize;
    }

    public Point findPointBottomRight(Position p) {
        int boxSize = (int) (BASE_BOX_SIZE_INCHES * dpi * scale);
        int x = (p.across * boxSize) + boxSize;
        int y = (p.down * boxSize) + boxSize;

        return new Point(x, y);
    }

    public Point findPointTopLeft(Position p) {
        int boxSize = (int) (BASE_BOX_SIZE_INCHES  * dpi * scale);
        int x = p.across * boxSize;
        int y = p.down * boxSize;

        return new Point(x, y);
    }

    public float fitTo(int shortDimension) {
        this.bitmap = null;
        // (pixels / boxes) / (pixels per inch / inches)
        Box[][] boxes = this.board.getBoxes();
        int numBoxes = Math.max(boxes.length, boxes[0].length);
        double newScale = (double) shortDimension / (double) numBoxes / ((double) dpi * (double) BASE_BOX_SIZE_INCHES);
        LOG.warning("fitTo "+shortDimension+" dpi"+ dpi +" == "+newScale);
        if(newScale < getDeviceMinScale()){
            newScale = getDeviceMinScale();
        }
        this.scale = (float) newScale;
        return this.scale;
    }

    public float zoomIn() {
        this.bitmap = null;
        this.scale = scale * 1.25F;
        if(scale > this.getDeviceMaxScale()){
            this.scale = this.getDeviceMaxScale();
        }
        return scale;
    }

    public float zoomOut() {
        this.bitmap = null;
        this.scale = scale / 1.25F;
        if(scale < this.getDeviceMinScale()){
            scale = this.getDeviceMinScale();
        }
        return scale;
    }

    public float zoomReset() {
        this.bitmap = null;
        this.scale = 1.0F;
        return scale;
    }

    public float zoomInMax() {
        this.bitmap = null;
        this.scale = getDeviceMaxScale();

        return scale;
    }

    private void drawBox(Canvas canvas,
                         int x, int y,
                         int row, int col,
                         int boxSize,
                         Box box,
                         Word currentWord,
                         Position highlight,
                         boolean displayScratchAcross, boolean displayScratchDown) {
        int numberTextSize = boxSize / 4;
        int noteTextSize = boxSize / 3;
        int letterTextSize = Math.round(boxSize * 0.7F);

        // scale paints
        numberText.setTextSize(numberTextSize);
        noteText.setTextSize(noteTextSize);
        letterText.setTextSize(letterTextSize);
        red.setTextSize(letterTextSize);
        white.setTextSize(letterTextSize);

        boolean inCurrentWord = (currentWord != null) && currentWord.checkInWord(col, row);

        Paint thisLetter;

        Rect r = new Rect(x + 1, y + 1, (x + boxSize) - 1, (y + boxSize) - 1);

        if (box == null) {
            canvas.drawRect(r, this.blackBox);
        } else {
            // Background colors
            if ((highlight.across == col) && (highlight.down == row)) {
                canvas.drawRect(r, this.currentLetterHighlight);
            } else if ((currentWord != null) && currentWord.checkInWord(col, row)) {
                canvas.drawRect(r, this.currentWordHighlight);
            } else if (this.board.isShowErrors() && !box.isBlank() &&
                    (box.getSolution() != box.getResponse())) {
                box.setCheated(true);
                canvas.drawRect(r, this.red);
            } else if (this.hintHighlight && box.isCheated()) {
                canvas.drawRect(r, this.cheated);
            } else {
                canvas.drawRect(r, this.white);
            }

            if (box.isAcross() || box.isDown()) {
                canvas.drawText(Integer.toString(box.getClueNumber()), x + 2, y + numberTextSize + 2, this.numberText);
            }

            // Draw circle
            if (box.isCircled()) {
                canvas.drawCircle(x + (boxSize / 2) + 0.5F, y + (boxSize / 2) + 0.5F, (boxSize / 2) - 1.5F, blackCircle);
            }

            thisLetter = this.letterText;

            if (board.isShowErrors() && (box.getSolution() != box.getResponse())) {
                if (!box.isBlank()){
                    box.setCheated(true);
                }
                if ((highlight.across == col) && (highlight.down == row)) {
                    thisLetter = this.white;
                } else if (inCurrentWord) {
                    thisLetter = red;
                }
            }

            if (!box.isBlank()) {
                canvas.drawText(Character.toString(box.getResponse()),
                                x + (boxSize / 2),
                                y + (int) (letterTextSize * 1.25),
                                thisLetter);
            } else {
                if (displayScratchAcross && box.isPartOfAcross()) {
                    int clueNumber = box.getPartOfAcrossClueNumber();
                    Note note = board.getPuzzle().getNote(clueNumber, true);
                    if (note != null) {
                        String scratch = note.getScratch();
                        int pos = box.getAcrossPosition();
                        if (scratch != null && pos < scratch.length()) {
                            canvas.drawText(Character.toString(scratch.charAt(pos)),
                                            x + (boxSize / 3),
                                            y + boxSize - (noteTextSize / 2),
                                            this.noteText);
                        }
                    }
                }
                if (displayScratchDown && box.isPartOfDown()) {
                    int clueNumber = box.getPartOfDownClueNumber();
                    Note note = board.getPuzzle().getNote(clueNumber, false);
                    if (note != null) {
                        String scratch = note.getScratch();
                        int pos = box.getDownPosition();
                        if (scratch != null && pos < scratch.length()) {
                            canvas.drawText(Character.toString(scratch.charAt(pos)),
                                            x + boxSize - noteTextSize - 2,
                                            y + 2 + (boxSize / 2),
                                            this.noteText);
                        }
                    }
                }
            }
        }

        Paint boxColor = (((highlight.across == col) && (highlight.down == row)) && (currentWord != null))
                ? this.currentLetterBox : this.blackLine;

        // Draw left
        if ((col != (highlight.across + 1)) || (row != highlight.down)) {
            canvas.drawLine(x, y, x, y + boxSize, boxColor);
        }

        // Draw top
        if ((row != (highlight.down + 1)) || (col != highlight.across)) {
            canvas.drawLine(x, y, x + boxSize, y, boxColor);
        }

        // Draw right
        if ((col != (highlight.across - 1)) || (row != highlight.down)) {
            canvas.drawLine(x + boxSize, y, x + boxSize, y + boxSize, boxColor);
        }

        // Draw bottom
        if ((row != (highlight.down - 1)) || (col != highlight.across)) {
            canvas.drawLine(x, y + boxSize, x + boxSize, y + boxSize, boxColor);
        }
    }
}
