import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;  
import java.util.Collections;
import java.util.Comparator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import simpack.accessor.tree.SimpleTreeAccessor;
import simpack.api.ITreeNode;
import simpack.measure.tree.TopDownOrderedMaximumSubtree;
import simpack.measure.tree.TreeEditDistance;
import simpack.measure.tree.BottomUpMaximumSubtree;
import simpack.util.conversion.WorstCaseDistanceConversion;
import simpack.util.tree.comparator.NamedTreeNodeComparator;
import simpack.util.xml.XMLIterator;
import simpack.util.xml.XMLVisitor;

public class SimilarityMeasurement {
	List<String> all_test_cases = new ArrayList<String>();
	List<String> changed_test_cases = new ArrayList<String>();
	HashMap<String, String> test_cases_sims = new HashMap<String, String>();
	HashMap<String, String> test_cases_prev_sims = new HashMap<String, String>();

	public ITreeNode parseXmlToITreeNode(File xml) throws Exception{
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setValidating(false);
	    DocumentBuilder dBuilder = factory.newDocumentBuilder();
	    Document doc = dBuilder.parse(xml);
	    Node root = (Node) doc.getDocumentElement();
    	
    	XMLVisitor visitor = new XMLVisitor();
	    XMLIterator xmlIterator = new XMLIterator(true, true);

    	xmlIterator.setVisitor(visitor);
    	xmlIterator.scanNodes(root);

    	return visitor.getTree();
    }
    
	public void calculateTreeSimilarity(String changed_ast_path, String all_ast_path, String sims_path) throws Exception {    
		all_test_cases = new ArrayList<String>();
		changed_test_cases = new ArrayList<String>();
		test_cases_sims = new HashMap<String, String>();

		File[] changed_ast_files = new File(changed_ast_path).listFiles();
		if (changed_ast_files.length != 0) {
	        for (File ast_file_1 : changed_ast_files) {
	        	 if(ast_file_1.isFile()) {
					 String test_case_1 = ast_file_1.getName().toString().replaceAll("\\.xml$", "");
					 test_case_1 = test_case_1.substring(test_case_1.lastIndexOf("/")+1);
					 
		       		 if(!changed_test_cases.contains(test_case_1)) changed_test_cases.add(test_case_1);
		       		 
					 ITreeNode xmlTreeNode1 = parseXmlToITreeNode(ast_file_1);
					 File[] all_ast_files  = new File(all_ast_path).listFiles();
			         for (File ast_file_2 : all_ast_files) {
		        	    if(ast_file_2.isFile()) {
			        	   String test_case_2 = ast_file_2.getName().toString().replaceAll("\\.xml$", "");
			        	   test_case_2 = test_case_2.substring(test_case_2.lastIndexOf("/")+1);
	
			       		   if(!all_test_cases.contains(test_case_2)) all_test_cases.add(test_case_2);
			       		   
			        	   if (!test_case_1.equals(test_case_2) && !changed_test_cases.contains(test_case_2)) {
				       		   ITreeNode xmlTreeNode2 = parseXmlToITreeNode(ast_file_2);
					       	   
				       		   long TopDownStartTime = System.nanoTime();
				       		   TopDownOrderedMaximumSubtree TopDown = new TopDownOrderedMaximumSubtree(new SimpleTreeAccessor(xmlTreeNode1), new SimpleTreeAccessor(xmlTreeNode2), new NamedTreeNodeComparator());              
				       		   Double TopDownSim = TopDown.getSimilarity();
				       		   long TopDownEndTime = System.nanoTime();
				       		   long TopDownElapsedTime = TopDownEndTime - TopDownStartTime;
				       		
				       		   long BottomUpStartTime = System.nanoTime();
				       		   BottomUpMaximumSubtree BottomUp = new BottomUpMaximumSubtree(new SimpleTreeAccessor(xmlTreeNode1), new SimpleTreeAccessor(xmlTreeNode2), null, true, true);
					       	   Double BottomUpSim = BottomUp.getSimilarity();
				       		   long BottomUpEndTime = System.nanoTime();
				       		   long BottomUpElapsedTime = BottomUpEndTime - BottomUpStartTime;
					       	   
				       		   long CombinedStartTime = System.nanoTime();
					       	   Double CombinedSimilarity = new CombinedSimilarity().getCombinedSimilarity(TopDown, BottomUp);
				       		   long CombinedEndTime = System.nanoTime();
				       		   long CombinedElapsedTime = CombinedEndTime - CombinedStartTime;

				       		   long EditDistanceStartTime = System.nanoTime();
					           TreeEditDistance EditDistance = new TreeEditDistance(new SimpleTreeAccessor(xmlTreeNode1), new SimpleTreeAccessor(xmlTreeNode2), new NamedTreeNodeComparator(), new WorstCaseDistanceConversion());
					       	   Double EditDistanceSim = EditDistance.getSimilarity();
				       		   long EditDistanceEndTime = System.nanoTime();
				       		   long EditDistanceElapsedTime = EditDistanceEndTime - EditDistanceStartTime;

				       		   test_cases_sims.put(test_case_1 + "," + test_case_2, Double.toString(TopDownSim) + "," + TopDownElapsedTime + "," + (BottomUpSim == null ? Double.toString(0.0) : Double.toString(BottomUpSim)) + "," + BottomUpElapsedTime + "," + Double.toString(CombinedSimilarity) + "," + CombinedElapsedTime + "," + Double.toString(EditDistanceSim) + "," + EditDistanceElapsedTime);
			       		   }
		        	    }
			         }
	        	 }
			}
		}
		else {
			File[] all_ast_files  = new File(all_ast_path).listFiles();
	        for (File ast_file_2 : all_ast_files) {
	        	if(ast_file_2.isFile()) {
	        	   String test_case_2 = ast_file_2.getName().toString().replaceAll("\\.xml$", "");
	        	   test_case_2 = test_case_2.substring(test_case_2.lastIndexOf("/")+1);
		       		if(!all_test_cases.contains(test_case_2)) 
		       			all_test_cases.add(test_case_2);
	        	}
       	    }
		}
	}
	
	public String getFileContent(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		StringBuilder fileData = new StringBuilder();
		  
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}

		reader.close();
		return fileData.toString();
	}

	public HashMap<String, String> getSimilarityMeasurementsFromFile(String sims_file) throws IOException {
		HashMap<String, String> sims = new HashMap<String, String>();

		File file = new File(sims_file);
		String csv = getFileContent(file);
		int i = 0;
		for (String line : csv.split("\n")) {
			i++;
			if (i == 1)
				continue;
			String[] cols = line.split(",");
			sims.put(cols[0] + "," + cols[1], cols[2] + "," + cols[3] + "," + cols[4] + "," + cols[5] + "," + cols[6] + "," + cols[7] + "," + cols[8] + "," + cols[9]);
		}
		return sims;
	}
	
	public void getSimilarityForMinimization(int index, String sims_path, String version, int start) throws Exception {    
	    if(start != 0)
	    	test_cases_prev_sims = getSimilarityMeasurementsFromFile(sims_path + "/" + start + ".csv");

		if (index > 1) {
			List<String> original_test_cases = new ArrayList<String>(all_test_cases);
			original_test_cases.removeAll(changed_test_cases);
			for(String tc_1 : original_test_cases) {
				HashMap<String, String> orig_sim = (HashMap<String, String>) test_cases_prev_sims.entrySet().stream().filter(t->t.getKey().startsWith(tc_1+",")).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			    for(String tc_pair : orig_sim.keySet())
			    	if (original_test_cases.contains(tc_pair.split(",")[1]))
			    		test_cases_sims.put(tc_pair, orig_sim.get(tc_pair));
			}
	    }
	    
        String SimilarityForMinimization = sims_path + "/" + version + ".csv";
		FileWriter SimilarityForMinimizationWriter = new FileWriter(SimilarityForMinimization);
		SimilarityForMinimizationWriter.write("test_case_1,test_case_2,top_down_similarity,top_down_time_nanosec,bottom_up_similarity,bottom_up_time_nanosec,combined_similarity,combined_time_nanosec,tree_edit_distance_similarity,tree_edit_distance_time_nanosec\n");
	    for(String test_case : test_cases_sims.keySet())
    	    SimilarityForMinimizationWriter.write(test_case + "," + test_cases_sims.get(test_case) + "\n");
		SimilarityForMinimizationWriter.close();
		test_cases_prev_sims = new HashMap<String, String>(test_cases_sims);
	}
	
	public static void main(String[] args) throws Exception {
		String project_name = args[0];
		String changed_asts_path = args[1];
		String all_asts_path = args[2];
		String sims_path = args[3];
		String level = args[4];
		int start = args.length == 6 ? Integer.parseInt(args[5]) : 0;
		
		String project_changed_asts_path = changed_asts_path + "/" + project_name + "/" + level + "/";
		String project_all_asts_path = all_asts_path + "/" + project_name + "/" + level + "/";
		String project_sims_path = sims_path + "/" + project_name;

		if (!new File(changed_asts_path).exists() || !new File(all_asts_path).exists() ) {
			System.out.println("AST input directories might not exist..");
			System.exit(0);
		}
		
		if (!new File(sims_path).exists()) new File(sims_path).mkdirs();
		if (!new File(project_sims_path).exists()) new File(project_sims_path).mkdirs();

		String commit_dirs[] = Arrays.stream(new File(project_changed_asts_path).listFiles()).map(File::getName).toArray(String[]::new);
		int[] versions = Arrays.stream(commit_dirs).mapToInt(Integer::parseInt).toArray();  
		Arrays.sort(versions);
		SimilarityMeasurement sim = new SimilarityMeasurement();
		int i = 0;
		for (int version : versions) {
			i++;
			if (version > start) {
				System.out.println(" " + project_name + ": " + i + "/" + commit_dirs.length);
				sim.calculateTreeSimilarity(project_changed_asts_path + version, project_all_asts_path + version, project_sims_path);
				sim.getSimilarityForMinimization(i, project_sims_path, version + "", start);
			}
		}
	}
}