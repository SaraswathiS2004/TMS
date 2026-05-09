package com.tms.servlets.actions;

import com.tms.data.dto.People;
import com.tms.data.dto.RelationType;
import com.tms.data.respository.TmsDB;
import com.tms.servlets.message.Message;
import com.tms.sheet.impl.InvitationsTable;
import com.tms.sheet.impl.PersonFunctionsTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PeopleActions {

    // FUNCTION_IDS encodes "functionId:invitedStatus" pairs, e.g. "1:INVITED,2:NOT_INVITED"
    private static final String ALL_PEOPLE_SQL =
        "SELECT i.ID, i.NAME, i.CITY, i.RELATION_TYPE, i.NUMBER_OF_PEOPLE_WILL_COME," +
        "  GROUP_CONCAT(CONCAT(pf.FUNCTION_ID, ':', pf.INVITED_STATUS) ORDER BY pf.FUNCTION_ID SEPARATOR ',') AS FUNCTION_IDS" +
        " FROM Invitations i" +
        " LEFT JOIN Person_Functions pf ON i.ID = pf.PERSON_ID" +
        " GROUP BY i.ID, i.NAME, i.CITY, i.RELATION_TYPE, i.NUMBER_OF_PEOPLE_WILL_COME" +
        " ORDER BY i.NAME";

    private static final String BY_ID_SQL =
        "SELECT i.ID, i.NAME, i.CITY, i.RELATION_TYPE, i.NUMBER_OF_PEOPLE_WILL_COME," +
        "  GROUP_CONCAT(CONCAT(pf.FUNCTION_ID, ':', pf.INVITED_STATUS) ORDER BY pf.FUNCTION_ID SEPARATOR ',') AS FUNCTION_IDS" +
        " FROM Invitations i" +
        " LEFT JOIN Person_Functions pf ON i.ID = pf.PERSON_ID" +
        " WHERE i.ID = ?" +
        " GROUP BY i.ID, i.NAME, i.CITY, i.RELATION_TYPE, i.NUMBER_OF_PEOPLE_WILL_COME";

    private static final String BY_FUNCTION_SQL =
        "SELECT i.ID, i.NAME, i.CITY, i.RELATION_TYPE, i.NUMBER_OF_PEOPLE_WILL_COME," +
        "  GROUP_CONCAT(CONCAT(pf2.FUNCTION_ID, ':', pf2.INVITED_STATUS) ORDER BY pf2.FUNCTION_ID SEPARATOR ',') AS FUNCTION_IDS" +
        " FROM Invitations i" +
        " INNER JOIN Person_Functions pf ON i.ID = pf.PERSON_ID AND pf.FUNCTION_ID = ?" +
        " LEFT JOIN Person_Functions pf2 ON i.ID = pf2.PERSON_ID" +
        " GROUP BY i.ID, i.NAME, i.CITY, i.RELATION_TYPE, i.NUMBER_OF_PEOPLE_WILL_COME" +
        " ORDER BY i.NAME";

    private static final String NOT_INVITED_SQL =
        "SELECT i.ID, i.NAME, i.CITY, i.RELATION_TYPE, i.NUMBER_OF_PEOPLE_WILL_COME," +
        "  '' AS FUNCTION_IDS" +
        " FROM Invitations i" +
        " WHERE NOT EXISTS (SELECT 1 FROM Person_Functions pf WHERE pf.PERSON_ID = i.ID)" +
        " ORDER BY i.NAME";

    private Connection conn;

    public PeopleActions() throws SQLException, ClassNotFoundException {
        conn = TmsDB.getInstance().getConnection();
    }

    public Message addPeople(People people) {
        Message message = new Message();
        try {
            PreparedStatement pre = conn.prepareStatement(
                "INSERT INTO Invitations (NAME, CITY, RELATION_TYPE, NUMBER_OF_PEOPLE_WILL_COME, INVITED_STATUS)" +
                " VALUES (?, ?, ?, ?, 'NOT_INVITED')",
                Statement.RETURN_GENERATED_KEYS
            );
            pre.setString(1, people.getName());
            pre.setString(2, people.getCity());
            pre.setString(3, people.getRelationType().toString());
            pre.setInt(4, people.getNumberOfPerson());

            int rows = pre.executeUpdate();
            if (rows > 0) {
                ResultSet keys = pre.getGeneratedKeys();
                if (keys.next() && people.getInvitedFunctionIds() != null) {
                    int newId = keys.getInt(1);
                    insertFunctionAssociations(newId, people.getInvitedFunctionIds());
                }
                message.setMessage("Successfully Added!");
                message.setStatus(Message.Status.SUCCESS);
                new InvitationsTable().triggerSync();
                new PersonFunctionsTable().triggerSync();
            } else {
                message.setMessage("Could not add person.");
                message.setStatus(Message.Status.FAIL);
            }
        } catch (Exception e) {
            System.out.println(e);
            message.setStatus(Message.Status.FAIL);
        }
        return message;
    }

    public ArrayList<People> listPeople(String id, String functionId, String noFunction)
            throws SQLException, ClassNotFoundException {
        ResultSet rs;
        if (id != null) {
            PreparedStatement pre = conn.prepareStatement(BY_ID_SQL);
            pre.setInt(1, Integer.parseInt(id));
            rs = pre.executeQuery();
        } else if (functionId != null) {
            PreparedStatement pre = conn.prepareStatement(BY_FUNCTION_SQL);
            pre.setInt(1, Integer.parseInt(functionId));
            rs = pre.executeQuery();
        } else if ("true".equals(noFunction)) {
            rs = conn.prepareStatement(NOT_INVITED_SQL).executeQuery();
        } else {
            rs = conn.prepareStatement(ALL_PEOPLE_SQL).executeQuery();
        }

        ArrayList<People> list = new ArrayList<>();
        while (rs.next()) {
            list.add(buildPeople(rs));
        }
        return list;
    }

    // Full update: person details + function associations in one call
    public Message updatePeople(People people) {
        Message message = new Message();
        try {
            // Update person details when name is supplied
            if (people.getName() != null && !people.getName().isEmpty()) {
                PreparedStatement pre = conn.prepareStatement(
                    "UPDATE Invitations SET NAME=?, CITY=?, RELATION_TYPE=?, NUMBER_OF_PEOPLE_WILL_COME=? WHERE ID=?"
                );
                pre.setString(1, people.getName());
                pre.setString(2, people.getCity());
                pre.setString(3, people.getRelationType().toString());
                pre.setInt(4, people.getNumberOfPerson());
                pre.setInt(5, people.getId());
                pre.executeUpdate();
            }
            // Replace function associations
            PreparedStatement del = conn.prepareStatement("DELETE FROM Person_Functions WHERE PERSON_ID=?");
            del.setInt(1, people.getId());
            del.executeUpdate();
            if (people.getInvitedFunctionIds() != null) {
                insertFunctionAssociations(people.getId(), people.getInvitedFunctionIds());
            }
            message.setMessage("Updated successfully.");
            message.setStatus(Message.Status.SUCCESS);
            new InvitationsTable().triggerSync();
            new PersonFunctionsTable().triggerSync();
        } catch (Exception e) {
            System.out.println(e);
            message.setStatus(Message.Status.FAIL);
        }
        return message;
    }

    public Message updateFunctionInvitations(int personId, List<Integer> functionIds) {
        Message message = new Message();
        try {
            PreparedStatement del = conn.prepareStatement(
                "DELETE FROM Person_Functions WHERE PERSON_ID = ?"
            );
            del.setInt(1, personId);
            del.executeUpdate();

            if (functionIds != null) {
                insertFunctionAssociations(personId, functionIds);
            }
            message.setMessage("Updated successfully.");
            message.setStatus(Message.Status.SUCCESS);
            new PersonFunctionsTable().triggerSync();
        } catch (Exception e) {
            System.out.println(e);
            message.setStatus(Message.Status.FAIL);
        }
        return message;
    }

    public Message deletePeople(int id) {
        Message message = new Message();
        try {
            PreparedStatement pre = conn.prepareStatement("DELETE FROM Invitations WHERE ID = ?");
            pre.setInt(1, id);
            int rows = pre.executeUpdate();
            if (rows > 0) {
                message.setMessage("Person deleted successfully.");
                message.setStatus(Message.Status.SUCCESS);
                new InvitationsTable().triggerSync();
                new PersonFunctionsTable().triggerSync();
            } else {
                message.setMessage("Person not found.");
                message.setStatus(Message.Status.FAIL);
            }
        } catch (Exception e) {
            System.out.println(e);
            message.setStatus(Message.Status.FAIL);
        }
        return message;
    }

    public Message updateFunctionStatus(int personId, int functionId, String status) {
        Message message = new Message();
        try {
            PreparedStatement pre = conn.prepareStatement(
                "UPDATE Person_Functions SET INVITED_STATUS = ? WHERE PERSON_ID = ? AND FUNCTION_ID = ?"
            );
            pre.setString(1, status);
            pre.setInt(2, personId);
            pre.setInt(3, functionId);
            int rows = pre.executeUpdate();
            if (rows > 0) {
                message.setMessage("Status updated.");
                message.setStatus(Message.Status.SUCCESS);
                new PersonFunctionsTable().triggerSync();
            } else {
                message.setMessage("Record not found.");
                message.setStatus(Message.Status.FAIL);
            }
        } catch (Exception e) {
            System.out.println(e);
            message.setStatus(Message.Status.FAIL);
        }
        return message;
    }

    // Kept for CLI backward compat (used by old MarkAsInvited feature)
    public Message markAsInvited(int id, com.tms.data.dto.InvitedStatus invitedStatus) {
        Message message = new Message();
        try {
            com.tms.data.dto.PeopleInvited peopleInvited = new com.tms.data.dto.PeopleInvited();
            message.setMessage(peopleInvited.setData(id, invitedStatus));
            message.setStatus(Message.Status.SUCCESS);
        } catch (Exception e) {
            System.out.println(e);
            message.setStatus(Message.Status.FAIL);
        }
        return message;
    }

    private void insertFunctionAssociations(int personId, List<Integer> functionIds) throws SQLException {
        for (int functionId : functionIds) {
            PreparedStatement pf = conn.prepareStatement(
                "INSERT IGNORE INTO Person_Functions (PERSON_ID, FUNCTION_ID) VALUES (?, ?)"
            );
            pf.setInt(1, personId);
            pf.setInt(2, functionId);
            pf.executeUpdate();
        }
    }

    private People buildPeople(ResultSet rs) throws SQLException, ClassNotFoundException {
        People p = new People();
        p.setId(rs.getInt("ID"));
        p.setName(rs.getString("NAME"));
        p.setCity(rs.getString("CITY"));
        p.setNumberOfPerson(rs.getInt("NUMBER_OF_PEOPLE_WILL_COME"));
        p.setRelationType(RelationType.valueOf(rs.getString("RELATION_TYPE")));

        // FUNCTION_IDS format: "fnId:status,fnId:status" e.g. "1:INVITED,2:NOT_INVITED"
        String functionIds = rs.getString("FUNCTION_IDS");
        if (functionIds != null && !functionIds.isEmpty()) {
            List<Integer> ids = new ArrayList<>();
            Map<String, String> statuses = new LinkedHashMap<>();
            for (String part : functionIds.split(",")) {
                String[] kv = part.split(":");
                int fnId = Integer.parseInt(kv[0].trim());
                String status = kv.length > 1 ? kv[1].trim() : "NOT_INVITED";
                ids.add(fnId);
                statuses.put(String.valueOf(fnId), status);
            }
            p.setInvitedFunctionIds(ids);
            p.setFunctionStatuses(statuses);
        }
        return p;
    }
}
