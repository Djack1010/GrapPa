# nedo

Project description (work in progress...)

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Dependencies

The project needs Java7 and Soot2.5 to be run.
The complete jar of Soot2.5 ant is dependencies can be found in the "/nedo/extLib" folder. The <projects.basedir> property in the config file "/nedo/pom.xml" needs to be set with the base directory of the project.

#### TEMP_DEPENDENCIES
Set paths "nedoPath" and "struct2vecPath" in "/nedo/src/main/java/SourceCode/MainCPG.java"

#### External tools required for vectorization: 
Struct2Vec
```
git clone https://github.com/leoribeiro/struc2vec.git
```
CGMM
```
git clone https://github.com/diningphil/CGMM.git
```


### Compile

The project can be compiled using maven
```
mvn compile
```

## Run

The project can be run directly by its Main class "/nedo/target/classes/SourceCode/MainCPG.class".
Otherwise the script "/nedo/GeneralScriptsFolder/RunSoot/run.sh" and the file "/nedo/GeneralScriptsFolder/RunSoot/config.txt" provide some instructions/functionalities to run the project on different files.
```
./run.sh -help
USAGE: ./run.sh [ ((-targ | -mut) CLASS | -allclasses) [-graph2vec TOOLNAME]]
-targ CLASS: run on a single CLASS file
-mut CLASS: run on a mutated CLASS file
-allclasses: run on all class file in SOURCE_ANALYSIS_FOLDER
-graph2vec TOOLNAME: print graph on file as input format for TOOLNAME
	Available TOOLNAME: struc2vec | CGMM
```
### Examples
```
./run.sh -targ AnnotationUtils -graph2vec struc2vec
./run.sh -mut builder.Builder
./run.sh -allclasses -graph2vec struc2vec
```
## Authors

* **Giacomo Iadarola** - *contributor* - [Djack1010](https://github.com/Djack1010)
