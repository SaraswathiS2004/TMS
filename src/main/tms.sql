-- =============================================================
-- TMS — Function Manager Database Schema
-- Database: Tms
-- =============================================================

CREATE DATABASE IF NOT EXISTS Tms;
USE Tms;

-- -------------------------------------------------------------
-- Table: Invitations
-- Stores each invitee. INVITED_STATUS is kept for backward
-- compatibility with the CLI but is no longer used by the web API.
-- Invitation associations are tracked via Person_Functions.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Invitations (
    ID                        INT          NOT NULL AUTO_INCREMENT,
    NAME                      VARCHAR(100) NOT NULL,
    CITY                      VARCHAR(100) NOT NULL,
    RELATION_TYPE             VARCHAR(20)  NOT NULL,   -- CLOSE | DISTANCE | FRIENDS
    NUMBER_OF_PEOPLE_WILL_COME INT         NOT NULL DEFAULT 1,
    INVITED_STATUS            VARCHAR(30)  NOT NULL DEFAULT 'NOT_INVITED',
    PRIMARY KEY (ID)
);

-- -------------------------------------------------------------
-- Table: Functions
-- Stores dynamic event categories (e.g. Marriage, Engagement,
-- Reception). Add any number of functions without code changes.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Functions (
    ID            INT          NOT NULL AUTO_INCREMENT,
    NAME          VARCHAR(100) NOT NULL,
    COLOR         VARCHAR(30)  NOT NULL DEFAULT '#4f46e5',
    DISPLAY_ORDER INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (ID)
);

-- -------------------------------------------------------------
-- Table: Person_Functions  (many-to-many join)
-- Tracks which invitees are invited to which functions.
-- Cascade deletes keep this table clean automatically.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Person_Functions (
    PERSON_ID      INT         NOT NULL,
    FUNCTION_ID    INT         NOT NULL,
    INVITED_STATUS VARCHAR(30) NOT NULL DEFAULT 'NOT_INVITED',
    PRIMARY KEY (PERSON_ID, FUNCTION_ID),
    CONSTRAINT fk_pf_person   FOREIGN KEY (PERSON_ID)   REFERENCES Invitations(ID) ON DELETE CASCADE,
    CONSTRAINT fk_pf_function FOREIGN KEY (FUNCTION_ID) REFERENCES Functions(ID)   ON DELETE CASCADE
);

-- -- =============================================================
-- -- Sample seed data (optional — remove before production use)
-- -- =============================================================

-- -- Seed functions
-- INSERT INTO Functions (NAME, COLOR, DISPLAY_ORDER) VALUES
--     ('Engagement', '#7c3aed', 1),
--     ('Marriage',   '#dc2626', 2);

-- -- Seed invitees
-- INSERT INTO Invitations (NAME, CITY, RELATION_TYPE, NUMBER_OF_PEOPLE_WILL_COME, INVITED_STATUS) VALUES
--     ('Raman',     'Tirunelveli', 'CLOSE',    4, 'NOT_INVITED'),
--     ('Murugan',   'Madurai',     'DISTANCE', 2, 'NOT_INVITED'),
--     ('Priya',     'Chennai',     'FRIENDS',  3, 'NOT_INVITED');

-- =============================================================
-- Useful queries for reference
-- =============================================================

-- All invitees with their invited function IDs (comma-separated)
-- SELECT i.ID, i.NAME, i.CITY, i.RELATION_TYPE, i.NUMBER_OF_PEOPLE_WILL_COME,
--        GROUP_CONCAT(pf.FUNCTION_ID ORDER BY pf.FUNCTION_ID SEPARATOR ',') AS FUNCTION_IDS
-- FROM Invitations i
-- LEFT JOIN Person_Functions pf ON i.ID = pf.PERSON_ID
-- GROUP BY i.ID, i.NAME, i.CITY, i.RELATION_TYPE, i.NUMBER_OF_PEOPLE_WILL_COME
-- ORDER BY i.NAME;

-- Invitees for a specific function (e.g. function ID = 1)
-- SELECT i.* FROM Invitations i
-- INNER JOIN Person_Functions pf ON i.ID = pf.PERSON_ID AND pf.FUNCTION_ID = 1;

-- Invitees not yet assigned to any function
-- SELECT i.* FROM Invitations i
-- WHERE NOT EXISTS (SELECT 1 FROM Person_Functions pf WHERE pf.PERSON_ID = i.ID);

-- Expected headcount per function
-- SELECT f.NAME, COUNT(pf.PERSON_ID) AS INVITEES,
--        SUM(i.NUMBER_OF_PEOPLE_WILL_COME) AS EXPECTED
-- FROM Functions f
-- LEFT JOIN Person_Functions pf ON f.ID = pf.FUNCTION_ID
-- LEFT JOIN Invitations i ON pf.PERSON_ID = i.ID
-- GROUP BY f.ID, f.NAME;
