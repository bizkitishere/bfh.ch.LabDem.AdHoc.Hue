/*
 *  Cleaner.java
 *  LabDem
 *
 *  Copyright (c) 2015 BFH. All rights reserved.
 */
package bfh.ch.labdem.adhoc.hue.main;

import java.util.List;
import java.util.TimerTask;

/**
 * Will clear a given list whenever the run method is executed
 * @author Philippe Lüthi, Elia Kocher
 */
public class Cleaner extends TimerTask{

    private final List<Integer> MESSAGE_IDS;
    
    public Cleaner(List<Integer> ids){
        this.MESSAGE_IDS = ids;
    }
    
    @Override
    public void run() {
        //remove all items from the list
        MESSAGE_IDS.clear();
    }
    
}
