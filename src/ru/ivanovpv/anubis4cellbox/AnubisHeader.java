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

public class AnubisHeader
{
    byte[] descriptor={'A', 'N', 'B'};
    byte hiver;
    byte lover;
    long size;
    public static int HEADER_SIZE=16;

    public AnubisHeader(byte hiver, byte lover, long size)
    {
        this.hiver=hiver;
        this.lover=lover;
        this.size=size;
    }

    public AnubisHeader(byte[] buffer)
    {
        if(buffer==null || buffer.length < HEADER_SIZE)
            return;
        this.hiver=buffer[12];
        this.lover=buffer[13];
        size= ByteUtils.byteArrayToLong(buffer, 4);
    }


    public AnubisHeader(long size)
    {
        this.hiver=1;
        this.lover=2;
        this.size=size;
    }

    public byte[] getHeader()
    {
        int i;
        byte[] header, buf;
        buf=ByteUtils.longToByteArray(size);
        header=new byte[16]; //3+8+1+1+padding
        System.arraycopy(descriptor, 0, header, 0, 3);
        System.arraycopy(buf, 0, header, 4, 8);
        header[12]=hiver;
        header[13]=lover;
        for(i=14; i < 16; i++)
            header[i]=0;
        return header;
    }

    public long getSize()
    {
        return this.size;
    }

    public int getHiVersion()
    {
        return hiver;
    }

    public int getLoVersion()
    {
        return lover;
    }

    public String getVersionInfo()
    {
        StringBuilder sb=new StringBuilder().append(hiver).append('.').append(lover);
        return sb.toString();
    }
}
