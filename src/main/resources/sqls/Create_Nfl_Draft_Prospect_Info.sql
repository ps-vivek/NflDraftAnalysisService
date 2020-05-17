CREATE TABLE Nfl_Draft_Prospect_Info (
    id int NOT NULL AUTO_INCREMENT,
    PLAYER varchar(255) NOT NULL,
    COLLEGE varchar(200),
    YEAR int NOT NULL,
    STATUS varchar(150),
    POSITION varchar(10),
    TEAM varchar(255),
    CLASS varchar(50),
    GRADE float,
    CONFERENCE varchar(100),
	PRIMARY KEY (id)
);