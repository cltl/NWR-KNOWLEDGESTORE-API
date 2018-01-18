# NWR-TRIPLE-API
This module handles the interaction with the NewsReader KnowledgeStore. It supports both querying and posting as functions.

1. Querying

Queries the KnowledgeStore populated with NewsReader output or the TRiG files and represents the result as SEM-RDF or SEM-JSON

2. Posting Triples to the KnowledgeStore

You can post any ArrayList of Statements to the static address of the KnowledgeStore

vu.cltl.triple.write.WriteStatementsKnowledgeStore.storeTriples

static public void storeTriples (ArrayList<Statement> statements)
    
public static String ksAddress = "http://145.100.59.153:50053/";


3. Creating GRASP triples from communication

vu.cltl.triple.write.CreateGraspTriples.graspTripleString

static public String graspTripleString (String sourceId, 
                                     String authorUri,
                                     String authorName,
                                     String perspectiveValues,
                                     String eventLabel,
                                     String eventUri,
                                     String actorLabel,
                                     String actorUri)

static public Dataset graspDataSet (String sourceId, String authorUri,
                                             String authorName,
                                             String perspectiveValues,
                                             String eventLabel,
                                             String eventUri,
                                             String actorLabel,
                                             String actorUri)
                                             
- sourceId = unique identifier for the communication event itself, e.g. today's chat at 11am between Piek and Leolani
- authorUri = Can be empty. If empty, Leolani will create a URI using the author name and <http://cltl.nl/leolani/friends/<NAME>>. If non empty, it will use whatever URI is provided.
- authorName = The name to address the author
- perspectiveValues = Values for perspectives concatentated with ";". This is free text now and will be formally defined in GRaSP.
- eventLabel = the word used to make reference to an event, e.g. "bite"
- eventUri = if the event itself is known provide the URI, otherwise leave empty and Leolani will create a unique identifier using the sourceId, author, time of conversation and the label
- actorLabel = the word used to make reference to the actor
- actorUri = the URI of the actor. If empty, a URI is created from the label


The feedBrain class shows how you can create the GRaSP triples and send them to the KnowledgeStore.
