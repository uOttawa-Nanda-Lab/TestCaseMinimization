data_path="../../Data"
test_suites="suites_all_tests"
relevant_tests="suites_relevant_tests"
asts_output="defects4j-asts-all"
time_output="ast_generation_time"
java -jar bin/generate_ast.jar $data_path $test_suites $relevant_tests $asts_output $time_output

test_suites="suites_changed_tests"
relevant_tests="suites_relevant_tests"
asts_output="defects4j-asts-changed"
time_output="ast_generation_time"
java -jar bin/generate_ast.jar $data_path $test_suites $relevant_tests $asts_output $time_output
