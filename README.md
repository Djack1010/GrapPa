# GrapPa

The tool GrapPa implements the approach presented in the Master's thesis "Graph-based Classification for Detecting Instances of Bug Patterns" by Giacomo Iadarola, submitted at the Technische Universitat Darmstadt in October 2018. The name comes from the first (Graph) and the last (Patterns) words of the thesis title.

The approach converts Java source code files into graphs (Code Property Graphs) and then uses trained machine learning models to classify each of them as buggy or non-buggy, with regard of a specific bug pattern.

The tool contains three trained models to detect Null Pointer, Array Index Out of Bounds and String Index Out of Bounds Exceptions, and can be easily extended to more bug patterns by providing datasets of buggy/non-buggy examples.

The three datasets used for training the models are available at https://github.com/Djack1010/BUG_DB.

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
Need to be cloned on local machine and set the `CGMM_FOLDER` in `config.txt`

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

The file `\nedo\GeneralScriptsFolder\RunSoot\config.txt` needs to be set properly to run the scripts.

### Script run.sh

Run GrapPa on a target project folder set in Config.txt. It performs the static analysis and store the generated graphs.
```
USAGE: ./run.sh
[ OP CLASS [ -meth METHOD] | -analysis [ -depen PATH ] [ -bp BUG_PAT ] | -allclasses [ -graph2vec TOOLNAME] | -cpgtofile ]
Available OP = -targ | -mut 
	-targ CLASS: run on a single CLASS file
	-mut CLASS: run on a mutated CLASS file
	Available OP2 = -meth
		-meth METHOD: run on a specific METHOD

-analysis: classify class files in SOURCE_ANALYSIS_FOLDER
	-depen PATH: add dependencies to class path
	-bp BUG_PAT: select bug pattern for classifying data [default NULL]
	BUG_PAT available: NULL | ARRAY | STRING

-targAnalysis FILE: classify targeted class file in SOURCE_ANALYSIS_FOLDER
	-bp BUG_PAT: select bug pattern for classifying data [default NULL]
	BUG_PAT available: NULL | ARRAY | STRING

-allclasses: run on all class files in SOURCE_ANALYSIS_FOLDER
	-graph2vec TOOLNAME: print graph on file as input format for TOOLNAME (see Readme for available TOOLNAME options)
	TOOLNAME list separated by semicolon : (Example: -graph2vec struc2vec:CGMM )

-cpgtofile: run on all class files in SOURCE_ANALYSIS_FOLDER and store graph in project_folder/graphDB

```
The most important option is the `-analysis` argument, which enable all the GrapPa's components sequentially on the source code of an input project. It has some optional arguments to set, `-depen PATH` in case that dependencies are required to run the input project, and `-bo BUG_PAT` to select the bug pattern we want to analyze, otherwise `NULL` is set as default (the Null Pointer Bug Pattern). 
The analysis takes as input the project referenced in the variable `SOURCE_ANALYSIS_FOLDER`, in the `config.txt` file. Then, it runs the CPGs generation component and creates a graph for every method encountered in the .java files in `SOURCE_ANALYSIS_FOLDER`. After that, it exports the graph in the CGMM format, generating .adjlist files. The second GrapPa's component then vectorizes the graphs, and each graph is encoded using the trained model chosen by the `-bp BUG_PAT` argument. Finally, the vectors are taken as input by the third and last GrapPa's component, which use the neural network model to classify them and output the result.

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
CGMM_FOLDER=<Path to the folder which contains the CGMM tool, available at https://github.com/Djack1010/CGMM.git>

### Examples
```
./run.sh -targ AnnotationUtils -graph2vec struc2vec
./run.sh -mut builder.Builder
./run.sh -allclasses -graph2vec CGMM:struc2vec
./run.sh -targ builder.Builder -meth toString
./run.sh -cpgtofile
./run.sh -analysis
```
## Authors & References

* **Giacomo Iadarola** - *main contributor* - [Djack1010](https://github.com/Djack1010)

Cite this work by refering to the Master thesis (bibtex format):
```
@mastersthesis{iadarola2018graph,
  title={Graph-based classification for detecting instances of bug patterns},
  author={Iadarola, Giacomo},
  year={2018},
  school={University of Twente}
}
```
