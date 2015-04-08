package com.totsp.crossword.io;

import com.totsp.crossword.puz.Box;
import com.totsp.crossword.puz.Puzzle;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Converts a puzzle from the XML format used by uclick syndicated puzzles
 * to the Across Lite .puz format.  The format is:
 * 
 * <crossword>
 *   <Title v="[Title]" />
 *   <Author v="[Author]" />
 *   <Width v="[Width]" />
 *   <Height v="[Height]" />
 *   <AllAnswer v="[Grid]" />
 *   <across>
 *   	<a[i] a="[Answer]" c="[Clue]" n="[GridIndex]" cn="[ClueNumber]" />
 *   </across>
 *   <down>
 *   	<d[j] ... />
 *   </down>
 * </crossword>
 * 
 * [Grid] contains all of the letters in the solution, reading left-to-right,
 * top-to-bottom, with - for black squares. [i] is an incrementing number for
 * each across clue, starting at 1. [GridIndex] is the offset into [Grid] at
 * which the clue starts.  [Clue] text is HTML escaped.
 */
public class UclickXMLIO {
	private static String CHARSET_NAME = "utf8";

	private static class UclickXMLParser extends DefaultHandler {
		private Puzzle puz;
		private Map<Integer, String> acrossNumToClueMap = new HashMap<Integer, String>();
		private Map<Integer, String> downNumToClueMap = new HashMap<Integer, String>();
		private boolean inAcross = false;
		private boolean inDown = false;
		private int maxClueNum = -1;
		
		public UclickXMLParser(Puzzle puz) {
			this.puz = puz;
		}
		
		@Override
		public void startElement(String nsURI, String strippedName,
				String tagName, Attributes attributes) throws SAXException {
			strippedName = strippedName.trim();
			String name = strippedName.length() == 0 ? tagName.trim() : strippedName;
			//System.out.println("Start" + name);
			if (inAcross) {
				int clueNum = Integer.parseInt(attributes.getValue("cn"));
				if (clueNum > maxClueNum) {
					maxClueNum = clueNum;
				}
				try {
					acrossNumToClueMap.put(clueNum, URLDecoder.decode(attributes.getValue("c"), CHARSET_NAME));
				} catch (UnsupportedEncodingException e) {
					acrossNumToClueMap.put(clueNum, attributes.getValue("c"));
				}
			} else if (inDown) {
				int clueNum = Integer.parseInt(attributes.getValue("cn"));
				if (clueNum > maxClueNum) {
					maxClueNum = clueNum;
				}
				try {
					downNumToClueMap.put(clueNum, URLDecoder.decode(attributes.getValue("c"), CHARSET_NAME));
				} catch (UnsupportedEncodingException e) {
					downNumToClueMap.put(clueNum, attributes.getValue("c"));
				}
			} else if (name.equalsIgnoreCase("title")) {
				puz.setTitle(attributes.getValue("v"));
			} else if (name.equalsIgnoreCase("author")) {
				puz.setAuthor(attributes.getValue("v"));
			} else if (name.equalsIgnoreCase("width")) {
				puz.setWidth(Integer.parseInt(attributes.getValue("v")));
				System.out.println("Width "+attributes.getValue("v"));
			} else if (name.equalsIgnoreCase("height")) {
				puz.setHeight(Integer.parseInt(attributes.getValue("v")));
			} else if (name.equalsIgnoreCase("allanswer")) {
				String rawGrid = attributes.getValue("v");
				Box[] boxesList = new Box[puz.getHeight()*puz.getWidth()];
				for (int i = 0; i < rawGrid.length(); i++) {
					char sol = rawGrid.charAt(i);
					if (sol != '-') {
						boxesList[i] = new Box();
						boxesList[i].setSolution(sol);
						boxesList[i].setResponse(' ');
					}
				}
				puz.setBoxesList(boxesList);
				puz.setBoxes(puz.buildBoxes());
			} else if (name.equalsIgnoreCase("across")) {
				inAcross = true;
			} else if (name.equalsIgnoreCase("down")) {
				inDown = true;
			}
		}
		
		@Override
		public void endElement(String nsURI, String strippedName,
				String tagName) throws SAXException {
			//System.out.println("EndElement " +nsURI+" : "+tagName);
			strippedName = strippedName.trim();
			String name = strippedName.length() == 0 ? tagName.trim() : strippedName;
			//System.out.println("End : "+name);
			
			if (name.equalsIgnoreCase("across")) {
				inAcross = false;
			} else if (name.equalsIgnoreCase("down")) {
				inDown = false;
			} else if (name.equalsIgnoreCase("crossword")) {
				int numberOfClues = acrossNumToClueMap.size() + downNumToClueMap.size();
				puz.setNumberOfClues(numberOfClues);
				String[] rawClues = new String[numberOfClues];
				int i = 0;
				for(int clueNum = 1; clueNum <= maxClueNum; clueNum++) {
					if(acrossNumToClueMap.containsKey(clueNum)) {
						rawClues[i] = acrossNumToClueMap.get(clueNum);
						i++;
					}
					if(downNumToClueMap.containsKey(clueNum)) {
						rawClues[i] = downNumToClueMap.get(clueNum);
						i++;
					}
				}
				puz.setRawClues(rawClues);
			}
		}
	}
	
	public static boolean convertUclickPuzzle(InputStream is, DataOutputStream os,
			String copyright, Date d) {
		Puzzle puz = new Puzzle();
		puz.setDate(d);
		puz.setCopyright(copyright);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			//parser.setProperty("http://xml.org/sax/features/validation", false);
			XMLReader xr = parser.getXMLReader();
			xr.setContentHandler(new UclickXMLParser(puz));
			xr.parse(new InputSource(is));

	        puz.setVersion(IO.VERSION_STRING);
	        puz.setNotes("");

			IO.saveNative(puz, os);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Unable to parse XML file: " + e.getMessage());
			return false;
		}
	}
}
