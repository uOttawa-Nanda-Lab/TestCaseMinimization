data_path="../../Data"
unique_test_case_path="$data_path/projects_unique_test_cases.cvs"
faults_tests_path="$data_path/faults_tests.csv"
runs=10

for budget in 25 50 75; do
    python3 src/random_selection.py $minimization_results $faults_tests_path $budget $runs
done;