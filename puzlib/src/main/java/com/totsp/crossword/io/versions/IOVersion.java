package com.totsp.crossword.io.versions;

import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.puz.PuzzleMeta;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface IOVersion {
	public void write(Puzzle puz, DataOutputStream os) throws IOException;
	public void read(Puzzle puz, DataInputStream is) throws IOException;
	public PuzzleMeta readMeta(DataInputStream is) throws IOException;
}
