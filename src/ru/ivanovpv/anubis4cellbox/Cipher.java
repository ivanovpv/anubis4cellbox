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
 * Abstract wrapper over Anubis cipher
 */
public abstract class Cipher
{
    private int digestType;
    public final static int DIGEST_SHA1=0;
    public final static int DIGEST_WHIRLPOOL=1;

    public abstract byte[] encrypt(byte[] buffer);
    public abstract byte[] decrypt(byte[] buffer);
    public abstract void clean();
    public abstract int getCipherType();

    protected Cipher()
    {
    }

    /**
     * Generates digest of supplied String based on selected algorythm
     * @param password  password used to generate key
     * @param digestType - selected keygen algorythm either SHA1 or WHIRLPOOL
     * @param keySize - key length - has to be less than 512 bits
     * @return byte array containing generated digest
     */
    protected final byte[] generateDigest(String password, int digestType, int keySize)
    {
        this.digestType=digestType;
        switch(this.digestType)
        {
            case DIGEST_WHIRLPOOL:
                Whirlpool w = new Whirlpool();
                byte[] digest = new byte[Whirlpool.DIGESTBYTES];
                w.NESSIEinit();
                w.NESSIEadd(password);
                w.NESSIEfinalize(digest);
                byte[] cut_digest=new byte[keySize];
                System.arraycopy(digest, 0, cut_digest, 0, keySize);
                return cut_digest;
            case DIGEST_SHA1:
            default: //by default generate SHA1
                SHA1 kg=new SHA1(keySize);//SHA keygen
                byte[] buf= ByteUtils.stringToByteArray(password);
                kg.update(buf, 0, buf.length);
                kg.generate();
                return kg.getDigest();
        }
    }

    public int getDigestType()
    {
        return this.digestType;
    }
}
