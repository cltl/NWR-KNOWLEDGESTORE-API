package vu.cltl.triple.read;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;
import org.apache.jena.riot.RDFDataMgr;
import vu.cltl.triple.TrigUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class DumpByType {

    static HashMap<String, TreeSet> instanceStatementMap = new HashMap<String, TreeSet>();
    static HashMap<String, ArrayList<String>> typeInstanceMap = new HashMap<String, ArrayList<String>>();
    
    static public void main (String [] args) {
        String pathToTripleFiles = "/Users/piek/Desktop/DigHum-2018/4775434/OBO_XML_7-2/sessions/rdf";
        Dataset dataset = TDBFactory.createDataset();
        ArrayList<File> tripleFiles = TrigUtil.makeRecursiveFileList(new File(pathToTripleFiles), ".trig");
        for (int i = 0; i < tripleFiles.size(); i++) {
            File trigFile = tripleFiles.get(i);
            //if (trigFile.getName().equals("all4.trig"))  dataset = RDFDataMgr.loadDataset(trigFile.getAbsolutePath());
            dataset = RDFDataMgr.loadDataset(trigFile.getAbsolutePath());
            makeStatementMap(dataset);
            System.out.println("typeInstanceMap = " + typeInstanceMap.size());
            System.out.println("instanceStatementMap = " + instanceStatementMap.size());
            dumpDataToXls(trigFile);
            dataset = null;
        }
    }



    public static void makeStatementMap (Dataset dataset) {
        instanceStatementMap = new HashMap<String, TreeSet>();
        typeInstanceMap = new HashMap<String, ArrayList<String>>();
        Model namedModel = dataset.getDefaultModel();
        StmtIterator siter = namedModel.listStatements();
        ArrayList<String> instances = null;
        TreeSet statements = null;
        int count = 0;
        while (siter.hasNext()) {
            Statement statement = siter.nextStatement();
            count++;
            if (count%10000==0) {
                System.out.println("Nr. statements processed = " + count+ " out of:"+namedModel.size());
            }
            String sessionKey = statement.getSubject().getLocalName();
            String[] fields = sessionKey.split("-");
            sessionKey = fields[0];
            if (fields.length > 1) {
                sessionKey += "-" + fields[1];
            }

            String subjectId = statement.getSubject().getLocalName();
            // System.out.println("statement.getPredicate() = " + statement.getPredicate());
            if (statement.getPredicate().getLocalName().equalsIgnoreCase("type")) {
                String type = statement.getObject().asResource().getLocalName();
                if (typeInstanceMap.containsKey(type)) {
                    instances = typeInstanceMap.get(type);
                    if (!instances.contains(subjectId)) {
                        instances.add(subjectId);
                        typeInstanceMap.put(type, instances);
                    }
                } else {
                    instances = new ArrayList<String>();
                    instances.add(subjectId);
                    typeInstanceMap.put(type, instances);
                    System.out.println("typeInstanceMap.size() = " + typeInstanceMap.size());
                }
            }
            else {
               if (instanceStatementMap.containsKey(subjectId)) {
                   statements = instanceStatementMap.get(subjectId);
                   statements.add(statementToSimpleString(statement));
                   instanceStatementMap.put(subjectId, statements);
               }
               else {
                   statements = new TreeSet();
                   statements.add(statementToSimpleString(statement));
                   instanceStatementMap.put(subjectId, statements);
               }
            }
        }
    }

    static void dumpDataToXls (File file) {
        for (Map.Entry<String,ArrayList<String>> entry : typeInstanceMap.entrySet()) {
            String type = entry.getKey();
            System.out.println("type = " + type);
            try {
                OutputStream fos = new FileOutputStream(file.getAbsolutePath()+type+".xls");
                for (int i = 0; i < typeInstanceMap.get(type).size(); i++) {
                    String instance = typeInstanceMap.get(type).get(i);
                    if (instanceStatementMap.containsKey(instance)) {
                        TreeSet statements = instanceStatementMap.get(instance);
                        String str = instance+"\t"+statements.toString() + "\n";
                        fos.write(str.getBytes());
                    }
                }
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    static String statementToSimpleString (Statement statement) {
        String value = "";
        String predicate = statement.getPredicate().getLocalName();
        String object = "";
        if (statement.getObject().isLiteral()) {
            object = statement.getObject().asLiteral().getString();
        }
        else {
            object = statement.getObject().asResource().getLocalName();
        }
        value = predicate+":"+object;
        return value;
    }

    static String sortedString (ArrayList<Statement> statements) {
        String str = "";
        TreeSet set = new TreeSet();
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            set.add(statementToSimpleString(statement));
        }
        str = set.toString();
        return str;
    }
}
