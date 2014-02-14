== ClearPath

== Manual

CA GNDEMO Process:

In PatternGeneration Project:

1) input/CAInputFileGeneration

2) (Optional) output/CAOutputKMLGeneration to generate kml

3) pattern/CAAdjListPattern


Clean Data Process:

In PatternGeneration Project:

1) input/InputFileGeneration

     writeAverageCube
     
     readAverageCube(i, 0);
     
     changeInterval();
     
     writeAverage15Cube(i, 0);
     
     renameAverageFile(i, 0);
     
2) process/DataClean

3) output/OutputDatabaseGeneration


AdjList Create Process:

In ArterialPatternGeneration_shireesh Project:

1) GetAverageSpeedForArterials1to5New

2) GetAverageSpeedForHighways

3) CreateListForArterials1to5New

4) CreateListForHighways1to5New

5) CreateAdjList1to5New
