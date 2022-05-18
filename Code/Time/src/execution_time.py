import pandas as pd
import os
import csv
import sys

# Execution Time for AST generation
def calculate_ast_generation_execution_time(ast_generation_path):
    ast_df = pd.read_csv(ast_generation_path)
    projects = ast_df.project.unique()
    project_avg_time = {}
    for project in projects:
        project_df = ast_df[ast_df['project'] == project]
        
        time = project_df.ast_generation_time_nanosec.mean()*(1e-9)
        
        project_avg_time[project] = time
    return project_avg_time

# Execution Time for each pair of test case 
def calculate_similarity_measurerment_execution_time_per_test_case_pair(similarity_path):
    projects = os.listdir(similarity_path)
    time_per_project = {}
    for project in projects:
        time_per_similarity = {}
        sim_path = similarity_path + '/' + project
        for root, dirs, files in os.walk(sim_path):
            versions = files
        versions = sorted(versions, key=lambda x:int(x[:-4]))
        
        top_down_versions = []
        bottom_up_versions = []
        combined_versions = []
        tree_edit_distance_versions = []
        
        for version in versions:
            sim_path_version = sim_path + '/' + version
            
            usecols = ['test_case_1', 'test_case_2', 'top_down_time_nanosec','bottom_up_time_nanosec','combined_time_nanosec','tree_edit_distance_time_nanosec']
            df_sim = pd.read_csv(sim_path_version, usecols = usecols)
            
            top_down_time = df_sim.top_down_time_nanosec.mean()
            top_down_versions.append(top_down_time)
            
            bottom_up_time = df_sim.bottom_up_time_nanosec.mean()
            bottom_up_versions.append(bottom_up_time)
            
            combined_time = df_sim.combined_time_nanosec.mean()
            combined_versions.append(combined_time)
            
            edit_time = df_sim.tree_edit_distance_time_nanosec.mean()
            tree_edit_distance_versions.append(edit_time)

        time_per_similarity['top_down_similarity_time_seconds'] = sum(top_down_versions)/len(top_down_versions) * (1e-9)
        time_per_similarity['bottom_up_similarity_time_seconds'] = sum(bottom_up_versions)/len(bottom_up_versions) * (1e-9)
        time_per_similarity['combined_similarity_time_seconds'] = sum(bottom_up_versions)/len(bottom_up_versions) * (1e-9)+sum(top_down_versions)/len(top_down_versions) * (1e-9)+sum(combined_versions)/len(combined_versions) * (1e-9)
        time_per_similarity['tree_edit_distance_similarity_time_seconds'] = sum(tree_edit_distance_versions)/len(tree_edit_distance_versions) * (1e-9)
        time_per_similarity['combined_similarity#tree_edit_distance_similarity_time_seconds'] = (sum(top_down_versions)/len(top_down_versions) * (1e-9)) + (sum(bottom_up_versions)/len(bottom_up_versions) * (1e-9))
        time_per_similarity['top_down_similarity#bottom_up_similarity_time_seconds'] = sum(bottom_up_versions)/len(bottom_up_versions) * (1e-9)+sum(top_down_versions)/len(top_down_versions) * (1e-9)+sum(combined_versions)/len(combined_versions) * (1e-9) + sum(tree_edit_distance_versions)/len(tree_edit_distance_versions) * (1e-9)
        time_per_project[project] = time_per_similarity

    return time_per_project

# Execution Time for calculating similarity of test cases per version
def calculate_similarity_measurerment_execution_time_per_version(similarity_path):
    projects = os.listdir(similarity_path)
    time_per_project = {}
    for project in projects:
        time_per_similarity = {}
        sim_path = similarity_path + '/' + project
        for root, dirs, files in os.walk(sim_path):
            versions = files
        versions = sorted(versions, key=lambda x:int(x[:-4]))
        
        top_down_versions = []
        bottom_up_versions = []
        combined_versions = []
        tree_edit_distance_versions = []
        
        for version in versions:
            sim_path_version = sim_path + '/' + version
            
            usecols = ['test_case_1', 'test_case_2', 'top_down_time_nanosec','bottom_up_time_nanosec','combined_time_nanosec','tree_edit_distance_time_nanosec']
            df_sim = pd.read_csv(sim_path_version, usecols = usecols)
            
            top_down_time = df_sim.top_down_time_nanosec.sum()
            top_down_versions.append(top_down_time)
            
            bottom_up_time = df_sim.bottom_up_time_nanosec.sum()
            bottom_up_versions.append(bottom_up_time)
            
            combined_time = df_sim.combined_time_nanosec.sum()
            combined_versions.append(combined_time)
            
            edit_time = df_sim.tree_edit_distance_time_nanosec.sum()
            tree_edit_distance_versions.append(edit_time)
        
        time_per_similarity['top_down_similarity_time_seconds'] = sum(top_down_versions)/len(top_down_versions) * (1e-9)
        time_per_similarity['bottom_up_similarity_time_seconds'] = sum(bottom_up_versions)/len(bottom_up_versions) * (1e-9)
        time_per_similarity['combined_similarity_time_seconds'] = sum(bottom_up_versions)/len(bottom_up_versions) * (1e-9)+sum(top_down_versions)/len(top_down_versions) * (1e-9)+sum(combined_versions)/len(combined_versions) * (1e-9)
        time_per_similarity['tree_edit_distance_similarity_time_seconds'] = sum(tree_edit_distance_versions)/len(tree_edit_distance_versions) * (1e-9)
        time_per_similarity['combined_similarity#tree_edit_distance_similarity_time_seconds'] = (sum(top_down_versions)/len(top_down_versions) * (1e-9)) + (sum(bottom_up_versions)/len(bottom_up_versions) * (1e-9))
        time_per_similarity['top_down_similarity#bottom_up_similarity_time_seconds'] = sum(bottom_up_versions)/len(bottom_up_versions) * (1e-9)+sum(top_down_versions)/len(top_down_versions) * (1e-9)+sum(combined_versions)/len(combined_versions) * (1e-9) + sum(tree_edit_distance_versions)/len(tree_edit_distance_versions) * (1e-9)
        time_per_project[project] = time_per_similarity
        
    return time_per_project

# Execution Time for search algorithm
def calculate_search_algorithms_execution_time(minimization_results_path):
    projects = ['Cli', 'Codec', 'Collections', 'Compress', 'Csv', 'Gson', 'JacksonCore', 'JacksonXml', 'Jsoup', 'JxPath']
    time_per_budget = {}
    for budget in ['50', '25', '75']:
        sims = ['top_down_similarity', 'bottom_up_similarity', 'combined_similarity', 'tree_edit_distance_similarity', 'combined_similarity#tree_edit_distance_similarity', 'top_down_similarity#bottom_up_similarity']
        time_per_project = {}
        for project in projects:
            time_per_similarity = {}
            for sim in sims:
                time_runs = []
                for run in range(1, 11):
                    minimization_results_path_budget_run_sim_project = minimization_results_path + '/' + budget + '/' + 'run_' + str(run) + '/' + sim + '/' + project + '.csv'
                    if os.path.isfile(minimization_results_path_budget_run_sim_project):
                        minimization_results = pd.read_csv(minimization_results_path_budget_run_sim_project)
                        time_runs.append(minimization_results.time.mean())
                avg_time = sum(time_runs) / len(time_runs)
                time_per_similarity[sim + '_time_seconds'] = avg_time
            time_per_project[project] = time_per_similarity 
        time_per_budget[budget] = time_per_project

    return time_per_budget

if __name__ == '__main__':
    ast_generation_path = sys.argv[1]
    similarity_path = sys.argv[2]
    minimization_results_path = sys.argv[3]
    time_results_path = sys.argv[4]
    time_results_file_per_version = sys.argv[5]
    sim_time_results_file_per_test_case_pair = sys.argv[6]

    print('Calculating AST generation execution time..')
    ast_generation_execution_time = calculate_ast_generation_execution_time(ast_generation_path)
    print('Calculating similarity measurerment execution time per test case pair..')
    similarity_measurerment_execution_time_per_test_case_pair = calculate_similarity_measurerment_execution_time_per_test_case_pair(similarity_path)
    print('Calculating similarity measurerment execution time per project version..')
    similarity_measurerment_execution_time_per_version = calculate_similarity_measurerment_execution_time_per_version(similarity_path)
    print('Calculating search algorithms execution time..')
    search_algorithms_execution_time = calculate_search_algorithms_execution_time(minimization_results_path)

    print('Generating execution time summary..')
    for budget in search_algorithms_execution_time.keys():
        budget_projects = search_algorithms_execution_time[budget]
        os.makedirs(time_results_path + '/' + budget)
        time_results_per_version_file = open(time_results_path + '/' + budget+'/' + time_results_file_per_version, 'w')
        time_per_version_writer = csv.writer(time_results_per_version_file)
        sim_time_results_per_test_case_pair_file = open(time_results_path + '/' + sim_time_results_file_per_test_case_pair, 'w')
        sim_time_per_test_case_pair_writer = csv.writer(sim_time_results_per_test_case_pair_file)
        time_per_version_writer.writerow(['project', 'similarity_measure', 'ast_generation_time_seconds', 'similarity_measureement_time_seconds', 'search_algorithms_time_seconds', 'total_time_seconds'])
        sim_time_per_test_case_pair_writer.writerow(['project', 'similarity_measure', 'ast_generation_time_seconds'])

        for project in budget_projects.keys():
            project_sims = budget_projects[project]
            for sim in project_sims.keys():
                srch_algm_time_per_sim = project_sims[sim]
                time_per_version_writer.writerow([project, sim, ast_generation_execution_time[project], similarity_measurerment_execution_time_per_version[project][sim], srch_algm_time_per_sim, ast_generation_execution_time[project] + similarity_measurerment_execution_time_per_version[project][sim] + srch_algm_time_per_sim])
                time_results_per_version_file.flush()
    time_results_per_version_file.close()
    
    for project in similarity_measurerment_execution_time_per_test_case_pair.keys():
        project_sims = similarity_measurerment_execution_time_per_test_case_pair[project]
        for sim in project_sims.keys():
            sim_time = project_sims[sim]
            sim_time_per_test_case_pair_writer.writerow([project, sim, sim_time])
            sim_time_results_per_test_case_pair_file.flush()
    sim_time_results_per_test_case_pair_file.close()

    print('Done.')