# NflDraftAnalysisService
The repository is created for performing data analysis related to the yearly drafts that occur in the NFL. 

Brief Intro:
•	For starters, drafts are the medium through which nations best players from colleges step into the NFL arena. Also, the team which had the worst record last year gets to pick first in the draft. Likewise, team which won a championship last year, gets to pick last in a draft.  In summary, teams pick in the reverse order of the standings that they finished last season. 
•	There are totally 7 rounds of draft with each team picking 32 players. There are additional compensatory picks awarded to teams based on a mystery formula that NFL only knows. The prospects who are picked earlier in the drafts generally tend to have stellar college records. The teams with worst record make use of the chance to get best players from college in earlier rounds to rebuild and strengthen their rosters. 
•	Teams are allowed to trade their draft picks with other teams.
NFL evaluates prospect grades every year before each draft. The way that is being performed is out of scope for this repo. This repo makes use of the already available data to perform
data analysis and prediction.

Source: https://www.nfl.com/draft/tracker/prospects?year=2020
---------------------------------------
|Prospect Grade legends provided by NFL|
---------------------------------------
8.0	     The perfect prospect
7.3-7.5	 Perennial All-Pro
7.0-7.1	 Pro Bowl talent
6.7-6.8	 Year 1 quality starter
6.5	     Boom or bust prospect
6.3-6.4	 Will be starter within first two seasons
6.1-6.2	 Good backup who could become starter
6.0	     Developmental traits-based prospect
5.8-5.9	 Backup/special-teamer
5.5-5.6	 Chance to make end of roster or practice squad
5.4	     Priority free agent
5.0-5.1	 Chance to be in an NFL training camp
NO GRADE Likely needs time in developmental league
 
For example, below are the grades of QB prospects in 2017.
https://www.nfl.com/draft/tracker/prospects/QB?college=allColleges&page=1&status=ALL&year=2017
--------------------------------------
|Set up needed for running the project|
--------------------------------------
•	Clone the project from GIT and run in an IDE environment like eclipse. Minimum of JDK 8 is required to run the project.
•	The project has mysql for db operations. Ensure the connections are set up properly and corresponding properties are updated in application.yaml. Ensure that the below script is run:
https://github.com/ps-vivek/NflDraftAnalysisService/blob/master/src/main/resources/sqls/Create_Nfl_Draft_Prospect_Info.sql
	Once above steps are performed, then run the standalone spring boot app.

For reading more about the APIs available, please have a look at the below resource:
----------------------------------------------------------------------------------------------
|https://github.com/ps-vivek/NflDraftAnalysisService/blob/master/Nfl_Draft_Analysis_Intro.docx|
-----------------------------------------------------------------------------------------------
