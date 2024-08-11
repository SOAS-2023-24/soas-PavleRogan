package api.dtos;

import java.math.BigDecimal;

public class CryptoExchangeDto {

	private String to;
	private String from;
    private BigDecimal exchangeValue;

    public CryptoExchangeDto() {
    }

    public CryptoExchangeDto(String from, String to, BigDecimal exchangeValue) {
        this.from = from;
        this.to = to;
        this.exchangeValue = exchangeValue;
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

    public BigDecimal getExchangeValue() {
        return exchangeValue;
    }

    public void setExchangeValue(BigDecimal exchangeValue) {
        this.exchangeValue = exchangeValue;
    }
    
}
