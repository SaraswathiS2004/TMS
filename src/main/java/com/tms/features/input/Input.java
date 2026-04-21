package com.tms.features.input;

import java.util.Scanner;

public class Input {

    private static Scanner scan;

    private Input(){

    }

    public static Scanner getInstance(){

        if(scan == null){
            scan = new Scanner(System.in);
        }
        return scan;
    }

}
