import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.io.File;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.Source;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class GenerateAST {
	String METHOD_TREE = "";
	List<String> visited_classes = new ArrayList<String>();
	List<String> visited_methods = new ArrayList<String>();

	public void extractAST(char[] test_file_content, String version_path, String test_file_name, String actual_tests_path, String relevant_tests_path, String project_version, FileWriter AST_generation_time_per_test_case) throws IOException {
		ASTParser parser = ASTParser.newParser(AST.JLS16);
		parser.setSource(test_file_content);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setEnvironment(null, new String[] {""}, new String[] {"UTF-8"}, true);
		parser.setUnitName("");
		
	    final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
	    ASTVisitor visitor = new ASTVisitor() {
    		String test_path = version_path + "/" + test_file_name;
			public String fixChars(String text) {
				return text.replace(">", "_")
						   .replace("<", "_")
						   .replace("[", "_")
						   .replace("]", "_")
						   .replace("(", "_")
						   .replace(")", "_")
						   .replace(",", "_")
						   .replace("/", "_")
						   .replace("\"", "_")
						   .replace("|", "_")
						   .replace("'", "_")
						   .replace("?", "_")
						   .replace(" ", "_");
			}
			
			public String escapeChars(String text) {
				return text.replace("&",  "&amp;")
						   .replace("'",  "&apos;")
						   .replace("\"", "&quot;")
						   .replace("<",  "&lt;")
						   .replace(">",  "&gt;"); 

			}
						
	    	public boolean visit(TypeDeclaration class_node) {
	   			if(visited_classes.contains(version_path+"#"+class_node.getName().toString()))
	    			return false;

	   			visited_classes.add(version_path+"#"+class_node.getName().toString());
	    		class_node.accept(new ASTVisitor() {
	    			int var_id_counter = 0;
	    			HashMap<String, String> var_ids = new HashMap<String, String>();
					public String geVarId(String var_name) {
						String var_id = "";
						if (var_ids.keySet().contains(var_name))
							var_id = var_ids.get(var_name);
						else {
							var_id = "id_" + ++var_id_counter;
							var_ids.put(var_name, var_id);
						}
						return var_id;
					}

			    	public boolean visit(MethodDeclaration method_node) {
			    		var_id_counter = 0;
			    		var_ids.clear();
			    		METHOD_TREE = "";
			    		if(!method_node.isConstructor() && !String.valueOf(method_node.modifiers()).contains("private") && (method_node.getName().toString().startsWith("test") || method_node.getName().toString().endsWith("Test") || String.valueOf(method_node.modifiers()).contains("@Test")|| String.valueOf(method_node.modifiers()).contains("@ParameterizedTest"))) {
				   			if(visited_methods.contains(version_path+"#"+class_node.getName().toString()+"."+method_node.getName().toString()))
				    			return false;

				   			visited_methods.add(version_path+"#"+class_node.getName().toString()+"."+method_node.getName().toString());

			    			long MethodStartTime = System.nanoTime();
			    			method_node.accept(new ASTVisitor() {
				    			public String getNodeName(String node_type) {
				    				return (mapNode.containsKey(node_type) ? mapNode.get(node_type) : node_type);
				    			}
				    			
			    				public boolean hasOperator(ASTNode node){
				    				if(node instanceof InfixExpression || node instanceof PrefixExpression || node instanceof PostfixExpression || node instanceof InstanceofExpression || node instanceof MethodInvocation)
					    				return true;
					    			else
					    				return false;
			    				}
			    				
			    				public String getOperator(ASTNode expr){
				    				if(expr instanceof InfixExpression)
					    				return operatorName.get(((InfixExpression)expr).getOperator().toString());
					    			else if(expr instanceof PrefixExpression)
				    					return operatorName.get(((PrefixExpression)expr).getOperator().toString());
					    			else if(expr instanceof PostfixExpression)
				    					return operatorName.get(((PostfixExpression)expr).getOperator().toString());
					    			else if(expr instanceof InstanceofExpression)
				    					return "INSTANCEOF";
					    			else
					    				return "";
			    				}
			    				
				    			public void exportXML(ASTNode node, boolean pre) {
				    				String node_type = NodeType[node.getNodeType()];
				    				String parent_type = NodeType[node.getParent().getNodeType()];
	
				    				if(node instanceof MethodDeclaration) {
		    							METHOD_TREE += pre? "<TESTCASE><SIGNATURE>" : (((MethodDeclaration)node).getBody() == null ? "</SIGNATURE></TESTCASE>" : "</TESTCASE>");
				    				}
				    				else if(node.getParent() instanceof IfStatement) {
				    					if (node == ((IfStatement)node.getParent()).getExpression())
				    						METHOD_TREE += (pre ? "<EXPR>" : "");
				    					else if (node == ((IfStatement)node.getParent()).getThenStatement()) {
				    						//if ((((IfStatement)node.getParent()).getThenStatement()) instanceof Block) 
				    						METHOD_TREE += pre ? "</EXPR><THEN>" : "</THEN>";
				    					}
				    					else if (node == ((IfStatement)node.getParent()).getElseStatement()) 
				    						METHOD_TREE += pre ? "<ELSE>" : "";
				    				}			    					
				    				else if(node.getParent() instanceof ForStatement) {
				    					if (node == ((ForStatement)node.getParent()).getBody())
				    						METHOD_TREE += pre ? "</EXPR><BODY>" : "</BODY>";
				    				}
				    				else if(node.getParent() instanceof WhileStatement) {
				    					if (node == ((WhileStatement)node.getParent()).getBody())
				    						METHOD_TREE += pre ? "</EXPR><BODY>" : "";
				    				}
				    				else if(node.getParent() instanceof DoStatement) {
				    					if (node == ((DoStatement)node.getParent()).getExpression())
				    						METHOD_TREE += pre ? "</BODY><EXPR>" : "";
				    				}
				    				else if(node.getParent() instanceof TryStatement) {
				    					if (node == ((TryStatement)node.getParent()).getBody())
				    						METHOD_TREE += pre ? "<BODY>" : "</BODY>";
				    					else if (node == ((TryStatement)node.getParent()).getFinally())
				    						METHOD_TREE += pre ? "<FINALLY>" : "</FINALLY>";
				    				}			    					
				    				else if(node.getParent() instanceof MethodDeclaration) {
				    					if (node == ((MethodDeclaration)node.getParent()).getBody()) {
				    						// Test method name is excluded
					    					METHOD_TREE += pre ? "</SIGNATURE><BODY>" : "</BODY>";
				    					}
				    				}				
				    				if(node != null && !IgnoreNodes.contains(node_type) && !(node instanceof MethodDeclaration)){
				    					if(getNodeName(node_type) == "TYPE" && (node.getParent() instanceof MethodDeclaration)) {
					    					if(node.toString().endsWith("Exception"))
						    						METHOD_TREE += (pre ? "<": "</") + "THROWS-" + fixChars(node.toString()) + ">";
					    					else {
						    						METHOD_TREE += (pre ? "<": "</") + "RETURN_TYPE-" + fixChars(node.toString()) + ">";
					    					}return;
					    				}
					    				else if(node instanceof MethodInvocation) {
			    							MethodInvocation call = (MethodInvocation)node;
				    						if(pre) {
				    							String name = (call.getExpression() == null ? "" : "") + call.getName();
					    							METHOD_TREE += "<" + getNodeName(node_type) + ">" + "<METHOD-" + fixChars(name) + "/><PARAMS>";
				    						}
				    						else
				    							METHOD_TREE += "</PARAMS></" + getNodeName(node_type) + ">";
				    						return;
				    					}
					    				else if (node instanceof Assignment) {
					    					String op = operatorName.get(((Assignment)node).getOperator().toString());
					    					METHOD_TREE += pre ? "<ASSIGNMENT-"+op+">" : "</ASSIGNMENT-"+op+">";
					    				}
					    				else if (node instanceof VariableDeclarationFragment) {
					    					METHOD_TREE += pre ? "<ASSIGNMENT>" : "</ASSIGNMENT>";
					    				}
					    				else if(node instanceof IfStatement) {
					    					if(((IfStatement)node).getElseStatement() == null)
						    					METHOD_TREE += pre ? "<IF>" : "</IF>";
					    					else
						    					METHOD_TREE += pre ? "<IF>" : "</ELSE></IF>";
	
					    				}
					    				else if(node instanceof ForStatement) {
					    					METHOD_TREE += pre ? "<FOR><EXPR>" : "</FOR>";
					    				}
					    				else if(node instanceof WhileStatement) {
					    					METHOD_TREE += pre ? "<WHILE><EXPR>" : "</BODY></WHILE>";
					    				}
					    				else if(node instanceof DoStatement) {
					    					METHOD_TREE += pre ? "<DO><BODY>" : "</EXPR></DO>";
					    				}
					    				else if(hasOperator(node)) {
				    						METHOD_TREE += (pre ? "<" : "</") + getOperator(node) + ">";
				    						if ((node.getParent() instanceof IfStatement && node == ((IfStatement)node.getParent()).getExpression()))
				    							METHOD_TREE += pre ? "" : "";
					    				}
					    				else if (Types.contains(node_type) || Names.contains(node_type) || Values.contains(node_type)) {
			    	    					if(!IgnoreNodes.contains(parent_type) && !Types.contains(parent_type) && !Names.contains(parent_type))
			    	    						if(Types.contains(node_type)) {
					    							METHOD_TREE += (pre ? "<" : "</") + getNodeName(node_type) + "-" + fixChars(node.toString()) + ">";
			    	    						}
			    	    						else {
				    	    						if(Names.contains(node_type) && parent_type.equals("METHOD_INVOCATION")) {
				    	    							if(parent_type.equals("METHOD_INVOCATION")) {
					    	    							MethodInvocation call = (MethodInvocation)node.getParent();
					    	    							boolean is_param = false;
					    	    							for (int i = 0; i < call.arguments().size(); i++) {
					    	    								if (node.toString().equals(call.arguments().get(i).toString())) {
					    	    									is_param = true;
					    	    									break;
					    	    								}
															}
					    	    							if(is_param) {
								    							METHOD_TREE += (pre ? "<" : "</") + getNodeName(node_type) + "-" + ((Values.contains(node_type) ? escapeChars(node.toString()) : fixChars(geVarId(node.toString())))) + ">";
					    	    							}
					    	    						}
				    	    						}
				    	    						else if(!(node.getParent() instanceof MethodDeclaration)) { // Skip method's name here
				    	    							if(!Values.contains(node_type))
					    	    							METHOD_TREE += (pre ? "<" : "</") + getNodeName(node_type) + "-" + fixChars(geVarId(node.toString())) + ">";
				    	    						}
			    	    						}
			    	    				}
			    	    				else {
			    	    					METHOD_TREE += (pre ? "<" : "</") + getNodeName(node_type) + ">";
			    	    				}
			    					}	    				
				    			}
				    			
				    			public boolean isIrrelevant(ASTNode node){
			    					if (node instanceof Javadoc){
			    						return true;
			    					}
			    					else if (node instanceof MethodInvocation){
			    						IMethodBinding binding = ((MethodInvocation)node).resolveMethodBinding();
			    						ITypeBinding declaringType = null;
			    						if(binding != null)
			    							declaringType = binding.getDeclaringClass();
			    						if(node.toString().startsWith("System.out.print") || 
			    							assertMethods.contains(((MethodInvocation)node).getName().toString()) ||
			    							(declaringType != null && declaringType.getQualifiedName().toString().toLowerCase().contains("slf4j".toLowerCase())) ||
				    						(declaringType != null && declaringType.getQualifiedName().toString().toLowerCase().contains("log4j".toLowerCase())) ||
			    							(declaringType != null && declaringType.getQualifiedName().toString().toLowerCase().contains("logback".toLowerCase()))
			    					      )
			    							return true;
			    						else
			    							return false;
					    			}
		    						else
		    							return false;
				    			}
				    			
				    			public boolean visit(MethodInvocation node){
				    				return isIrrelevant(node) ? false : true;
				    			}
	
				    			@Override
				    			public void preVisit(ASTNode node) {
				    				if (!isIrrelevant(node)) {
			    						exportXML(node, true);
										super.preVisit(node);
				    				}
				    			}
				    			
				    			@Override
				    			public void postVisit(ASTNode node) {
				    				if (!isIrrelevant(node)) {
					    				exportXML(node, false);
										super.postVisit(node);
				    				}
				    			}
				    		});
			    		
			    			// Fix misplaced closing XML tags
			    			METHOD_TREE = METHOD_TREE.replace("</THEN></THROW_STATEMENT>", "</THROW_STATEMENT></THEN>")
									  				 .replace("</THEN></RETURN_STATEMENT>", "</RETURN_STATEMENT></THEN>")
									  				 .replace("</THEN></BREAK_STATEMENT>", "</BREAK_STATEMENT></THEN>")
									  				 .replace("</THEN></CONTINUE_STATEMENT>", "</CONTINUE_STATEMENT></THEN>")
									  				 .replace("</BODY></BODY></FOR></FOR>", "</BODY></FOR></BODY></FOR>");
									  				
							try {
								FileWriter output = new FileWriter(test_path + "." + method_node.getName() + ".xml");
								output.write(prettyPrint(METHOD_TREE));							
								output.close();

				        		long MethodEndTime = System.nanoTime();
				           		long MethodElapsedTime = MethodEndTime - MethodStartTime;
	 		           		    AST_generation_time_per_test_case.write(project_version + ","  + test_file_name + "." + method_node.getName() + "," + MethodElapsedTime + "\n");
	 		           		    AST_generation_time_per_test_case.flush();
							}
							catch(Exception ex) {}
							
			    		}
			    		return super.visit(method_node);
			    	}
	    		});
	    		
	    		if (class_node != null && class_node.getSuperclassType() != null) {
	    			String superclass = class_node.getSuperclassType().toString();
	    			String super_class_path = getSuperClassLocation(superclass, actual_tests_path, relevant_tests_path);

	    			if (!super_class_path.equals("")) {	    			
		    			for (File file : new File(super_class_path).listFiles()) {
		    				if (file.getName().contains("."+superclass+".java")) {
		    					try {
		    						char[] test_content = readFileToString(file.getPath()).toCharArray();
			    					extractAST(test_content, version_path, test_file_name, actual_tests_path, relevant_tests_path, project_version, AST_generation_time_per_test_case);
		    					}
		    					catch (Exception ex) {}
				    			break;
		    				}
		    			}
	    			}
	    		}
	    		return super.visit(class_node);
	    	}
	    	
	    	public String getSuperClassLocation(String superclass, String actual_tests_path, String relevant_tests_path) {
    			for(File file : (new File(relevant_tests_path).listFiles()))
    				if (file.getName().contains(superclass+".java"))
    					return relevant_tests_path;
    			for(File file : (new File(actual_tests_path).listFiles()))
    				if (file.getName().contains(superclass+".java"))
    					return actual_tests_path;
    			return "";
	    	}
	   };
	   cu.accept(visitor);
	}
	
    public static String prettyPrint(String xml) throws TransformerException {
    	try {
	        Source xmlInput = new StreamSource(new StringReader(xml));
	        StreamResult xmlOutput = new StreamResult(new StringWriter());
	        Transformer transformer = TransformerFactory.newInstance().newTransformer();
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
	        transformer.transform(xmlInput, xmlOutput);
	        return xmlOutput.getWriter().toString();
    	}
    	catch(Exception ex) {
    		System.out.println(xml);
    		System.exit(0);
    		return xml;
    	}
    }

	public String readFileToString(String test_path) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(test_path));
 
		char[] buf = new char[10];
		int numRead = 0;
		StringBuilder fileData = new StringBuilder(1024);
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
 
		reader.close();
 
		return  fileData.toString();	
	}

	public void ASTExtraction(String data_path, String test_suites, String relevant_tests, String asts_output, String time_output) throws Exception {
		String suites_relevant_tests = data_path + "/" + relevant_tests;
		String test_suites_path = data_path + "/" + test_suites;
		String asts_path = data_path + "/" + asts_output;
		File[] projects_folders = new File(test_suites_path).listFiles();

		File ast_dir = new File(asts_path);
		if (!ast_dir.exists()){
			ast_dir.mkdirs();
		}

	    String AST_generation_time_per_version_path = data_path + "/" + time_output + "/" + time_output + "_per_version_for_" + test_suites + ".csv";
	    String AST_generation_time_per_test_case_path = data_path + "/" + time_output + "/" + time_output + "_per_test_case_for_" + test_suites + ".csv";
		FileWriter AST_generation_time_per_version = new FileWriter(AST_generation_time_per_version_path);
		FileWriter AST_generation_time_per_test_case = new FileWriter(AST_generation_time_per_test_case_path);
		AST_generation_time_per_version.write("project,version,ast_generation_time_nanosec\n");
		AST_generation_time_per_test_case.write("project,version,test_case,ast_generation_time_nanosec\n");

		int i = 0;
		for (File project_folder : projects_folders) {
			String project_name =  project_folder.getName();
			System.out.println(++i +":"+project_name);

			File project_ast_methods_dir = new File(asts_path + '/' + project_name);
			if (!project_ast_methods_dir.exists())
				project_ast_methods_dir.mkdirs();
			
			if(project_folder.listFiles() == null)
				continue;
			
			File project_versions[] = project_folder.listFiles();
			Arrays.sort(project_versions);
			
			for (File version_folder : project_versions) {
       		    long VersionStartTime = System.nanoTime();

				String version = version_folder.getName();
				String version_method_level_path = asts_path + '/' + project_name + "/" + version;
				String version_relevant_tests_path = suites_relevant_tests + "/" + project_name + "/" + version;
				File version_methods_dir = new File(version_method_level_path);
				
				if (!version_methods_dir.exists())
					version_methods_dir.mkdirs();
				if(version_folder.listFiles() == null) {
					System.out.println(version);
					continue;
				}
				
				for (File test_file : version_folder.listFiles()) {
					String test_file_name = test_file.getName();

					if (test_file_name.endsWith(".java")) {
						String test_file_path = test_file.getPath();
						char[] test_file_content = readFileToString(test_file_path).toCharArray();
						extractAST(test_file_content, version_method_level_path, test_file_name.replaceAll("\\.java$", ""), version_folder.getPath(), version_relevant_tests_path, project_name + "," + version, AST_generation_time_per_test_case);
					}
				}
				
       		    long VersionEndTime = System.nanoTime();
       		    long VersionElapsedTime = VersionEndTime - VersionStartTime;
       		    AST_generation_time_per_version.write(project_name + "," + version + "," + VersionElapsedTime + "\n");
       		    AST_generation_time_per_version.flush();
			}
		}
		AST_generation_time_per_version.close();
		AST_generation_time_per_test_case.close();
	}
	
	public static void main(String[] args) throws Exception {
		String data_path = args[0];
		String test_suites = args[1];
		String relevant_tests = args[2];
		String asts_output = args[3];
		String time_output = args[4];
		
		GenerateAST my_ast = new GenerateAST();
		my_ast.ASTExtraction(data_path, test_suites, relevant_tests, asts_output, time_output);
    }
	
	public static final Map<String, String> mapNode = new HashMap<String, String>(){{
		put("SINGLE_VARIABLE_DECLARATION", "PARAM");
		put("QUALIFIED_NAME", "NAME");
		put("SIMPLE_NAME", "NAME");
		put("PRIMITIVE_TYPE", "TYPE");
		put("SIMPLE_TYPE", "TYPE");
		put("QUALIFIED_TYPE", "TYPE");
		put("NAME_QUALIFIED_TYPE", "TYPE");
		put("UNION_TYPE", "TYPE");
		put("WILDCARD_TYPE", "TYPE");
		put("INTERSECTION_TYPE", "TYPE");
		put("ARRAY_TYPE", "TYPE");
		put("PARAMETERIZED_TYPE", "TYPE");
		put("VARIABLE_DECLARATION_STATEMENT", "VAR_DECL");
		put("BOOLEAN_LITERAL", "VALUE");
		put("CHARACTER_LITERAL", "VALUE");
		put("NUMBER_LITERAL", "VALUE");
		put("NULL_LITERAL", "VALUE");
		put("STRING_LITERAL", "VALUE");
		put("TYPE_LITERAL", "VALUE");
		put("IF_STATEMENT", "IF");
		put("FOR_STATEMENT", "FOR");
		put("ENHANCED_FOR_STATEMENT", "FOR");
		put("TRY_STATEMENT", "TRY");
		put("CATCH_CLAUSE", "CATCH");
		put("PARENTHESIZED_EXPRESSION", "PARENTHESIS");
		put("WHILE_STATEMENT", "WHILE");
		put("DO_STATEMENT", "DOWHILE");
		put("METHOD_INVOCATION", "CALL");
		put("SINGLE_MEMBER_ANNOTATION", "ANNOTATION");
		put("NORMAL_ANNOTATION", "ANNOTATION");
		put("ANNOTATION_TYPE_DECLARATION", "ANNOTATION");
		put("ANNOTATION_TYPE_MEMBER_DECLARATION", "ANNOTATION");
	}};
	public static final List<String> Types = Arrays.asList("PRIMITIVE_TYPE", "SIMPLE_TYPE", "QUALIFIED_TYPE", "NAME_QUALIFIED_TYPE", "UNION_TYPE", "WILDCARD_TYPE", "INTERSECTION_TYPE", "ARRAY_TYPE", "PARAMETERIZED_TYPE");
	public static final List<String> Names = Arrays.asList("SIMPLE_NAME", "QUALIFIED_NAME");
	public static final List<String> Values = Arrays.asList("BOOLEAN_LITERAL", "CHARACTER_LITERAL", "NUMBER_LITERAL", "NULL_LITERAL", "STRING_LITERAL", "TYPE_LITERAL");
	public static final List<String> Expressions = Arrays.asList("INFIX_EXPRESSION", "PREFIXEXPRESSION", "POSTFIXEXPRESSION", "INSTANCEOFEXPRESSION");
	public static final List<String> IgnoreNodes = Arrays.asList("MARKER_ANNOTATION", "MODIFIER", "BLOCK", "EXPRESSION_STATEMENT");
	public static final String [] NodeType = new String[] {
		"NONE",
		"ANONYMOUS_CLASS_DECLARATION",
		"ARRAY_ACCESS",
		"ARRAY_CREATION",
		"ARRAY_INITIALIZER",
		"ARRAY_TYPE",
		"ASSERT_STATEMENT",
		"ASSIGNMENT",
		"BLOCK",
		"BOOLEAN_LITERAL",
		"BREAK_STATEMENT",
		"CAST_EXPRESSION",
		"CATCH_CLAUSE",
		"CHARACTER_LITERAL",
		"CLASS_INSTANCE_CREATION",
		"COMPILATION_UNIT",
		"CONDITIONAL_EXPRESSION",
		"CONSTRUCTOR_INVOCATION",
		"CONTINUE_STATEMENT",
		"DO_STATEMENT",
		"EMPTY_STATEMENT",
		"EXPRESSION_STATEMENT",
		"FIELD_ACCESS",
		"FIELD_DECLARATION",
		"FOR_STATEMENT",
		"IF_STATEMENT",
		"IMPORT_DECLARATION",
		"INFIX_EXPRESSION",
		"INITIALIZER",
		"JAVADOC",
		"LABELED_STATEMENT",
		"METHOD_DECLARATION",
		"METHOD_INVOCATION",
		"NULL_LITERAL",
		"NUMBER_LITERAL",
		"PACKAGE_DECLARATION",
		"PARENTHESIZED_EXPRESSION",
		"POSTFIX_EXPRESSION",
		"PREFIX_EXPRESSION",
		"PRIMITIVE_TYPE",
		"QUALIFIED_NAME",
		"RETURN_STATEMENT",
		"SIMPLE_NAME",
		"SIMPLE_TYPE",
		"SINGLE_VARIABLE_DECLARATION",
		"STRING_LITERAL",
		"SUPER_CONSTRUCTOR_INVOCATION",
		"SUPER_FIELD_ACCESS",
		"SUPER_METHOD_INVOCATION",
		"SWITCH_CASE",
		"SWITCH_STATEMENT",
		"SYNCHRONIZED_STATEMENT",
		"THIS_EXPRESSION",
		"THROW_STATEMENT",
		"TRY_STATEMENT",
		"TYPE_DECLARATION",
		"TYPE_DECLARATION_STATEMENT",
		"TYPE_LITERAL",
		"VARIABLE_DECLARATION_EXPRESSION",
		"VARIABLE_DECLARATION_FRAGMENT",
		"VARIABLE_DECLARATION_STATEMENT",
		"WHILE_STATEMENT",
		"INSTANCEOF_EXPRESSION",
		"LINE_COMMENT",
		"BLOCK_COMMENT",
		"TAG_ELEMENT",
		"TEXT_ELEMENT",
		"MEMBER_REF",
		"METHOD_REF",
		"METHOD_REF_PARAMETER",
		"ENHANCED_FOR_STATEMENT",
		"ENUM_DECLARATION",
		"ENUM_CONSTANT_DECLARATION",
		"TYPE_PARAMETER",
		"PARAMETERIZED_TYPE",
		"QUALIFIED_TYPE",
		"WILDCARD_TYPE",
		"NORMAL_ANNOTATION",
		"MARKER_ANNOTATION",
		"SINGLE_MEMBER_ANNOTATION",
		"MEMBER_VALUE_PAIR",
		"ANNOTATION_TYPE_DECLARATION",
		"ANNOTATION_TYPE_MEMBER_DECLARATION",
		"MODIFIER",
		"UNION_TYPE",
		"DIMENSION",
		"LAMBDA_EXPRESSION",
		"INTERSECTION_TYPE",
		"NAME_QUALIFIED_TYPE",
		"CREATION_REFERENCE",
		"EXPRESSION_METHOD_REFERENCE",
		"SUPER_METHOD_REFERENCE",
		"TYPE_METHOD_REFERENCE",
		"MODULE_DECLARATION",
		"REQUIRES_DIRECTIVE",
		"EXPORTS_DIRECTIVE",
		"OPENS_DIRECTIVE",
		"USES_DIRECTIVE",
		"PROVIDES_DIRECTIVE",
		"MODULE_MODIFIER",
		"SWITCH_EXPRESSION",
		"YIELD_STATEMENT",
		"TEXT_BLOCK",
		"RECORD_DECLARATION",
		"PATTERN_INSTANCEOF_EXPRESSION"
	};
	public static final Map<String, String> operatorName = new HashMap<String, String>(){{
		put("*",  "TIMES");
		put("/",  "DIVIDE");
		put("%",  "MOD");
		put("+",  "PLUS");
		put("++", "PLUS_PLUS");
		put("-",  "MINUS");
		put("--", "MINUS_MINUS");
		put("<<", "LEFT_SHIFT");
		put(">>", "RIGHT_SHIFT_SIGNED");
		put(">>>","RIGHT_SHIFT_UNSIGNED");
		put("<",  "LESS");
		put(">" , "GREATER");
		put("<=", "LESS_EQUALS");
		put(">=", "GREATER_EQUALS");
		put("==", "EQUALS_EQUALS");
		put("!=", "NOT_EQUALS");
		put("^",  "XOR");
		put("&",  "AND");
		put("|",  "OR");
		put("&&", "AND_AND");
		put("||", "OR_OR");
		put("=", "EQUALS");
		put("+=", "PLUS_EQUALS");
		put("-=", "MINUS_EQUALS");
		put("*=", "TIMES_EQUALS");
		put("/=", "DIVIDE_EQUALS");
		put("%=", "DMOD_EQUALS");
		put("<<=", "LEFT_SHIFT_EQUALS");
		put(">>=", "RIGHT_SHIFT_EQUALS");
		put("&=", "AND_EQUALS");
		put("|=", "OR_EQUALS");
		put("^=", "EXCLUSIVE_OR_EQUALS");
	}};
	public static final List<String> assertMethods = Arrays.asList("assertArrayEquals", "assertEquals", "assertFalse", "assertNotNull", "assertNotSame", "assertNull", "assertSame", "assertThat", "assertTrue", "fail");

}