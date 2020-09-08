CREATE TABLE nfl_draft_prospect_info 
  ( 
     id         INT NOT NULL auto_increment, 
     player     VARCHAR(255) NOT NULL, 
     college    VARCHAR(200), 
     year       INT NOT NULL, 
     status     VARCHAR(150), 
     position   VARCHAR(10), 
     team       VARCHAR(255), 
     class      VARCHAR(50), 
     grade      FLOAT, 
     conference VARCHAR(100), 
     draftedRound VARCHAR(10),
     PRIMARY KEY (id) 
  ); 

CREATE TABLE overall_team_standings_by_year 
  ( 
     seed       INT NOT NULL, 
     team       VARCHAR(255), 
     wins       INT NOT NULL, 
     loss       INT NOT NULL, 
     tie        INT NOT NULL, 
     position   VARCHAR(255), 
     reason     VARCHAR(255), 
     conference VARCHAR(100), 
     year       INT NOT NULL, 
     PRIMARY KEY (team, year) 
  ); 