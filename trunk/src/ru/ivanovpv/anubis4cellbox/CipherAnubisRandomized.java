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

/**
 * Randomized Anubis Buffer (RAB) - simple one. For each block (16 bytes - one random number)
 * Also controlled real length of buffer (padding bytes will be stripped)
 */
final public class CipherAnubisRandomized extends Cipher
{
    private R250RNG r;
    private Anubis anubis;
    private static final int BLOCK_SIZE=16; //no more than 16 bytes!
    private static final int KEY_SIZE=40; //320 bits


    public CipherAnubisRandomized(byte[] key)
    {
        r=new R250RNG();
        anubis = new Anubis();
        anubis.keySetup(key);
    }

    public CipherAnubisRandomized(String password)
    {
        r=new R250RNG();
        byte[] digest=generateDigest(password, DIGEST_WHIRLPOOL, KEY_SIZE);
        anubis = new Anubis();
        anubis.keySetup(digest);
    }

    public CipherAnubisRandomized(String password, int type)
    {
        r=new R250RNG();
        byte[] digest=generateDigest(password, type, KEY_SIZE);
        anubis = new Anubis();
        anubis.keySetup(digest);
    }

    private int getRandomPosition()
    {
        return r.r250n(BLOCK_SIZE);
    }

    private byte getRandomVal()
    {
        return (byte )r.r250n(256);
    }


    /**
     * Anubis encryption method. buffer will be rounded/padded in accordance with Anubis block size (16 bytes)
     * @param buffer byte array to be encrypted. Attention! Not guaranteed that array will not be modified
     * @return encrypted byte array
     */
    @Override
    public final byte[] encrypt(byte[] buffer)
    {
        byte[] buf=new byte[BLOCK_SIZE];
        buffer=randomizeBuffer(buffer);
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
        int len=buffer.length-BLOCK_SIZE;
        byte[] buf=new byte[BLOCK_SIZE];
        for(int i=0; i <= len; i+=BLOCK_SIZE)
        {
            System.arraycopy(buffer, i, buf, 0, BLOCK_SIZE);
            anubis.decrypt(buf);
            System.arraycopy(buf, 0, buffer, i, BLOCK_SIZE);
        }
        return deRandomizeBuffer(buffer);
    }

    @Override
    public void clean()
    {
        anubis.clean();
    }

    @Override
    public int getCipherType()
    {
        return 2;
    }


    /**
     * Gets back randomized buffer (RB)
     * @param buffer input byte array
     * @return randomized byte array (always new buffer)
     */
    private byte[] randomizeBuffer(byte[] buffer)
    {
        int length=buffer.length;
        byte[] val=new byte[length/BLOCK_SIZE];//1 random byte for each block
        int seedPosition=this.getRandomPosition(); //position of 1st random value
        int pos=0;
        for(int i=0; i < val.length; i++)
        {
            val[i]=this.getRandomVal();
            pos=pos+(seedPosition+i)%BLOCK_SIZE; //shifting random vals positions
            buffer[pos]^=val[i]; //xoring
        }
        int size=(8+val.length+buffer.length); //real length of RB
        size=BLOCK_SIZE*(size/BLOCK_SIZE) + BLOCK_SIZE*((size%BLOCK_SIZE==0)?0:1); //rounded buffer size including padding bytes
        byte[] buf=new byte[size];
        //length of buffer
        byte[] buftmp= ByteUtils.intToByteArray(length);
        System.arraycopy(buftmp, 0, buf, 0, buftmp.length);
        //seed position
        buftmp=ByteUtils.intToByteArray(seedPosition);
        System.arraycopy(buftmp, 0, buf, 4, buftmp.length);
        //array of vals
        System.arraycopy(val, 0, buf, 8, val.length);
        //buffer
        System.arraycopy(buffer, 0, buf, 8+val.length, buffer.length);
        for(int i=8+val.length+buffer.length; i < size; i++)
        {
            buf[i]=this.getRandomVal();
            break; //randomize only 1 byte of paddding
        }
        return buf;
    }

    /**
     * Gets back derandomized buffer (DRB)
     * @param buffer input byte array
     * @return randomized byte array
     */
    private byte[] deRandomizeBuffer(byte[] buffer)
    {
        int length=ByteUtils.byteArrayToInt(buffer, 0); //real length of buffer
        if(length < 0 || length > 1024*1024) //can't be more than 1 megs or less than zero
            throw new RuntimeException("Error derandomizing decryption buffer");
        byte[] buf=new byte[length];
        int seedPosition=ByteUtils.byteArrayToInt(buffer, 4); //random value position in 1st block
        byte[] val=new byte[length/BLOCK_SIZE];
        int pos=0;
        System.arraycopy(buffer, 8, val, 0, val.length); //getting random vals
        System.arraycopy(buffer, 8+val.length, buf, 0, length); //getting actual buffer and cutting padding bytes
        //derandomize buffer
        for(int i=0; i < val.length; i++)
        {
            pos=pos+(seedPosition+i)%BLOCK_SIZE; //calculate positions in blocks
            buf[pos] ^= val[i];  //xoring
        }
        return buf;
    }

}

