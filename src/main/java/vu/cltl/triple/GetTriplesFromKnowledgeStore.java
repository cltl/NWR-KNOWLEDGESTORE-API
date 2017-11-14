package vu.cltl.triple;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.*;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;

import java.util.ArrayList;
import java.util.HashMap;

import static com.hp.hpl.jena.rdf.model.ResourceFactory.createStatement;

/**
 * Created by filipilievski on 2/7/16.
 */
public class GetTriplesFromKnowledgeStore {
    public static int DEBUG = 0;
    public static int qCount = 0;
    public static String service = "https://knowledgestore2.fbk.eu";
    public static String serviceEndpoint = "https://knowledgestore2.fbk.eu/nwr/wikinews-new/sparql";
    public static String user = "nwr_partner";
    public static String pass = "ks=2014!";

    HttpAuthenticator authenticator = new SimpleAuthenticator(user, pass.toCharArray());
   // public static vu.cltl.triple.TrigTripleData trigTripleData = new vu.cltl.triple.TrigTripleData();

    static public void setServicePoint (String service, String ks) {
        if (ks.isEmpty()) {
            serviceEndpoint = service+ "/sparql";
        }
        else {
            serviceEndpoint = service + "/" + ks + "/sparql";
        }
    }

    static public void setServicePoint (String service, String ks, String username, String password) {
        //serviceEndpoint = "https://knowledgestore2.fbk.eu/"+ks+"/sparql";
        setServicePoint(service, ks);
        user = username;
        pass = password;
    }


    public static ArrayList<Statement> readTriplesFromKs(String subjectUri, String sparqlQuery){

        ArrayList<Statement> triples = new ArrayList<Statement>();
        HttpAuthenticator authenticator = new SimpleAuthenticator(user, pass.toCharArray());
        try {
            qCount++;
            QueryExecution x = QueryExecutionFactory.sparqlService(serviceEndpoint, sparqlQuery, authenticator);
            ResultSet resultset = x.execSelect();
            while (resultset.hasNext()) {
                QuerySolution solution = resultset.nextSolution();
                String relString = solution.get("predicate").toString();
                RDFNode obj = solution.get("object");
                Model model = ModelFactory.createDefaultModel();
                Resource subj = model.createResource(subjectUri);
                Statement s = createStatement(subj, ResourceFactory.createProperty(relString), obj);
                triples.add(s);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return triples;
    }


    public static ArrayList<String> readEventIdsFromKs(String sparqlQuery)throws Exception {
        ArrayList<String> eventIds = new ArrayList<String>();
        //System.out.println("serviceEndpoint = " + serviceEndpoint);
        //System.out.println("sparqlQuery = " + sparqlQuery);
        //System.out.println("user = " + user);
        //System.out.println("pass = " + pass);
        HttpAuthenticator authenticator = new SimpleAuthenticator(user, pass.toCharArray());

        QueryExecution x = QueryExecutionFactory.sparqlService(serviceEndpoint, sparqlQuery, authenticator);
        ResultSet resultset = x.execSelect();
        while (resultset.hasNext()) {
            QuerySolution solution = resultset.nextSolution();
            //System.out.println("solution.toString() = " + solution.toString());
            //( ?event = <http://www.newsreader-project.eu/data/Dasym-Pilot/425819_relink_dominant.naf#ev24> )
            String currentEvent = solution.get("event").toString();
            //System.out.println("currentEvent = " + currentEvent);
            //http://www.newsreader-project.eu/data/Dasym-Pilot/425819_relink_dominant.naf#ev24
            if (!eventIds.contains(currentEvent)) {
                eventIds.add(currentEvent);
            }
        }
        return eventIds;
    }


    public static void getEventDataFromKs(String sparqlQuery, TrigTripleData trigTripleData)throws Exception {
        //System.out.println("serviceEndpoint = " + serviceEndpoint);
        //System.out.println("sparqlQuery = " + sparqlQuery);
        //System.out.println("user = " + user);
        //System.out.println("pass = " + pass);
        HttpAuthenticator authenticator = new SimpleAuthenticator(user, pass.toCharArray());

        Property inDateTimeProperty = ResourceFactory.createProperty("http://www.w3.org/TR/owl-time#inDateTime");
        Property beginTimeProperty = ResourceFactory.createProperty("http://www.w3.org/TR/owl-time#hasBeginning");
        Property endTimeProperty = ResourceFactory.createProperty("http://www.w3.org/TR/owl-time#hasEnd");


        QueryExecution x = QueryExecutionFactory.sparqlService(serviceEndpoint, sparqlQuery, authenticator);
        ResultSet resultset = x.execSelect();
        String oldEvent="";
        ArrayList<Statement> instanceRelations = new ArrayList<Statement>();
        ArrayList<Statement> otherRelations = new ArrayList<Statement>();
        while (resultset.hasNext()) {
            QuerySolution solution = resultset.nextSolution();
            String relString = solution.get("relation").toString();
            String currentEvent = solution.get("event").toString();
            RDFNode obj = solution.get("object");
            String objUri = obj.toString();
            Statement s = createStatement((Resource) solution.get("event"), ResourceFactory.createProperty(relString), obj);
            if (isSemRelation(relString) || isESORelation(relString) || isFNRelation(relString) || isPBRelation(relString))
            {
                otherRelations.add(s);
                if (isSemTimeRelation(relString)) {
                    if (solution.get("indatetime")!=null){
//                            System.out.println("in " + solution.get("indatetime"));
                        String uri = ((Resource) obj).getURI();
                        Statement s2 = createStatement((Resource) obj, inDateTimeProperty, solution.get("indatetime"));
                        if (trigTripleData.tripleMapInstances.containsKey(uri)) {
                            ArrayList<Statement> triples = trigTripleData.tripleMapInstances.get(uri);
                            triples.add(s2);
                            trigTripleData.tripleMapInstances.put(uri, triples);
                        } else {
                            ArrayList<Statement> triples = new ArrayList<Statement>();
                            triples.add(s2);
                            trigTripleData.tripleMapInstances.put(uri, triples);
                        }
                    }
                    else {
                        if (solution.get("begintime")!=null){
//                            System.out.println("begin " + solution.get("begintime"));
                            String uri = ((Resource) obj).getURI();
                            Statement s2 = createStatement((Resource) obj, beginTimeProperty, solution.get("begintime"));
                            if (trigTripleData.tripleMapInstances.containsKey(uri)) {
                                ArrayList<Statement> triples = trigTripleData.tripleMapInstances.get(uri);
                                triples.add(s2);
                                trigTripleData.tripleMapInstances.put(uri, triples);
                            } else {
                                ArrayList<Statement> triples = new ArrayList<Statement>();
                                triples.add(s2);
                                trigTripleData.tripleMapInstances.put(uri, triples);
                            }
                        }
                        else if (solution.get("endtime")!=null) {
//                            System.out.println("end " + solution.get("endtime"));
                            String uri = ((Resource) obj).getURI();
                            Statement s2 = createStatement((Resource) solution.get("object"), endTimeProperty, solution.get("endtime"));
                            if (trigTripleData.tripleMapInstances.containsKey(uri)) {
                                ArrayList<Statement> triples = trigTripleData.tripleMapInstances.get(uri);
                                triples.add(s2);
                                trigTripleData.tripleMapInstances.put(uri, triples);
                            } else {
                                ArrayList<Statement> triples = new ArrayList<Statement>();
                                triples.add(s2);
                                trigTripleData.tripleMapInstances.put(uri, triples);
                            }
                        }
                    }
                }
            }
            else // Instances
            {
                instanceRelations.add(s);
            }

            if (!oldEvent.equals("")) {
                if (!currentEvent.equals(oldEvent)){
                    if (instanceRelations.size()>0){
                        trigTripleData.tripleMapInstances.put(oldEvent, instanceRelations);
                    }
                    if (otherRelations.size()>0){
                        trigTripleData.tripleMapOthers.put(oldEvent, otherRelations);
                    }
                    instanceRelations = new ArrayList<Statement>();
                    otherRelations = new ArrayList<Statement>();
                }
            }
            oldEvent=currentEvent;
        }
     //   System.out.println(" * instance statements = "+trigTripleData.tripleMapInstances.size());
     //   System.out.println(" * sem statements = " + trigTripleData.tripleMapOthers.size());
    }


    public static void readTriplesFromKs(String sparqlQuery, TrigTripleData trigTripleData)throws Exception {
        //System.out.println("serviceEndpoint = " + serviceEndpoint);
        //System.out.println("sparqlQuery = " + sparqlQuery);
        //System.out.println("user = " + user);
        //System.out.println("pass = " + pass);
        HttpAuthenticator authenticator = new SimpleAuthenticator(user, pass.toCharArray());

        Property inDateTimeProperty = ResourceFactory.createProperty("http://www.w3.org/TR/owl-time#inDateTime");
        Property beginTimeProperty = ResourceFactory.createProperty("http://www.w3.org/TR/owl-time#hasBeginning");
        Property endTimeProperty = ResourceFactory.createProperty("http://www.w3.org/TR/owl-time#hasEnd");


        QueryExecution x = QueryExecutionFactory.sparqlService(serviceEndpoint, sparqlQuery, authenticator);
        ResultSet resultset = x.execSelect();
        String oldEvent="";
        ArrayList<Statement> instanceRelations = new ArrayList<Statement>();
        ArrayList<Statement> otherRelations = new ArrayList<Statement>();
        while (resultset.hasNext()) {
            QuerySolution solution = resultset.nextSolution();
            String relString = solution.get("relation").toString();
            String currentEvent = solution.get("event").toString();
            RDFNode obj = solution.get("object");
            String objUri = obj.toString();
            Statement s = createStatement((Resource) solution.get("event"), ResourceFactory.createProperty(relString), obj);
            if (isSemRelation(relString) || isESORelation(relString) || isFNRelation(relString) || isPBRelation(relString))
            {
                otherRelations.add(s);
                if (isSemTimeRelation(relString)) {
                    if (solution.get("indatetime")!=null){
//                            System.out.println("in " + solution.get("indatetime"));
                        String uri = ((Resource) obj).getURI();
                        Statement s2 = createStatement((Resource) obj, inDateTimeProperty, solution.get("indatetime"));
                        if (trigTripleData.tripleMapInstances.containsKey(uri)) {
                            ArrayList<Statement> triples = trigTripleData.tripleMapInstances.get(uri);
                            triples.add(s2);
                            trigTripleData.tripleMapInstances.put(uri, triples);
                        } else {
                            ArrayList<Statement> triples = new ArrayList<Statement>();
                            triples.add(s2);
                            trigTripleData.tripleMapInstances.put(uri, triples);
                        }
                    }
                    else {
                        if (solution.get("begintime")!=null){
//                            System.out.println("begin " + solution.get("begintime"));
                            String uri = ((Resource) obj).getURI();
                            Statement s2 = createStatement((Resource) obj, beginTimeProperty, solution.get("begintime"));
                            if (trigTripleData.tripleMapInstances.containsKey(uri)) {
                                ArrayList<Statement> triples = trigTripleData.tripleMapInstances.get(uri);
                                triples.add(s2);
                                trigTripleData.tripleMapInstances.put(uri, triples);
                            } else {
                                ArrayList<Statement> triples = new ArrayList<Statement>();
                                triples.add(s2);
                                trigTripleData.tripleMapInstances.put(uri, triples);
                            }
                        }
                        else if (solution.get("endtime")!=null) {
//                            System.out.println("end " + solution.get("endtime"));
                            String uri = ((Resource) obj).getURI();
                            Statement s2 = createStatement((Resource) solution.get("object"), endTimeProperty, solution.get("endtime"));
                            if (trigTripleData.tripleMapInstances.containsKey(uri)) {
                                ArrayList<Statement> triples = trigTripleData.tripleMapInstances.get(uri);
                                triples.add(s2);
                                trigTripleData.tripleMapInstances.put(uri, triples);
                            } else {
                                ArrayList<Statement> triples = new ArrayList<Statement>();
                                triples.add(s2);
                                trigTripleData.tripleMapInstances.put(uri, triples);
                            }
                        }
                    }
                }
            }
            else // Instances
            {
                instanceRelations.add(s);
            }

            if (!oldEvent.equals("")) {
                if (!currentEvent.equals(oldEvent)){
                    if (instanceRelations.size()>0){
                        trigTripleData.tripleMapInstances.put(oldEvent, instanceRelations);
                    }
                    if (otherRelations.size()>0){
                        trigTripleData.tripleMapOthers.put(oldEvent, otherRelations);
                    }
                    instanceRelations = new ArrayList<Statement>();
                    otherRelations = new ArrayList<Statement>();
                }
            }
            oldEvent=currentEvent;
        }
     //   System.out.println(" * instance statements = "+trigTripleData.tripleMapInstances.size());
     //   System.out.println(" * sem statements = " + trigTripleData.tripleMapOthers.size());
    }

    public static HashMap<String, Integer>  getTopicsAndLabelCountsFromKnowledgeStore(String sparqlQuery)throws Exception {
        HashMap<String, Integer> cntPredicates = new HashMap<String, Integer>();
        HttpAuthenticator authenticator = new SimpleAuthenticator(user, pass.toCharArray());

        QueryExecution x = QueryExecutionFactory.sparqlService(serviceEndpoint, sparqlQuery, authenticator);
        ResultSet resultset = x.execSelect();

        //// The problem is that the full hiearchy is given for
        while (resultset.hasNext()) {
            QuerySolution solution = resultset.nextSolution();
            String topic = solution.get("topic").toString();
            String type = "";
            String count = solution.get("count").toString();
            int idx = count.indexOf("^^");
            if (idx>-1) count = count.substring(0, idx);
            /*System.out.println("label = " + label);
            System.out.println("type = " + type);
            System.out.println("count = " + count);*/
            if (!topic.isEmpty()) {
                cntPredicates.put(topic,  Integer.parseInt(count));
            }
        }
        return cntPredicates;
    }

    private static boolean isEventUri (String subject) {
        String name = subject;
        int idx = subject.lastIndexOf("#");
        if (idx>-1) name = name.substring(idx);
        return name.toLowerCase().startsWith("#ev");
    }

    private static boolean isSemRelation(String relation) {
        return relation.startsWith("http://semanticweb.cs.vu.nl/2009/11/sem/");
    }

    private static boolean isSemTimeRelation(String relation) {
        return relation.startsWith("http://semanticweb.cs.vu.nl/2009/11/sem/hasTime");
    }

    private static boolean isDenotedByRelation(String relation) {
        //http://groundedannotationframework.org/gaf#denotedBy
        return relation.endsWith("denotedBy");
    }
    //<http://groundedannotationframework.org/grasp#hasAttribution>
    public static boolean isAttributionRelation(String relation) {
        return relation.endsWith("hasAttribution");
    }

    public static boolean isProvRelation(String relation) {
        return relation.startsWith("http://www.w3.org/ns/prov");
    }

    private static boolean isFNRelation(String relation) {
        return relation.startsWith("http://www.newsreader-project.eu/ontologies/framenet/");
    }
    private static boolean isPBRelation(String relation) {
        return relation.startsWith("http://www.newsreader-project.eu/ontologies/propbank/");
    }
    private static boolean isESORelation(String relation) {
        if (relation.indexOf("hasPreSituation")>-1  ||
            relation.indexOf("quantity-attribute")>-1 ||
            relation.indexOf("hasPostSituation")>-1 ||
            relation.indexOf("hasDuring")>-1) {
            return false;
        }
        return relation.startsWith("http://www.newsreader-project.eu/domain-ontology");
    }

}
