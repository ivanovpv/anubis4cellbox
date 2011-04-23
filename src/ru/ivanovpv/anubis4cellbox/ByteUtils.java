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

public class ByteUtils
{
    //public static final String ENCODING="ISO-8859-1";   //used in J2ME version
    public static final String ENCODING="UTF-8";          //used in Android version
    String s;
    StringBuilder sb;
    public static byte[] longToByteArray(long value)
    {
        return new byte[]
                {
                (byte)(value >>> 56),
                (byte)(value >>> 48),
                (byte)(value >>> 40),
                (byte)(value >>> 32),
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value
                };

    }

    public static long byteArrayToLong(byte[] buffer, int offset)
    {
        return (buffer[offset] << 56)
                + ((buffer[offset+1] & 0xFF) << 48)
                + ((buffer[offset+2] & 0xFF) << 40)
                + ((buffer[offset+3] & 0xFF) << 32)
                + ((buffer[offset+4] & 0xFF) << 24)
                + ((buffer[offset+5] & 0xFF) << 16)
                + ((buffer[offset+6] & 0xFF) << 8)
                + (buffer[offset+7] & 0xFF);
    }

    public static int byteArrayToInt(byte[] buffer, int offset)
    {
        return (buffer[offset] << 24)
                + ((buffer[offset+1] & 0xFF) << 16)
                + ((buffer[offset+2] & 0xFF) << 8)
                + (buffer[offset+3] & 0xFF);
    }

    public static byte[] intToByteArray(int value)
    {
        return new byte[]
                {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value
                };    
    }

    public static byte[] stringToByteArray(String s)
    {
        try
        {
            return s.getBytes(ENCODING);
        }
        catch (UnsupportedEncodingException ex)
        {
            return null;
        }
    }

    public static byte[] charArrayToByteArray(char[] buffer)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter out = new OutputStreamWriter(baos, ENCODING);
            Reader reader=new CharArrayReader(buffer);
            for(int ch; (ch  = reader.read()) != -1; )
            {
                out.write(ch);
            }
            return baos.toByteArray();
         }
        catch (Exception ex)
        {
            return null;
        }
    }

    public static String byteArrayToString(byte[] buf, int offset)
    {
        try
        {
            return new String(buf, offset, buf.length-offset, ENCODING);
        }
        catch (UnsupportedEncodingException ex)
        {
            return null;
        }
    }
    /**
    * Convenience method to convert an int to a hex char.
    *
    * @param i the int to convert
    * @return char the converted char
    */
    public static char toHexChar(int i)
    {
        if ((0 <= i) && (i <= 9 ))
            return (char)('0' + i);
        else
            return (char)('A' + (i-10));
    }
    /**
    * Convenience method to convert a byte to a hex string.
    *
    * @param data the byte to convert
    * @return String the converted byte
    */
    public static String byteToHex(byte data)
    {
        StringBuilder buf = new StringBuilder();
        buf.append(toHexChar((data>>>4)&0x0F));
        buf.append(toHexChar(data&0x0F));
        return buf.toString();
    }

    /**
    * Convenience method to convert a byte array to a hex string.
    *
    * @param array the byte[] to convert
    * @return String the converted byte[]
    */
    public static String bytesToHex(byte[] array)
    {
        char[] val = new char[2*array.length];
        String hex = "0123456789ABCDEF";
        for (int i = 0; i < array.length; i++)
        {
            int b = array[i] & 0xff;
            val[2*i] = hex.charAt(b >>> 4);
            val[2*i + 1] = hex.charAt(b & 15);
        }
        return String.valueOf(val);
    }

    /**
    * Convenience method to convert hex string tp byte array
    *
    * @param hex hex string to convert
    * @return byte[] array converted byte[]
    */
    public static byte[] hexToBytes(String hex)
    {
        int len = hex.length();
        hex=hex.toUpperCase();
        if(hex.length()%2!=0)
            return null;
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
        {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }
}
