package vu.cltl.triple.naf;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;
import eu.kyotoproject.kaf.KafEntity;
import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafSense;
import org.apache.jena.riot.RDFDataMgr;
import vu.cltl.triple.TrigUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RewriteNafWithDomainKnowledge {
    static HashMap<String, ArrayList<Statement>> statementMap = new HashMap<String, ArrayList<Statement>>();
    static boolean DEBUG = false;
    static  String statementPrefixFilter = "";
    static public void main (String [] args) {
        String pathToTripleFiles = "/Users/piek/Desktop/DigHum-2018/4775434/OBO_XML_7-2/sessions/rdf";
        String pathToNafFolder = "/Users/piek/Desktop/DigHum-2018/4775434/OBO_XML_7-2/sessions/naf-out/naf16";
        statementPrefixFilter = "16";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equalsIgnoreCase("--rdf-folder") && args.length>(i+1)) {
                pathToTripleFiles = args[i+1];
            }
            else if (arg.equalsIgnoreCase("--naf-folder") && args.length>(i+1)) {
                pathToNafFolder = args[i+1];
            }
            else if (arg.equalsIgnoreCase("--prefix") && args.length>(i+1)) {
                statementPrefixFilter = args[i+1];
            }
        }
        Dataset dataset = TDBFactory.createDataset();
        KafSaxParser kafSaxParser = new KafSaxParser();
        ArrayList<File> tripleFiles = TrigUtil.makeRecursiveFileList(new File(pathToTripleFiles), ".trig");
        for (int i = 0; i < tripleFiles.size(); i++) {
            File trigFile = tripleFiles.get(i);
            System.out.println("trigFile.getName() = " + trigFile.getName());
            //if (trigFile.getName().equals("all4.trig"))  dataset = RDFDataMgr.loadDataset(trigFile.getAbsolutePath());
            dataset = RDFDataMgr.loadDataset(trigFile.getAbsolutePath());
            makeStatementMap(dataset);
            dataset = null;
            //break;
        }
        System.out.println("Final statementMap = " + statementMap.size());
        int count = 0;
        ArrayList<File> nafFiles = TrigUtil.makeRecursiveFileList(new File(pathToNafFolder), ".naf");
        System.out.println("nafFiles.size() = " + nafFiles.size());
        for (int i = 0; i < nafFiles.size(); i++) {
            File nafFile = nafFiles.get(i);
            /*if (!nafFile.getName().equals("1691-02-18_t16910218-1-crimeloc7.txt.naf")) {
                DEBUG = false;
                continue;
            }
            else DEBUG = true;*/

            count++;
            if (count%1000==0) {
                System.out.println("Nr. naf files processed = " + count+ " out of:"+nafFiles.size());
            }
            //1675-12-08_s16751208-1.txt.naf
            String nafFileName = nafFile.getName();
            String sessionKey = nafFileName;
            int idx_e = sessionKey.indexOf(".");
            if (idx_e>-1) {
                sessionKey = sessionKey.substring(0, idx_e);
            }
            int idx = sessionKey.indexOf("_");
            if (idx>-1) {
                String [] fields = sessionKey.substring(idx+1).split("-");
                sessionKey = fields[0];
                if (fields.length>1) {
                        sessionKey+="-"+fields[1];
                }
            }
            HashMap<String, ArrayList<String>> nameMap = new HashMap<String, ArrayList<String>>();
            if (DEBUG) System.out.println("naf sessionKey = " + sessionKey);
            ArrayList<Statement> statements = statementMap.get(sessionKey);
            if (statements != null) {
                if (DEBUG) System.out.println("statements.size() = " + statements.size());
                nameMap = getNameMap(statements);
            }
            else {
                if (DEBUG) System.out.println("No statements for = " + sessionKey);
            }
            kafSaxParser.parseFile(nafFile);
            for (int j = 0; j < kafSaxParser.kafEntityArrayList.size(); j++) {
                KafEntity kafEntity = kafSaxParser.kafEntityArrayList.get(j);
/*                if (kafEntity.getType().equalsIgnoreCase("PER") || kafEntity.getType().equalsIgnoreCase("LOC") ||
                    kafEntity.getType().equalsIgnoreCase("ORG") || kafEntity.getType().equalsIgnoreCase("MISC") ) { }*/
                kafEntity.setExternalReferences(new ArrayList<KafSense>());
                kafEntity.setTokenStrings(kafSaxParser);
                ArrayList<String> tokenStringArray = kafEntity.getTokenStringArray();
                HashMap<String, Integer> idCounts = new HashMap<String, Integer>();
                if (DEBUG) System.out.println("tokenStringArray.toString() = " + tokenStringArray.toString());
                for (int k = 0; k < tokenStringArray.size(); k++) {
                    String tokenString = tokenStringArray.get(k);
                    if (DEBUG) System.out.println("tokenString = " + tokenString);
                    String[] tokens = tokenString.split(" ");
                    for (int l = 0; l < tokens.length; l++) {
                        String token = tokens[l].toLowerCase();
                        if (DEBUG) System.out.println("token = " + token);
                        if (!nameMap.containsKey(token)) {
                            token = subStringMatch(token, nameMap);
                        }
                        if (!token.isEmpty() && nameMap.containsKey(token)) {
                            ArrayList<String> tokenIds = nameMap.get(token);
                            if (DEBUG) System.out.println("tokenIds.toString() = " + tokenIds.toString());
                            for (int m = 0; m < tokenIds.size(); m++) {
                                String id = tokenIds.get(m);
                                if (idCounts.containsKey(id)) {
                                    Integer cnt = idCounts.get(id);
                                    cnt++;
                                    idCounts.put(id, cnt);
                                } else {
                                    idCounts.put(id, 1);
                                }
                            }
                        } else {
                            if (DEBUG) {
                                System.out.println("no match for token = " + token);
                                System.out.println("nameMap.keySet().toString() = " + nameMap.keySet().toString());
                            }
                        }
                    }

                }
                ArrayList<String> maxIds = getMaxIds(idCounts);
                if (DEBUG) System.out.println("maxIds = " + maxIds.toString());
                if (maxIds.size() > 0) {
                    ArrayList<KafSense> externalReferences = new ArrayList<KafSense>();
                    for (int k = 0; k < maxIds.size(); k++) {
                        String id = maxIds.get(k);
                        //   System.out.println("id = " + id);
                        KafSense kafSense = new KafSense();
                        kafSense.setResource("OldBaileyAnnotation");
                        kafSense.setSensecode(id);
                        externalReferences.add(kafSense);
                    }
                    kafEntity.setExternalReferences(externalReferences);
                }
            }
            try {
                OutputStream fos = new FileOutputStream(nafFile+".dom");
                kafSaxParser.writeNafToStream(fos);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<String> getMaxIds (HashMap<String, Integer> idCounts) {
        ArrayList<String> ids = new ArrayList<String>();
        Integer maxCount = 0;
        for (Map.Entry<String,Integer> entry : idCounts.entrySet()) {
            Integer cnt = entry.getValue();
            if (cnt>maxCount) {
                maxCount = cnt;
                ids = new ArrayList<String>();
                ids.add(entry.getKey());
            }
            else if (cnt==maxCount) {
                ids.add(entry.getKey());
            }
        }
        return ids;
    }


    public static HashMap<String, ArrayList<String>> getNameMap (ArrayList<Statement> statements) {
        HashMap<String, ArrayList<String>> nameMap = new HashMap<String, ArrayList<String>>();
        ArrayList<String> ids = null;
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            String subjectId = statement.getSubject().getLocalName();
           // System.out.println("statement.getPredicate() = " + statement.getPredicate());
            if (statement.getPredicate().getLocalName().equalsIgnoreCase("given") ||
                    statement.getPredicate().getLocalName().equalsIgnoreCase("surname") ||
                    statement.getPredicate().getLocalName().equalsIgnoreCase("mention")) {
                String name = statement.getObject().asLiteral().getString().toLowerCase();
                if (nameMap.containsKey(name)) {
                    ids = nameMap.get(name);
                    if (!ids.contains(subjectId)) {
                        ids.add(subjectId);
                      //  System.out.println("subjectId = " + subjectId);
                        nameMap.put(name, ids);
                    }

                }
                else {
                    ids = new ArrayList<String>();
                    ids.add(subjectId);
                    nameMap.put(name, ids);
                }
            }
        }
        if (DEBUG) System.out.println("nameMap.size() = " + nameMap.size());
        return nameMap;
    }

    public static void makeStatementMap (Dataset dataset) {
        Model namedModel = dataset.getDefaultModel();
        StmtIterator siter = namedModel.listStatements();
        String [] fields = null;
        ArrayList<Statement> triples = null;
        int count = 0;
        while (siter.hasNext()) {
            Statement s = siter.nextStatement();
            String sessionKey = s.getSubject().getLocalName();
            fields = sessionKey.split("-");
            sessionKey = fields[0];
            if (fields.length>1) {
                sessionKey+="-"+fields[1];
            }
            if (!sessionKey.substring(1).startsWith(statementPrefixFilter))  {
                continue;
            }
            count++;
            if (count%10000==0) {
                System.out.println("Nr. statements processed = " + count+ " out of:"+namedModel.size());
            }
            //oldbailey:t18070408-48-person439
            //System.out.println("trig sessionKey = " + sessionKey);
            if (statementMap.containsKey(sessionKey)) {
                triples = statementMap.get(sessionKey);
                triples.add(s);
                statementMap.put(sessionKey, triples);
            } else {
                triples = new ArrayList<Statement>();
                triples.add(s);
                statementMap.put(sessionKey, triples);
            }
        }
        System.out.println("statementMap = " + statementMap.size());
    }

    public static String subStringMatch (String token, HashMap<String, ArrayList<String>> nameMap) {
        String match = "";
        for (Map.Entry<String,ArrayList<String>> entry : nameMap.entrySet()) {
            if (entry.getKey().indexOf(token)>-1) {
                if (match.isEmpty()) {
                    match = entry.getKey();
                }
                else if (entry.getKey().length()<match.length()) {
                    match = entry.getKey();
                }
            }
            /*else if (token.equals("cloth")) {
                System.out.println("entry.getKey() = " + entry.getKey());
            }*/
        }
        return match;
    }

    public static int distance(String a, String b)
    {
        a = a.toLowerCase();
        b = b.toLowerCase();
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++)
        {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++)
            {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }
}
