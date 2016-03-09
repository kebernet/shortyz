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
    private static final float BASE_BOX_SIZE_INCHES = 0.5F;
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
    private final Paint red = new Paint();
    private final Paint white = new Paint();
    private Bitmap bitmap;
    private Playboard board;
    private int dpi;
    private float scale = 1.0F;
    private boolean hintHighlight;
    private int widthPixels;

    public PlayboardRenderer(Playboard board, float logicalDensity, int widthPixels, boolean hintHighlight) {
        this.dpi = Math.round(160F * logicalDensity);
        this.widthPixels = widthPixels;
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
        float retValue;
        LOG.info("Board "+board.getBoxes().length+" widthPixels "+widthPixels);
        // inches * pixels per inch * units
        if( .75 * dpi * board.getBoxes().length < widthPixels){
            retValue = (widthPixels /board.getBoxes().length) / BASE_BOX_SIZE_INCHES;
        } else {
            retValue = 1.5F;
        }
        LOG.warning("getDeviceMaxScale "+retValue);
        return retValue;
    }

    public float getDeviceMinScale(){
        //inches * (pixels / pixels per inch);
        float retValue = (0.20F * (160F / dpi)) / BASE_BOX_SIZE_INCHES;
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

    public Bitmap draw(Word reset) {
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

            int boxSize = (int) (.5 * dpi * scale);

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
                    this.drawBox(canvas, x, y, row, col, boxSize, boxes[col][row], currentWord);
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
        int boxSize = (int) (BASE_BOX_SIZE_INCHES * this.dpi * scale) ;
        Bitmap bitmap = Bitmap.createBitmap(word.length * boxSize, boxSize, Bitmap.Config.RGB_565);
        bitmap.eraseColor(Color.BLACK);

        Canvas canvas = new Canvas(bitmap);

        for (int i = 0; i < word.length; i++) {
            int x = i * boxSize;
            int y = 0;
            this.drawBox(canvas, x, y, word[i].down, word[i].across, boxSize, boxes[i], null);
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
        double newScale = (double) shortDimension / (double) this.board.getBoxes().length / ((double) dpi * (double) BASE_BOX_SIZE_INCHES);
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
        this.scale = 0.5F;

        return scale;
    }

    public float zoomInMax() {
        this.bitmap = null;
        this.scale = getDeviceMaxScale();

        return scale;
    }

    private void drawBox(Canvas canvas, int x, int y, int row, int col, int boxSize, Box box, Word currentWord) {
        int numberTextSize = boxSize / 4;
        int letterTextSize = Math.round(boxSize * 0.7F);

        // scale paints
        numberText.setTextSize(numberTextSize);
        letterText.setTextSize(letterTextSize);
        red.setTextSize(letterTextSize);
        white.setTextSize(letterTextSize);

        boolean inCurrentWord = (currentWord != null) && currentWord.checkInWord(col, row);
        Position highlight = this.board.getHighlightLetter();

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
            } else if (this.hintHighlight && box.isCheated()) {
                canvas.drawRect(r, this.cheated);
            } else if (this.board.isShowErrors() && (box.getResponse() != ' ') &&
                    (box.getSolution() != box.getResponse())) {
                box.setCheated(true);
                canvas.drawRect(r, this.red);
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
                if(box.getResponse() != ' '){
                    box.setCheated(true);
                }
                if ((highlight.across == col) && (highlight.down == row)) {
                    thisLetter = this.white;
                } else {
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
