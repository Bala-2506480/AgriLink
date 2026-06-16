-- ================================================================
-- AgriLink seed / reset script  (corrected to match JPA entities)
-- Fixes vs. original agrilink_db.sql:
--   1. crop_plan now includes approvedBy / approvedAt  (CropPlan entity)
--   2. NO final DROP TABLE section  -- leaves a populated, working schema
-- NOTE: growth_observation.growthStage keeps the ORIGINAL enum values
--       'Grain filling' / 'Harvest-ready' on purpose -- GrowthStageConverter
--       maps the Java enum names to exactly these DB strings.
-- ================================================================

DROP DATABASE IF EXISTS agrilink_db;
CREATE DATABASE agrilink_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE agrilink_db;

-- ----------------------------------------------------------------
-- user_details
-- ----------------------------------------------------------------
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
    CONSTRAINT pk_user_details       PRIMARY KEY (userId),
    CONSTRAINT uq_user_details_email UNIQUE (emailAddress)
);

-- ----------------------------------------------------------------
-- farmer_profile
-- ----------------------------------------------------------------
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
    CONSTRAINT pk_farmer_profile PRIMARY KEY (farmerId),
    CONSTRAINT uq_farmer_user    UNIQUE (userId),
    CONSTRAINT fk_farmer_user    FOREIGN KEY (userId)
        REFERENCES user_details(userId) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- ----------------------------------------------------------------
-- land_holding
-- ----------------------------------------------------------------
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
    CONSTRAINT pk_land_holding       PRIMARY KEY (holdingId),
    CONSTRAINT fk_landHolding_farmer FOREIGN KEY (farmerId)
        REFERENCES farmer_profile(farmerId) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_area CHECK (areaAcres > 0)
);

-- ----------------------------------------------------------------
-- crop_catalog
-- ----------------------------------------------------------------
CREATE TABLE crop_catalog (
    cropId               INT          NOT NULL AUTO_INCREMENT,
    cropName             VARCHAR(100) NOT NULL,
    cropCategory         ENUM('Cereal','Pulse','Vegetable',
                              'Fruit','Cash','Oilseed','Spice','Fodder') NOT NULL,
    cropSeason           ENUM('Kharif','Rabi','Zaid','Perennial') NOT NULL,
    typicalDurationDays  INT          NOT NULL,
    expectedYieldPerAcre DECIMAL(8,2) NOT NULL,
    catalogStatus        ENUM('Ac','In') NOT NULL DEFAULT 'Ac',
    CONSTRAINT pk_crop_catalog     PRIMARY KEY (cropId),
    CONSTRAINT uq_crop_name_season UNIQUE (cropName, cropSeason),
    CONSTRAINT chk_duration        CHECK (typicalDurationDays > 0),
    CONSTRAINT chk_yield           CHECK (expectedYieldPerAcre > 0)
);

-- ----------------------------------------------------------------
-- crop_plan   (FIX 1: approvedBy / approvedAt added)
-- ----------------------------------------------------------------
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
    CONSTRAINT pk_crop_plan        PRIMARY KEY (planId),
    CONSTRAINT uq_crop_plan        UNIQUE (farmerId, holdingId, cropId, planSeason, planYear),
    CONSTRAINT fk_cropPlan_farmer  FOREIGN KEY (farmerId)
        REFERENCES farmer_profile(farmerId) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_cropPlan_holding FOREIGN KEY (holdingId)
        REFERENCES land_holding(holdingId) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_cropPlan_crop    FOREIGN KEY (cropId)
        REFERENCES crop_catalog(cropId) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_plan_area  CHECK (areaPlanted > 0),
    CONSTRAINT chk_plan_dates CHECK (expectedHarvestDate > sowingDate)
);

-- ----------------------------------------------------------------
-- growth_observation   (enum values match GrowthStageConverter output)
-- ----------------------------------------------------------------
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
    CONSTRAINT fk_obs_plan    FOREIGN KEY (planId)
        REFERENCES crop_plan(planId) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_obs_officer FOREIGN KEY (officerId)
        REFERENCES user_details(userId) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- ================================================================
-- SEED DATA
-- ================================================================
INSERT INTO user_details (userName, userRole, emailAddress, phoneNumber, regionId) VALUES
('Rajesh Menon', 'AgriOfficer',      'rajesh.m@agrilink.in', '9000000002', 1),
('Ravi Kumar',   'ExtensionOfficer', 'ravi.k@agrilink.in',   '9000000003', 1),
('Priya Singh',  'ExtensionOfficer', 'priya.s@agrilink.in',  '9000000004', 1),
('Suresh Babu',  'Farmer',           'suresh.b@agrilink.in', '9444100001', 1),
('Muthu Raj',    'Farmer',           'muthu.r@agrilink.in',  '9444100002', 1);

INSERT INTO farmer_profile (userId, farmerName, dateOfBirth, genderType, nationalIdNumber,
                            villageName, districtName, stateName, phoneNumber, profileStatus) VALUES
((SELECT userId FROM user_details WHERE emailAddress='suresh.b@agrilink.in'),
 'Suresh Babu','1978-04-12','Male','AADHAAR-TN-110001','Kolathur','Chennai','Tamil Nadu','9444100001','Verified'),
((SELECT userId FROM user_details WHERE emailAddress='muthu.r@agrilink.in'),
 'Muthu Raj','1985-09-20','Male','AADHAAR-TN-110002','Thiruvallur','Thiruvallur','Tamil Nadu','9444100002','Active');

INSERT INTO land_holding (farmerId, surveyNumber, areaAcres, soilType, irrigationSource, ownershipType) VALUES
((SELECT farmerId FROM farmer_profile WHERE nationalIdNumber='AADHAAR-TN-110001'),
 'SY-CHN-001-A', 3.5, 'Alluvial', 'Canal',    'Owned'),
((SELECT farmerId FROM farmer_profile WHERE nationalIdNumber='AADHAAR-TN-110002'),
 'SY-TRV-002-A', 5.0, 'Red',      'Rain',     'Leased');

INSERT INTO crop_catalog (cropName, cropCategory, cropSeason, typicalDurationDays, expectedYieldPerAcre) VALUES
('Rice',  'Cereal', 'Kharif', 120, 25.00),
('Wheat', 'Cereal', 'Rabi',   135, 20.00),
('Maize', 'Cereal', 'Kharif',  90, 18.00);

INSERT INTO crop_plan (farmerId, holdingId, cropId, planSeason, planYear,
                       sowingDate, expectedHarvestDate, areaPlanted, planStatus) VALUES
((SELECT farmerId FROM farmer_profile WHERE nationalIdNumber='AADHAAR-TN-110001'),
 (SELECT holdingId FROM land_holding WHERE surveyNumber='SY-CHN-001-A'),
 (SELECT cropId FROM crop_catalog WHERE cropName='Rice' AND cropSeason='Kharif'),
 'Kharif', 2026, '2026-06-15', '2026-10-13', 3.5, 'Pl'),
((SELECT farmerId FROM farmer_profile WHERE nationalIdNumber='AADHAAR-TN-110002'),
 (SELECT holdingId FROM land_holding WHERE surveyNumber='SY-TRV-002-A'),
 (SELECT cropId FROM crop_catalog WHERE cropName='Maize' AND cropSeason='Kharif'),
 'Kharif', 2026, '2026-06-20', '2026-09-18', 5.0, 'Pl');

-- Reference IDs for API testing
SELECT u.userId AS officerId, u.userName, u.userRole
  FROM user_details u WHERE u.userRole IN ('ExtensionOfficer','AgriOfficer');
SELECT planId, farmerId, holdingId, cropId, planStatus FROM crop_plan;
