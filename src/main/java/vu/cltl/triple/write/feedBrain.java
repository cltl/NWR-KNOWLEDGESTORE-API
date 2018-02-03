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

    static String testparameters1 = "--source chat1 --turn 1 --author-name Piek --subject-label bite --subject-type http://www.newsreader-project.eu/domain-ontology#Attack --object-label rabbit --object-type http://dbpedia.org/resource/Animal --perspective CERTAIN;SCARED;NEGATIVE;BELIEF";
    static String testparameters2 = "--source chat2 --turn 1 --author-name Piek --subject-label Selene --subject-type http://dbpedia.org/resource/Person --predicate-uri " +
            "http://www.semanticweb.org/leolani/N2MY/comesFrom --object-label Mexico --object-type http://dbpedia.org/resource/Country --perspective LIKE";
    static String testparameters3 = "--source chat2 --turn 2 --object-name Bram --subject-label Selene --subject-type http://dbpedia.org/resource/Person --predicate-uri " +
            "http://www.semanticweb.org/leolani/N2MY/knows --object-label Piek --object-type http://dbpedia.org/resource/Person --perspective SCARED";

    static public void main (String [] args) {
        String authorName = "";
                String authorURI = "";
                String sourceId = "";
                String predicateUri = "";
                String perspective = "";
                String subjectLabel = "";
                String subjectUri = "";
                String subjectType = "";
                String objectLabel = "";
                String objectUri = "";
                String objectType = "";
                String turn = "";
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
        }
        if (authorURI.isEmpty()) {
            authorURI = CreateGraspTriples.friendsUri + authorName;
        }
        Dataset dataset  = null;
        dataset = CreateGraspTriples.graspDataSet(sourceId, turn, authorURI, authorName, perspective, subjectLabel, subjectUri, subjectType, predicateUri, objectLabel, objectUri, objectType);
        ArrayList<org.openrdf.model.Statement> statements = new ArrayList<org.openrdf.model.Statement>();
        Iterator<String> it = dataset.listNames();
        while (it.hasNext()) {
            String name = it.next();

            Model namedModel = dataset.getNamedModel(name);
            StmtIterator siter = namedModel.listStatements();
            while (siter.hasNext()) {
                Statement s = siter.nextStatement();
                statements.add(castJenaOpenRdf(s, name));
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

    static public org.openrdf.model.Statement castJenaOpenRdf(Statement jenaStatement, String modelName) {
        org.openrdf.model.Statement statement = null;
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
