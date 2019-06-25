package com.springboot.rest.scraper;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.springboot.rest.beans.Stock;
import com.sun.istack.internal.logging.Logger;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class YahooFinanceScraper {
	HttpClient client = HttpClientBuilder.create().build();
	HttpContext context = new BasicHttpContext();
	private static final Logger logger = Logger.getLogger(YahooFinanceScraper.class);
	
	public String getPage(String stockName) {
        String rtn = null;
        String url = String.format("https://finance.yahoo.com/quote/%s/?p=%s", stockName, stockName);
        HttpGet request = new HttpGet(url);
        logger.info("url for request "+url);

        request.addHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.13) Gecko/20101206 Ubuntu/10.10 (maverick) Firefox/3.6.13");
        try {
            HttpResponse response = client.execute(request, context);
            logger.info("Response Code : " + response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rtn = result.toString();
            HttpClientUtils.closeQuietly(response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        logger.info("returning from getPage");
        return rtn;
    }
	
	public List<String> splitPageData(String page) {
        // Return the page as a list of string using } to split the page
        return Arrays.asList(page.split("}"));
    }
	
	public String findCrumb(List<String> lines) {
        String crumb = "";
        String rtn = "";
        for (String l : lines) {
            if (l.indexOf("CrumbStore") > -1) {
                rtn = l;
                break;
            }
        }
        // ,"CrumbStore":{"crumb":"OKSUqghoLs8"        
        if (rtn != null && !rtn.isEmpty()) {
            String[] vals = rtn.split(":");                 // get third item
            crumb = vals[2].replace("\"", "");              // strip quotes
            crumb = StringEscapeUtils.unescapeJava(crumb);  // unescape escaped values (particularly, \u002f
            }
        return crumb;
    }
	
	public String getCrumb(String stockName) {
        return findCrumb(splitPageData(getPage(stockName)));
    }
	
	public List<Stock> downloadData(String stockName, long startDate, long endDate, String crumb) {
        String filename = String.format("%s.csv", stockName);
        File generatedFile = null;
        List <Stock> stockData = new ArrayList<Stock>();
        String url = String.format("https://query1.finance.yahoo.com/v7/finance/download/%s?period1=%s&period2=%s&interval=1d&events=history&crumb=%s", stockName, startDate, endDate, crumb);
        HttpGet request = new HttpGet(url);
        logger.info(url);
        
        request.addHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.13) Gecko/20101206 Ubuntu/10.10 (maverick) Firefox/3.6.13");
        try {
            HttpResponse response = client.execute(request, context);
            logger.info("Response Code : " + response.getStatusLine().getStatusCode());
            HttpEntity entity = response.getEntity();

            String reasonPhrase = response.getStatusLine().getReasonPhrase();
            int statusCode = response.getStatusLine().getStatusCode();
            
            logger.info(String.format("statusCode: %d", statusCode));
            logger.info(String.format("reasonPhrase: %s", reasonPhrase));
            
            if (entity != null) {
                BufferedInputStream bis = new BufferedInputStream(entity.getContent());
                generatedFile = new File(filename);
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(generatedFile));
                int inByte;
                while((inByte = bis.read()) != -1) 
                    bos.write(inByte);
                bis.close();
                bos.close();
            }
            HttpClientUtils.closeQuietly(response);
            Pattern pattern = Pattern.compile(",");
            //Creating Stock objects to be shown on UI
            //Using try-with-resource to automatically take care of closing the resources
            try (BufferedReader in = new BufferedReader(new FileReader(generatedFile));) {
                stockData = in.lines().skip(1).map(line->{
                    String[] x = pattern.split(line);
                    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate localDate = LocalDate.parse(x[0], formatter);
                    Float open = (x[1].equals("null")) ? null : Float.parseFloat(x[1]);
                    Float high = (x[2].equals("null")) ? null : Float.parseFloat(x[2]);
                    Float low = (x[3].equals("null")) ? null : Float.parseFloat(x[3]);
                    Float close = (x[4].equals("null")) ? null : Float.parseFloat(x[4]);
                    Float adjClose = (x[5].equals("null")) ? null : Float.parseFloat(x[5]);
                    Integer volume = (x[6].equals("null")) ? null : Integer.parseInt(x[6]);
                    return new Stock(localDate, open, high, low, close, adjClose, volume);
                }).collect(Collectors.toList());
                
            } catch (Exception ex) {
            	ex.printStackTrace();
            }
            logger.info("size of stockdata "+stockData.size());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
        	try{
        		//Throws exception is file isnt deleted
        	Files.deleteIfExists(generatedFile.toPath()); 
        	} catch(Exception ex){
        		ex.printStackTrace();
        	}
        }
        return stockData;
    }
	
	//For local Yahoo API testing
	public static void main (String[] args) {
            YahooFinanceScraper c = new YahooFinanceScraper();
            List<String> canadianPublicBanks = Arrays.asList("RY.TO","TD","BNS.TO","BMO","CM.TO");
            if (canadianPublicBanks.size() > 0 ) {
            for (String symbol: canadianPublicBanks) {
                String crumb = c.getCrumb(symbol);
                if (crumb != null && !crumb.isEmpty()) {
                	logger.info(String.format("Downloading data to %s", symbol));
                	logger.info("Crumb: " + crumb);
                    long start = System.currentTimeMillis()/1000L - (2*24*60*60); //Gets data of past 2 days Unix time format
                    c.downloadData(symbol,start , System.currentTimeMillis()/1000L, crumb);
                } else {
                	logger.info(String.format("Error retreiving data for %s", symbol));
                }
            }
        } else {
        	logger.info("Problem with stock names for API hit");
        }
    }
}
