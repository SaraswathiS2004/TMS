package com.tms;

import com.tms.features.list.allpeople.AllPeopleView;
import com.tms.features.list.bothinvited.BothInvitedView;
import com.tms.features.list.engagementinvited.EngagementInvitedView;
import com.tms.features.list.marriageinvited.MarriageInvitedView;
import com.tms.features.list.notinvited.NotInvitedView;
import com.tms.features.peopleAdded.PeoplesAddedView;
import com.tms.features.MarkAsInvited.MarkAsInvitedView;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {


        Scanner scan = new Scanner(System.in);
        int process = 0;
        String[] questions = {"1.People Added" , "2.Mark as Invited" , "3.List All People" ,"4.List Marriage Invited People"  , "5.List Engagement Invited People", "6.List Both Invited People" , "7.List Not Invited People" ,"8.Exit"};
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
                    MarkAsInvitedView view = new MarkAsInvitedView();
                    view.init();
                    break;
                case 3:
                    AllPeopleView allPeopleView = new AllPeopleView();
                    allPeopleView.init();
                    break;
                case 4:
                    MarriageInvitedView marriageInvitedView = new MarriageInvitedView();
                    marriageInvitedView.init();
                    break;
                case 5:
                    EngagementInvitedView engagementInvitedView = new EngagementInvitedView();
                    engagementInvitedView.init();
                    break;
                case 6:
                    BothInvitedView bothInvitedView = new BothInvitedView();
                    bothInvitedView.init();
                    break;
                case 7:
                    NotInvitedView notInvitedView = new NotInvitedView();
                    notInvitedView.init();
                    break;
                default:
                    break;
            }
        }
        while(process != 8);
    }
}
