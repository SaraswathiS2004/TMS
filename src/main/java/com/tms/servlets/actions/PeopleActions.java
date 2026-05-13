package com.tms.servlets.actions;

import com.ormx.OrmX;
import com.ormx.db.query.Condition;
import com.tms.data.dto.InvitedStatus;
import com.tms.data.dto.People;
import com.tms.data.dto.PeopleInvited;
import com.tms.data.dto.RelationType;
import com.tms.db.InvitationsTable;
import com.tms.db.PersonFunctionsTable;
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
            long newId = OrmX.insert(InvitationsTable.TABLE_NAME)
                .set(InvitationsTable.NAME, people.getName())
                .set(InvitationsTable.CITY, people.getCity())
                .set(InvitationsTable.RELATION_TYPE, people.getRelationType().toString())
                .set(InvitationsTable.NUMBER_OF_PEOPLE_WILL_COME, people.getNumberOfPerson())
                .set(InvitationsTable.INVITED_STATUS, "NOT_INVITED")
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
                OrmX.update(InvitationsTable.TABLE_NAME)
                    .set(InvitationsTable.NAME, people.getName())
                    .set(InvitationsTable.CITY, people.getCity())
                    .set(InvitationsTable.RELATION_TYPE, people.getRelationType().toString())
                    .set(InvitationsTable.NUMBER_OF_PEOPLE_WILL_COME, people.getNumberOfPerson())
                    .where(Condition.eq(InvitationsTable.ID, people.getId()))
                    .execute();
            }
            OrmX.delete(PersonFunctionsTable.TABLE_NAME)
                .where(Condition.eq(PersonFunctionsTable.PERSON_ID, people.getId()))
                .execute();
            if (people.getInvitedFunctionIds() != null) {
                insertFunctionAssociations(people.getId(), people.getInvitedFunctionIds());
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
            OrmX.delete(PersonFunctionsTable.TABLE_NAME)
                .where(Condition.eq(PersonFunctionsTable.PERSON_ID, personId))
                .execute();
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
            int rows = OrmX.delete(InvitationsTable.TABLE_NAME)
                .where(Condition.eq(InvitationsTable.ID, id))
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
            int rows = OrmX.update(PersonFunctionsTable.TABLE_NAME)
                .set(PersonFunctionsTable.INVITED_STATUS, status)
                .where(Condition.eq(PersonFunctionsTable.PERSON_ID, personId)
                    .and(Condition.eq(PersonFunctionsTable.FUNCTION_ID, functionId)))
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
        List<Map<String, Object>> invRows = OrmX.select(InvitationsTable.TABLE_NAME)
            .where(Condition.eq(InvitationsTable.ID, id))
            .fetchRaw();
        List<Map<String, Object>> pfRows = OrmX.select(PersonFunctionsTable.TABLE_NAME)
            .where(Condition.eq(PersonFunctionsTable.PERSON_ID, id))
            .fetchRaw();
        return mergeAndBuild(invRows, pfRows);
    }

    private ArrayList<People> listByFunction(int functionId) {
        List<Map<String, Object>> pfForFn = OrmX.select(PersonFunctionsTable.TABLE_NAME)
            .where(Condition.eq(PersonFunctionsTable.FUNCTION_ID, functionId))
            .fetchRaw();
        if (pfForFn.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> personIds = pfForFn.stream()
            .map(r -> ((Number) r.get(PersonFunctionsTable.PERSON_ID)).intValue())
            .collect(Collectors.toList());
        List<Map<String, Object>> invRows = new ArrayList<>(OrmX.select(InvitationsTable.TABLE_NAME)
            .where(Condition.in(InvitationsTable.ID, personIds))
            .fetchRaw());
        invRows.sort((a, b) -> ((String) a.get(InvitationsTable.NAME))
            .compareToIgnoreCase((String) b.get(InvitationsTable.NAME)));
        List<Map<String, Object>> allPfRows = OrmX.select(PersonFunctionsTable.TABLE_NAME)
            .where(Condition.in(PersonFunctionsTable.PERSON_ID, personIds))
            .fetchRaw();
        return mergeAndBuild(invRows, allPfRows);
    }

    private ArrayList<People> listWithNoFunction() {
        List<Map<String, Object>> allInvRows = OrmX.select(InvitationsTable.TABLE_NAME).fetchRaw();
        List<Map<String, Object>> allPfRows = OrmX.select(PersonFunctionsTable.TABLE_NAME).fetchRaw();
        Set<Integer> assignedPersonIds = allPfRows.stream()
            .map(r -> ((Number) r.get(PersonFunctionsTable.PERSON_ID)).intValue())
            .collect(Collectors.toSet());
        List<Map<String, Object>> unassigned = new ArrayList<>(allInvRows.stream()
            .filter(r -> !assignedPersonIds.contains(((Number) r.get(InvitationsTable.ID)).intValue()))
            .collect(Collectors.toList()));
        unassigned.sort((a, b) -> ((String) a.get(InvitationsTable.NAME))
            .compareToIgnoreCase((String) b.get(InvitationsTable.NAME)));
        return mergeAndBuild(unassigned, List.of());
    }

    private ArrayList<People> listAll() {
        List<Map<String, Object>> invRows = new ArrayList<>(OrmX.select(InvitationsTable.TABLE_NAME).fetchRaw());
        invRows.sort((a, b) -> ((String) a.get(InvitationsTable.NAME))
            .compareToIgnoreCase((String) b.get(InvitationsTable.NAME)));
        List<Map<String, Object>> pfRows = OrmX.select(PersonFunctionsTable.TABLE_NAME).fetchRaw();
        return mergeAndBuild(invRows, pfRows);
    }

    private ArrayList<People> mergeAndBuild(List<Map<String, Object>> invRows, List<Map<String, Object>> pfRows) {
        Map<Integer, List<Map<String, Object>>> pfByPerson = new LinkedHashMap<>();
        for (Map<String, Object> pf : pfRows) {
            int personId = ((Number) pf.get(PersonFunctionsTable.PERSON_ID)).intValue();
            pfByPerson.computeIfAbsent(personId, k -> new ArrayList<>()).add(pf);
        }
        ArrayList<People> result = new ArrayList<>();
        for (Map<String, Object> inv : invRows) {
            int personId = ((Number) inv.get(InvitationsTable.ID)).intValue();
            result.add(buildPeople(inv, pfByPerson.getOrDefault(personId, List.of())));
        }
        return result;
    }

    private People buildPeople(Map<String, Object> inv, List<Map<String, Object>> pfRows) {
        People p = new People();
        p.setId(((Number) inv.get(InvitationsTable.ID)).intValue());
        p.setName((String) inv.get(InvitationsTable.NAME));
        p.setCity((String) inv.get(InvitationsTable.CITY));
        p.setNumberOfPerson(((Number) inv.get(InvitationsTable.NUMBER_OF_PEOPLE_WILL_COME)).intValue());
        p.setRelationType(RelationType.valueOf((String) inv.get(InvitationsTable.RELATION_TYPE)));
        if (!pfRows.isEmpty()) {
            List<Integer> ids = new ArrayList<>();
            Map<String, String> statuses = new LinkedHashMap<>();
            for (Map<String, Object> pf : pfRows) {
                int fnId = ((Number) pf.get(PersonFunctionsTable.FUNCTION_ID)).intValue();
                String fnStatus = (String) pf.get(PersonFunctionsTable.INVITED_STATUS);
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
            List<Map<String, Object>> existing = OrmX.select(PersonFunctionsTable.TABLE_NAME)
                .where(Condition.eq(PersonFunctionsTable.PERSON_ID, personId)
                    .and(Condition.eq(PersonFunctionsTable.FUNCTION_ID, functionId)))
                .fetchRaw();
            if (existing.isEmpty()) {
                OrmX.insert(PersonFunctionsTable.TABLE_NAME)
                    .set(PersonFunctionsTable.PERSON_ID, personId)
                    .set(PersonFunctionsTable.FUNCTION_ID, functionId)
                    .set(PersonFunctionsTable.INVITED_STATUS, "NOT_INVITED")
                    .execute();
            }
        }
    }
}
