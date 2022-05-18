import pandas as pd
import os
import sys

def evaluate_results(minimization_results, faults_tests_path):
    if os.listdir(minimization_results):
        faults_tests = pd.read_csv(faults_tests_path)
        budgets = os.listdir(minimization_results)
        all_budget_results = {}
        for budget in budgets:
            type_results = {}
            if os.path.isdir(minimization_results+'/'+budget):
                runs = os.listdir(minimization_results+'/'+budget)
                projs_runs = {}
                for run in runs:
                    if os.path.isdir(minimization_results+'/'+budget+'/'+run):
                        sim_types = os.listdir(minimization_results+'/'+budget+'/'+run)
                        for sim_type in sim_types:
                            if budget+'-'+sim_type not in all_budget_results.keys():
                                all_budget_results[budget+'-'+sim_type] = []
                            if os.path.isdir(minimization_results+'/'+budget+'/'+run+'/'+sim_type):
                                if sim_type not in type_results.keys():
                                    type_results[sim_type] = []
                                projs = os.listdir(minimization_results+'/'+budget+'/'+run+'/'+sim_type)
                                versions_ = []
                                for proj in projs:
                                    if proj.endswith('.csv'):
                                        project_name = proj.replace('.csv', '')
                                        GA_results_proj = pd.read_csv(minimization_results+'/'+budget+'/'+run+'/'+sim_type+'/'+proj)
                                        faults_tests_proj = faults_tests[faults_tests.project == project_name]
                                        if budget+'%' + '-' + sim_type + '-' + project_name not in projs_runs.keys():
                                            projs_runs[budget+'%' + '-' + sim_type + '-' + project_name] = []
                                        faults_detected = 0
                                        versions_.append(len(GA_results_proj))
                                        for _, row in GA_results_proj.iterrows():
                                            version = row['version']
                                            subset = row['subset']
                                            faulty_tests = list(faults_tests_proj[faults_tests_proj.version == version].test_name)
                                            tests_in_subset = [test for test in faulty_tests if test in subset]
                                            faults_detected += 1 if len(tests_in_subset) > 0 else 0
                                            all_budget_results[budget+'-'+sim_type] = all_budget_results[budget+'-'+sim_type] + [float(1 if len(tests_in_subset) > 0 else 0)]
                                        FaultsDetectionRate = faults_detected/len(GA_results_proj) if len(GA_results_proj) > 0 else 'NaN'
                                        projs_runs[budget+'%' + '-' + sim_type + '-' + project_name] = projs_runs[budget+'%' + '-' + sim_type + '-' + project_name] + [float(FaultsDetectionRate)]
                                        type_results[sim_type] = type_results[sim_type] + [float(FaultsDetectionRate)]

                print('------',budget+'%','------')
                for proj in projs_runs.keys():
                    print(proj, ':', ('%.2f' % round(sum(projs_runs[proj])/len(projs_runs[proj]), 2)))
                print('------',budget+'%','Average ------')
                for result_type in type_results.keys():
                    print(result_type, ':', round(sum(type_results[result_type])/len(type_results[result_type]), 2) if len(type_results[result_type]) > 0 else '')

        print('------',budget+'%','Overall ------')
        for budget_sim in all_budget_results.keys():
            print(budget_sim, ':', round(sum(all_budget_results[budget_sim])/len(all_budget_results[budget_sim]), 2) if len(all_budget_results[budget_sim]) > 0 else '')

if __name__ == '__main__':
    minimization_results = sys.argv[1]
    faults_tests_path = sys.argv[2]

    evaluate_results(minimization_results, faults_tests_path)
