-- ================================================================
-- AgriLink database setup script
-- Matches the JPA entities exactly:
--   crop_catalog.catalogStatus  ENUM('Ac','In')      -> CatalogStatus
--   crop_plan.planStatus        ENUM('Pl','So','Gr','Ha','Fa','Ca') -> PlanStatus
--   crop_plan.sowingDate        (column name matches @Column)
--   growth_observation.growthStage ENUM(...) -> GrowthStageConverter
-- Run in MySQL Workbench or:  mysql -u root -p < agrilink_db.sql
-- ================================================================
DROP DATABASE IF EXISTS agrilink_db;

CREATE DATABASE agrilink_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE agrilink_db;

-- ================================================================
-- CREATE TABLES (parent -> child)
-- ================================================================

-- TABLE 1 : user_details
CREATE TABLE user_details (
    userId        INT          NOT NULL AUTO_INCREMENT,
    userName      VARCHAR(100) NOT NULL,
    userRole      ENUM('Farmer','ExtensionOfficer','AgriOfficer',
                       'ProcurementOfficer','SubsidyAdmin',
                       'ComplianceAnalyst','Admin') NOT NULL,
    emailAddress  VARCHAR(150) NOT NULL,
    phoneNumber   VARCHAR(15)  NULL,
    regionId      INT          NULL,
    accountStatus ENUM('Active','Inactive','Suspended') NOT NULL DEFAULT 'Active',
    CONSTRAINT pk_user_details        PRIMARY KEY (userId),
    CONSTRAINT uq_user_details_email  UNIQUE      (emailAddress)
);

-- TABLE 2 : audit_log
CREATE TABLE audit_log (
    auditId      INT          NOT NULL AUTO_INCREMENT,
    userId       INT          NOT NULL,
    actionType   VARCHAR(100) NOT NULL,
    moduleName   VARCHAR(60)  NOT NULL,
    recordId     INT          NULL,
    oldValue     JSON         NULL,
    newValue     JSON         NULL,
    ipAddress    VARCHAR(45)  NULL,
    createdAt    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_audit_log   PRIMARY KEY (auditId),
    CONSTRAINT fk_audit_user  FOREIGN KEY (userId)
        REFERENCES user_details(userId)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- TABLE 3 : farmer_profile
CREATE TABLE farmer_profile (
    farmerId          BIGINT       NOT NULL AUTO_INCREMENT,
    userId            INT          NOT NULL,
    farmerName        VARCHAR(100) NOT NULL,
    dateOfBirth       DATE         NULL,
    genderType        ENUM('Male','Female','Other') NULL,
    nationalIdNumber  VARCHAR(50)  NULL,
    villageName       VARCHAR(100) NULL,
    districtName      VARCHAR(100) NULL,
    stateName         VARCHAR(100) NULL,
    phoneNumber       VARCHAR(15)  NULL,
    bankAccountNumber VARCHAR(20)  NULL,
    profileStatus     ENUM('Active','Inactive','Verified') NOT NULL DEFAULT 'Active',
    CONSTRAINT pk_farmer_profile  PRIMARY KEY (farmerId),
    CONSTRAINT uq_farmer_user     UNIQUE      (userId),
    CONSTRAINT fk_farmer_user     FOREIGN KEY (userId)
        REFERENCES user_details(userId)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- TABLE 4 : land_holding
CREATE TABLE land_holding (
    holdingId        BIGINT       NOT NULL AUTO_INCREMENT,
    farmerId         BIGINT       NOT NULL,
    surveyNumber     VARCHAR(50)  NULL,
    areaAcres        DECIMAL(8,2) NOT NULL,
    soilType         ENUM('Clay','Sandy','Loam','Black',
                          'Alluvial','Red','Laterite','Loamy') NULL,
    irrigationSource ENUM('Rain','Canal','Borewell','None',
                          'Drip','Sprinkler','River') NULL,
    ownershipType    ENUM('Owned','Leased','SharedCropping') NOT NULL DEFAULT 'Owned',
    holdingStatus    ENUM('Active','Inactive','Disputed') NOT NULL DEFAULT 'Active',
    CONSTRAINT pk_land_holding        PRIMARY KEY (holdingId),
    CONSTRAINT fk_landHolding_farmer  FOREIGN KEY (farmerId)
        REFERENCES farmer_profile(farmerId)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_area CHECK (areaAcres > 0)
);

-- TABLE 5 : crop_catalog
CREATE TABLE crop_catalog (
    cropId               INT          NOT NULL AUTO_INCREMENT,
    cropName             VARCHAR(100) NOT NULL,
    cropCategory         ENUM('Cereal','Pulse','Vegetable',
                              'Fruit','Cash','Oilseed','Spice','Fodder') NOT NULL,
    cropSeason           ENUM('Kharif','Rabi','Zaid','Perennial') NOT NULL,
    typicalDurationDays  INT          NOT NULL,
    expectedYieldPerAcre DECIMAL(8,2) NOT NULL,
    catalogStatus        ENUM('Ac','In') NOT NULL DEFAULT 'Ac',
    CONSTRAINT pk_crop_catalog      PRIMARY KEY (cropId),
    CONSTRAINT uq_crop_name_season  UNIQUE      (cropName, cropSeason),
    CONSTRAINT chk_duration         CHECK (typicalDurationDays > 0),
    CONSTRAINT chk_yield            CHECK (expectedYieldPerAcre > 0)
);

-- TABLE 6 : crop_plan
CREATE TABLE crop_plan (
    planId              INT          NOT NULL AUTO_INCREMENT,
    farmerId            BIGINT       NOT NULL,
    holdingId           BIGINT       NOT NULL,
    cropId              INT          NOT NULL,
    planSeason          ENUM('Kharif','Rabi','Zaid','Perennial') NOT NULL,
    planYear            YEAR         NOT NULL,
    sowingDate          DATE         NOT NULL,
    expectedHarvestDate DATE         NOT NULL,
    areaPlanted         DECIMAL(8,2) NOT NULL,
    planStatus          ENUM('Pl','So','Gr',
                              'Ha','Fa','Ca') NOT NULL DEFAULT 'Pl',
    approvedBy          INT          NULL,
    approvedAt          DATETIME     NULL,
    CONSTRAINT pk_crop_plan         PRIMARY KEY (planId),
    CONSTRAINT uq_crop_plan         UNIQUE      (farmerId, holdingId, cropId, planSeason, planYear),
    CONSTRAINT fk_cropPlan_farmer   FOREIGN KEY (farmerId)
        REFERENCES farmer_profile(farmerId)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_cropPlan_holding  FOREIGN KEY (holdingId)
        REFERENCES land_holding(holdingId)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_cropPlan_crop     FOREIGN KEY (cropId)
        REFERENCES crop_catalog(cropId)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_plan_area  CHECK (areaPlanted > 0),
    CONSTRAINT chk_plan_dates CHECK (expectedHarvestDate > sowingDate)
);

-- TABLE 7 : growth_observation
CREATE TABLE growth_observation (
    observationId     INT     NOT NULL AUTO_INCREMENT,
    planId            INT     NOT NULL,
    officerId         INT     NOT NULL,
    observationDate   DATE    NOT NULL,
    growthStage       ENUM('Germination','Vegetative','Tillering',
                           'Flowering','Grain filling',
                           'Maturity','Harvest-ready') NOT NULL,
    pestOrDiseaseFlag BOOLEAN NOT NULL DEFAULT FALSE,
    fieldRemarks      TEXT    NULL,
    createdAt         DATETIME NULL,
    CONSTRAINT pk_growth_observation PRIMARY KEY (observationId),
    CONSTRAINT fk_obs_plan           FOREIGN KEY (planId)
        REFERENCES crop_plan(planId)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_obs_officer        FOREIGN KEY (officerId)
        REFERENCES user_details(userId)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- ================================================================
-- SEED DATA
-- ================================================================

-- user_details
INSERT INTO user_details (userName, userRole, emailAddress, phoneNumber, regionId, accountStatus) VALUES
('Rajesh Menon',  'AgriOfficer',      'rajesh.m@agrilink.in',  '9000000002', 1, 'Active'),
('Ravi Kumar',    'ExtensionOfficer', 'ravi.k@agrilink.in',    '9000000003', 1, 'Active'),
('Priya Singh',   'ExtensionOfficer', 'priya.s@agrilink.in',   '9000000004', 1, 'Active'),
('Suresh Babu',   'Farmer',           'suresh.b@agrilink.in',  '9444100001', 1, 'Active'),
('Muthu Raj',     'Farmer',           'muthu.r@agrilink.in',   '9444100002', 1, 'Active'),
('Paunammal K',   'Farmer',           'pauna.k@agrilink.in',   '9444100003', 2, 'Active');

-- audit_log
INSERT INTO audit_log (userId, actionType, moduleName, recordId) VALUES
((SELECT userId FROM user_details WHERE emailAddress='rajesh.m@agrilink.in'),
 'LOGIN', 'user_details', NULL),
((SELECT userId FROM user_details WHERE emailAddress='ravi.k@agrilink.in'),
 'CREATE', 'growth_observation', NULL);

-- farmer_profile
INSERT INTO farmer_profile
    (userId, farmerName, dateOfBirth, genderType, nationalIdNumber,
     villageName, districtName, stateName, phoneNumber, profileStatus)
VALUES
((SELECT userId FROM user_details WHERE emailAddress='suresh.b@agrilink.in'),
 'Suresh Babu','1978-04-12','Male','AADHAAR-TN-110001',
 'Kolathur','Chennai','Tamil Nadu','9444100001','Verified'),
((SELECT userId FROM user_details WHERE emailAddress='muthu.r@agrilink.in'),
 'Muthu Raj','1985-09-20','Male','AADHAAR-TN-110002',
 'Thiruvallur','Thiruvallur','Tamil Nadu','9444100002','Active'),
((SELECT userId FROM user_details WHERE emailAddress='pauna.k@agrilink.in'),
 'Paunammal K','1990-02-14','Female','AADHAAR-TN-110003',
 'Kancheepuram','Kancheepuram','Tamil Nadu','9444100003','Active');

-- land_holding
INSERT INTO land_holding
    (farmerId, surveyNumber, areaAcres, soilType, irrigationSource, ownershipType, holdingStatus)
VALUES
((SELECT farmerId FROM farmer_profile WHERE nationalIdNumber='AADHAAR-TN-110001'),
 'SY-CHN-001-A', 3.5, 'Alluvial', 'Canal',    'Owned',  'Active'),
((SELECT farmerId FROM farmer_profile WHERE nationalIdNumber='AADHAAR-TN-110001'),
 'SY-CHN-001-B', 2.0, 'Black',    'Borewell', 'Owned',  'Active'),
((SELECT farmerId FROM farmer_profile WHERE nationalIdNumber='AADHAAR-TN-110002'),
 'SY-TRV-002-A', 5.0, 'Red',      'Rain',     'Leased', 'Active'),
((SELECT farmerId FROM farmer_profile WHERE nationalIdNumber='AADHAAR-TN-110003'),
 'SY-KAN-003-A', 4.0, 'Loamy',    'Drip',     'Owned',  'Active');

-- crop_catalog
INSERT INTO crop_catalog
    (cropName, cropCategory, cropSeason, typicalDurationDays, expectedYieldPerAcre, catalogStatus)
VALUES
('Rice',      'Cereal',    'Kharif',    120, 25.00, 'Ac'),
('Wheat',     'Cereal',    'Rabi',      135, 20.00, 'Ac'),
('Maize',     'Cereal',    'Kharif',     90, 18.00, 'Ac'),
('Tomato',    'Vegetable', 'Zaid',       75, 40.00, 'Ac'),
('Sugarcane', 'Cash',      'Perennial', 365, 60.00, 'Ac'),
('Chilli',    'Spice',     'Rabi',      150, 10.00, 'Ac'),
('Groundnut', 'Oilseed',   'Kharif',    110, 12.00, 'Ac');

-- crop_plan
INSERT INTO crop_plan
    (farmerId, holdingId, cropId, planSeason, planYear,
     sowingDate, expectedHarvestDate, areaPlanted, planStatus)
VALUES
((SELECT farmerId FROM farmer_profile WHERE nationalIdNumber='AADHAAR-TN-110001'),
 (SELECT holdingId FROM land_holding WHERE surveyNumber='SY-CHN-001-A'),
 (SELECT cropId FROM crop_catalog WHERE cropName='Rice' AND cropSeason='Kharif'),
 'Kharif', 2025, '2025-06-15', '2025-10-13', 3.5, 'Pl'),
((SELECT farmerId FROM farmer_profile WHERE nationalIdNumber='AADHAAR-TN-110001'),
 (SELECT holdingId FROM land_holding WHERE surveyNumber='SY-CHN-001-B'),
 (SELECT cropId FROM crop_catalog WHERE cropName='Wheat' AND cropSeason='Rabi'),
 'Rabi', 2025, '2025-11-01', '2026-03-16', 2.0, 'Pl'),
((SELECT farmerId FROM farmer_profile WHERE nationalIdNumber='AADHAAR-TN-110002'),
 (SELECT holdingId FROM land_holding WHERE surveyNumber='SY-TRV-002-A'),
 (SELECT cropId FROM crop_catalog WHERE cropName='Maize' AND cropSeason='Kharif'),
 'Kharif', 2025, '2025-06-20', '2025-09-18', 5.0, 'Pl'),
((SELECT farmerId FROM farmer_profile WHERE nationalIdNumber='AADHAAR-TN-110003'),
 (SELECT holdingId FROM land_holding WHERE surveyNumber='SY-KAN-003-A'),
 (SELECT cropId FROM crop_catalog WHERE cropName='Chilli' AND cropSeason='Rabi'),
 'Rabi', 2025, '2025-10-10', '2026-03-09', 4.0, 'Pl');

-- growth_observation
INSERT INTO growth_observation
    (planId, officerId, observationDate, growthStage, pestOrDiseaseFlag, fieldRemarks)
VALUES
((SELECT cp.planId FROM crop_plan cp
  JOIN farmer_profile fp ON cp.farmerId=fp.farmerId
  JOIN crop_catalog cc   ON cp.cropId=cc.cropId
  WHERE fp.nationalIdNumber='AADHAAR-TN-110001'
    AND cc.cropName='Rice' AND cp.planSeason='Kharif' AND cp.planYear=2025),
 (SELECT userId FROM user_details WHERE emailAddress='ravi.k@agrilink.in'),
 '2025-07-05','Germination',FALSE,
 'Healthy germination. Stand count within expected range.'),
((SELECT cp.planId FROM crop_plan cp
  JOIN farmer_profile fp ON cp.farmerId=fp.farmerId
  JOIN crop_catalog cc   ON cp.cropId=cc.cropId
  WHERE fp.nationalIdNumber='AADHAAR-TN-110001'
    AND cc.cropName='Rice' AND cp.planSeason='Kharif' AND cp.planYear=2025),
 (SELECT userId FROM user_details WHERE emailAddress='ravi.k@agrilink.in'),
 '2025-07-25','Vegetative',TRUE,
 'Aphid infestation on lower leaves. Recommend neem oil spray.'),
((SELECT cp.planId FROM crop_plan cp
  JOIN farmer_profile fp ON cp.farmerId=fp.farmerId
  JOIN crop_catalog cc   ON cp.cropId=cc.cropId
  WHERE fp.nationalIdNumber='AADHAAR-TN-110002'
    AND cc.cropName='Maize' AND cp.planSeason='Kharif' AND cp.planYear=2025),
 (SELECT userId FROM user_details WHERE emailAddress='priya.s@agrilink.in'),
 '2025-07-10','Germination',FALSE,
 'Good emergence. Uniform stand across field.'),
((SELECT cp.planId FROM crop_plan cp
  JOIN farmer_profile fp ON cp.farmerId=fp.farmerId
  JOIN crop_catalog cc   ON cp.cropId=cc.cropId
  WHERE fp.nationalIdNumber='AADHAAR-TN-110002'
    AND cc.cropName='Maize' AND cp.planSeason='Kharif' AND cp.planYear=2025),
 (SELECT userId FROM user_details WHERE emailAddress='priya.s@agrilink.in'),
 '2025-08-05','Vegetative',FALSE,
 'Healthy canopy. Irrigation schedule on track.');

-- ================================================================
-- VERIFY
-- ================================================================
SHOW TABLES;
SELECT userId, userName, userRole FROM user_details;
SELECT planId, farmerId, holdingId, cropId, planStatus, sowingDate FROM crop_plan;
SELECT observationId, planId, growthStage, pestOrDiseaseFlag FROM growth_observation;
