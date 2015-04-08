package com.totsp.crossword.view;

import java.util.logging.Logger;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.totsp.crossword.puz.Box;
import com.totsp.crossword.puz.Playboard;
import com.totsp.crossword.puz.Playboard.Position;
import com.totsp.crossword.puz.Playboard.Word;
import com.totsp.crossword.view.ScrollingImageView.Point;


public class PlayboardRenderer {
    private static final int BOX_SIZE = 30;
    private static final Logger LOG = Logger.getLogger("com.totsp.crossword");
    private final Paint blackBox = new Paint();
    private final Paint blackCircle = new Paint();
    private final Paint blackLine = new Paint();
    private final Paint cheated = new Paint();
    private final Paint currentLetterBox = new Paint();
    private final Paint currentLetterHighlight = new Paint();
    private final Paint currentWordHighlight = new Paint();
    private final Paint letterText = new Paint();
    private final Paint numberText = new Paint();
    private final Paint red = new Paint();
    private final Paint white = new Paint();
    private Bitmap bitmap;
    private Playboard board;
    private float logicalDensity;
    private float scale = 1.0F;
    private boolean hintHighlight;

    public PlayboardRenderer(Playboard board, float logicalDensity, boolean hintHighlight) {
        this.scale = scale * logicalDensity;
        this.logicalDensity = logicalDensity;
        this.board = board;
        this.hintHighlight = hintHighlight;
        blackLine.setColor(Color.BLACK);
        blackLine.setStrokeWidth(2.0F);

        numberText.setTextAlign(Align.LEFT);
        numberText.setColor(Color.BLACK);
        numberText.setAntiAlias(true);
        numberText.setTypeface(Typeface.MONOSPACE);

        letterText.setTextAlign(Align.CENTER);
        letterText.setColor(Color.BLACK);
        letterText.setAntiAlias(true);
        letterText.setTypeface(Typeface.SANS_SERIF);

        blackBox.setColor(Color.BLACK);

        blackCircle.setColor(Color.BLACK);
        blackCircle.setAntiAlias(true);
        blackCircle.setStyle(Style.STROKE);

        currentWordHighlight.setColor(Color.parseColor("#FFAE57"));
        currentLetterHighlight.setColor(Color.parseColor("#EB6000"));
        currentLetterBox.setColor(Color.parseColor("#FFFFFF"));
        currentLetterBox.setStrokeWidth(2.0F);

        white.setTextAlign(Align.CENTER);
        white.setColor(Color.WHITE);
        white.setAntiAlias(true);
        white.setTypeface(Typeface.SANS_SERIF);

        red.setTextAlign(Align.CENTER);
        red.setColor(Color.RED);
        red.setAntiAlias(true);
        red.setTypeface(Typeface.SANS_SERIF);

        this.cheated.setColor(Color.parseColor("#FFE0E0"));
    }

    public float getDeviceMaxScale(){
        float deviceMaxScale = 2.5F;

        // TODO find a more precise way to calculate this rather than device density
        if (logicalDensity > 2.0F)
            deviceMaxScale = 4.25F;

        return deviceMaxScale;
    }

    public float getDeviceMinScale(){
        return logicalDensity * 0.5F;
    }

    public float setLogicalScale(float scale) {
        this.scale *= scale;

        if (scale > getDeviceMaxScale()) {
            scale = getDeviceMaxScale();
        } else if (scale < getDeviceMinScale()) {
            scale = getDeviceMinScale();
        } else if (("" + scale).equals("NaN")) {
            scale = 1.0f * this.logicalDensity;
        }

        this.bitmap = null;

        return this.scale;
    }

    public void setScale(float scale) {
        if (scale > getDeviceMaxScale()) {
            scale = getDeviceMaxScale();
        } else if (scale < getDeviceMinScale()) {
            scale = getDeviceMinScale();
        } else if (("" + scale).equals("NaN")) {
            scale = 1.0f * this.logicalDensity;
        }
        this.bitmap = null;
        this.scale = scale;
    }

    public float getScale()
    {
        return this.scale;
    }

    public Bitmap draw(Word reset) {
        try {
            Box[][] boxes = this.board.getBoxes();
            boolean renderAll = reset == null;

            if (scale > getDeviceMaxScale()) {
                scale = getDeviceMaxScale();
            } else if (scale < getDeviceMinScale()) {
                scale = getDeviceMinScale();
            } else if (scale == Float.NaN) {
                scale = 1.0f * this.logicalDensity;
            }

            if (bitmap == null) {
                LOG.warning("New bitmap");
                bitmap = Bitmap.createBitmap((int) (boxes.length * BOX_SIZE * scale),
                        (int) (boxes[0].length * BOX_SIZE * scale), Bitmap.Config.RGB_565);
                bitmap.eraseColor(Color.BLACK);
                renderAll = true;
            }

            Canvas canvas = new Canvas(bitmap);

            // board data
            int boxSize = (int) (BOX_SIZE * scale);
            Word currentWord = this.board.getCurrentWord();

            for (int col = 0; col < boxes.length; col++) {
                for (int row = 0; row < boxes[col].length; row++) {
                    if (!renderAll) {
                        if (!currentWord.checkInWord(col, row) && (reset != null) && !reset.checkInWord(col, row)) {
                            continue;
                        }
                    }

                    int x = col * boxSize;
                    int y = row * boxSize;
                    this.drawBox(canvas, x, y, row, col, scale, boxes[col][row], currentWord);
                }
            }

            return bitmap;
        } catch (OutOfMemoryError e) {
            return bitmap;
        }
    }

    public Bitmap drawWord() {
        Position[] word = this.board.getCurrentWordPositions();
        Box[] boxes = this.board.getCurrentWordBoxes();
        int boxSize = (int) (BOX_SIZE * this.logicalDensity);
        Bitmap bitmap = Bitmap.createBitmap((int) (word.length * boxSize), (int) (boxSize), Bitmap.Config.RGB_565);
        bitmap.eraseColor(Color.BLACK);

        Canvas canvas = new Canvas(bitmap);

        for (int i = 0; i < word.length; i++) {
            int x = (int) (i * boxSize);
            int y = 0;
            this.drawBox(canvas, x, y, word[i].down, word[i].across, this.logicalDensity, boxes[i], null);
        }

        return bitmap;
    }

    public Position findBox(Point p) {
        int boxSize = (int) (BOX_SIZE * scale);

        if (boxSize == 0) {
            boxSize = (int) (BOX_SIZE * 0.25D);
        }

        int col = p.x / boxSize;
        int row = p.y / boxSize;

        return new Position(col, row);
    }

    public int findBoxNoScale(Point p) {
        int boxSize = (int) (BOX_SIZE * this.logicalDensity);

        return p.x / boxSize;
    }

    public Point findPointBottomRight(Position p) {
        int boxSize = (int) (BOX_SIZE * scale);
        int x = (p.across * boxSize) + boxSize;
        int y = (p.down * boxSize) + boxSize;

        return new Point(x, y);
    }

    public Point findPointTopLeft(Position p) {
        int boxSize = (int) (BOX_SIZE * scale);
        int x = p.across * boxSize;
        int y = p.down * boxSize;

        return new Point(x, y);
    }

    public float fitTo(int shortDimension) {
        this.bitmap = null;

        double newScale = (double) shortDimension /
                (double) (this.board.getBoxes().length) /
                (double) BOX_SIZE;
        if(newScale < getDeviceMinScale()){
            newScale = getDeviceMinScale();
        }
        this.scale = (float) newScale;
        return this.scale;
    }

    public float zoomIn() {
        this.bitmap = null;
        this.scale = scale * 1.25F;

        return scale;
    }

    public float zoomOut() {
        this.bitmap = null;
        this.scale = scale / 1.25F;

        return scale;
    }

    public float zoomReset() {
        this.bitmap = null;
        this.scale = 1.0F * logicalDensity;

        return scale;
    }

    public float zoomInMax()
    {
        this.bitmap = null;
        this.scale = getDeviceMaxScale();

        return scale;
    }

    private void drawBox(Canvas canvas, int x, int y, int row, int col, float scale, Box box, Word currentWord) {
        int boxSize = (int) (BOX_SIZE * scale);
        int numberTextSize = (int) (scale * 8F);
        int letterTextSize = (int) (scale * 20);

        // scale paints
        numberText.setTextSize(scale * 8F);
        letterText.setTextSize(scale * 20F);
        red.setTextSize(scale * 20F);
        white.setTextSize(scale * 20F);

        boolean inCurrentWord = (currentWord != null) && currentWord.checkInWord(col, row);
        Position highlight = this.board.getHighlightLetter();

        Paint thisLetter = null;

        Rect r = new Rect(x + 1, y + 1, (x + boxSize) - 1, (y + boxSize) - 1);

        if (box == null) {
            canvas.drawRect(r, this.blackBox);
        } else {
            // Background colors
            if ((highlight.across == col) && (highlight.down == row)) {
                canvas.drawRect(r, this.currentLetterHighlight);
            } else if ((currentWord != null) && currentWord.checkInWord(col, row)) {
                canvas.drawRect(r, this.currentWordHighlight);
            } else if (this.hintHighlight && box.isCheated() && !(board.isShowErrors() && box.getResponse() != box.getSolution())) {
                canvas.drawRect(r, this.cheated);
            } else if (this.board.isShowErrors() && (box.getResponse() != ' ') &&
                    (box.getSolution() != box.getResponse())) {
                box.setCheated(true);
                canvas.drawRect(r, this.red);
            } else {
                canvas.drawRect(r, this.white);
            }

            if (box.isAcross() | box.isDown()) {
                canvas.drawText(Integer.toString(box.getClueNumber()), x + 2, y + numberTextSize + 2, this.numberText);
            }

            // Draw circle
            if (box.isCircled()) {
                canvas.drawCircle(x + (boxSize / 2) + 0.5F, y + (boxSize / 2) + 0.5F, (boxSize / 2) - 1.5F, blackCircle);
            }

            thisLetter = this.letterText;

            if (board.isShowErrors() && (box.getSolution() != box.getResponse())) {
                if ((highlight.across == col) && (highlight.down == row)) {
                    thisLetter = this.white;
                } else if (inCurrentWord) {
                    thisLetter = red;
                }
            }

            canvas.drawText(Character.toString(box.getResponse()), x + (boxSize / 2),
                    y + (int) (letterTextSize * 1.25), thisLetter);
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