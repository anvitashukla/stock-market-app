package com.springboot.rest.beans;


import java.time.LocalDate;

public class Stock {
	private LocalDate date;
	private Float open;
	private Float high;
	private Float low;
	private Float close;
	private Float adjClose;
	private Integer volume;
	
	//For casting values from Yahoo Finance to POJO object
	public Stock(LocalDate date, Float open, Float high, Float low, Float close, Float adjClose, Integer volume) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.adjClose = adjClose;
        this.volume = volume;
    }

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public Float getOpen() {
		return open;
	}

	public void setOpen(Float open) {
		this.open = open;
	}

	public Float getHigh() {
		return high;
	}

	public void setHigh(Float high) {
		this.high = high;
	}

	public Float getLow() {
		return low;
	}

	public void setLow(Float low) {
		this.low = low;
	}

	public Float getClose() {
		return close;
	}

	public void setClose(Float close) {
		this.close = close;
	}

	public Float getAdjClose() {
		return adjClose;
	}

	public void setAdjClose(Float adjClose) {
		this.adjClose = adjClose;
	}

	public Integer getVolume() {
		return volume;
	}

	public void setVolume(Integer volume) {
		this.volume = volume;
	}
}