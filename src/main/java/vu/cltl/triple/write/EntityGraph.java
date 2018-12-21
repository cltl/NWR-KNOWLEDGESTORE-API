package vu.cltl.triple.write;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import vu.cltl.triple.TrigUtil;
import vu.cltl.triple.objects.ResourcesUri;
import vu.cltl.triple.objects.TrigTripleData;
import vu.cltl.triple.read.TrigTripleReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EntityGraph {
    static String testparameter = "--ont-file /Users/piek/Desktop/Deloitte/CLTL_CEO_version_1_sameas.owl" +
            " --trig /Users/piek/Desktop/Deloitte/wikinews-rdf/" +
            " --event-graph /Users/piek/Desktop/Deloitte/entity-event.trig" +
            " --document-graph /Users/piek/Desktop/Deloitte/entity-document.trig";
    final static String owl= "http://www.w3.org/2002/07/owl#";
    final static String cltl= "http://cltl.nl/ontology#";
    final static String predicateName = "sameAs";
    static boolean LOCATIONS = false;

    static OntModel ontologyModel;
    static HashMap<String, ArrayList<String>> mappingsToClass = new HashMap<String, ArrayList<String>>();

    static public void main (String[] args) {
        String pathToTrigFiles = "";
        String pathToEntityEventGraph = "";
        String pathToEntityDocumentGraph = "";
        String pathToOwlOntology = "";
        if (args.length==0) {
                    args = testparameter.split(" ");
        }
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--ont-file") && args.length>(i+1)) {
               pathToOwlOntology = args[i+1];
            }
            else if (arg.equals("--trig") && args.length>(i+1)) {
                pathToTrigFiles = args[i+1];
            }
            else if (arg.equals("--event-graph") && args.length>(i+1)) {
                pathToEntityEventGraph = args[i+1];
            }
            else if (arg.equals("--locations") && args.length>(i+1)) {
                LOCATIONS = true;
            }
            else if (arg.equals("--document-graph") && args.length>(i+1)) {
                pathToEntityDocumentGraph = args[i+1];
            }
        }
        File trigFolder = new File (pathToTrigFiles);
        if (pathToEntityDocumentGraph.isEmpty()) {
            String name = trigFolder.getName();
            pathToEntityDocumentGraph = trigFolder.getParentFile().getAbsolutePath()+"/"+name+".entity.doc.trig";
        }
        if (pathToEntityEventGraph.isEmpty()) {
            String name = trigFolder.getName();
            pathToEntityEventGraph = trigFolder.getParentFile().getAbsolutePath()+"/"+name+".entity.eventgit .trig";
        }
        /// Read the CEO OWL ontology to find mappings of FrameNet and SUMO to CEO to get the latest coverage
        if (!new File(pathToOwlOntology).exists()) {
            System.out.println("cannot find pathToOwlOntology = " + pathToOwlOntology);
            return;
        }
        ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        readOwlFile(pathToOwlOntology);
        OntClass myClass = ontologyModel.getOntClass(cltl+"Physical");
        descendHierarchyForValues(myClass, predicateName);
        /////////

        Dataset entityDataSet =  TDBFactory.createDataset();
        Dataset documentDataSet =  TDBFactory.createDataset();
        TrigTripleReader trigTripleReader = new TrigTripleReader();
        ArrayList<File> trigFiles = TrigUtil.makeRecursiveFileList(trigFolder, ".trig");
        for (int f = 0; f < trigFiles.size(); f++) {
            File file = trigFiles.get(f);
            ArrayList<String> documentEntities = new ArrayList<String>();
            TrigTripleData trigTripleData = trigTripleReader.readTripleFromTrigFile(file);
            for (Map.Entry<String,ArrayList<Statement>> entry : trigTripleData.tripleMapInstances.entrySet()) {
                //System.out.println("Key = " + entry.getKey() +  ", Value = " + entry.getValue());
                ArrayList<Statement> statements = entry.getValue();
                if (isEvent(statements)) {
                    ArrayList<String> esoURIs = getEsoTpes(statements);
                    if (esoURIs.isEmpty()) {
                        //// If there are no ESO/CEO types in the RDF, we try to obtain a mappings from FrameNet frames to ESO/CEO
                        ArrayList<String> frames = getFrameNetTypes(statements);
                        for (int i = 0; i < frames.size(); i++) {
                            String frame = frames.get(i);
                            if (mappingsToClass.containsKey(frame)) {
                                ArrayList<String> classes = mappingsToClass.get(frame);
                                for (int j = 0; j < classes.size(); j++) {
                                    String c = classes.get(j);
                                    if (!esoURIs.contains(c)) esoURIs.add(c);
                                }
                            }
                        }
                    }
                    if (!esoURIs.isEmpty()) {
                        ArrayList<String> eventEntities = new ArrayList<String>();
                        for (int i = 0; i < statements.size(); i++) {
                            Statement statement = statements.get(i);
                            if (statement.getObject().isURIResource()) {
                                String objectUri = statement.getObject().asResource().getURI();
                                if (trigTripleData.tripleMapInstances.containsKey(objectUri)) {
                                    ArrayList<Statement> objectStatements = trigTripleData.tripleMapInstances.get(objectUri);
                                    if (isEntity(objectStatements)) {
                                        if (!LOCATIONS && !isLocation(objectStatements)) {
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
                        }
                        if (eventEntities.size() > 1) {
                            for (int i = 0; i < eventEntities.size(); i++) {
                                String entityUri = eventEntities.get(i);
                                for (int j = i + 1; j < eventEntities.size(); j++) {
                                    String entityUri2 = eventEntities.get(j);
                                   /* System.out.println("entityUri = " + entityUri);
                                    System.out.println("esoURI = " + esoURI);
                                    System.out.println("entityUri2 = " + entityUri2);*/
                                    for (int k = 0; k < esoURIs.size(); k++) {
                                        String esoURI =  esoURIs.get(k);
                                        addStatement(entityDataSet.getDefaultModel(), entityUri, esoURI, entityUri2);
                                        ArrayList<Statement> objectStatements = trigTripleData.tripleMapInstances.get(entityUri);
                                        entityDataSet.getDefaultModel().add(objectStatements);
                                    }

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

    static ArrayList<String> getEsoTpes (ArrayList<Statement> statements) {
        ArrayList<String> types = new ArrayList<String>();
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement.getPredicate().getLocalName().equals("type")) {
               // System.out.println("statement.getObject().asResource().getNameSpace() = " + statement.getObject().asResource().getNameSpace());
                if (statement.getObject().asResource().getNameSpace().equals("http://cltl.nl/ontology/ceo#")) {
                    String esoUri = statement.getObject().asResource().getURI();
                    types.add(esoUri);
                }
                else if (statement.getObject().asResource().getNameSpace().equals("http://cltl.nl/ontology/eso#")) {
                    String esoUri = statement.getObject().asResource().getURI();
                    types.add(esoUri);
                }
            }
        }
        return types;
    }

    static ArrayList<String> getFrameNetTypes (ArrayList<Statement> statements) {
        ArrayList<String> types = new ArrayList<String>();
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement.getPredicate().getLocalName().equals("type")) {
                if (statement.getObject().asResource().getNameSpace().indexOf("framenet")>-1) {
                   // System.out.println("statement.getObject().asResource().getNameSpace() = " + statement.getObject().asResource().getNameSpace());
                    String esoUri = statement.getObject().asResource().getURI();
                    types.add(esoUri);
                }
            }
        }
        return types;
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

    static boolean isPerson (ArrayList<Statement> statements) {
            for (int i = 0; i < statements.size(); i++) {
                Statement statement = statements.get(i);
                if (statement.getPredicate().getLocalName().equals("type")) {
                    if (statement.getObject().asResource().getLocalName().equalsIgnoreCase("PERS")) {
                        return true;
                    }
                    else if (statement.getObject().asResource().getLocalName().equalsIgnoreCase("PERSON")) {
                        return true;
                    }
                }
            }
            return false;
    }

    static boolean isOrganisation (ArrayList<Statement> statements) {
            for (int i = 0; i < statements.size(); i++) {
                Statement statement = statements.get(i);
                if (statement.getPredicate().getLocalName().equals("type")) {
                    if (statement.getObject().asResource().getLocalName().equalsIgnoreCase("ORG")) {
                        return true;
                    }
                    else if (statement.getObject().asResource().getLocalName().equalsIgnoreCase("ORGANISATION")) {
                        return true;
                    }
                }
            }
            return false;
    }

    static boolean isLocation (ArrayList<Statement> statements) {
            for (int i = 0; i < statements.size(); i++) {
                Statement statement = statements.get(i);
                if (statement.getPredicate().getLocalName().equals("type")) {
                    if (statement.getObject().asResource().getLocalName().equalsIgnoreCase("LOC")) {
                        return true;
                    }
                    else if (statement.getObject().asResource().getLocalName().equalsIgnoreCase("LOCATION")) {
                        return true;
                    }
                    else if (statement.getObject().asResource().getLocalName().equalsIgnoreCase("PLACE")) {
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

    static void readOwlFile (String pathToOwlFile) {
               InputStream in = FileManager.get().open(pathToOwlFile);
               File owlFile = new File(pathToOwlFile);
               if (!owlFile.exists()) {
                   System.out.println("Cannot find pathToOwlFile = " + pathToOwlFile);
               }
               ontologyModel.read(in, "RDF/XML-ABBREV");
   }

    static String getPredicateValue (Statement statement, String predicateName) {
        String value = "";
        if (statement.getPredicate().getLocalName().equals(predicateName)) {
            if (statement.getObject().isURIResource()) {
                value = statement.getObject().asResource().getLocalName();
            }
            else {
                value = statement.getObject().toString();
                int idx = value.indexOf("^^");
                if (idx > -1) value = value.substring(0, idx);
            }
        }
        return value;
    }

    static void descendHierarchyForValues (OntClass ontClass, String predicateName) {
        StmtIterator pI = ontClass.listProperties();
        while (pI.hasNext()) {
            Statement statement = pI.next();
            String value = getPredicateValue(statement, predicateName);
            if (mappingsToClass.containsKey(value)) {
                ArrayList<String> classes = mappingsToClass.get(value);
                classes.add(ontClass.getLocalName());
                mappingsToClass.put(value, classes);
            }
            else {
                ArrayList<String> classes = new ArrayList<String>();
                classes.add(ontClass.getLocalName());
                mappingsToClass.put(value, classes);
            }

        }
        // Go deep
        for (Iterator j = ontClass.listSubClasses(); j.hasNext();) {
            OntClass c = (OntClass) j.next();
            descendHierarchyForValues(c, predicateName);
        }
    }
}
