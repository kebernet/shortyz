package com.totsp.crossword.io;


/**
 * Implementation of the Tiny Encryption Algorithm (TEA).
 * The Tiny Encryption Algorithm is one of the fastest and most efficient
 * cryptographic algorithms in existence. It was developed by David Wheeler and
 * Roger Needham at the Computer Laboratory of Cambridge University.
 *
 * See http://www.cl.cam.ac.uk/ftp/users/djw3/tea.ps
 *
 * This software was written to provide simple encryption for J2ME.
 * The homepage for this software is http://winterwell.com/software/TEA.php
 *
 * (c) 2008 Joe Halliwell <joe.halliwell@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

public class TEA {
    private final static int SUGAR = 0x9E3779B9;
    private final static int CUPS  = 32;
    private final static int UNSUGAR = 0xC6EF3720;

    private int[] S = new int[4];

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    //0a1634be95150fec57d638d6d93549320c71892252f93c9423df7898e1269b344ab893ed3a07c797bd3fb722324e90e601d97309ed87a6e2461bc878425471e0e258a0ebf5f96076be2e9f2d88ab5fa454220836be9e7fa733dac92603ab2cbf
    //sjri5jf85jdor560
//
//    public static void main(String... args) throws Exception {
//        System.out.println("sjri5jf85jdor560".length());
//        TEA tea = new TEA("sjri5jf85jdor560".getBytes(StandardCharsets.UTF_16));
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        IOUtils.copy(new FileInputStream("/Users/rcooper/Downloads/cs150608.jpz"), baos);
//        System.out.println(baos.toByteArray().length);
//        byte[] result = tea.decrypt(baos.toByteArray());
//        System.out.println(new String(result));
//    }

    /**
     * Initialize the cipher for encryption or decryption.
     * @param key a 16 byte (128-bit) key
     */
    public TEA(byte[] key) {
        if (key == null)
            throw new RuntimeException("Invalid key: Key was null");
        if (key.length < 16)
            throw new RuntimeException("Invalid key: Length was less than 16 bytes");
        for (int off=0, i=0; i<4; i++) {
            S[i] = ((key[off++] & 0xff)) |
                    ((key[off++] & 0xff) <<  8) |
                    ((key[off++] & 0xff) << 16) |
                    ((key[off++] & 0xff) << 24);
        }
    }

    /**
     * Encrypt an array of bytes.
     * @param clear the cleartext to encrypt
     * @return the encrypted text
     */
    public byte[] encrypt(byte[] clear) {
        int paddedSize = ((clear.length/8) + (((clear.length%8)==0)?0:1)) * 2;
        int[] buffer = new int[paddedSize + 1];
        buffer[0] = clear.length;
        pack(clear, buffer, 1);
        brew(buffer);
        return unpack(buffer, 0, buffer.length * 4);
    }

    /**
     * Decrypt an array of bytes.
     * @param crypt the cipher text to decrypt
     * @return the decrypted text
     */
    public byte[] decrypt(byte[] crypt) {
//        assert crypt.length % 4 == 0;
//        assert (crypt.length / 4) % 2 == 1;
        int[] buffer = new int[crypt.length / 4];
        pack(crypt, buffer, 0);
        unbrew(buffer);
        return unpack(buffer, 1, buffer[0]);
    }

    void brew(int[] buf) {
        //assert buf.length % 2 == 1;
        int i, v0, v1, sum, n;
        i = 1;
        while (i<buf.length) {
            n = CUPS;
            v0 = buf[i];
            v1 = buf[i+1];
            sum = 0;
            while (n-->0) {
                sum += SUGAR;
                v0  += ((v1 << 4 ) + S[0] ^ v1) + (sum ^ (v1 >>> 5)) + S[1];
                v1  += ((v0 << 4 ) + S[2] ^ v0) + (sum ^ (v0 >>> 5)) + S[3];
            }
            buf[i] = v0;
            buf[i+1] = v1;
            i+=2;
        }
    }

    void unbrew(int[] buf) {
        //assert buf.length % 2 == 1;
        int i, v0, v1, sum, n;
        i = 1;
        while (i<buf.length) {
            n = CUPS;
            v0 = buf[i];
            v1 = buf[i+1];
            sum = UNSUGAR;
            while (n--> 0) {
                v1  -= ((v0 << 4 ) + S[2] ^ v0) + (sum ^ (v0 >>> 5)) + S[3];
                v0  -= ((v1 << 4 ) + S[0] ^ v1) + (sum ^ (v1 >>> 5)) + S[1];
                sum -= SUGAR;
            }
            buf[i] = v0;
            buf[i+1] = v1;
            i+=2;
        }
    }

    void pack(byte[] src, int[] dest, int destOffset) {
//        assert destOffset + (src.length / 4) <= dest.length;
        int i = 0, shift = 24;
        int j = destOffset;
        dest[j] = 0;
        while (i<src.length) {
            dest[j] |= ((src[i] & 0xff) << shift);
            if (shift==0) {
                shift = 24;
                j++;
                if (j<dest.length) dest[j] = 0;
            }
            else {
                shift -= 8;
            }
            i++;
        }
    }

    byte[] unpack(int[] src, int srcOffset, int destLength) {
//        assert destLength <= (src.length - srcOffset) * 4;
        byte[] dest = new byte[destLength];
        int i = srcOffset;
        int count = 0;
        for (int j = 0; j < destLength; j++) {
            dest[j] = (byte) ((src[i] >> (24 - (8*count))) & 0xff);
            count++;
            if (count == 4) {
                count = 0;
                i++;
            }
        }
        return dest;
    }

    /* Simple usage example */
    public static String quote = "Now rise, and show your strength. Be eloquent, and deep, and tender; see, with a clear eye, into Nature, and into life:  spread your white wings of quivering thought, and soar, a god-like spirit, over the whirling world beneath you, up through long lanes of flaming stars to the gates of eternity!";

//    public static void main(String[] args) {
//		/* Create a cipher using the first 16 bytes of the passphrase */
//        TEA tea = new TEA("And is there honey still for tea?".getBytes());
//
//        byte[] original = quote.getBytes();
//
//		/* Run it through the cipher... and back */
//        byte[] crypt = tea.encrypt(original);
//        byte[] result = tea.decrypt(crypt);
//
//		/* Ensure that all went well */
//        String test = new String(result);
//        if (!test.equals(quote))
//            throw new RuntimeException("Fail");
//    }
}
