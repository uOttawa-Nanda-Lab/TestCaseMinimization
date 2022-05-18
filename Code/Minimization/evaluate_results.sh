data_path="../../Data"
minimization_results="$data_path/minimization_results"
faults_tests_path="$data_path/faults_tests.csv"

python3 src/evaluate_results.py $minimization_results $faults_tests_path