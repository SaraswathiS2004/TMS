package com.tms;

import com.tms.data.dto.InvitedStatus;
import com.tms.data.dto.RelationType;
import com.tms.features.peopleAdded.PeoplesAddedView;
import com.tms.features.personinvited.InvitedView;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {


        Scanner scan = new Scanner(System.in);
        int process = 0;
        String[] questions = {"1.People Added" , "2.Is People Invited" , "3.List the People" , "4.Exit"};
        do {

            for(int i = 0; i< questions.length; i++){
                System.out.println(questions[i]);
            }
            process = scan.nextInt();

            switch (process){
                case 1:
                    PeoplesAddedView peoplesAddedView =new PeoplesAddedView();
                    peoplesAddedView.init();
                    break;
                case 2:
                    InvitedView view = new InvitedView();
                    view.init();
                default:
                    break;
            }

        }
        while(process != 4);
    }
}
