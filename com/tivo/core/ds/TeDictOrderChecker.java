////////////////////////////////////////////////////////////
// 
// Copyright (C) 2005 TiVo Inc.
// All rights reserved.
// 
////////////////////////////////////////////////////////////

package com.tivo.core.ds;

/**
 * Class that builds a dictionary, making sure that elements are
 * added in alphabetical order.
 */
    
class TeDictOrderChecker {
        
    private String mPrevName = "";
    private TeDict mDict = new TeDict();

    /**
     * Adds the "type" from the top-level element.  This one is
     * allowed to be out of order in XML because it comes from the
     * top level element name.
     */

    void addTopLevelType(String value) {
        mDict.add("type", value);
    }
        
    void add(String name, String value) {
        checkName(name);
        mDict.add(name, value);
    }

    void add(String name, TeDict value) {
        checkName(name);
        mDict.add(name, value);
    }
        
    TeDict getDict() {
        return mDict;
    }

    private void checkName(String name) {
        if (name.compareTo(mPrevName) < 0) {
            throw new RuntimeException("element " + name +
                                       " should come before " + mPrevName);
        }
        mPrevName = name;
    }
}
