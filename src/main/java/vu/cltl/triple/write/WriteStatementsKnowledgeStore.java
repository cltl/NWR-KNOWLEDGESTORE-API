package vu.cltl.triple.write;

import eu.fbk.knowledgestore.KnowledgeStore;
import eu.fbk.knowledgestore.Session;
import eu.fbk.knowledgestore.client.Client;
import eu.fbk.rdfpro.RDFProcessor;
import eu.fbk.rdfpro.RDFSource;
import eu.fbk.rdfpro.RDFSourceException;
import org.openrdf.model.*;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import vu.cltl.triple.objects.ResourcesUri;

import java.util.ArrayList;

public class WriteStatementsKnowledgeStore implements RDFProcessor {



   // private static final Logger LOGGER = LoggerFactory.getLogger(WriteStatementsKnowledgeStore.class);

    static String testargs = "--subj http://www.lealani.org/event#1 --pred http://semanticweb.cs.vu.nl/2009/11/sem/hasActor --obj bob --literal";

    static public void main (String [] args) {
        String subjectUri = "";
        String predicateUri = "";
        String object = "";
        String ksAddress = "http://145.100.57.176:50053/";
        boolean literal = false;
        if (args.length ==0) {
            args = testargs.split(" ");
        }
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--subj") && args.length>(i+1)) {
                subjectUri = args[i+1];
            }
            else if (arg.equals("--pred") && args.length>(i+1)) {
                predicateUri = args[i+1];
            }
            else if (arg.equals("--obj") && args.length>(i+1)) {
                object = args[i+1];
            }
            else if (arg.equals("--ks") && args.length>(i+1)) {
                ksAddress = args[i+1];
            }
            else if (arg.equals("--literal")) {
                literal = true;
            }
        }
        ArrayList<Statement> statementArrayList = new ArrayList<Statement>();
        ValueFactory valueFactory = ValueFactoryImpl.getInstance();
        Resource ng = valueFactory.createURI(ResourcesUri.grasp, "g1");
        URI subject = valueFactory.createURI(subjectUri);
        URI sem = valueFactory.createURI(predicateUri);//ResourcesUri.sem, "hasTime"
        if (literal) {
            Literal objectLiteral = valueFactory.createLiteral(object);
            Statement statement = valueFactory.createStatement(subject, sem, objectLiteral, ng);
            statementArrayList.add(statement);
        }
        else {
            URI objectUri = valueFactory.createURI(object);
            Statement statement = valueFactory.createStatement(subject, sem, objectUri, ng);

            statementArrayList.add(statement);
        }
        //System.out.println("statementArrayList = " + statementArrayList.toString());
        storeTriples(statementArrayList, ksAddress);
    }

    static public void storeTriples (ArrayList<Statement> statements, String ksAddress) {
        KnowledgeStore ksClient  = null;
        if (ksAddress != null) {
            ksClient = Client.builder(ksAddress).compressionEnabled(true).maxConnections(2).validateServer(false).build();
            Session session = ksClient.newSession();
            try {
                session.sparqlupdate().statements(statements).exec();
            } catch (Exception e) {
              //  e.printStackTrace();
            } finally {
                session.close();
            }
        }
    }

    @Override
    public int getExtraPasses() {
        return 0;
    }

    @Override
    public RDFSource wrap(RDFSource source) {
        return null;
    }

    @Override
    public RDFHandler wrap(RDFHandler rdfHandler) {
        return null;
    }

    @Override
    public void apply(RDFSource input, RDFHandler output, int passes) throws RDFSourceException, RDFHandlerException {

    }
}
