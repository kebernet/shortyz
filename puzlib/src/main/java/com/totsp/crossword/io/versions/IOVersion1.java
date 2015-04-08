package com.totsp.crossword.io.versions;

import com.totsp.crossword.io.IO;
import com.totsp.crossword.puz.Box;
import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.puz.PuzzleMeta;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

public class IOVersion1 implements IOVersion {

	public void read(Puzzle puz, DataInputStream dis) throws IOException {
		PuzzleMeta meta = readMeta(dis);
		applyMeta(puz, meta);
		Box[][] boxes = puz.getBoxes();
		for(Box[] row : boxes ){
			for(Box b : row){
				if(b == null){
					continue;
				}
				b.setCheated(dis.readBoolean());
				b.setResponder(IO.readNullTerminatedString(dis));
			}
		}
		try{
			puz.setTime(dis.readLong());
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
		
		
	}
	
	protected void applyMeta(Puzzle puz, PuzzleMeta meta){
		//System.out.println("Applying V1 Meta");
		puz.setSource(meta.source);
		puz.setDate(meta.date);
	}

	public PuzzleMeta readMeta(DataInputStream dis) throws IOException {
		//System.out.println("Read V1");
		PuzzleMeta meta = new PuzzleMeta();
		meta.author = IO.readNullTerminatedString(dis);
		meta.source = IO.readNullTerminatedString(dis);
		meta.title = IO.readNullTerminatedString(dis);
		meta.date = new Date( dis.readLong() );
		meta.percentComplete = dis.readInt();
		return meta;
		
	}

	public void write(Puzzle puz, DataOutputStream dos) throws IOException {
		IO.writeNullTerminatedString(dos, puz.getAuthor());
		IO.writeNullTerminatedString(dos, puz.getSource());
		IO.writeNullTerminatedString(dos, puz.getTitle());
		dos.writeLong(puz.getDate() == null ? 0 : puz.getDate().getTime());
		dos.writeInt(puz.getPercentComplete());
		//System.out.println("Meta written.");
		Box[][] boxes = puz.getBoxes();
		for(Box[] row : boxes ){
			for(Box b : row){
				if(b == null){
					continue;
				}
				dos.writeBoolean(b.isCheated());
				IO.writeNullTerminatedString(dos, b.getResponder());
			}
		}
		dos.writeLong(puz.getTime());
	}

	
}
