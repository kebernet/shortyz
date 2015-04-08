package com.totsp.crossword.puz;

import java.io.Serializable;

public class Box implements Serializable {
    private String responder;
    private boolean across;
    private boolean cheated;
    private boolean down;
    private boolean circled;
    private char response = ' ';
    private char solution;
    private int clueNumber;

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
        	System.out.println("across");
            return false;
        }

        if (isCheated() != other.isCheated()) {
        	System.out.println("cheated");
            return false;
        }

        if (getClueNumber() != other.getClueNumber()) {
        	System.out.println("clueNumber");
            return false;
        }

        if (isDown() != other.isDown()) {
        	System.out.println("down");
            return false;
        }
        
        if (isCircled() != other.isCircled()) {
        	System.out.println("circled");
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
        	System.out.println("response");
            return false;
        }

        if (getSolution() != other.getSolution()) {
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
}
