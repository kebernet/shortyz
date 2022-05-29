package com.totsp.crossword.net;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

import android.net.Uri;
import android.text.Html;

import com.totsp.crossword.io.JPZIO;
import com.totsp.crossword.puz.PuzzleMeta;
import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.puz.Box;
import com.totsp.crossword.versions.AndroidVersionUtils;
import com.totsp.crossword.versions.DefaultUtil;
import com.totsp.crossword.io.IO;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.net.URL;
import java.util.Map.Entry;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

// import java.util.Base64; // does not work with old JDKs < 8 - so use apache version
import org.apache.commons.codec.binary.Base64;
import cz.jirutka.unidecode.Unidecode;

public abstract class AbstractAmuseLabsDownloader extends AbstractDownloader {

    protected String picker_url;
    protected AbstractAmuseLabsDownloader(String baseUrl, File downloadDirectory, String downloaderName, String pickerUrl) {
        super(baseUrl, downloadDirectory, downloaderName);
        LOG.info("AbstractAmuseLabsDownloader pickerUrl=" + pickerUrl);
        LOG.info("AbstractAmuseLabsDownloader baseUrl=" + baseUrl);
        picker_url = pickerUrl;
    }


    public static void LongStringLOG(String message) {

	int _logCharLimit = 2000;
        // If the message is less than the limit just show
        if (message.length() < _logCharLimit) {
            LOG.info(message);
	    return;
        }
        int sections = message.length() / _logCharLimit;
        for (int i = 0; i <= sections; i++) {
            int max = _logCharLimit * (i + 1);
            if (max >= message.length()) {
                LOG.info(message.substring(_logCharLimit * i));
            } else {
                LOG.info(message.substring(_logCharLimit * i, max));
	    }
        }
    }

	 
    
    protected String getRequestText(URL url, Map<String, String> headers) {
        String s = null;
        OkHttpClient httpclient = new OkHttpClient();
        LOG.info("AbstractAmuseLabsDownloader getRequestText url="+url.toString());
        Request.Builder requestBuilder = new Request.Builder()
            .url(url.toString());

        for (Entry<String, String> e : headers.entrySet()) {
            requestBuilder = requestBuilder.header(e.getKey(), e.getValue());
        }

        try {
            //LOG.info("AbstractAmuseLabsDownloader getRequestText execute start");
            Response response = httpclient.newCall(requestBuilder.build()).execute();
            // LOG.info("AbstractAmuseLabsDownloader getRequestText execute complete");
            s = response.body().string();
            //LOG.info("AbstractAmuseLabsDownloader getRequestText received body" + s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    protected String getPickerTokenSuffix(Date date, Map<String, String> headers) {
        String picker_source;
        try {
            URL url = new URL(picker_url);
            picker_source = getRequestText(url, headers);
        } catch (Exception e) {
            LOG.info("AbstractAmuseLabsDownloader getPickerTokenSuffix caught exception");
            e.printStackTrace();
            return "";
        }

        String[] lines = picker_source.split(System.lineSeparator());
        for (String line: lines) {
            if (line.contains("pickerParams.rawsps")) {
                String[] tokens = line.split("\'");
                String rawps = tokens[1];
                // LongStringLOG("AbstractAmuseLabsDownloader getPickerTokenSuffix Got rawps: " + rawps);
                // String rawps_decoded = new String(Base64.getDecoder().decode(rawps)); // Requires Java 9 or greater
		String rawps_decoded = new String(Base64.decodeBase64(rawps.getBytes()));
                //LOG.info("AbstractAmuseLabsDownloader getPickerTokenSuffix Got rawps_decoded: " + rawps_decoded);
                JsonObject jsonObject = new JsonParser().parse(rawps_decoded).getAsJsonObject();
                String picker_token = jsonObject.get("pickerToken").getAsString();
                LOG.info("AbstractAmuseLabsDownloader getPickerTokenSuffix Got pickerToken=" + picker_token);
                return ("&pickerToken=" + picker_token);
            } else {
                // LOG.info("Did not get rawsps in line: " + line);
            }
        }
        LOG.info("AbstractAmuseLabsDownloader getPickerTokenSuffix Did not get pickerToken");
        return "";
    }

    protected Puzzle parsePuzzle(JsonObject puzjson) {
        Puzzle puz = new Puzzle();
	Unidecode ud = Unidecode.toAscii();
        String title = puzjson.get("title").getAsString();
        //LOG.info("AbstractAmuseLabsDownloader parsePuzzle Got title=" + title);
        puz.setTitle(ud.decode(title));
        String author = puzjson.get("author").getAsString();
        //LOG.info("AbstractAmuseLabsDownloader parsePuzzle Got author=" + author);
        puz.setAuthor(ud.decode(author));
        String copyright = puzjson.get("copyright").getAsString();
        //LOG.info("AbstractAmuseLabsDownloader parsePuzzle Got copyright=" + copyright);
        puz.setCopyright(ud.decode(copyright));
        int w = puzjson.get("w").getAsInt();
        //LOG.info("AbstractAmuseLabsDownloader parsePuzzle Got w=" + w);
        puz.setWidth(w);
        int h = puzjson.get("h").getAsInt();
        //LOG.info("AbstractAmuseLabsDownloader parsePuzzle Got h=" + h);
        puz.setHeight(h);
        JsonArray cell_info = puzjson.getAsJsonArray("cellInfos");
        boolean[][] circledBoxes = new boolean[h][w];
        if (cell_info != null) {
            //LOG.info("AbstractAmuseLabsDownloader parsePuzzle Got cellInfo len="+cell_info.size());
            for (JsonElement cinfo : cell_info) {
                JsonObject cinfoObj = cinfo.getAsJsonObject();
                Boolean is_circled = cinfoObj.get("isCircled").getAsBoolean();
                if ((is_circled != null) && is_circled.booleanValue()) {
                    circledBoxes[cinfoObj.get("y").getAsInt()][cinfoObj.get("x").getAsInt()] = true;
                }
            }
        }

        JsonArray box = puzjson.getAsJsonArray("box");
        Box[][] boxes = new Box[h][w];
        for (int col = 0; col < w; col++) {
            //LOG.info("AbstractAmuseLabsDownloader parsePuzzle Process col=" + col);
            JsonArray row_data = box.get(col).getAsJsonArray();
            for (int row = 0; row < h; row++) {
                char cell = row_data.get(row).getAsCharacter();
                //LOG.info("AbstractAmuseLabsDownloader parsePuzzle Got row=" + row + " col=" + col + " cell=" +cell);
                if (cell == '\u0000') {
                    //markup += '\u0000';
                } else {
                    Box b = new Box();
                    b.setSolution(cell);
                    if (circledBoxes[row][col]) {
                        //LOG.info("AbstractAmuseLabsDownloader parsePuzzle circled=true row=" + row + " col=" + col + " cell=" +cell);
                        puz.setGEXT(true);
                        b.setCircled(true);
                    }
                    boxes[row][col] = b;
                }
            }
        }
        int[][] clueNums = new int[h][w];
        JsonArray clueNums_json = puzjson.getAsJsonArray("clueNums");
        //LOG.info("AbstractAmuseLabsDownloader parsePuzzle Process clueNums");
        for (int col = 0; col < w; col++) {
            //LOG.info("AbstractAmuseLabsDownloader parsePuzzle Process clueNums col=" + col);
            JsonArray row_data = clueNums_json.get(col).getAsJsonArray();
            for (int row = 0; row < h; row++) {
                clueNums[row][col] = row_data.get(row).getAsInt();
                //LOG.info("AbstractAmuseLabsDownloader parsePuzzle clueNum=" + clueNums[row][col] + " row="+row);
            }
        }
        JsonArray placedWords_json = puzjson.getAsJsonArray("placedWords");
        JsonArray acrossWords = new JsonArray();
        JsonArray downWords = new JsonArray();
        Map<Integer, String> acrossNumToClueMap = new HashMap<Integer, String>();
        Map<Integer, String> downNumToClueMap = new HashMap<Integer, String>();
        // LOG.info("AbstractAmuseLabsDownloader parsePuzzle Process placedWords");
        int maxClueNum = -1;
        for (JsonElement word : placedWords_json) {
            JsonObject wordObj = word.getAsJsonObject();
            int clueNumber = wordObj.get("clueNum").getAsInt();
            JsonObject clueObj = wordObj.get("clue").getAsJsonObject();
            String clueText = ud.decode(clueObj.get("clue").getAsString());
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                clueText = Html.fromHtml(clueText, Html.FROM_HTML_MODE_LEGACY).toString();
            } else {
                clueText = Html.fromHtml(clueText).toString();
            }

	    //LOG.info("AbstractAmuseLabsDownloader parsePuzzle clueText=" + clueText + " clueNumber=" + clueNumber);
            if (wordObj.get("acrossNotDown").getAsBoolean()) {
                //LOG.info("AbstractAmuseLabsDownloader parsePuzzle Process placedWords word is across ");
                acrossWords.add(word);
                acrossNumToClueMap.put(clueNumber, clueText);
            } else {
                //LOG.info("AbstractAmuseLabsDownloader parsePuzzle Process placedWords word is down " );
                downWords.add(word);
                downNumToClueMap.put(clueNumber, clueText);
            }
            if (clueNumber > maxClueNum) {
                maxClueNum = clueNumber;
            }
        }

        int numberOfClues = acrossNumToClueMap.size() + downNumToClueMap.size();
        puz.setNumberOfClues(numberOfClues);
        //LOG.info("AbstractAmuseLabsDownloader parsePuzzle Process set number of clues="+numberOfClues);
        // LOG.info("AbstractAmuseLabsDownloader parsePuzzle Process max clue number="+maxClueNum);

        String[] rawClues = new String[numberOfClues];
        String[] acrossClues = new String[acrossNumToClueMap.size()];
        String[] downClues = new String[downNumToClueMap.size()];
        int cnum = 0;
        int acnum = 0;
        int dcnum = 0;
        for (int clueNum = 1; clueNum <= maxClueNum; clueNum++) {
            //LOG.info("AbstractAmuseLabsDownloader parsePuzzle Processing clue="+clueNum);
	    boolean p = false;
            if (acrossNumToClueMap.containsKey(clueNum)) {
                //LOG.info("parsePuzzle Processing across clue="+clueNum);
                rawClues[cnum] = acrossNumToClueMap.get(clueNum);
		acrossClues[acnum] = acrossNumToClueMap.get(clueNum);
		acnum++;
                //LOG.info("parsePuzzle Processing across clue text="+rawClues[cnum]);
		p = true;
                cnum++;
            }
            if (downNumToClueMap.containsKey(clueNum)) {
                //LOG.info("AbstractAmuseLabsDownloader parsePuzzle Processing down clue="+clueNum);
                rawClues[cnum] = downNumToClueMap.get(clueNum);
		downClues[dcnum] = downNumToClueMap.get(clueNum);
		dcnum++;
                //LOG.info("parsePuzzle Processing down clue text="+rawClues[cnum]);
		p = true;
                cnum++;
            }
	    if (!p) {
                LOG.info("AbstractAmuseLabsDownloader parsePuzzle no value for clue="+clueNum);
	    }
        }
	LOG.info("AbstractAmuseLabsDownloader parsePuzzle processed clues="+cnum + " across=" + acnum + " down=" + dcnum);

        //LOG.info("AbstractAmuseLabsDownloader parsePuzzle set boxes");
        puz.setBoxes(boxes);

        //LOG.info("parsePuzzle set raw clues");
        puz.setRawClues(rawClues);
	puz.setDownClues(downClues);
	puz.setAcrossClues(acrossClues);
	
        // verify clue numbers
	Box[][] pboxes = puz.getBoxes();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int cn = clueNums[y][x];
                if (cn != 0) {
		    Box pbox = pboxes[y][x];
                    if (pbox.getClueNumber() != cn) {
                        LOG.info("AbstractAmuseLabsDownloader parsePuzzle Invalid clue number y=" + y + " x=" + x + " clueNum=" + cn + " found="+puz.getBoxes()[y][x].getClueNumber());
                    } else {
			//LOG.info("Box col=" + y + " row=" + x + " char=" + pbox.getSolution());
		    }
                } else {
		    //LOG.info("AbstractAmuseLabsDownloader parsePuzzle No clue number for y=" + y + " x=" + x );
		}
            }
        }

        return puz;
    }

    static protected String reverseString(String str) {
	StringBuffer b = new StringBuffer(str).reverse();
	return b.toString();
    }
    
    protected String decode_rawc(String rawc) {
	if (! rawc.contains(".")) {
	    LOG.info("AbstractAmuseLabsDownloader decode_rawc return same rawc");
	    return new String(Base64.decodeBase64(rawc.getBytes()));
	}
	String rawcParts[] = rawc.split("\\.");
	char buffer[] = rawcParts[0].toCharArray();
	char key1[] = reverseString(rawcParts[1]).toCharArray();
	int key[] = new int[key1.length];
	for (int i = 0; i < key.length; i++) {
	    key[i] = Character.digit(key1[i], 16) + 2;
	}

	int i = 0;
	int sc = 0;
	while (i < (buffer.length - 1)) {
	    int sl = Math.min(key[sc % key.length], buffer.length - i);
	    for (int j = 0; j < Math.floorDiv(sl, 2); j++) {
		char c = buffer[i+j];
		buffer[i+j] = buffer[i + sl - j - 1];
		buffer[i + sl - j - 1] = c;
	    }
	    i += sl;
	    sc++;
	}
	String newRawc = new String(buffer);
	LOG.info("AbstractAmuseLabsDownloader decode_rawc return newRawc=" + newRawc);
	return new String(Base64.decodeBase64(newRawc.getBytes()));
    }
    
    protected File download(Date date, String urlSuffix, Map<String, String> headers) {
        String pickerSuffix = getPickerTokenSuffix(date, headers);
        String dlurl = baseUrl + pickerSuffix + "&id=" + urlSuffix;
        LOG.info("AbstractAmuseLabsDownloader download picker full url="+dlurl);

        String pdata = "";
        try {
            URL url = new URL(dlurl);
            pdata  = getRequestText(url, headers);
            LOG.info("AbstractAmuseLabsDownloader download get picker info complete");
        } catch (Exception e) {
            LOG.info("AbstractAmuseLabsDownloader download caught error url=" + dlurl);
            e.printStackTrace();
        }

        String[] lines = pdata.split(System.lineSeparator());
        Puzzle p = null;
        for (String line: lines) {
            if (line.contains("window.rawc")) {
                //LOG.info("AbstractAmuseLabsDownloader download Got window.rawc in line: " + line);
                String[] tokens = line.split("\'");

                String rawc = tokens[1];

                // String rawc_decoded = new String(Base64.getDecoder().decode(rawc)); // Required JDK 8 or above
                LongStringLOG("AbstractAmuseLabsDownloader download Got rawc: " + rawc);
		String rawc_decoded = decode_rawc(rawc);
                LongStringLOG("AbstractAmuseLabsDownloader download Got rawc_decoded: " + rawc_decoded);
                JsonObject jsonObject = new JsonParser().parse(rawc_decoded).getAsJsonObject();
                p = parsePuzzle(jsonObject);
                LOG.info("AbstractAmuseLabsDownloader download Parsed puzzle ");
                break;
            }
        }
        if (p == null) {
            LOG.info("AbstractAmuseLabsDownloader download - error downloading puzzle");
            return null;
        }
        String fname = this.createFileName(date);
        try {
            LOG.info("AbstractAmuseLabsDownloader download write to file dir=" + downloadDirectory + " fname=" + fname);
            File puzFile = new File(downloadDirectory, fname);
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(puzFile));
            p.setDate(date);
            p.setVersion(IO.VERSION_STRING);
            IO.saveNative(p, dos);
	    LOG.info("AbstractAmuseLabsDownloader download completed - wrote file");
            return puzFile;
        } catch (FileNotFoundException e) {
            LOG.info("AbstractAmuseLabsDownloader caught FileNotFoundException dir=" + downloadDirectory + " fname=" + fname);
            e.printStackTrace();
        } catch (IOException e) {
            LOG.info("AbstractAmuseLabsDownloader caught IOException dir=" + downloadDirectory + " fname=" + fname);
            e.printStackTrace();
        }
        return null;
    }

    public String createFileName(Date date) {
        return (date.getYear() + 1900) + "-" + (date.getMonth() + 1) + "-" + date.getDate() + "-" +
            this.getName().replaceAll(" ", "") + ".puz";
    }

    protected File download(Date date, String urlSuffix, Map<String, String> headers, boolean canDefer) {
        LOG.info("AbstractAmuseLabsDownloader.download: canDefer download not tested");
        LOG.info("Mkdirs: " + this.downloadDirectory.mkdirs());
        LOG.info("Exist: " + this.downloadDirectory.exists());

        try {
            URL url = new URL(this.baseUrl + urlSuffix);
            LOG.info("Downloading from "+url);

            File f = new File(downloadDirectory, this.createFileName(date)+".jpz");
            PuzzleMeta meta = new PuzzleMeta();
            meta.date = date;
            meta.source = getName();
            meta.sourceUrl = url.toString();
            meta.updatable = false;

            utils.storeMetas(Uri.fromFile(f), meta);
            if( canDefer ){
                if (utils.downloadFile(url, f, headers, true, this.getName())) {
                    DownloadReceiver.metas.remove(Uri.fromFile(f));

                    return f;
                } else {
                    return Downloader.DEFERRED_FILE;
                }
            } else {
                AndroidVersionUtils.Factory.getInstance().downloadFile(url, f, headers, true, this.getName());
                return f;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
