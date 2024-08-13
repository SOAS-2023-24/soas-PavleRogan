package soas.trade_service.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "trade_service")
public class TradeServiceModel {

	@Id
	private long id;
	
	@Column(name = "currency_from")
	private String from;
	
	@Column(name = "to_currency")
	private String to;
	
	@Column(name = "rate")
	private BigDecimal conversionRate;
	
	public TradeServiceModel() {
		
	}

	public TradeServiceModel(long id, String from, String to, BigDecimal conversionRate) {
		super();
		this.id = id;
		this.from = from;
		this.to = to;
		this.conversionRate = conversionRate;
	}



	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public BigDecimal getConversionRate() {
		return conversionRate;
	}

	public void setConversion(BigDecimal conversionRate) {
		this.conversionRate = conversionRate;
	}
	
	
	
}
