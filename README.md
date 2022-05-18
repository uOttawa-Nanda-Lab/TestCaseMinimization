# Black-box, Similarity-based Test Case Minimization
This is the _Data_ and _Code_ related to our Test Case Minimization report. The following is a step-by-step guideline to run all the steps of our proposed approach, including the generation of Abstract Syntax Trees (ASTs) for test suites, the calculation of similarity measures, and the search-based algorithms. This helps reproduce the results reported in the report.

## Data
Due to space restrictions on GitHub, all data files are uploaded on OneDrive and can be accessed through [THIS LINK](https://uottawa-my.sharepoint.com/personal/tghaleb_uottawa_ca/_layouts/15/guestaccess.aspx?folderid=0f0805c2fc6494ce09e68304d1a11e7bb&authkey=AYEuYQ3GyZrGzm-Vr3KfjUw&e=jnipWh). Data files should be placed in the `Data` directory after being downloaded.

## AST Generation:
The source code for this step is in the `Code/AST/` directory.

### Requirements:
- Eclipse IDE (the version we used was 2021-12)
- The libraries (the _.jar_ files in the `Code/AST/lib/` directory) 

### Input:
All zipped data files should be unzipped before running each step.
* Data/suites_all_tests.zip => Data/suites_all_tests/
* Data/suites_changed_tests.zip => Data/suites_changed_tests/
* Data/suites_relevant_tests.zip => Data/suites_relevant_tests/

### Output:
* Data/defects4j-asts-all
* Data/defects4j-asts-changed

### Running the experiment
To generate ASTS for all test cases in the project test suites, the `Code/AST/src/GenerateAST.java` file should be compiled and run using the Eclipse IDE by including all the required _.jar_ files in the `Code/AST/lib` directory as part of the classpath. 

We provide a bash script along with a pre-generated _.jar_ file in the `Code/AST/bin` directory to run this step, as follows:

```console
cd Code/AST
bash generate_ast.sh
```

Each test class file in the `Data/suites_all_tests/` and `Data/suites_changed_tests` directories is parsed to generate a corresponding AST (saved in an XML format in `Data/defects4j-asts-all` and `Data/defects4j-asts-changed`) for each test case method.

---

## Similarity Measurement:
The source code for this step is in the `Code/Similarity/` directory.

### Requirements:
- Eclipse IDE (the version we used was 2021-12)
- The libraries (the _.jar_ files in the `Code/Similarity/lib/` directory) 

### Input:
* Data/defects4j-asts-all
* Data/defects4j-asts-changed

### Output:
* Data/similarity_measurements

### Running the experiment
To measure the similarity between each pair of test cases, the `Code/Similarity/src/SimilarityMeasurement.java` file should be compiled and run using the Eclipse IDE by including all the required _.jar_ files in the `Code/Similarity/lib` directory as part of the classpath. 

We provide a bash script along with a pre-generated _.jar_ file in the `Code/Similarity/bin` directory to run this step, as follows:

```console
cd Code/Similarity
bash measure_similarity.sh
```

All XML files for each project in the `Data/defects4j-asts-all/` and `Data/defects4j-asts-changed` directories are parsed to create pairs of XML files containing one test case from the  `Data/defects4j-asts-changed` directory with another test case from the `Data/defects4j-asts-all/` directory. All similarity measurements are saved in the `Data/similarity_measurements` directory.

---

## Search-based Minimization Algorithms:
The source code for this step is in the `Code/Minimization/` directory.

### Requirements:
To run this step, Python 3 is required (the version we used was `python 3.10`). Also, the libraries in the `requirements.txt` file should be installed, as follows:

```console
cd Code/Minimization
pip install -r requirements.txt
```

### Input:
* Data/similarity_measurements

### Output:
* Data/minimization_results

### Running the minimization experiment
To minimize the test suites in our dataset, the following bash script should be executed:

```console
bash minimization.sh
```

All similarity measurements files in the `Data/similarity_measurements` directory are parsed for each version of the projects, independently. Each version is run 10 times using three minimization budgets (25%, 50%, and 75%). Genetic Algorithm (GA) is run using four similarity measures, namely top-down, bottom-up, combined, and tree edit distance. Non-Dominated Sorting Genetic Algorithm II (NSGA-II) is run using two combinations of similarity measures: top-down with bottom-up and combined, and tree edit distance. The minimization results are generated in the `Data/minimization_results` directory.

### Running the random selection experiment
To run the random selection experiment, the following bash script should be executed:

```console
bash random_selection.sh
```

### Evaluating the Fault Detection Rate (FDR) results

To evaluate the minimization results, the following bash script should be executed:

```console
bash evaluate_results.sh
```

---

## Execution Time:
The source code for this step is in the `Code/Time/` directory, which requires Python 3 to run (the version we used was `python 3.10` with `pandas 1.4`).

### Input:
* Data/ast_generation_time
* Data/similarity_measurements
* Data/minimization_results

### Output:
* Data/execution_time_results

### Running the minimization experiment
To calculate the average execution time required to run all the steps of the test case minimization solution, the following bash script should be executed:

```console
cd Code/Time
bash time.sh
```
