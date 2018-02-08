package vu.cltl.triple.write;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class storeGraspStatementInKnowledgeStore {

    static String testparameters1 = "--ks 145.100.57.176 --source chat1 --turn 1 --author-name Piek" +
            " --subject-label bite --subject-type http://www.newsreader-project.eu/domain-ontology#Attack" +
            " --predicate-uri http://semanticweb.cs.vu.nl/2009/11/sem/hasActor" +
            " --object-label rabbit --object-type http://dbpedia.org/resource/Animal" +
            " --perspective CERTAIN;SCARED;NEGATIVE;BELIEF" +
            " --debug"
            ;
    static String testparameters2 = "--ks 145.100.57.176 --source chat2 --turn 1 --author-name Piek" +
            " --subject-label Selene --subject-type http://dbpedia.org/resource/Person" +
            " --predicate-uri http://www.semanticweb.org/leolani/N2MY/comesFrom" +
            " --object-label Mexico --object-type http://dbpedia.org/resource/Country" +
            " --perspective LIKE" +
            " --debug"
            ;
    static String testparameters3 = "--ks 145.100.57.176 --source chat2 --turn 2 --author-name Bram" +
            " --subject-label Selene --subject-type http://dbpedia.org/resource/Person" +
            " --predicate-uri http://www.semanticweb.org/leolani/N2MY/knows" +
            " --object-label Piek --object-type http://dbpedia.org/resource/Person" +
            " --perspective SCARED" +
            " --debug"
            ;

    static String testparameters4 = "--ks 145.100.57.176 --source chat2 --turn 2 --author-name Piek" +
            " --subject-label Selene --subject-type http://dbpedia.org/resource/Person" +
            " --predicate-uri http://www.semanticweb.org/leolani/N2MY/knows" +
            " --object-label Piek --object-type http://dbpedia.org/resource/Person" +
            " --perspective LIKE" +
            " --debug"
            ;

    static String usage = "Usage:\n"+
            "--ks               IP address of the knowledge store\n"+
            "--source           unique identifier for the chat\n"+
            "--turn             unique identifier for the turn within the chat\n"+
            "--author-uri       <OPTIONAL> uri for the author of the turn. If omitted a uri is created from the author-name\n"+
            "--author-name      name of the author of the turn\n"+
            "--subject-label    word that represents the subject label\n"+
            "--subject-uri      <OPTIONAL>uri that represents the referent for the subject. If omitted a global uri is created from the label\n"+
            "--subject-type     <OPTIONAL>uri that represents the CLASS for the subject. CLASS information is not considered a statement but as interpretation\n"+
            "--predicate-uri    uri that represents the predicate\n"+
            "--object-label     word that represents the object label\n"+
            "--object-uri       <OPTIONAL>uri that represents the referent for the object. If omitted a global uri is created from the label\n"+
            "--object-type      <OPTIONAL>uri that represents the CLASS for the subject. CLASS information is not considered as a statement but as interpretation\n"+
            "--perspective      <OPTIONAL>list of perspective values separated by semicolons\n" +
            "--debug            <OPTIONAL>print out debug statements"
            ;
    static public void main (String [] args) {
        boolean DEBUG = false;
        String authorName = "";
        String authorURI = "";
        String source = "";
        String predicateUri = "";
        String perspective = "";
        String subjectLabel = "";
        String subjectUri = "";
        String subjectType = "";
        String objectLabel = "";
        String objectUri = "";
        String objectType = "";
        String turn = "1";
        String ks = "";
        String address = "http://145.100.57.176:50053/";
        if (args.length==0) {
            System.out.println(usage);
            args = testparameters4.split(" ");
        }
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--author-name") && args.length>(i+1)) {
                authorName = args[i+1];
            }
            else if (arg.equals("--author-uri") && args.length>(i+1)) {
                authorURI = args[i+1];
            }
            else if (arg.equals("--source") && args.length>(i+1)) {
                source = args[i+1];
            }
            else if (arg.equals("--turn") && args.length>(i+1)) {
                turn = args[i+1];
            }
            else if (arg.equals("--subject-label") && args.length>(i+1)) {
                subjectLabel = args[i+1];
            }
            else if (arg.equals("--subject-uri") && args.length>(i+1)) {
                subjectLabel = args[i+1];
            }
            else if (arg.equals("--subject-type") && args.length>(i+1)) {
                subjectType = args[i+1];
            }
            else if (arg.equals("--predicate-uri") && args.length>(i+1)) {
                predicateUri = args[i+1];
            }
            else if (arg.equals("--object-label") && args.length>(i+1)) {
                objectLabel = args[i+1];
            }
            else if (arg.equals("--object-uri") && args.length>(i+1)) {
                objectUri = args[i+1];
            }
            else if (arg.equals("--object-type") && args.length>(i+1)) {
                objectType = args[i+1];
            }
            else if (arg.equals("--perspective") && args.length>(i+1)) {
                perspective = args[i+1];
            }
            else if (arg.equals("--ks") && args.length>(i+1)) {
                ks = args[i+1];
            }
            else if (arg.equals("--debug")) {
                DEBUG = true;
            }
        }
        if (DEBUG) {
            System.out.println("ks = " + ks);
            System.out.println("source or chat = " + source);
            System.out.println("turn = " + turn);
            System.out.println("authorName = " + authorName);
            System.out.println("authorURI = " + authorURI);
            System.out.println("subjectLabel = " + subjectLabel);
            System.out.println("subjectType = " + subjectType);
            System.out.println("subjectUri = " + subjectUri);
            System.out.println("predicateUri = " + predicateUri);
            System.out.println("objectLabel = " + objectLabel);
            System.out.println("objectType = " + objectType);
            System.out.println("objectUri = " + objectUri);
            System.out.println("perspective = " + perspective);
        }
        if (ks.isEmpty()) {
            System.out.println("Please specify address for the ks = " + ks);
            return;
        }
        else {
            address = "http://"+ks+":50053/";
        }
        if (authorURI.isEmpty()) {
            authorURI = CreateGraspTriples.friendsUri + authorName;
        }
        Dataset dataset  = null;
        dataset = CreateGraspTriples.graspDataSet(source, turn, authorURI, authorName, perspective, subjectLabel, subjectUri, subjectType, predicateUri, objectLabel, objectUri, objectType);
        ArrayList<org.openrdf.model.Statement> statements = new ArrayList<org.openrdf.model.Statement>();
        Iterator<String> it = dataset.listNames();
        while (it.hasNext()) {
            String name = it.next();

            Model namedModel = dataset.getNamedModel(name);
            StmtIterator siter = namedModel.listStatements();
            while (siter.hasNext()) {
                Statement s = siter.nextStatement();
                org.openrdf.model.Statement statement =castJenaOpenRdf(s, name);
                if (statement!=null) {
                    statements.add(statement);
                }
            }
        }
        if (DEBUG) {
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                RDFDataMgr.write(os, dataset, RDFFormat.TRIG_PRETTY);
                String rdfString = new String(os.toByteArray(), "UTF-8");
                System.out.println("rdfString = " + rdfString);
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
       // System.out.println("address = " + address);
        WriteStatementsKnowledgeStore.storeTriples(statements, address);
    }

    static public org.openrdf.model.Statement castJenaOpenRdf(Statement jenaStatement, String modelName) {
        org.openrdf.model.Statement statement = null;
        try {
            ValueFactory valueFactory = ValueFactoryImpl.getInstance();
            URI modelURI = valueFactory.createURI(modelName);
            URI subject = valueFactory.createURI(jenaStatement.getSubject().getURI());
            URI sem = valueFactory.createURI(jenaStatement.getPredicate().getURI());
            if (jenaStatement.getObject().isLiteral()) {
                Literal objectLiteral = valueFactory.createLiteral(jenaStatement.getObject().toString());
                 statement = valueFactory.createStatement(subject, sem, objectLiteral, modelURI);
            }

            else {
                URI objectUri = valueFactory.createURI(jenaStatement.getObject().asResource().getURI());
                 statement = valueFactory.createStatement(subject, sem, objectUri, modelURI);
            }
        } catch (Exception e) {
            System.out.println("jenaStatement.toString() = " + jenaStatement.toString());
            e.printStackTrace();
        }
        return statement;
    }

    org.openrdf.model.BNode bNode = null;
   /* ModelBuilder builder = new ModelBuilder();
    builder.setNamespace("ex", "http://example.org/");

    // In named graph 1, we add info about Picasso
    builder.namedGraph("ex:namedGraph1")
    		.subject("ex:Picasso")
    			.add(RDF.TYPE, EX.ARTIST)
    			.add(FOAF.FIRST_NAME, "Pablo");

    // In named graph 2, we add info about Van Gogh.
    builder.namedGraph("ex:namedGraph2")
    	.subject("ex:VanGogh")
    		.add(RDF.TYPE, EX.ARTIST)
    		.add(FOAF.FIRST_NAME, "Vincent");*/
}
