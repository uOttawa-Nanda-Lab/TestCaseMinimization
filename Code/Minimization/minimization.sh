data_path="../../Data"
input_path="$data_path/similarity_measurements"

for budget in 50 25 75; do
    echo "Budget: $budget"; 
    for run in {1..10}; do
        echo "   Run: $run"; 
        for project in $(ls $input_path); do
            echo "       Project: $project"; 
            for sim in "top_down_similarity" "bottom_up_similarity" "combined_similarity" "tree_edit_distance_similarity"; do
                mkdir -p $data_path/minimization_results/$budget/run_$run/$sim
                output_path="$data_path/minimization_results/$budget/run_$run/$sim/$project.csv"
                echo "          Similarity: $sim";
                python3 src/GA.py $project $sim $input_path $output_path $budget $run
            done;
            for sim in "top_down_similarity#bottom_up_similarity" "combined_similarity#tree_edit_distance_similarity"; do
                mkdir -p $data_path/minimization_results/$budget/run_$run/$sim
                input_path="$data_path/similarity_measurements"
                output_path="$data_path/minimization_results/$budget/run_$run/$sim/$project.csv"
                echo "          Similarity: $sim";
                sims=(${sim//#/ })
                sim1=${sims[0]}
                sim2=${sims[1]}
                python3 src/NSGA2.py $project $sim1 $sim2 $input_path $output_path $budget $run
            done;
        done;
    done;
done;
