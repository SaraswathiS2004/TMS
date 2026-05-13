package com.tms;

import com.ormx.db.ConnectionManager;
import com.ormx.schema.SchemaRegistry;
import com.tms.features.list.allpeople.AllPeopleView;
import com.tms.features.list.bothinvited.BothInvitedView;
import com.tms.features.list.engagementinvited.EngagementInvitedView;
import com.tms.features.list.marriageinvited.MarriageInvitedView;
import com.tms.features.list.notinvited.NotInvitedView;
import com.tms.features.peopleAdded.PeoplesAddedView;
import com.tms.features.MarkAsInvited.MarkAsInvitedView;

import java.io.InputStream;
import java.util.Scanner;

public class Main {

    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    public static void main(String[] args) throws Exception {
        initOrmX();

        Scanner scan = new Scanner(System.in);
        int process = 0;
        String[] questions = {
            "1.People Added", "2.Mark as Invited", "3.List All People",
            "4.List Marriage Invited People", "5.List Engagement Invited People",
            "6.List Both Invited People", "7.List Not Invited People", "8.Exit"
        };
        do {
            for (String question : questions) {
                System.out.println(question);
            }
            process = scan.nextInt();

            switch (process) {
                case 1: new PeoplesAddedView().init(); break;
                case 2: new MarkAsInvitedView().init(); break;
                case 3: new AllPeopleView().init(); break;
                case 4: new MarriageInvitedView().init(); break;
                case 5: new EngagementInvitedView().init(); break;
                case 6: new BothInvitedView().init(); break;
                case 7: new NotInvitedView().init(); break;
                default: break;
            }
        } while (process != 8);
    }

    private static void initOrmX() throws Exception {
        try (InputStream schema = Main.class.getResourceAsStream("/db-schema.xml")) {
            if (schema != null) { SchemaRegistry.getInstance().load(schema); }
        }
        String dbName  = SchemaRegistry.getInstance().getDatabaseName();
        String jdbcUrl = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + dbName
            + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        ConnectionManager.init(jdbcUrl, DB_USER, DB_PASS);
    }
}
