<dependency>
    <groupId>com.sample</groupId>
    <artifactId>sample</artifactId>
    <version>1.0</version>
    <scope>system</scope>
    <systemPath>/home/giacomo/Tests/external_material/major-1.3.2_jre7/major/config/config.jar</systemPath>
</dependency>
AFTER DEPENDENCY OF JUNIT

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.7.0</version>
    <configuration>
        <fork>true</fork>
        <executable>/home/giacomo/Tests/external_material/major-1.3.2_jre7/major/bin/javac</executable>
        <useIncrementalCompilation>false</useIncrementalCompilation>
        <compilerArgs>
            <arg>-XMutator:${mutType}</arg>
	    <arg>-J-Dmajor.export.mutants=${mutEn}</arg>
        </compilerArgs>               
    </configuration>
</plugin>
IN THE PLUGINS (BEST REPLACE COMPILER PLUGINS IF ANY) ------XXXXX CHECK PATH FOR MAJOR JAVAC!!!

<mutType>NONE</mutType>
<mutEn>false</mutEn>
<tarTest>*</tarTest>
IN PROPERTIES

<configuration>
    <forkedProcessTimeoutInSeconds>120</forkedProcessTimeoutInSeconds>
    <includes>
        <include>**/${tarTest}*Test.java</include>
    </includes>
</configuration>
IN MAVEN-SUREFIRE-PLUGINS
NB --->>> forkedProcessTimeoutInSeconds DEPENDS ON THE TEST TIME, CHECK mvn test BEFORE SET A TOO LOW VALUE!

THEN IN ANALYSIS FACTORY RUN
mutAnalysis.sh OutputFileName InputFolderName
IT CREATES OutputFileName WITH ALL ERRORS AND DELETE USELESS MUTANTS (WHICH DID NOT GENERATE ERROR)
THEN RUN
mutCounter.sh AnalysisFileName
IT CREATES AND SORT THE OCCURENCES OF ERRORS IN THE InputFileName AND OUT IN Counter.txt (FIXED NAME)
mutExtractor.sh AnalysisFileName Error LockNum
IT ANALYZES WHERE Error OCCURS IN WHICH CODE FILE, READING AnalysisFileName, AND SAVE THE CODE (BOTH ORIGINAL AND MUTATED) IN A SEPARETED FOLDER
	NOTE -> LockNum NEEDS TO BE SET BECAUSE SEVERAL mutExtractor.sh CAN WORK ON SAME FILES TO EXTRACT DIFFERENT ERROR, SO EACH ONE NEED TO HAVE A SPECIFIC LOCKER!
	NOTE2 -> SUGGESTED TO USE THIS ALWAYS TROUGH bossExtractor.sh

---###---FASTER WAY---###---
mutAnalysis.sh NEEDS TO BE RUN ON ITS OWN, BUT NAMING OutputFileName AS *_Analysis.txt IT WILL BE RECOGNIZE BY bossCounter.sh
bossCounter.sh [FolderDivision]
IF ANY VALUE IS SET AS FolderDivision, IT WILL REUSE THE temp_partAnalysis FOLDER (WHICH SHOULD CONTAINS change (HARDCODED) SELECTION OF LINES IN THE *_Analysis.txt)
	OTHERWISE THE temp_partAnalysis FOLDER WILL BE CREATED
WRT THE NUMBER OF FILES IN temp_partAnalysis, IT WILL RUN SEVERAL PROCESS OF mutCounter.sh IN PARALLEL
FINALLY RUN (THE EXTRACTOR MERGES ALL INFO FROM DIFFERENT MUTATED CODE, SHOULD BE RUN WHEN ALL THE PREVIOUS STEPS ARE PERFORMED FOR ALL THE MUTATED FOLDER (AOR,COR, etc...))
bossExtractor.sh Error LockNum [ FolderDivision ]
IT ANALYZES WHERE Error OCCURS IN WHICH CODE FILE, READING THE temp_partAnalysis FOLDER, AND SAVE THE CODE (BOTH ORIGINAL AND MUTATED) IN A SEPARETED FOLDER
LockNum NEEDS TO BE SET WHEN DIFFERENT Error FOLDER ARE CREATING, SPECIFY DIFFERENT LOCKERS FOR EACH OF THEM.
IF ANY VALUE IS SET AS FolderDivision, IT WILL REUSE THE temp_partAnalysis FOLDER (WHICH SHOULD CONTAINS change (HARDCODED) SELECTION OF LINES IN THE *_Analysis.txt)
	OTHERWISE THE temp_partAnalysis FOLDER WILL BE CREATED

