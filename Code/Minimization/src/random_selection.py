import numpy as np
import pandas as pd
import sys

def random_selection(unique_test_case_path, faults_tests_path, budget, runs):
    unique_test_case_names = pd.read_csv(unique_test_case_path)
    faults_tests = pd.read_csv(faults_tests_path)
    project_names = unique_test_case_names.project.unique()

    projs_runs = {}
    for proj in project_names:
        print(proj)
        unique_test_case_names_proj = unique_test_case_names[unique_test_case_names.project == proj]
        faults_tests_Pro = faults_tests[faults_tests.project == proj]
        Versions = unique_test_case_names_proj.version.unique()
        
        if proj not in projs_runs.keys():
            projs_runs[proj] = []

        for run in range(1, runs+1):
            faults_detected = 0
            for version in Versions:
                unique_test_case_names_proj_version = unique_test_case_names_proj[unique_test_case_names_proj.version == version]
                all_test_cases = list(unique_test_case_names_proj_version.test_case.unique())
                
                #Randomly select a subset
                np.random.seed(run)
                n_select = round(len(all_test_cases)*budget/100)
                random_subset = np.random.choice(all_test_cases, n_select)
                
                #Evaluate the result
                faulty_tests = list(faults_tests_Pro[faults_tests_Pro.version == version].test_name)
                faulty_tests = [test.replace("::", ".") for test in faulty_tests]
                tests_in_subset = [test for test in faulty_tests if test in random_subset]
                faults_detected += 1 if len(tests_in_subset) > 0 else 0
        
            FaultsDetectionRatio = faults_detected/len(Versions) if len(Versions) > 0 else 'NaN'
            projs_runs[proj] = projs_runs[proj] + [round(float(FaultsDetectionRatio), 2)]

    print('------',budget+'%','------')
    print(projs_runs)
    for proj in projs_runs.keys():
        print(proj, ':', ('%.2f' % round(sum(projs_runs[proj])/len(projs_runs[proj]), 2)))

if __name__ == '__main__':
    unique_test_case_path = sys.argv[1]
    faults_tests_path = sys.argv[2]
    budget = sys.argv[3]
    runs = sys.argv[4]

    random_selection(unique_test_case_path, faults_tests_path, budget, runs)
