package com.totsp.crossword.io;


import com.totsp.crossword.puz.Box;
import com.totsp.crossword.puz.Puzzle;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Converts a puzzle from the XML format used by JPZ puzzles into the Across
 * Lite .puz format. Strings are HTML formatted, UTF-8. Any unsupported features
 * are either ignored or cause abort. The format is:
 * 
 * <crossword-compiler-applet> ... <rectangular-puzzle
 * xmlns="http://crossword.info/xml/rectangular-puzzle"
 * alphabet="ABCDEFGHIJKLMNOPQRSTUVWXYZ"> <metadata> <title>[Title]</title>
 * <creator>[Author]</creator> <copyright>[Copyright]</copyright>
 * <description>[Notes]</description> </metadata> <crossword> <grid
 * width="[Width]" height="[Height]"> <grid-look numbering-scheme="normal" ...
 * /> <cell x="1" y="1" solution="M" number="1"></cell> ... <cell x="1" y="6"
 * type="block"</cell> ... </grid> ... <clues ordering="normal">
 * <title>...Across...</title> <clue ... number="1">...</clue> ... </clues>
 * <clues ordering="normal"> <title>...Down...</title> <clue ...
 * number="1">...</clue> ... </clues> </crossword> </rectangular-puzzle>
 * </crossword-compiler-applet>
 */
public class JPZIO {
	
	public static int copyStream(InputStream sourceStream,
			OutputStream destinationStream) throws IOException {
		int bytesRead = 0;
		int totalBytes = 0;
		byte[] buffer = new byte[1024];

		while (bytesRead >= 0) {
			bytesRead = sourceStream.read(buffer, 0, buffer.length);

			if (bytesRead > 0) {
				destinationStream.write(buffer, 0, bytesRead);
			}

			totalBytes += bytesRead;
		}

		destinationStream.flush();
		destinationStream.close();

		return totalBytes;
	}

	private static InputStream unzipOrPassthrough(InputStream is)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		copyStream(is, baos);
		try {
			ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(baos.toByteArray()));
			ZipEntry entry = zis.getNextEntry();
			while (entry.isDirectory()) {
				entry = zis.getNextEntry();
			}
			baos = new ByteArrayOutputStream();
			copyStream(zis, baos);
			is = new ByteArrayInputStream(baos.toByteArray());
		} catch (Exception e) {
			System.out.println("Not zipped");
			return new ByteArrayInputStream(baos.toByteArray());
		}

		// replace &nbsp; with space

		Scanner in = new Scanner(is);
		ByteArrayOutputStream replaced = new ByteArrayOutputStream();
		BufferedWriter out = new BufferedWriter(
				new OutputStreamWriter(replaced));
		while (in.hasNextLine()) {
			String line = in.nextLine();
			line = line.replaceAll("&nbsp;", " ");
			out.write(line + "\n");
		}
		out.flush();
		out.close();
		is.close();
		return new ByteArrayInputStream(replaced.toByteArray());

	}

	public static Puzzle readPuzzle(InputStream is) {
		Puzzle puz = new Puzzle();
		SAXParserFactory factory = SAXParserFactory.newInstance();

		try {
			SAXParser parser = factory.newSAXParser();

//			 parser.setProperty("http://xml.org/sax/features/validation",
//			 false);
			XMLReader xr = parser.getXMLReader();
			xr.setContentHandler(new JPZXMLParser(puz));
			xr.parse(new InputSource(unzipOrPassthrough(is)));

			puz.setVersion(IO.VERSION_STRING);

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Unable to parse XML file: " + e.getMessage());
			throw new RuntimeException(e);
		}
		return puz;
	}

	public static boolean convertJPZPuzzle(InputStream is, DataOutputStream os,
			Date d) {

		try {
			Puzzle puz = readPuzzle(is);
			puz.setDate(d);
			puz.setVersion(IO.VERSION_STRING);

			IO.saveNative(puz, os);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Unable to parse XML file: " + e.getMessage());

			return false;
		}
	}

	private static class JPZXMLParser extends DefaultHandler {
		private Map<Integer, String> acrossNumToClueMap = new HashMap<Integer, String>();
		private Map<Integer, String> downNumToClueMap = new HashMap<Integer, String>();
		private Puzzle puz;
		private StringBuilder curBuffer;
		private Box[][] boxes;
		private int[][] clueNums;
		private boolean inAcross = false;
		private boolean inClues = false;
		private boolean inDown = false;
		private boolean inMetadata = false;
		private int clueNumber = 0;
		private int height;
		private int maxClueNum = -1;
		private int width;

		public JPZXMLParser(Puzzle puz) {
			this.puz = puz;
		}

		@Override
		public void characters(char[] ch, int start, int length) {
			if (curBuffer != null) {
				curBuffer.append(ch, start, length);
			}
		}

		@Override
		public void endElement(String nsURI, String strippedName, String tagName)
				throws SAXException {
			strippedName = strippedName.trim();

			String name = (strippedName.length() == 0) ? tagName.trim()
					: strippedName;

			if (name.equalsIgnoreCase("metadata")) {
				inMetadata = false;
			} else if (inMetadata) {
				if (name.equalsIgnoreCase("title")) {
					puz.setTitle(curBuffer.toString());
					curBuffer = null;
				} else if (name.equalsIgnoreCase("creator")) {
					puz.setAuthor(curBuffer.toString());
					curBuffer = null;
				} else if (name.equalsIgnoreCase("copyright")) {
					puz.setCopyright(curBuffer.toString());
					curBuffer = null;
				} else if (name.equalsIgnoreCase("description")) {
					puz.setNotes(curBuffer.toString());
					curBuffer = null;
				}
			} else if (name.equalsIgnoreCase("grid")) {
				puz.setBoxes(boxes);
			} else if (name.equalsIgnoreCase("clues")) {
				inClues = false;
				inAcross = false;
				inDown = false;
			} else if (inClues) {
				if (name.equalsIgnoreCase("title")) {
					String title = curBuffer.toString();

					if (title.contains("Across")) {
						inAcross = true;
					} else if (title.contains("Down")) {
						inDown = true;
					} else {
						throw new SAXException(
								"Clue list is neither across nor down.");
					}

					curBuffer = null;
				} else if (name.equalsIgnoreCase("clue")) {
					if (inAcross) {
						acrossNumToClueMap
								.put(clueNumber, curBuffer.toString());
					} else if (inDown) {
						downNumToClueMap.put(clueNumber, curBuffer.toString());
					} else {
						throw new SAXException("Unexpected end of clue tag.");
					}
				}
			} else if (name.equalsIgnoreCase("crossword")) {
				int numberOfClues = acrossNumToClueMap.size()
						+ downNumToClueMap.size();
				puz.setNumberOfClues(numberOfClues);

				String[] rawClues = new String[numberOfClues];
				int i = 0;

				for (int clueNum = 1; clueNum <= maxClueNum; clueNum++) {
					if (acrossNumToClueMap.containsKey(clueNum)) {
						rawClues[i] = acrossNumToClueMap.get(clueNum);
						i++;
					}

					if (downNumToClueMap.containsKey(clueNum)) {
						rawClues[i] = downNumToClueMap.get(clueNum);
						i++;
					}
				}

				puz.setRawClues(rawClues);

				// verify clue numbers
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						if (clueNums[y][x] != 0) {
							if (puz.getBoxes()[y][x].getClueNumber() != clueNums[y][x]) {
								throw new SAXException(
										"Irregular numbering scheme.");
							}
						}
					}
				}
			}
		}

		@Override
		public void startElement(String nsURI, String strippedName,
				String tagName, Attributes attributes) throws SAXException {
			strippedName = strippedName.trim();

			String name = (strippedName.length() == 0) ? tagName.trim()
					: strippedName;

			if (name.equalsIgnoreCase("metadata")) {
				inMetadata = true;
			} else if (inMetadata) {
				if (name.equalsIgnoreCase("title")) {
					curBuffer = new StringBuilder();
				} else if (name.equalsIgnoreCase("creator")) {
					curBuffer = new StringBuilder();
				} else if (name.equalsIgnoreCase("copyright")) {
					curBuffer = new StringBuilder();
				} else if (name.equalsIgnoreCase("description")) {
					curBuffer = new StringBuilder();
				}
			} else if (name.equalsIgnoreCase("grid")) {
				width = Integer.parseInt(attributes.getValue("width"));
				height = Integer.parseInt(attributes.getValue("height"));
				puz.setWidth(width);
				puz.setHeight(height);
				boxes = new Box[height][width];
				clueNums = new int[height][width];
			} else if (name.equalsIgnoreCase("cell")) {
				int x = Integer.parseInt(attributes.getValue("x")) - 1;
				int y = Integer.parseInt(attributes.getValue("y")) - 1;
				String sol = attributes.getValue("solution");

				if (sol != null) {
					boxes[y][x] = new Box();
					boxes[y][x].setSolution(sol.charAt(0));

					if ("circle".equalsIgnoreCase(attributes
							.getValue("background-shape"))) {
						puz.setGEXT(true);
						boxes[y][x].setCircled(true);
					}

					String number = attributes.getValue("number");

					if (number != null) {
						clueNums[y][x] = Integer.parseInt(number);
					}
				}
			} else if (name.equalsIgnoreCase("clues")) {
				inClues = true;
			} else if (inClues) {
				if (name.equalsIgnoreCase("title")) {
					curBuffer = new StringBuilder();
				} else if (name.equalsIgnoreCase("clue")) {
					clueNumber = Integer
							.parseInt(attributes.getValue("number"));

					if (clueNumber > maxClueNum) {
						maxClueNum = clueNumber;
					}

					curBuffer = new StringBuilder();
				}
			}
		}
	}
}
