/*
 * Copyright (c) 2011 Pavel Ivanov (ivanovpv@gmail.com)
 */

package ru.ivanovpv.anubis4cellbox;

import java.security.SecureRandom;

/**
 * Provides simple - very basic password salting utilities
 */
public final class Salt
{
    public static final int LENGTH=8;
    private static final int SHIFT=16;
    private String saltString;
    private final String TAG=Salt.class.getName();

    public Salt()
    {
        this.saltString=randomString(LENGTH);
    }

    public Salt(int length)
    {
        this.saltString=randomString(length);
    }

    /**
     * Creates salt from stored
     * @param enSaltString - Caesar encoded salt string
     */
    public Salt(String enSaltString)
    {
        this.saltString=decodeSaltString(enSaltString);
    }

    public String getSaltString()
    {
        return this.saltString;
    }

    public String getEncodedSaltString()
    {
        return encodeSaltString();
    }

    /**
     * "Encrypts" salt with Caesar's code (substituion)
     * @return
     */
    private String encodeSaltString()
    {
        StringBuilder sb=new StringBuilder();
        for(int i=0; i < saltString.length(); i++)
            sb.append((char )(saltString.charAt(i)+SHIFT));
        return sb.toString();
    }

    private String decodeSaltString(String enSaltString)
    {
        StringBuilder sb=new StringBuilder();
        for(int i=0; i < enSaltString.length(); i++)
            sb.append((char )(enSaltString.charAt(i)-SHIFT));
        return sb.toString();
    }


    private String randomString(int length)
    {
        SecureRandom wheel = new SecureRandom();
        StringBuilder sb=new StringBuilder();
        int i, random;

        char[] printableAscii = new char[]{'!', '\"', '#', '$', '%', '(', ')', '*', '+', '-', '.', '/', '\'',
            '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', ':', '<', '=', '>', '?', '@',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '[', '\\', ']', '^', '_', '`', '{', '|', '}', '~',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

        for (i = 0; i < length; i++)
        {
            random = wheel.nextInt(printableAscii.length);
            sb.append(printableAscii[random]);
        }
        return sb.toString();
    }
}
