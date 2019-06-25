package com.springboot.rest.controllers;

import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;
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
	private static final Logger logger = Logger.getLogger(StockRetriever.class);
	
	//This function returns a list of Stock objects to display on index.html
	@RequestMapping(method = RequestMethod.GET, value="/stockValues")
	  @ResponseBody
	  public List<Stock> getEntirePeriod(@RequestParam String period, @RequestParam String stockName) {
	  return getRecords(period,stockName);
	  }
	
	//This function retrieves the start period from where data has to be retrieved from Yahoo Finance API
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
        if (stockName != null) {
            String crumb = scraper.getCrumb(stockName); //Crumb is a unique identifier used when get data from Yahoo's API
            if (crumb != null && !crumb.isEmpty()) {
            	logger.info(String.format("Downloading data to %s", stockName));
            	logger.info("Crumb: " + crumb);
                stockData = scraper.downloadData(stockName,startPeriod , System.currentTimeMillis()/1000L, crumb);
            } else {
            	logger.info(String.format("Error retreiving data for %s", stockName));
            }
    } else {
    	logger.error("stockName not received well from index.html");
    }
        return stockData;
	}	
}
