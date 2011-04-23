/*
 * Copyright (c) 2011 Pavel Ivanov (ivanovpv@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. */

package ru.ivanovpv.anubis4cellbox;

import java.io.*;
import java.util.Random;

/**
 * Standard wrapper around Anubis. As-is, just rounds buffer to blocks limits,
 * During decryption doesn't stripe padding bytes
 */

public class CipherAnubis extends Cipher
{
    private Anubis anubis;
    private static final byte PADDING_BYTE=0; //doesn't really matter
    private static final int BLOCK_SIZE=16; //no more than 16 bytes!
    private static final int KEY_SIZE=40; //320 bits

    /**
     * Test only constructor
     * @param key test key
     */
    public CipherAnubis(byte[] key)
    {
        anubis = new Anubis();
        anubis.keySetup(key);
    }


    /**
     * Default constructor uses SHA1 keygen procedure for backward compatibility
     * @param password password used to generate key
     */
    public CipherAnubis(String password)
    {
        byte[] digest=generateDigest(password, DIGEST_WHIRLPOOL, KEY_SIZE);
        anubis = new Anubis();
        anubis.keySetup(digest);
    }

    /**
     * Constructor with selectable keygen procedure
     * @param password password used to generate key
     * @param type - selected keygen algorythm either SHA1 or WHIRLPOOL
     */
    public CipherAnubis(String password, int type)
    {
        byte[] digest=generateDigest(password, type, KEY_SIZE);
        anubis = new Anubis();
        anubis.keySetup(digest);
    }


    /**
     * Anubis encryption method. buffer will be rounded/padded in accordance with Anubis block size (16 bytes)
     * @param buffer byte array to be encrypted. Attention! Returned buffer is always new, input buffer unmodified
     * @return encrypted byte array
     */
    @Override
    public final byte[] encrypt(byte[] buffer)
    {
        byte[] buf=new byte[BLOCK_SIZE];
        buffer=roundBuffer(buffer);
        for(int i=0; i < buffer.length; i+=BLOCK_SIZE)
        {
            System.arraycopy(buffer, i, buf, 0, BLOCK_SIZE);
            anubis.encrypt(buf);
            System.arraycopy(buf, 0, buffer, i, BLOCK_SIZE);
        }
        return buffer;
    }

    /**
     * Anubis decryption method
     * Method doesn't check non-zero renundancy for Anubis block size (16 bytes). E.g. if buffer length will be 22 bytes
     * then only 16 bytes will be decrypted, rest of array will remain untouched
     * @param buffer byte array to be decrypted. Array content will be modified.
     * @return  decrypted byte array
     */
    @Override
    public final byte[] decrypt(byte[] buffer)
    {
        //int len=buffer.length-BLOCK_SIZE;
        byte[] buf=new byte[BLOCK_SIZE];
        for(int i=0; i < buffer.length; i+=BLOCK_SIZE)
        {
            System.arraycopy(buffer, i, buf, 0, BLOCK_SIZE);
            anubis.decrypt(buf);
            System.arraycopy(buf, 0, buffer, i, BLOCK_SIZE);
        }
        return buffer;
    }

    @Override
    public void clean()
    {
        anubis.clean();
    }

    @Override
    public int getCipherType()
    {
        return 1;
    }


    public static void testStandard320()
    {
        String s;
        byte[] key=new byte[40];
        key[0]=(byte )32;
        for(int i=1; i < key.length; i++)
            key[i]=0;
        System.out.println("key: "+ Anubis.display(key));
        Cipher aw=new CipherAnubis(key);
        byte[] ebuf, dbuf, buffer=new byte[30];
        for(int i=0; i < buffer.length; i++)
            buffer[i]=0;
        s=ByteUtils.bytesToHex(buffer);
        System.out.println("Buffer: "+s);
        long start=System.currentTimeMillis();
        ebuf=aw.encrypt(buffer);
        long end=System.currentTimeMillis();
        s= ByteUtils.bytesToHex(ebuf);
        System.out.println("Cipher: "+s);
        System.out.println(" - encrypted in "+(end-start)+" ms");
        start=System.currentTimeMillis();
        dbuf=aw.decrypt(ebuf);
        end=System.currentTimeMillis();
        s=ByteUtils.bytesToHex(dbuf);
        System.out.println("Decipher: "+s);
        System.out.println(" - decrypted in "+(end-start)+" ms");
    }
    /**
     * Rounds buffer in accordance with Anubis padding policy (16 bytes)
     * @param buffer byte array to be rounded/padded
     * @return rounded byte array (always new buffer)
     */
    protected final byte[] roundBuffer(byte[] buffer)
    {
        int length=buffer.length;
        int size=BLOCK_SIZE*(length/BLOCK_SIZE) + BLOCK_SIZE*((length%BLOCK_SIZE==0)?0:1); //rounded buffer size including padding bytes
        byte[] newbuf=new byte[size];
        System.arraycopy(buffer, 0, newbuf, 0, length);
        for(int i=length; i < newbuf.length; i++)
           newbuf[i]=PADDING_BYTE;
        return newbuf;
    }


    public final void encrypt(InputStream is, OutputStream os, long size) throws IOException
    {
        long pos=0;
        long bsize;
        byte[] buf, buffer=new byte[BLOCK_SIZE];
        AnubisHeader ah;

        ah=new AnubisHeader(size);
        buf=ah.getHeader();
        os.write(buf);
        do
        {
            bsize=size-pos;
            if(bsize > BLOCK_SIZE)
                bsize=is.read(buffer);
            else
                bsize=is.read(buffer, 0, (int )(size-pos));
            buf=roundBuffer(buffer);
            anubis.encrypt(buf);
            os.write(buf);
            pos+=bsize;
        }
        while(bsize==BLOCK_SIZE && pos < size);
    }

    public final void decrypt(InputStream is, OutputStream os) throws IOException
    {
        long pos=0;
        long rsize, bsize;
        byte[] buffer=new byte[BLOCK_SIZE];
        AnubisHeader ah;
        is.read(buffer, 0, AnubisHeader.HEADER_SIZE);
        ah=new AnubisHeader(buffer);
        long size=ah.getSize();
        do
        {
            rsize=is.read(buffer);
            bsize=size-pos;
            anubis.decrypt(buffer);
            if(bsize <= BLOCK_SIZE)
                os.write(buffer, 0, (int )bsize);
            else
                os.write(buffer);
            pos+=rsize;
        }
        while(rsize==BLOCK_SIZE && pos < size);
    }

    public static void testRNG()
    {
        R250RNG r=new R250RNG();
        Random ru=new Random();
        long start, end;
        start=System.currentTimeMillis();
        for(int i=0; i < 100000000; i++)
        {
            r.r250n(256);
        }
        end=System.currentTimeMillis();
        System.out.println("r250="+(end-start));
        start=System.currentTimeMillis();
        for(int i=0; i < 100000000; i++)
        {
            ru.nextInt(256);
        }
        end=System.currentTimeMillis();
        System.out.println("Standard="+(end-start));
    }

    // runs original 320 bit test vector
    public static void main(String[] args)
    {
        testStandard320();
        //testRNG();
    }
}

