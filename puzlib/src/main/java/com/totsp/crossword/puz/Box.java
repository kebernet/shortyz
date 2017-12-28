package com.totsp.crossword.puz;

import java.io.Serializable;

public class Box implements Serializable {
    private static final char BLANK = ' ';
    private static final int NOCLUE = -1;

    private String responder;
    private boolean across;
    private boolean cheated;
    private boolean down;
    private boolean circled;
    private char response = BLANK;
    private char solution;
    private int clueNumber;
    private int partOfAcrossClueNumber = NOCLUE;
    private int partOfDownClueNumber = NOCLUE;
    private int acrossPosition;
    private int downPosition;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        Box other = (Box) obj;

        if (isAcross() != other.isAcross()) {
        	return false;
        }

        if (isCheated() != other.isCheated()) {
        	return false;
        }

        if (getClueNumber() != other.getClueNumber()) {
        	return false;
        }

        if (isDown() != other.isDown()) {
        	return false;
        }
        
        if (isCircled() != other.isCircled()) {
        	return false;
        }

        if (getResponder() == null) {
            if (other.getResponder() != null) {
                return false;
            }
        } else if (!responder.equals(other.responder)) {
            return false;
        }

        if (getResponse() != other.getResponse()) {
        	return false;
        }

        if (getSolution() != other.getSolution()) {
            return false;
        }

        if (getPartOfAcrossClueNumber() != other.getPartOfAcrossClueNumber()) {
            return false;
        }

        if (getPartOfDownClueNumber() != other.getPartOfDownClueNumber()) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (isAcross() ? 1231 : 1237);
        result = (prime * result) + (isCheated() ? 1231 : 1237);
        result = (prime * result) + getClueNumber();
        result = (prime * result) + (isDown() ? 1231 : 1237);
        result = (prime * result) + (isCircled() ? 1231 : 1237);
        result = (prime * result) +
            ((getResponder() == null) ? 0 : getResponder().hashCode());
        result = (prime * result) + getResponse();
        result = (prime * result) + getSolution();
        result = (prime * result) + getPartOfAcrossClueNumber();
        result = (prime * result) + getPartOfDownClueNumber();

        return result;
    }

    @Override
    public String toString() {
        return this.getClueNumber() + this.getSolution() + " ";
    }

    /**
     * @param responder the responder to set
     */
    public void setResponder(String responder) {
        this.responder = responder;
    }

    /**
     * @return the across
     */
    public boolean isAcross() {
        return across;
    }

    /**
     * @param across the across to set
     */
    public void setAcross(boolean across) {
        this.across = across;
    }

    /**
     * @return the cheated
     */
    public boolean isCheated() {
        return cheated;
    }

    /**
     * @param cheated the cheated to set
     */
    public void setCheated(boolean cheated) {
        this.cheated = cheated;
    }

    /**
     * @return the down
     */
    public boolean isDown() {
        return down;
    }

    /**
     * @param down the down to set
     */
    public void setDown(boolean down) {
        this.down = down;
    }
    
    /**
     * @return if the box is circled
     */
    public boolean isCircled() {
    	return circled;
    }
    
    /**
     * @param circled the circled to set
     */
    public void setCircled(boolean circled) {
    	this.circled = circled;
    }

    /**
     * @return the response
     */
    public char getResponse() {
        return response;
    }

    /**
     * @param response the response to set
     */
    public void setResponse(char response) {
        this.response = response;
    }

    /**
     * @return the solution
     */
    public char getSolution() {
        return solution;
    }

    /**
     * @param solution the solution to set
     */
    public void setSolution(char solution) {
        this.solution = solution;
    }

    /**
     * @return the clueNumber
     */
    public int getClueNumber() {
        return clueNumber;
    }

    /**
     * @param clueNumber the clueNumber to set
     */
    public void setClueNumber(int clueNumber) {
        this.clueNumber = clueNumber;
    }

    /**
     * @return the responder
     */
    public String getResponder() {
        return responder;
    }

    /**
     * @return if the current box is blank
     */
    public boolean isBlank() { return getResponse() == BLANK; }

    /**
     * @param clueNumber across clue that box is a part of
     */
    public void setPartOfAcrossClueNumber(int clueNumber) {
        this.partOfAcrossClueNumber = clueNumber;
    }

    /**
     * @returns across clue that box is a part of (if isPartOfAcross()
     * returns true)
     */
    public int getPartOfAcrossClueNumber() {
        return partOfAcrossClueNumber;
    }

    /**
     * @returns true if box is part of across clue
     */
    public boolean isPartOfAcross() {
        return partOfAcrossClueNumber != NOCLUE;
    }

    /**
     * @param clueNumber down clue that box is a part of
     */
    public void setPartOfDownClueNumber(int clueNumber) {
        this.partOfDownClueNumber = clueNumber;
    }

    /**
     * @returns down clue that box is a part of (if isPartOfDown()
     * returns true)
     */
    public int getPartOfDownClueNumber() {
        return partOfDownClueNumber;
    }

    /**
     * @returns true if box is part of down clue
     */
    public boolean isPartOfDown() {
        return partOfDownClueNumber != NOCLUE;
    }

    /**
     * @param position if part of an across clue, the position in the
     * across word
     */
    public void setAcrossPosition(int position) {
        this.acrossPosition = position;
    }

    /**
     * @return position in the across word if isPartOfAcross returns
     * true
     */
    public int getAcrossPosition() {
        return acrossPosition;
    }

    /**
     * @param position if part of a down clue, the position in the
     * down word
     */
    public void setDownPosition(int position) {
        this.downPosition = position;
    }

    /**
     * @return position in the down word if isPartOfDown returns
     * true
     */
    public int getDownPosition() {
        return downPosition;
    }
}
