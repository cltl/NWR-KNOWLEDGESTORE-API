package vu.cltl.triple.read;

import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafWordForm;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * Created by piek on 09/02/16.
 */
public class GetMentionsFromKnowledgeStore {
    static final int nContext = 75;
    final String USER_AGENT = "Mozilla/5.0";
    public static String serviceBase = "https://knowledgestore2.fbk.eu/";
    public static String user = "nwr_partner";
    public static String pass = "ks=2014!";


    public static String getText(String stringUrl) throws Exception {
        //stringUrl = "https://knowledgestore2.fbk.eu/nwr/cars3/files?id=%3Chttp%3A%2F%2Fwww.newsreader-project.eu%2Fdata%2Fcars%2F2004%2F10%2F18%2F4DKT-30W0-00S0-W39B.xml.naf%3E";

        URL url = new URL(stringUrl);
        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        // connection.setRequestProperty("Accept", "application/xml");
        connection.setRequestProperty("Accept", "application/octet-stream");
        InputStream xml = connection.getInputStream();
        String text =  xml.toString();
        return text;
    }


    public static String getNafRawText(String stringUrl) throws Exception {
        //stringUrl = "https://knowledgestore2.fbk.eu/nwr/cars3/files?id=%3Chttp%3A%2F%2Fwww.newsreader-project.eu%2Fdata%2Fcars%2F2004%2F10%2F18%2F4DKT-30W0-00S0-W39B.xml.naf%3E";

        URL url = new URL(stringUrl);
        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        // connection.setRequestProperty("Accept", "application/xml");
        connection.setRequestProperty("Accept", "application/octet-stream");
        InputStream xml = connection.getInputStream();
        KafSaxParser kafSaxParser = new KafSaxParser();
        kafSaxParser.parseFile(xml);
        String rawText =  kafSaxParser.rawText;
        return rawText;
    }

    public static ArrayList<KafWordForm> getNafWordFormList(String stringUrl) throws Exception {
        //stringUrl = "https://knowledgestore2.fbk.eu/nwr/cars3/files?id=%3Chttp%3A%2F%2Fwww.newsreader-project.eu%2Fdata%2Fcars%2F2004%2F10%2F18%2F4DKT-30W0-00S0-W39B.xml.naf%3E";
        KafSaxParser kafSaxParser = new KafSaxParser();

        URL url = new URL(stringUrl);
        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept-Encoding", "gzip");  /// gets gzipped NAF
        InputStream xml = connection.getInputStream();
        InputStream gzipStream = new GZIPInputStream(xml);
        kafSaxParser.parseFile(gzipStream);
        System.out.println(gzipStream);
        return kafSaxParser.kafWordFormList;
    }

    public static String getStringFromDocument(Document doc) throws TransformerException {
        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        return writer.toString();
    }


    public static String getKSNafRawText(String urlString) throws Exception {
        String content = "";
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass.toCharArray());
            }
        });
        content = getNafRawText(urlString);
        return content;
    }

    public static String makeRequestUrl (String SERVICE, String knowledgeStore, String mentionUri) {
        //https://knowledgestore2.fbk.eu/nwr/wikinews-new/files?id=<
        //knowledgestore2.fbk.eu/nwr/wikinews-new
       // knowledgeStore = "nwr/wikinews-new";
        String str = "";
        if (knowledgeStore.isEmpty()) {
            str = SERVICE+"/files?id=<"+mentionUri+".naf>";
        }
        else {
            str = SERVICE + "/"+knowledgeStore+"/files?id=<"+mentionUri+".naf>";
        }
        return str;
    }

    public static String makeTextRequestUrl (String knowledgeStore, String mentionUri) {
        //https://knowledgestore2.fbk.eu/nwr/wikinews-new/files?id=<
        //knowledgestore2.fbk.eu/nwr/wikinews-new
       // knowledgeStore = "nwr/wikinews-new";
        String str = "";
        if (knowledgeStore.isEmpty()) {
            str = serviceBase+"/files?id=<"+mentionUri+".naf>";
        }
        else {
            str = serviceBase + knowledgeStore+"/files?id=<"+mentionUri+".naf>";
        }
        return str;
    }





    public static void createRawTextIndexFromMentions(ArrayList<JSONObject> objects, JSONObject timeLineObject, String KS) {
        ArrayList<String> sourceUriList = new ArrayList<String>();
        for (int i = 0; i < objects.size(); i++) {
            JSONObject jsonObject = objects.get(i);
            try {
                JSONArray mentions = (JSONArray) jsonObject.get("mentions");
                for (int j = 0; j < mentions.length(); j++) {
                    JSONObject mObject  = mentions.getJSONObject(j);
                    String uString = mObject.getString("uri");
                    if (!sourceUriList.contains(uString)) {
                        sourceUriList.add(uString);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Getting sourcedocuments for unique sources = " + sourceUriList.size());
        long startTime = System.currentTimeMillis();
        Authenticator.setDefault(new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass.toCharArray());
            }
        });
        for (int i = 0; i < sourceUriList.size(); i++) {
            String sourceUri = sourceUriList.get(i);
            try {
                String nafURI = makeTextRequestUrl(KS, sourceUri);
                //String nafURI = makeRequestUrl(KS, sourceUri);
                //String text = GetNAF.getFile(nafURI);
                String text = getText(nafURI);
                JSONObject jsonSnippetObject = new JSONObject();
                jsonSnippetObject.put("uri", sourceUri);
                jsonSnippetObject.put("text", text);
                timeLineObject.append("sources", jsonSnippetObject);
            } catch (Exception e) {
               // e.printStackTrace();
                JSONObject jsonSnippetObject = new JSONObject();
                try {
                    jsonSnippetObject.put("uri", sourceUri);
                    jsonSnippetObject.put("text", "NAF file not found");
                    timeLineObject.append("sources", jsonSnippetObject);
                } catch (JSONException e1) {
                   // e1.printStackTrace();
                }
            }
        }
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("Time elapsed to get sources from KS:");
        System.out.println(estimatedTime/1000.0);

    }

    public static void createRawTextIndexFromMentions(ArrayList<JSONObject> objects, JSONObject timeLineObject, String SERVICE, String KS, final String KSuser, final String KSpass) {
        ArrayList<String> sourceUriList = new ArrayList<String>();
        for (int i = 0; i < objects.size(); i++) {
            JSONObject jsonObject = objects.get(i);
            try {
                JSONArray mentions = (JSONArray) jsonObject.get("mentions");
                for (int j = 0; j < mentions.length(); j++) {
                    JSONObject mObject  = mentions.getJSONObject(j);
                    String uString = mObject.getString("uri");
                    if (!sourceUriList.contains(uString)) {
                        sourceUriList.add(uString);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Getting sourcedocuments for unique sources = " + sourceUriList.size());
        long startTime = System.currentTimeMillis();
        Authenticator.setDefault(new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(KSuser, KSpass.toCharArray());
            }
        });
        for (int i = 0; i < sourceUriList.size(); i++) {
            String sourceUri = sourceUriList.get(i);
            try {
                String nafURI = makeRequestUrl(SERVICE, KS, sourceUri);
                String text = getNafRawText(nafURI);
                JSONObject jsonSnippetObject = new JSONObject();
                jsonSnippetObject.put("uri", sourceUri);
                jsonSnippetObject.put("text", text);
                timeLineObject.append("sources", jsonSnippetObject);
            } catch (Exception e) {
               // e.printStackTrace();
                JSONObject jsonSnippetObject = new JSONObject();
                try {
                    jsonSnippetObject.put("uri", sourceUri);
                    jsonSnippetObject.put("text", "NAF file not found");
                    timeLineObject.append("sources", jsonSnippetObject);
                } catch (JSONException e1) {
                   // e1.printStackTrace();
                }
            }
        }
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("Time elapsed to get sources from KS:");
        System.out.println(estimatedTime/1000.0);

    }

    static ArrayList<JSONObject> createRawTextIndexFromMentions (ArrayList<JSONObject> objects, String KS) {
        ArrayList<JSONObject> sourceObjects = new ArrayList<JSONObject>();
        ArrayList<String> sourceUriList = new ArrayList<String>();
        for (int i = 0; i < objects.size(); i++) {
            JSONObject jsonObject = objects.get(i);
            try {
                JSONArray mentions = (JSONArray) jsonObject.get("mentions");
                for (int j = 0; j < mentions.length(); j++) {
                    JSONObject mObject  = mentions.getJSONObject(j);
                    String uString = mObject.getString("uri");
                    if (!sourceUriList.contains(uString)) {
                        sourceUriList.add(uString);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Getting sourcedocuments for unique sources = " + sourceUriList.size());
        long startTime = System.currentTimeMillis();
        Authenticator.setDefault(new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass.toCharArray());
            }
        });
        for (int i = 0; i < sourceUriList.size(); i++) {
            String sourceUri = sourceUriList.get(i);
            try {
                String nafURI = makeTextRequestUrl(KS, sourceUri);
                //String nafURI = makeRequestUrl(KS, sourceUri);
                //String text = GetNAF.getFile(nafURI);
                String text = getText(nafURI);
                JSONObject jsonSnippetObject = new JSONObject();
                jsonSnippetObject.put("uri", sourceUri);
                jsonSnippetObject.put("text", text);
                sourceObjects.add(jsonSnippetObject);
            } catch (Exception e) {
               // e.printStackTrace();
                JSONObject jsonSnippetObject = new JSONObject();
                try {
                    jsonSnippetObject.put("uri", sourceUri);
                    jsonSnippetObject.put("text", "NAF file not found");
                    sourceObjects.add(jsonSnippetObject);
                } catch (JSONException e1) {
                   // e1.printStackTrace();
                }
            }
        }
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("Time elapsed to get sources from KS:");
        System.out.println(estimatedTime/1000.0);
        return sourceObjects;
    }

    static ArrayList<JSONObject> createRawTextIndexFromMentions (ArrayList<JSONObject> objects, String SERVICE, String KS, final String KSuser, final String KSpass) {
        ArrayList<JSONObject> sourceObjects = new ArrayList<JSONObject>();
        ArrayList<String> sourceUriList = new ArrayList<String>();
        for (int i = 0; i < objects.size(); i++) {
            JSONObject jsonObject = objects.get(i);
            try {
                JSONArray mentions = (JSONArray) jsonObject.get("mentions");
                for (int j = 0; j < mentions.length(); j++) {
                    JSONObject mObject  = mentions.getJSONObject(j);
                    String uString = mObject.getString("uri");
                    if (!sourceUriList.contains(uString)) {
                        sourceUriList.add(uString);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Getting sourcedocuments for unique sources = " + sourceUriList.size());
        long startTime = System.currentTimeMillis();
        Authenticator.setDefault(new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(KSuser, KSpass.toCharArray());
            }
        });
        for (int i = 0; i < sourceUriList.size(); i++) {
            String sourceUri = sourceUriList.get(i);
            try {
                String nafURI = makeRequestUrl(SERVICE, KS, sourceUri);
                String text = getNafRawText(nafURI);
                JSONObject jsonSnippetObject = new JSONObject();
                jsonSnippetObject.put("uri", sourceUri);
                jsonSnippetObject.put("text", text);
                sourceObjects.add(jsonSnippetObject);
            } catch (Exception e) {
               // e.printStackTrace();
                JSONObject jsonSnippetObject = new JSONObject();
                try {
                    jsonSnippetObject.put("uri", sourceUri);
                    jsonSnippetObject.put("text", "NAF file not found");
                    sourceObjects.add(jsonSnippetObject);
                } catch (JSONException e1) {
                   // e1.printStackTrace();
                }
            }
        }
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("Time elapsed to get sources from KS:");
        System.out.println(estimatedTime/1000.0);
        return sourceObjects;
    }



    public static String createSnippetIndexFromMentions(ArrayList<JSONObject> objects,
                                                      String SERVICE,
                                                      String KS,
                                                      final String KSuser,
                                                      final String KSpass) throws JSONException {
        String log = "";
        HashMap<String, ArrayList<String>> sourceUriList = new HashMap<String, ArrayList<String>>();
        HashMap<String, Integer> eventIdObjectMap = new HashMap<String, Integer>();
        for (int i = 0; i < objects.size(); i++) {
            JSONObject jsonObject = objects.get(i);
            try {
                String eventId = jsonObject.getString("instance");
                eventIdObjectMap.put(eventId, i);
                JSONArray mentions = (JSONArray) jsonObject.get("mentions");
                for (int j = 0; j < mentions.length(); j++) {
                    JSONObject mObject  = mentions.getJSONObject(j);
                    String uString = mObject.getString("uri");
                    if (sourceUriList.containsKey(uString)) {
                        ArrayList<String> eventIds = sourceUriList.get(uString);
                        if (!eventIds.contains(eventId)) {
                            eventIds.add(eventId);
                            sourceUriList.put(uString, eventIds);
                        }
                    }
                    else {
                        ArrayList<String> eventIds = new ArrayList<String>();
                        eventIds.add(eventId);
                        sourceUriList.put(uString, eventIds);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        log = " * Getting source information for " + sourceUriList.size()+" unique documents\n";
        long startTime = System.currentTimeMillis();
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(KSuser, KSpass.toCharArray());
            }
        });
        Set keySet = sourceUriList.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            /// we first get the tokens for the single NAF file.
            /// next we serve each event with mentions in this NAF file
            //System.out.println("key = " + key);
            ArrayList<KafWordForm> wordForms = null;
            try {
                String nafURI = makeRequestUrl(SERVICE, KS, key);
              //  System.out.println("nafURI = " + nafURI);
                wordForms = getNafWordFormList(nafURI);
/*                for (int i = 0; i < wordForms.size(); i++) {
                    KafWordForm kafWordForm = wordForms.get(i);
                    System.out.println("kafWordForm.getCharOffset() = " + kafWordForm.getCharOffset());
                    System.out.println("kafWordForm.toString() = " + kafWordForm.toString());
                }*/
            } catch (Exception e) {
                 e.printStackTrace();
            }
            ArrayList<String> eventIds = sourceUriList.get(key);
            for (int i = 0; i < eventIds.size(); i++) {
                String eventId = eventIds.get(i);
             //   System.out.println("eventId = " + eventId);
                int idx = eventIdObjectMap.get(eventId);
                JSONObject eventObject = objects.get(idx);
                JSONArray mentions = (JSONArray) eventObject.get("mentions");
              //  System.out.println("mentions.length() = " + mentions.length());
                for (int j = 0; j < mentions.length(); j++) {
                    JSONObject mObject  = mentions.getJSONObject(j);
                    String uString = mObject.getString("uri");
                    JSONArray offsetArray = mObject.getJSONArray("char");
                    Integer offsetBegin =  null;
                    try {
                        offsetBegin = Integer.parseInt(offsetArray.getString(0));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (uString.equals(key) && offsetBegin!=null &wordForms!=null) {
                        for (int k = 0; k < wordForms.size(); k++) {
                            KafWordForm kafWordForm = wordForms.get(k);
                            Integer kafOffset = Integer.parseInt(kafWordForm.getCharOffset());
                            if (kafOffset>offsetBegin) {
                                break;
                            }
                            if (kafOffset.equals(offsetBegin)) {
                                // we found the sentence and the word, now make the snippet
                                String wf = kafWordForm.getWf();
                                String sentenceId = kafWordForm.getSent();
                                String newText = kafWordForm.getWf();
                                if (k > 0) {
                                    int m = k-1;
                                    KafWordForm kafWordForm2 = wordForms.get(m);
                                    String sentenceId2 = kafWordForm2.getSent();
                                    while (sentenceId2.equals(sentenceId)) {
                                        newText = kafWordForm2.getWf() + " " + newText;
                                        m--;
                                        if (m >= 0) {
                                            kafWordForm2 = wordForms.get(m);
                                            sentenceId2 = kafWordForm2.getSent();
                                        }
                                        else {
                                            break;
                                        }
                                    }
                                }
                                offsetBegin = newText.lastIndexOf(wf);
                                int offsetEnd = offsetBegin + wf.length();
                                if ((k + 1) < wordForms.size()) {
                                    int m = k + 1;
                                    KafWordForm kafWordForm2 = wordForms.get(m);
                                    String sentenceId2 = sentenceId;
                                    while (sentenceId2.equals(sentenceId)) {
                                        newText = newText + " " + kafWordForm2.getWf();
                                        m++;
                                        if (m < wordForms.size()) {
                                            kafWordForm2 = wordForms.get(m);
                                            sentenceId2 = kafWordForm2.getSent();
                                        } else {
                                            break;
                                        }
                                    }

                                }
                               /* System.out.println("offsetBegin = " + offsetBegin);
                                System.out.println("offsetEnd = " + offsetEnd);
                                System.out.println("final newText = " + newText);*/
                                mObject.append("snippet", newText);
                                mObject.append("snippet_char", offsetBegin);
                                mObject.append("snippet_char", offsetEnd);

                                break;

                            } else {
                                ///not the word
                            }
                        }
                    }
                    else if (wordForms==null || offsetBegin==null) {
                        mObject.append("snippet", "Could not find the original text.");
                        mObject.append("snippet_char", 0);
                        mObject.append("snippet_char", 0);
                    }
                }
            }
            //break;
        }
        long estimatedTime = System.currentTimeMillis() - startTime;
        log = " * Time elapsed to get text snippets from KS:"+estimatedTime/1000.0+" seconds\n";
        return log;
    }


}
