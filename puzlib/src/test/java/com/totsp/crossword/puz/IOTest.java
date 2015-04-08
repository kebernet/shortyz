/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.puz;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

import junit.framework.TestCase;

import com.totsp.crossword.io.IO;
import com.totsp.crossword.puz.Playboard.Clue;

/**
 *
 * @author kebernet
 */
public class IOTest extends TestCase {
    
    public IOTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of load method, of class IO.
     */
    public void testLoad() throws Exception {
        Puzzle puz = IO.loadNative(new DataInputStream(IOTest.class.getResourceAsStream("/test.puz")));
        System.out.println("Loaded.");
        Box[][] boxes = puz.getBoxes();
        for(int x=0; x<boxes.length; x++){
            for(int y=0; y<boxes[x].length; y++){
                System.out.print( boxes[x][y]  == null ? "_ " : boxes[x][y].getSolution() +" ");
            }
            System.out.println();
        }
        System.out.println("One across: "+ puz.findAcrossClue(1));
        System.out.println("14  across: "+ puz.findAcrossClue(14));
        System.out.println("18  down  : "+ puz.findDownClue(18));
        System.out.println("2 down: "+puz.findDownClue(2));
    }
    
    public void testSave() throws Exception {
    	Puzzle puz = IO.loadNative(new DataInputStream(IOTest.class.getResourceAsStream("/test.puz")));
        System.out.println("Loaded.");
        File tmp = File.createTempFile("test", ".puz");
        tmp.deleteOnExit();
        IO.saveNative(puz, new DataOutputStream(new FileOutputStream(tmp)));
        
        Puzzle puz2 = IO.loadNative(new DataInputStream(new FileInputStream(tmp)));
//        System.out.println(puz.acrossClues[puz2.acrossClues.length -1 ]+" \n"+puz2.acrossClues[puz2.acrossClues.length -1 ]);
//        System.out.println(puz.acrossClues.length +" == "+puz2.acrossClues.length);
//        System.out.println(Arrays.equals(puz.acrossClues, puz2.acrossClues));
        Box[][] b1 = puz.getBoxes();
        Box[][] b2 = puz2.getBoxes();
        
        for(int x=0; x < b1.length; x++ ){
        	for(int y=0; y<b1[x].length; y++){
        		System.out.println(b1[x][y] +" == "+ b2[x][y] );
        	}
        }
        
        assertEquals(puz, puz2);
        
        Puzzle p = IO.load(tmp);
        p.setDate(new Date());
        p.setSource("Unit Test");
        
        IO.save(p, tmp);
        
        PuzzleMeta m = IO.meta(tmp);
        
        System.out.println(m.title +"\n"+m.source+"\n"+m.percentComplete);
        
        File metaFile = new File(tmp.getParentFile(), tmp.getName().substring(0, tmp.getName().lastIndexOf(".")) + ".shortyz");
        metaFile.delete();
    }
    
    public void testGext() throws Exception{
    	System.out.println("GEXT Test --------------------------");
    	
    	Puzzle puz = IO.loadNative(new DataInputStream(IOTest.class.getResourceAsStream("/2010-7-4-LosAngelesTimes.puz")));
        File tmp = File.createTempFile("test", ".puz");
        tmp.deleteOnExit();
        IO.saveNative(puz, new DataOutputStream(new FileOutputStream(tmp)));
        puz = IO.load(tmp);
    	assertTrue(puz.getGEXT());
    	assertTrue(puz.getBoxes()[2][2].isCircled());
    	
    }
    
    public void testCrack() throws Exception {
    	System.out.println("testCrack");
    	Puzzle p = IO.loadNative(new DataInputStream(IOTest.class.getResourceAsStream("/puz_110523margulies.puz")));
    	{
    		Playboard board = new Playboard(p);
	    	for(Clue c : board.getAcrossClues()){
	    		for(Box box : board.getWordBoxes(c.number, true)){
	    			System.out.print(box.getSolution());
	    		}
	    		System.out.println();
	    	}
    	}
    	System.out.println("========================");
    	
    	long incept = System.currentTimeMillis();
    	boolean b = IO.crack(p);
    	System.out.println(b + " "+(System.currentTimeMillis() - incept));
    	Playboard board = new Playboard(p);
    	for(Clue c : board.getAcrossClues()){
    		for(Box box : board.getWordBoxes(c.number, true)){
    			System.out.print(box.getSolution());
    		}
    		System.out.println();
    	}
    	System.out.println(b + " "+(System.currentTimeMillis() - incept));
    	
    }
    
    /**
     * Note: This is a sanity check, but any changes to unlock functionality should be tested more extensively.
     */
    public void testUnlockCode() throws Exception {
    	Puzzle puz = IO.loadNative(new DataInputStream(IOTest.class.getResourceAsStream("/2010-7-19-NewYorkTimes.puz")));
    	for(Box b :  puz.getBoxesList()){
    		if(b != null)
    		System.out.print(b.getSolution()+" ");
    	}
    	System.out.println();
    	try{
    	assertTrue(IO.tryUnscramble(puz, 2465, puz.initializeUnscrambleData()));
    	for(Box b :  puz.getBoxesList()){
    		if(b != null)
    		System.out.print(b.getSolution()+" ");
    	}
    	System.out.println();
        ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream());
        oos.writeObject(puz);
        oos.close();
    	} catch(Exception e){
    		e.printStackTrace();
    	}
    }

    
}
