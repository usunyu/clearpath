ClearPath Navteq Manual
======================

### 1) CA GNDEMO Process:
#### In PatternGeneration Project:
* input/CAInputFileGeneration
* (Optional) output/CAOutputKMLGeneration to generate kml
* pattern/CAAdjListPattern


### 2) Clean Data Process:
#### In PatternGeneration Project:
* input/InputFileGeneration


     writeAverageCube
     readAverageCube(i, 0);
     changeInterval();
     writeAverage15Cube(i, 0);
     renameAverageFile(i, 0);

     
* process/DataClean
* output/OutputDatabaseGeneration


### 3) AdjList Create Process:
#### In ArterialPatternGeneration_shireesh Project:
* GetAverageSpeedForArterials1to5New
* GetAverageSpeedForHighways
* CreateListForArterials1to5New
* CreateListForHighways1to5New
*  CreateAdjList1to5New
