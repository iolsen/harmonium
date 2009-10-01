////////////////////////////////////////////////////////////
// 
// Copyright (C) 2005 TiVo Inc.
// All rights reserved.
// 
////////////////////////////////////////////////////////////

package com.tivo.core.ds;

import java.util.Arrays;

/**
 * This version of TeArrays simply delegates to the J2SE Arrays class.
 *
 */
public class TeArrays {

    public static void sort(int[] a) {
        Arrays.sort(a);
    }

    public static void sort(String[] a) {
        Arrays.sort(a);
    }
    
    public static void main(String args[]) throws Exception
    {
        String[] stuff = {"vegetable", "animal", "mineral", "other" };
        sort(stuff);
        for (int i = 0; i < stuff.length; i++) {
            System.out.println(stuff[i]);
        }
    }
}
