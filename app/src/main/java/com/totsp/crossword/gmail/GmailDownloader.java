package com.totsp.crossword.gmail;

import android.content.Context;
import android.os.Environment;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.totsp.crossword.io.IO;
import com.totsp.crossword.io.JPZIO;
import com.totsp.crossword.net.Downloader;
import com.totsp.crossword.puz.Puzzle;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.totsp.crossword.util.SCollections.neverNull;

/**
 *
 * Created by rcooper on 6/28/15.
 */
@SuppressWarnings("SimpleDateFormat")
public class GmailDownloader implements Downloader {
    private static final Logger LOGGER = Logger.getLogger(GmailDownloader.class.getCanonicalName());
    private static final File CROSSWORDS = new File(Environment.getExternalStorageDirectory(), "crosswords/");
    private final Gmail gmailService;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private DateFormat emailDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");

    public GmailDownloader(Gmail gmailService) {
        this.gmailService = gmailService;
    }


    @Override
    public void setContext(Context context) {

    }

    @Override
    public int[] getDownloadDates() {
        return Downloader.DATE_DAILY;
    }

    @Override
    public String getName() {
        return "Gmail";
    }

    public String toString() {
        return getName();
    }

    @Override
    public String createFileName(Date date) {
        return "gmail";
    }

    @Override
    public File download(Date date) {
        LOGGER.fine("==starting gmail download....");
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DATE, -1);
            Date after = calendar.getTime();
            calendar.setTime(date);
            calendar.add(Calendar.DATE, 1);
            Date before = calendar.getTime();
            String query = "after:"+dateFormat.format(after)+" AND before:"+dateFormat.format(before)+" AND has:attachment AND (filename:.puz OR filename:.jpz)";
            LOGGER.info("Running query: "+query);
            ListMessagesResponse response = gmailService.users()
                    .messages()
                    .list("me")
                    .setQ(query)
                    .execute();
            LOGGER.fine("==Found "+ neverNull(response.getMessages()).size()+" messages.");
            for(Message message : neverNull(response.getMessages())){
                String singleFilename = "";

                Message fetched = gmailService.users().messages().get("me", message.getId())
                        .execute();
                LOGGER.fine("==Fetched message "+fetched.getId());
                HashMap<String, MessagePart> toDownload = new HashMap<>();
                scanParts(fetched.getPayload().getParts(), toDownload);
                for(Map.Entry<String, MessagePart> entry : toDownload.entrySet()){
                    LOGGER.info("==Reading : "+singleFilename);
                    String source = getSender(fetched.getPayload().getHeaders());
                    @SuppressWarnings("deprecation") String filename = (date.getYear() + 1900) + "-" + (date.getMonth() + 1) + "-" + date.getDate() + "-" +
                            source+"-"+singleFilename.replaceAll(" ", "") + ".puz";
                    File destination = new File(CROSSWORDS, filename);
                    if(!destination.exists()){
                        byte[] data = entry.getValue().getBody().getAttachmentId() != null ?
                                gmailService.users().messages().attachments().get("me", message.getId(), entry.getValue().getBody().getAttachmentId()).execute().decodeData()
                                : entry.getValue().getBody().decodeData();
                        Puzzle puzzle = singleFilename.endsWith("jpz") ?
                                JPZIO.readPuzzle(new ByteArrayInputStream(data))
                                : IO.loadNative(new DataInputStream(new ByteArrayInputStream(data)));
                        puzzle.setDate(date);
                        puzzle.setSource(source);
                        puzzle.setSourceUrl("gmail://" + fetched.getId());
                        puzzle.setUpdatable(false);
                        date = getSentDate(fetched.getPayload().getHeaders());
                        LOGGER.info("Downloaded "+filename);
                        IO.save(puzzle, destination);
                    }
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void scanParts(List<MessagePart> parts, Map<String, MessagePart> toDownload){
        for(MessagePart part : parts){
            LOGGER.info("Checking part "+part.getPartId()+" filename: "+part.getFilename());
            String filename = part.getFilename();
            String thisPuzzleName;
            if(filename != null && (filename.endsWith(".puz") || filename.endsWith(".jpz"))){
                thisPuzzleName = filename.substring(0, filename.lastIndexOf('.'));
                if(!toDownload.containsKey(thisPuzzleName)){
                    LOGGER.info("Adding part for download "+thisPuzzleName+" "+part.getPartId());
                    toDownload.put(thisPuzzleName, part);
                }
            } else if(part.getParts() != null && !part.getParts().isEmpty()){
                scanParts(part.getParts(), toDownload);
            }
        }
    }

    private Date getSentDate(List<MessagePartHeader> headers){
        for(MessagePartHeader header : headers){
            if("Date".equals(header.getName())){
                try {
                    return emailDateFormat.parse(header.getValue());
                } catch(ParseException e){
                    LOGGER.log(Level.WARNING, "Failed to parse date "+header.getValue(), e);
                }
            }
        }
        return new Date();
    }

    private String getSender(List<MessagePartHeader> headers){
        for(MessagePartHeader header: headers){
            if("From".equals(header.getName())){
                String sender = header.getValue();
                int addressIndex = sender.indexOf('<');
                if(addressIndex > 0){
                    sender = sender.substring(0, addressIndex).trim();
                }
                return sender;
            }
        }
        return "Unknown Gmail Source";
    }

    @Override
    public String sourceUrl(Date date) {
        return null;
    }

    @Override
    public boolean alwaysRun(){
        return true;
    }

    @Override
    public Date getGoodThrough() {
        return new Date();
    }

    @Override
    public Date getGoodFrom() {
        return new Date(0L);
    }
}
