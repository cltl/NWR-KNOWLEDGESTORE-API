package vu.cltl.triple.write;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.tdb.TDBFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import vu.cltl.triple.TrigUtil;
import vu.cltl.triple.objects.ResourcesUri;
import vu.cltl.triple.objects.TrigTripleData;
import vu.cltl.triple.read.TrigTripleReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

public class EntityGraph {

    static public void main (String[] args) {
        String pathToTrigFiles = "/Users/piek/Desktop/Deloitte/wikinews-rdf/";
        String pathToEntityEventGraph = "/Users/piek/Desktop/Deloitte/entity-event.trig";
        String pathToEntityDocumentGraph = "/Users/piek/Desktop/Deloitte/entity-document.trig";
        Dataset entityDataSet =  TDBFactory.createDataset();
        Dataset documentDataSet =  TDBFactory.createDataset();
        TrigTripleReader trigTripleReader = new TrigTripleReader();
        ArrayList<File> trigFiles = TrigUtil.makeRecursiveFileList(new File(pathToTrigFiles), ".trig");
        for (int f = 0; f < trigFiles.size(); f++) {
            File file = trigFiles.get(f);
            ArrayList<String> documentEntities = new ArrayList<String>();
            TrigTripleData trigTripleData = trigTripleReader.readTripleFromTrigFile(file);
            for (Map.Entry<String,ArrayList<Statement>> entry : trigTripleData.tripleMapInstances.entrySet()) {
                //System.out.println("Key = " + entry.getKey() +  ", Value = " + entry.getValue());
                ArrayList<Statement> statements = entry.getValue();
                if (isEvent(statements)) {
                    String esoURI = getEsoTpe(statements);
                    if (!esoURI.isEmpty()) {
                        ArrayList<String> eventEntities = new ArrayList<String>();
                        for (int i = 0; i < statements.size(); i++) {
                            Statement statement = statements.get(i);
                            if (statement.getObject().isURIResource()) {
                                String objectUri = statement.getObject().asResource().getURI();
                                if (trigTripleData.tripleMapInstances.containsKey(objectUri)) {
                                    ArrayList<Statement> objectStatements = trigTripleData.tripleMapInstances.get(objectUri);
                                    if (isEntity(objectStatements)) {
                                        if (!eventEntities.contains(objectUri)) {
                                            eventEntities.add(objectUri);
                                        }
                                        if (!documentEntities.contains(objectUri)) {
                                            documentEntities.add(objectUri);
                                        }
                                    }
                                }
                            }
                        }
                        if (eventEntities.size() > 1) {
                            for (int i = 0; i < eventEntities.size(); i++) {
                                String entityUri = eventEntities.get(i);
                                for (int j = i + 1; j < eventEntities.size(); j++) {
                                    String entityUri2 = eventEntities.get(j);
                                   /* System.out.println("entityUri = " + entityUri);
                                    System.out.println("esoURI = " + esoURI);
                                    System.out.println("entityUri2 = " + entityUri2);*/
                                    addStatement(entityDataSet.getDefaultModel(), entityUri, esoURI, entityUri2);
                                    ArrayList<Statement> objectStatements = trigTripleData.tripleMapInstances.get(entityUri);
                                    entityDataSet.getDefaultModel().add(objectStatements);

                                }
                            }
                        }
                    }
                }
            }
            if (documentEntities.size() > 1) {
                String documentUri = ResourcesUri.nwr;
                for (int i = 0; i < documentEntities.size(); i++) {
                    String entityUri = documentEntities.get(i);
                    for (int j = i + 1; j < documentEntities.size(); j++) {
                        String entityUri2 = documentEntities.get(j);
                        /*System.out.println("entityUri = " + entityUri);
                        System.out.println("documentUri = " + documentUri);
                        System.out.println("entityUri2 = " + entityUri2);*/
                        addStatement(documentDataSet.getDefaultModel(), entityUri, documentUri, entityUri2);
                        ArrayList<Statement> objectStatements = trigTripleData.tripleMapInstances.get(entityUri);
                        documentDataSet.getDefaultModel().add(objectStatements);
                    }
                }
            }
            //break;
        }
        ResourcesUri.prefixSimpleModel(entityDataSet.getDefaultModel());

        try {
            OutputStream fos = new FileOutputStream(pathToEntityEventGraph);
            RDFDataMgr.write(fos, entityDataSet, RDFFormat.TRIG_PRETTY);
            // RDFDataMgr.write(fos, entityDataSet, RDFFormat.TURTLE_PRETTY);
            //RDFDataMgr.write(fos, entityDataSet, RDFFormat.TTL);
            fos.close();
        } catch (Exception e) {
              e.printStackTrace();
        }

        ResourcesUri.prefixSimpleModel(documentDataSet.getDefaultModel());

        try {
            OutputStream fos = new FileOutputStream(pathToEntityDocumentGraph);
            RDFDataMgr.write(fos, documentDataSet, RDFFormat.TRIG_PRETTY);
            fos.close();
        } catch (Exception e) {
              e.printStackTrace();
        }

    }

    static boolean isEvent (ArrayList<Statement> statements) {
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement.getPredicate().getLocalName().equals("type")) {
                if (statement.getObject().asResource().getLocalName().equals("EVENT")) {
                    return true;
                }
            }
        }
        return false;
    }

    static String getEsoTpe (ArrayList<Statement> statements) {
        String esoUri = "";
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement.getPredicate().getLocalName().equals("type")) {
               // System.out.println("statement.getObject().asResource().getNameSpace() = " + statement.getObject().asResource().getNameSpace());
                if (statement.getObject().asResource().getNameSpace().equals("http://cltl.nl/ontology/ceo#")) {
                    esoUri = statement.getObject().asResource().getURI();
                    break;
                }
                else if (statement.getObject().asResource().getNameSpace().equals("http://cltl.nl/ontology/eso#")) {
                    esoUri = statement.getObject().asResource().getURI();
                    break;
                }
            }
        }
        return esoUri;
    }

    static boolean isEntity (ArrayList<Statement> statements) {
            for (int i = 0; i < statements.size(); i++) {
                Statement statement = statements.get(i);
                if (statement.getPredicate().getLocalName().equals("type")) {
                    if (statement.getObject().asResource().getLocalName().equals("ENTITY")) {
                        return true;
                    }
                }
            }
            return false;
    }

    static void addStatement (Model model, String subj, String predicate, String obj) {
        Resource subjectResource = model.createResource(subj);
        Resource objectResource = model.createResource(obj);
        Property property = model.createProperty(predicate);
        subjectResource.addProperty(property, objectResource);
    }
}
