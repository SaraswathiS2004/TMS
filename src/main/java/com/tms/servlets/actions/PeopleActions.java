package com.tms.servlets.actions;

import com.ormx.OrmX;
import com.ormx.db.query.Condition;
import com.tms.data.dto.InvitedStatus;
import com.tms.data.dto.People;
import com.tms.data.dto.PeopleInvited;
import com.tms.data.dto.RelationType;
import com.tms.db.Invitations;
import com.tms.db.Person_Functions;
import com.tms.servlets.message.Message;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PeopleActions {

    public Message addPeople(People people) {
        Message message = new Message();
        try {
            long newId = OrmX.insert(Invitations.TABLE_NAME)
                .set(Invitations.NAME, people.getName())
                .set(Invitations.CITY, people.getCity())
                .set(Invitations.RELATION_TYPE, people.getRelationType().toString())
                .set(Invitations.NUMBER_OF_PEOPLE_WILL_COME, people.getNumberOfPerson())
                .set(Invitations.INVITED_STATUS, "NOT_INVITED")
                .execute();

            if (people.getInvitedFunctionIds() != null) {
                insertFunctionAssociations((int) newId, people.getInvitedFunctionIds());
            }
            message.setMessage("Successfully Added!");
            message.setStatus(Message.Status.SUCCESS);
        } catch (Exception e) {
            System.out.println(e);
            message.setStatus(Message.Status.FAIL);
        }
        return message;
    }

    public ArrayList<People> listPeople(String id, String functionId, String noFunction) {
        if (id != null) {
            return listById(Integer.parseInt(id));
        } else if (functionId != null) {
            return listByFunction(Integer.parseInt(functionId));
        } else if ("true".equals(noFunction)) {
            return listWithNoFunction();
        } else {
            return listAll();
        }
    }

    public Message updatePeople(People people) {
        Message message = new Message();
        try {
            if (people.getName() != null && !people.getName().isEmpty()) {
                OrmX.update(Invitations.TABLE_NAME)
                    .set(Invitations.NAME, people.getName())
                    .set(Invitations.CITY, people.getCity())
                    .set(Invitations.RELATION_TYPE, people.getRelationType().toString())
                    .set(Invitations.NUMBER_OF_PEOPLE_WILL_COME, people.getNumberOfPerson())
                    .where(Condition.eq(Invitations.ID, people.getId()))
                    .execute();
            }
            if (people.getInvitedFunctionIds() != null) {
                syncFunctionAssociations(people.getId(), people.getInvitedFunctionIds());
            }
            message.setMessage("Updated successfully.");
            message.setStatus(Message.Status.SUCCESS);
        } catch (Exception e) {
            System.out.println(e);
            message.setStatus(Message.Status.FAIL);
        }
        return message;
    }

    public Message updateFunctionInvitations(int personId, List<Integer> functionIds) {
        Message message = new Message();
        try {
           
            if (functionIds != null) {
                insertFunctionAssociations(personId, functionIds);
            }
            message.setMessage("Updated successfully.");
            message.setStatus(Message.Status.SUCCESS);
        } catch (Exception e) {
            System.out.println(e);
            message.setStatus(Message.Status.FAIL);
        }
        return message;
    }

    public Message deletePeople(int id) {
        Message message = new Message();
        try {
            int rows = OrmX.delete(Invitations.TABLE_NAME)
                .where(Condition.eq(Invitations.ID, id))
                .execute();
            if (rows > 0) {
                message.setMessage("Person deleted successfully.");
                message.setStatus(Message.Status.SUCCESS);
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
            int rows = OrmX.update(Person_Functions.TABLE_NAME)
                .set(Person_Functions.INVITED_STATUS, status)
                .where(Condition.eq(Person_Functions.PERSON_ID, personId)
                    .and(Condition.eq(Person_Functions.FUNCTION_ID, functionId)))
                .execute();
            if (rows > 0) {
                message.setMessage("Status updated.");
                message.setStatus(Message.Status.SUCCESS);
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

    public Message markAsInvited(int id, InvitedStatus invitedStatus) {
        Message message = new Message();
        try {
            PeopleInvited peopleInvited = new PeopleInvited();
            message.setMessage(peopleInvited.setData(id, invitedStatus));
            message.setStatus(Message.Status.SUCCESS);
        } catch (Exception e) {
            System.out.println(e);
            message.setStatus(Message.Status.FAIL);
        }
        return message;
    }

    private ArrayList<People> listById(int id) {
        List<Map<String, Object>> invRows = OrmX.select(Invitations.TABLE_NAME)
            .where(Condition.eq(Invitations.ID, id))
            .fetchRaw();
        List<Map<String, Object>> pfRows = OrmX.select(Person_Functions.TABLE_NAME)
            .where(Condition.eq(Person_Functions.PERSON_ID, id))
            .fetchRaw();
        return mergeAndBuild(invRows, pfRows);
    }

    private ArrayList<People> listByFunction(int functionId) {
        List<Map<String, Object>> pfForFn = OrmX.select(Person_Functions.TABLE_NAME)
            .where(Condition.eq(Person_Functions.FUNCTION_ID, functionId))
            .fetchRaw();
        if (pfForFn.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> personIds = pfForFn.stream()
            .map(r -> ((Number) r.get(Person_Functions.PERSON_ID)).intValue())
            .collect(Collectors.toList());
        List<Map<String, Object>> invRows = new ArrayList<>(OrmX.select(Invitations.TABLE_NAME)
            .where(Condition.in(Invitations.ID, personIds))
            .fetchRaw());
        invRows.sort((a, b) -> ((String) a.get(Invitations.NAME))
            .compareToIgnoreCase((String) b.get(Invitations.NAME)));
        List<Map<String, Object>> allPfRows = OrmX.select(Person_Functions.TABLE_NAME)
            .where(Condition.in(Person_Functions.PERSON_ID, personIds))
            .fetchRaw();
        return mergeAndBuild(invRows, allPfRows);
    }

    private ArrayList<People> listWithNoFunction() {
        List<Map<String, Object>> allInvRows = OrmX.select(Invitations.TABLE_NAME).fetchRaw();
        List<Map<String, Object>> allPfRows = OrmX.select(Person_Functions.TABLE_NAME).fetchRaw();
        Set<Integer> assignedPersonIds = allPfRows.stream()
            .map(r -> ((Number) r.get(Person_Functions.PERSON_ID)).intValue())
            .collect(Collectors.toSet());
        List<Map<String, Object>> unassigned = new ArrayList<>(allInvRows.stream()
            .filter(r -> !assignedPersonIds.contains(((Number) r.get(Invitations.ID)).intValue()))
            .collect(Collectors.toList()));
        unassigned.sort((a, b) -> ((String) a.get(Invitations.NAME))
            .compareToIgnoreCase((String) b.get(Invitations.NAME)));
        return mergeAndBuild(unassigned, List.of());
    }

    private ArrayList<People> listAll() {
        List<Map<String, Object>> invRows = new ArrayList<>(OrmX.select(Invitations.TABLE_NAME).fetchRaw());
        invRows.sort((a, b) -> ((String) a.get(Invitations.NAME))
            .compareToIgnoreCase((String) b.get(Invitations.NAME)));
        List<Map<String, Object>> pfRows = OrmX.select(Person_Functions.TABLE_NAME).fetchRaw();
        return mergeAndBuild(invRows, pfRows);
    }

    private ArrayList<People> mergeAndBuild(List<Map<String, Object>> invRows, List<Map<String, Object>> pfRows) {
        Map<Integer, List<Map<String, Object>>> pfByPerson = new LinkedHashMap<>();
        for (Map<String, Object> pf : pfRows) {
            int personId = ((Number) pf.get(Person_Functions.PERSON_ID)).intValue();
            pfByPerson.computeIfAbsent(personId, k -> new ArrayList<>()).add(pf);
        }
        ArrayList<People> result = new ArrayList<>();
        for (Map<String, Object> inv : invRows) {
            int personId = ((Number) inv.get(Invitations.ID)).intValue();
            result.add(buildPeople(inv, pfByPerson.getOrDefault(personId, List.of())));
        }
        return result;
    }

    private People buildPeople(Map<String, Object> inv, List<Map<String, Object>> pfRows) {
        People p = new People();
        p.setId(((Number) inv.get(Invitations.ID)).intValue());
        p.setName((String) inv.get(Invitations.NAME));
        p.setCity((String) inv.get(Invitations.CITY));
        p.setNumberOfPerson(((Number) inv.get(Invitations.NUMBER_OF_PEOPLE_WILL_COME)).intValue());
        p.setRelationType(RelationType.valueOf((String) inv.get(Invitations.RELATION_TYPE)));
        if (!pfRows.isEmpty()) {
            List<Integer> ids = new ArrayList<>();
            Map<String, String> statuses = new LinkedHashMap<>();
            for (Map<String, Object> pf : pfRows) {
                int fnId = ((Number) pf.get(Person_Functions.FUNCTION_ID)).intValue();
                String fnStatus = (String) pf.get(Person_Functions.INVITED_STATUS);
                ids.add(fnId);
                statuses.put(String.valueOf(fnId), fnStatus);
            }
            p.setInvitedFunctionIds(ids);
            p.setFunctionStatuses(statuses);
        }
        return p;
    }

    private void insertFunctionAssociations(int personId, List<Integer> functionIds) {
        for (int functionId : functionIds) {
            List<Map<String, Object>> existing = OrmX.select(Person_Functions.TABLE_NAME)
                .where(Condition.eq(Person_Functions.PERSON_ID, personId)
                    .and(Condition.eq(Person_Functions.FUNCTION_ID, functionId)))
                .fetchRaw();
            if (existing.isEmpty()) {
                addOrResurrectAssociation(personId, functionId);
            }
        }
    }

    /**
     * Reconciles a person's function associations with the requested set: removes the
     * associations that were unchecked and adds the newly checked ones. Without this an
     * unchecked function would linger in the DB because only inserts were performed before.
     */
    private void syncFunctionAssociations(int personId, List<Integer> functionIds) {
        Set<Integer> existingIds = OrmX.select(Person_Functions.TABLE_NAME)
            .where(Condition.eq(Person_Functions.PERSON_ID, personId))
            .fetchRaw().stream()
            .map(r -> ((Number) r.get(Person_Functions.FUNCTION_ID)).intValue())
            .collect(Collectors.toSet());

        List<Integer> toRemove = existingIds.stream()
            .filter(id -> !functionIds.contains(id))
            .collect(Collectors.toList());
        if (!toRemove.isEmpty()) {
            OrmX.delete(Person_Functions.TABLE_NAME)
                .where(Condition.eq(Person_Functions.PERSON_ID, personId)
                    .and(Condition.in(Person_Functions.FUNCTION_ID, toRemove)))
                .execute();
        }

        for (int functionId : functionIds) {
            if (!existingIds.contains(functionId)) {
                addOrResurrectAssociation(personId, functionId);
            }
        }
    }

    /**
     * Adds an association, reviving a previously soft-deleted row for the same pair instead of
     * inserting a duplicate. Person_Functions is a sync table, so removals are soft deletes
     * (is_deleted=1) that keep the composite-PK row in place.
     */
    private void addOrResurrectAssociation(int personId, int functionId) {
        int resurrected = OrmX.update(Person_Functions.TABLE_NAME)
            .set("is_deleted", 0)
            .set(Person_Functions.INVITED_STATUS, "NOT_INVITED")
            .where(Condition.eq(Person_Functions.PERSON_ID, personId)
                .and(Condition.eq(Person_Functions.FUNCTION_ID, functionId)))
            .execute();
        if (resurrected == 0) {
            OrmX.insert(Person_Functions.TABLE_NAME)
                .set(Person_Functions.PERSON_ID, personId)
                .set(Person_Functions.FUNCTION_ID, functionId)
                .set(Person_Functions.INVITED_STATUS, "NOT_INVITED")
                .execute();
        }
    }
}
