package com.totsp.crossword.puz;

import com.totsp.crossword.puz.Playboard.Position;

import java.io.Serializable;
import java.util.Date;


public class PuzzleMeta implements Serializable {
	
	public String author;
	public String title;
	public String source;
	public Date date;
	public int percentComplete;
	public boolean updateable;
	public String sourceUrl;
	public Position position;
	public boolean across;
	
	
	public String toString(){
		return new StringBuilder("author: ")
		.append(author)
		.append( "title: ")
		.append(title)
		.append(" source: ")
		.append(source)
		.append(" sourceUrl: ")
		.append(sourceUrl)
		.append(" date: " )
		.append(date)
		.append(" percentCompelete: ")
		.append(percentComplete)
		.append(" updateable: ")
		.append(updateable)
		.append(" position: ")
		.append(position)
		.append(" across: ")
		.append(across)
		.toString();
		
	}
	

}