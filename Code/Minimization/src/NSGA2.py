import os
import sys
import time
import numpy as np
import pandas as pd
import csv
import autograd.numpy as anp

# NSGA2 Algorithm
from pymoo.algorithms.moo.nsga2 import NSGA2
from pymoo.util.termination.f_tol import MultiObjectiveSpaceToleranceTermination
from pymoo.optimize import minimize
from pymoo.factory import get_problem
from pymoo.core.problem import ElementwiseProblem,Problem
from pymoo.core.crossover import Crossover
from pymoo.core.mutation import Mutation
from pymoo.core.sampling import Sampling
from pymoo.factory import get_mutation

class InitialSampling(Sampling): 
    def _do(self, problem, n_samples, **kwargs):
        X = np.full((n_samples, problem.n_var), False, dtype=bool)

        for k in range(n_samples):
            I = np.random.permutation(problem.n_var)[:problem.n_max]
            X[k, I] = True

        return X

class SubsetProblem(ElementwiseProblem):
    def __init__(self,
                 L,
                 n_max,
                 df_sim
                 ):
        super().__init__(n_var=len(L), n_obj=2, n_constr=1)
        self.L = L #The L should be the unique variable names
        self.n_max = n_max
        self.df_sim = df_sim

    def _evaluate(self, x, out, *args, **kwargs):
        SelectedTestCases = self.L[x]

        df_sub = self.df_sim[self.df_sim['test_case_1'].isin(SelectedTestCases) & self.df_sim['test_case_2'].isin(SelectedTestCases)].copy()
        df_sub.sim_measure_1  = np.square(df_sub.sim_measure_1)
        df_sub.sim_measure_2 = np.square(df_sub.sim_measure_2)
        
        sim1 = sum(df_sub.groupby('test_case_1').sim_measure_1.max())
        sim2 = sum(df_sub.groupby('test_case_1').sim_measure_2.max())

        out["F"] = anp.column_stack([sim1, sim2])
        out["G"] = (self.n_max - np.sum(x)) ** 2

class BinaryCrossover(Crossover):
    def __init__(self):
        super().__init__(2, 1,prob=0.9)

    def _do(self, problem, X, **kwargs):
        n_parents, n_matings, n_var = X.shape

        _X = np.full((self.n_offsprings, n_matings, problem.n_var), False)

        for k in range(n_matings):
            p1, p2 = X[0, k], X[1, k]

            both_are_true = np.logical_and(p1, p2)
            _X[0, k, both_are_true] = True

            n_remaining = problem.n_max - np.sum(both_are_true)

            I = np.where(np.logical_xor(p1, p2))[0]

            S = I[np.random.permutation(len(I))][:n_remaining]
            _X[0, k, S] = True

        return _X

def MinimizationGA(project_name, input_path, output_path, sim_measure_1, sim_measure_2, budget, run):
    if len(os.listdir(input_path)) > 0:
        results_file_exists = False
        if os.path.isfile(output_path):
            results_file_exists = True
        results_file = open(output_path, 'a')
        writer = csv.writer(results_file)
        existing_versions = []
        if results_file_exists:
            existing_results = pd.read_csv(output_path)
            existing_versions = list(existing_results.version)
        else:
            writer.writerow(['project', 'version', 'sim_measure', 'budget', 'n_gen', 'time_seconds', 'function_value', 'subset'])
            results_file.flush()

        # Two similarity measures: [top-down & bottom-up] or [combined & tree edit distance]
        
        sim_path = input_path + "/" + project_name
         
        versions = os.listdir(sim_path)
        versions = sorted(versions, key=lambda x:int(x[:-4]))
        
        for version in versions:
            print("             Version :", version.replace('.csv', ''), "/", len(versions))
            version_number = int(version.replace('.csv', ''))
            if version_number not in existing_versions:
                existing_versions.append(version)
                start = time.time()
                sim_path_version = sim_path + "/" + version
                
                df_sim = pd.read_csv(sim_path_version, usecols = ['test_case_1', 'test_case_2', sim_measure_1, sim_measure_2])
                df_sim.columns = ['test_case_1', 'test_case_2', 'sim_measure_1', 'sim_measure_2']

                # Remove pairs of test cases: ["tc1,tc2" and "tc2,tc1"] => "tc1,tc2"
                df_sim = df_sim[~pd.DataFrame(np.sort(df_sim[['test_case_1', 'test_case_2']])).duplicated().values]
                
                tc_last = list(set(df_sim['test_case_2'].unique()) - set(df_sim['test_case_1'].unique()))[0]
                L = np.append(df_sim['test_case_1'].unique(), tc_last)
                
                test_case_1_list = []
                test_case_2_list = []
                sim_measure_1_list = []
                sim_measure_2_list = []
                
                for tc in L:
                    df_sub_1 = df_sim[df_sim.test_case_1 == tc]
                    test_case_1_list += list(df_sub_1.test_case_1)
                    test_case_2_list += list(df_sub_1.test_case_2)
                    sim_measure_1_list += list(df_sub_1.sim_measure_1)
                    sim_measure_2_list += list(df_sub_1.sim_measure_2)

                    df_sub_2 = df_sim[df_sim.test_case_2 == tc]
                    test_case_1_list += list(df_sub_2.test_case_2)
                    test_case_2_list += list(df_sub_2.test_case_1)
                    sim_measure_1_list += list(df_sub_2.sim_measure_1)
                    sim_measure_2_list += list(df_sub_2.sim_measure_2)

                df_sim_new = pd.DataFrame(list(zip(test_case_1_list,test_case_2_list,sim_measure_1_list,sim_measure_2_list)),
                                        columns=['test_case_1','test_case_2','sim_measure_1', 'sim_measure_2'])
                                        
                n_max = round(len(L) * int(budget)/100)
                problem = SubsetProblem(L, n_max, df_sim_new)

                try:
                    algorithm = NSGA2(pop_size = 100,
                            sampling = InitialSampling(),
                            crossover = BinaryCrossover(),
                            mutation = get_mutation("perm_inv", prob=0.01),
                            eliminate_duplicates = True)

                    termination = MultiObjectiveSpaceToleranceTermination(tol=0.0025,
                                                                        n_last=30,
                                                                        nth_gen=5,
                                                                        n_max_gen=1000)

                    result = minimize(problem,
                                    algorithm,
                                    termination,
                                    seed = int(run),
                                    verbose = False)
                
                    #print("Time elapsed:",lapsed)
                    #print("Function value:, result.F[0])
                    #print("Subset:", np.where(result.X))
                    #print(version, list(L[result.X]))
                    
                    res_list = result.F.tolist()
                    res_list.sort(key=lambda x: x[0])
                
                    function_value = res_list[round(len(res_list) * 0.5) - 1]
                    idx = result.F.tolist().index(function_value)
                
                    end = time.time()
                    elapsed = end - start

                    writer.writerow([project_name, version.replace('.csv', ''), sim_measure_1+'&'+sim_measure_2, budget, result.algorithm.n_gen, elapsed, function_value, list(L[result.X[idx]])])
                    results_file.flush()
                except Exception as e:
                            print("Version", version.replace('.csv', ''), 'has an exception ->', e)
            
if __name__ == '__main__':
    min_alg = sys.argv[0]
    project_name = sys.argv[1]
    sim_measure_1 = sys.argv[2]
    sim_measure_2 = sys.argv[3]
    input_path = sys.argv[4]
    output_path = sys.argv[5]
    budget = sys.argv[6]
    run = sys.argv[7]

    MinimizationGA(project_name, input_path, output_path, sim_measure_1, sim_measure_2, budget, run)
