data_path="../../Data"
changed_asts_path="$data_path/defects4j-asts-changed"
all_asts_path="$data_path/defects4j-asts-all"
measurements_output_path="$data_path/similarity_measurements"
level="TestMethods"

for project in $(ls $changed_asts_path); do 
    echo $project; 
    java -Xms12g -Xmx12g -jar bin/measure_similarity.jar $project $changed_asts_path $all_asts_path $measurements_output_path $level
done;
