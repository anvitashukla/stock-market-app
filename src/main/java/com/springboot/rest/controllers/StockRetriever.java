package com.springboot.rest.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.springboot.rest.beans.Stock;
import com.springboot.rest.scraper.YahooFinanceScraper;

@Controller
public class StockRetriever {
	
	@RequestMapping(method = RequestMethod.GET, value="/stockValues")
	  @ResponseBody
	  public List<Stock> getEntirePeriod(@RequestParam String period, @RequestParam String stockName) {
	  return getRecords(period,stockName);
	  //return "indexing here";
	  }
	
	private Long getStartPeriodForDataScrape(String period){
		long startValue = 0;
		//Unix time formats for Yahoo Finance
		switch(period)
		{
		case "entirePeriod" : { startValue = System.currentTimeMillis()/1000L - (10*365*24*60*60 + 2*24*60*60); break;}
		case "fiveYearPeriod" : {startValue = System.currentTimeMillis()/1000L - (5*365*24*60*60 + 1*24*60*60); break;}
		case "oneYearPeriod" : {startValue = System.currentTimeMillis()/1000L - 365*24*60*60; break;}
		case "oneMonthPeriod" : {startValue = System.currentTimeMillis()/1000L - 30*24*60*60; break;}
		case "oneDayPeriod" : {startValue = System.currentTimeMillis()/1000L - 24*60*60; break;}
		}
		
		return startValue;
	}
	private List<Stock> getRecords(String period, String stockName){
		long startPeriod = getStartPeriodForDataScrape(period);
		YahooFinanceScraper scraper = new YahooFinanceScraper();
		List<Stock> stockData = new ArrayList<Stock>();
        //List<String> canadianPublicBanks = Arrays.asList("TD","BNS.TO","BMO","CM.TO");
        if (stockName != null) {
            String crumb = scraper.getCrumb(stockName);
            if (crumb != null && !crumb.isEmpty()) {
                System.out.println(String.format("Downloading data to %s", stockName));
                System.out.println("Crumb: " + crumb);
                stockData = scraper.downloadData(stockName,startPeriod , System.currentTimeMillis()/1000L, crumb);
            } else {
                System.out.println(String.format("Error retreiving data for %s", stockName));
            }
    } else {
        System.out.println("Usage: java -classpath $CLASSPATH GetYahooQuotes SYMBOL");
    }
        return stockData;
	}	
}
