package com.totsp.crossword.io;

import com.totsp.crossword.puz.Box;
import com.totsp.crossword.puz.Puzzle;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: keber_000
 * Date: 2/9/14
 * Time: 5:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class BrainsOnlyIO {


    public static boolean convertBrainsOnly(InputStream is, DataOutputStream os, Date date){
        try {
            Puzzle puz = parse(is);
            puz.setDate(date);
            System.out.println("PARSED PUZZLE " + puz.getTitle());
            IO.saveNative(puz, os);
        } catch (IOException e) {
            System.err.println("Unable to dump puzzle to output stream.");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static Puzzle parse(InputStream is) throws IOException {
        Puzzle puz = new Puzzle();

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String title = readLineAtOffset(reader, 4);
        System.out.println("Title line: "+title);
        int startIndex = title.indexOf(" ") + 1;
        puz.setTitle(title.substring(startIndex >= 0 ? startIndex : 0));
        puz.setAuthor(readLineAtOffset(reader, 1));

        int width = Integer.parseInt(readLineAtOffset(reader, 1));
        int height = Integer.parseInt(readLineAtOffset(reader, 1));
        puz.setWidth(width);
        puz.setHeight(height);
        readLineAtOffset(reader, 4);
        Box[][] boxes = new Box[height][width];
        for(int down = 0; down < height; down++){
            String line = readLineAtOffset(reader, 0);
            //System.out.println("line: "+line);
            for(int across = 0; across < width; across++){
                char c = line.charAt(across);
                if(c == '#'){
                    continue;
                }
                Box b = new Box();
                b.setSolution(c);
                boxes[down][across] = b;
            }
        }
        puz.setBoxes(boxes);
        readLineAtOffset(reader, 0);
        ArrayList<String> acrossClues = new ArrayList<String>();
        for(String clue = readLineAtOffset(reader, 0); !"".equals(clue); clue = readLineAtOffset(reader, 0)){
            acrossClues.add(clue);
        }
        puz.setAcrossClues(acrossClues.toArray(new String[acrossClues.size()]));

        ArrayList<String> downClues = new ArrayList<String>();
        for(String clue = readLineAtOffset(reader, 0); !"".equals(clue); clue = readLineAtOffset(reader, 0)){
            downClues.add(clue);
        }
        puz.setDownClues(downClues.toArray(new String[downClues.size()]));


        ArrayList<Integer> acrossLookups = new ArrayList<Integer>();


        for(int h = 0; h < height; h++){
            for(int w = 0; w < width; w++){
                if(boxes[h][w] != null && boxes[h][w].getClueNumber() > 0 && boxes[h][w].isAcross()){
                    System.out.println("across index "+acrossLookups.size()+" clue value "+boxes[h][w].getClueNumber());
                    acrossLookups.add(boxes[h][w].getClueNumber());
                }
            }
        }
        puz.setAcrossCluesLookup(acrossLookups.toArray(new Integer[acrossLookups.size()]));

        ArrayList<Integer> downLookups = new ArrayList<Integer>();
        for(int h = 0; h < boxes.length; h++){
            for(int w = 0; w < width; w++){
                if(boxes[h][w] != null && boxes[h][w].getClueNumber() > 0 && boxes[h][w].isDown()){
                    downLookups.add(boxes[h][w].getClueNumber());
                }
            }
        }
        puz.setDownCluesLookup(downLookups.toArray(new Integer[downLookups.size()]));


        ArrayList<String> rawClues = new ArrayList<String>();
        for(int h = 0; h < height; h++){
            for(int w = 0; w < width; w++){
                if(boxes[h][w] != null && boxes[h][w].getClueNumber() > 0){
                    if(boxes[h][w].isAcross()){
                        rawClues.add(puz.findAcrossClue(boxes[h][w].getClueNumber()));
                    }
                    if(boxes[h][w].isDown()){
                        rawClues.add(puz.findDownClue(boxes[h][w].getClueNumber()));
                    }
                }
            }
        }
        puz.setRawClues(rawClues.toArray(new String[rawClues.size()]));
        puz.setNumberOfClues(rawClues.size());

        puz.setVersion(IO.VERSION_STRING);
        puz.setNotes("");
        return puz;
    }

    private static String readLineAtOffset(BufferedReader reader, int offset) throws IOException {
        String read = null;
        for(int i=0; i <= offset; i++){
            read = reader.readLine();
            if(read.endsWith("\r")){
                i++;
            }
        }
        if(read == null){
            throw new EOFException("End of line");
        }
        return read.trim();
    }
}
