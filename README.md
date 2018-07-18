# nedo

Project description (work in progress...)

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Dependencies

The project needs Java7 and Soot2.5 to be run.
The complete jar of Soot2.5 and its dependencies can be found in the "/nedo/extLib" folder. 
The <projects.basedir> property in the config file "/nedo/pom.xml" needs to be set with the base directory of the project.

#### External tools required for vectorization: 
CGMM
```
git clone https://github.com/Djack1010/CGMM.git
```


### Compile

The project can be compiled using maven
```
mvn compile
```

## Run

The project can be run directly by its Main class "/nedo/target/classes/SourceCode/MainCPG.class".
Some script are provided in "/nedo/GeneralScriptsFolder/RunSoot" to properly set the project for running.
The scripts available are:
run.sh -> Run nedo on a target project folder. It performs the static analysis and store the generated graphs.
load.sh -> Load stored graphs and perform operation on them.

The file config.txt needs to be set properly to run the scripts.

### Script run.sh
Run nedo on a target project folder set in Config.txt. It performs the static analysis and store the generated graphs.
```
./run.sh -help
USAGE: ./run.sh [ ( -targ CLASS | -mut CLASS ) [ -meth METHOD ] | -allclasses [ -graph2vec TOOLNAME] | -cpgtofile ]
	-targ CLASS: run on a single CLASS file
	-mut CLASS: run on a mutated CLASS file
	-meth METHOD: run on a specific METHOD
	-allclasses: run on all class files in SOURCE_ANALYSIS_FOLDER
	-cpgtofile: run on all class files in SOURCE_ANALYSIS_FOLDER and store graph in nedoFolder/graphDB
	-graph2vec TOOLNAME: print graph on file as input format for TOOLNAME
TOOLNAME list separated by semicolon : (Example: -graph2vec struc2vec:CGMM )
```
### Script load.sh
Load stored graphs in DB_GRAPH_FOLDER set in config.txt and then can output the graph as input format for some external tools (available tools: struc2vec, CGMM).
```
./load.sh -help
USAGE: ./load.sh [ -dgf PATH_DB_GRAPH_FOLDER ] [ -graph2vec TOOLNAME ]"
	If '-dgf' not set, needs to be set DB_GRAPH_FOLDER in Config.txt
	-graph2vec TOOLNAME: print graph on file as input format for TOOLNAME (see Readme for available TOOLNAME options)
	TOOLNAME list separated by semicolon : (Example: -graph2vec struc2vec:CGMM )
```
### File config.txt
File stores all the path to the data, libraries and class files necesarry to run the project.
In details, the variables that must be set:
SOURCE_ANALYSIS_FOLDER=<The root folder of the project to analyse> (for instance: /home/giacomo/ApacheLang3.4/src/main/java)
PACKAG_ANALYSIS_FOLDER=<The package folder of the project to analyse> (for instance: /org/apache/commons/lang3)
MUTATION_FOLDER=<The path to the folder which contain the mutated code to insert into the project>
DEFAULT_MAIN_CLASS=<Name of the class with a main method, used as main class for Soot. The file has to be placed in SOURCE_ANALYSIS_FOLDER/PACKAG_ANALYSIS_FOLDER>
DB_GRAPH_FOLDER=<Path to the folder which contained stored graph, for load.sh script>

### Examples
```
./run.sh -targ AnnotationUtils -graph2vec struc2vec
./run.sh -mut builder.Builder
./run.sh -allclasses -graph2vec CGMM:struc2vec
./run.sh -targ builder.Builder -meth toString
./run.sh -cpgtofile
```
## Authors

* **Giacomo Iadarola** - *contributor* - [Djack1010](https://github.com/Djack1010)
