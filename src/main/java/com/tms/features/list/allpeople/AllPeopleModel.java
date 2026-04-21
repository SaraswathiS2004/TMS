package com.tms.features.list.allpeople;

import com.tms.data.respository.TmsDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class AllPeopleModel {

    private AllPeopleView allPeopleView;

    private Connection conn;
    public AllPeopleModel(AllPeopleView allPeopleView) throws SQLException, ClassNotFoundException {
        this.allPeopleView = allPeopleView;
        conn = TmsDB.getInstance().getConnection();
    }

    public ResultSet DisplayAllPeople() throws SQLException {
        try {
            PreparedStatement pre = conn.prepareStatement("SELECT * FROM Invitations");
            ResultSet set = pre.executeQuery();
            return set;
        }
        catch (Exception e){
            System.out.println(e);
        }
        return null;
    }
}
