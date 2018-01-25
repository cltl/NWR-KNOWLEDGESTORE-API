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

public class feedBrain {

    static String testparameters1 = "--source chat1 --author-name Piek --event-label bite --event-type http://www.newsreader-project.eu/domain-ontology#Attack --actor-label rabbit --actor-type http://dbpedia.org/resource/Animal --perspective CERTAIN;SCARED;NEGATIVE;BELIEF";
    static String testparameters2 = "--source chat2 --author-name Piek --event-label Selene --event-type http://dbpedia.org/resource/Person --predicate-uri " +
            "http://www.semanticweb.org/leolani/N2MY/comesFrom --actor-label Mexico --actor-type http://dbpedia.org/resource/Country --perspective LIKE";
    static String testparameters3 = "--source chat3 --author-name Bram --event-label Selene --event-type http://dbpedia.org/resource/Person --predicate-uri " +
            "http://www.semanticweb.org/leolani/N2MY/knows --actor-label Piek --actor-type http://dbpedia.org/resource/Person --perspective SCARED";

    static public void main (String [] args) {
        String authorName = "";
                String authorURI = "";
                String sourceId = "";
                String predicate = "";
                String perspective = "";
                String eventLabel = "";
                String eventUri = "";
                String eventType = "";
                String actorLabel = "";
                String actorUri = "";
                String actorType = "";
        if (args.length==0) {
            args = testparameters3.split(" ");
        }
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--author-name") && args.length>(i+1)) {
                authorName = args[i+1];
            }
            else if (arg.equals("--source") && args.length>(i+1)) {
                sourceId = args[i+1];
            }
            else if (arg.equals("--event-label") && args.length>(i+1)) {
                eventLabel = args[i+1];
            }
            else if (arg.equals("--event-uri") && args.length>(i+1)) {
                eventLabel = args[i+1];
            }
            else if (arg.equals("--event-type") && args.length>(i+1)) {
                eventType = args[i+1];
            }
            else if (arg.equals("--predicate-uri") && args.length>(i+1)) {
                predicate = args[i+1];
            }
            else if (arg.equals("--actor-label") && args.length>(i+1)) {
                actorLabel = args[i+1];
            }
            else if (arg.equals("--actor-uri") && args.length>(i+1)) {
                actorUri = args[i+1];
            }
            else if (arg.equals("--actor-type") && args.length>(i+1)) {
                actorType = args[i+1];
            }
            else if (arg.equals("--perspective") && args.length>(i+1)) {
                perspective = args[i+1];
            }
        }
        if (authorURI.isEmpty()) {
            authorURI = CreateGraspTriples.friendsUri + authorName;
        }
        Dataset dataset  = null;
        if (predicate.isEmpty()) {
            dataset= CreateGraspTriples.graspDataSet(sourceId, authorURI, authorName, perspective, eventLabel, eventUri, actorLabel, actorUri);
        }
        else {
            dataset = CreateGraspTriples.graspDataSet(sourceId, authorURI, authorName, perspective, eventLabel, eventUri, eventType, predicate, actorLabel, actorUri, actorType);
        }
        ArrayList<org.openrdf.model.Statement> statements = new ArrayList<org.openrdf.model.Statement>();
        Iterator<String> it = dataset.listNames();
        while (it.hasNext()) {
            String name = it.next();
            Model namedModel = dataset.getNamedModel(name);
            StmtIterator siter = namedModel.listStatements();
            while (siter.hasNext()) {
                Statement s = siter.nextStatement();
                statements.add(castJenaOpenRdf(s));
            }
        }
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            RDFDataMgr.write(os, dataset, RDFFormat.TRIG_PRETTY);
            String rdfString = new String(os.toByteArray(),"UTF-8");
            System.out.println("rdfString = " + rdfString);
            os.close();
        } catch (Exception e) {
                      e.printStackTrace();
        }
        String address = "http://145.100.59.153:50053/";
        WriteStatementsKnowledgeStore.storeTriples(statements, address);
    }

    static public org.openrdf.model.Statement castJenaOpenRdf(Statement jenaStatement) {
        org.openrdf.model.Statement statement = null;
        ValueFactory valueFactory = ValueFactoryImpl.getInstance();
        URI subject = valueFactory.createURI(jenaStatement.getSubject().getURI());
        URI sem = valueFactory.createURI(jenaStatement.getPredicate().getURI());
        if (jenaStatement.getObject().isLiteral()) {
            Literal objectLiteral = valueFactory.createLiteral(jenaStatement.getObject().toString());
             statement = valueFactory.createStatement(subject, sem, objectLiteral);
        }

        else {
            URI objectUri = valueFactory.createURI(jenaStatement.getObject().asResource().getURI());
             statement = valueFactory.createStatement(subject, sem, objectUri);
        }
        return statement;
    }
}
