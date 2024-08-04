package api.dtos;

import java.math.BigDecimal;

public class CurrencyConversionDto {

	private CurrencyExchangeDto exchange;
	private BigDecimal quantity;
	private ConversionResult conversionResult;

	public CurrencyConversionDto() {

	}

	public CurrencyConversionDto(CurrencyExchangeDto exchange, BigDecimal quantity, String currencyTo,
			BigDecimal result) {
		this.exchange = exchange;
		this.quantity = quantity;
		CurrencyConversionDto.ConversionResult resultHolder = this.new ConversionResult(currencyTo, result);
		this.conversionResult = resultHolder;
	}

	public CurrencyExchangeDto getExchange() {
		return exchange;
	}

	public void setExchange(CurrencyExchangeDto exchange) {
		this.exchange = exchange;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public ConversionResult getConversionResult() {
		return conversionResult;
	}

	public void setConversionResult(ConversionResult conversionResult) {
		this.conversionResult = conversionResult;
	}

	private class ConversionResult {
		private String currencyTo;
		private BigDecimal result;

		public ConversionResult() {

		}

		public ConversionResult(String currencyTo, BigDecimal result) {
			this.currencyTo = currencyTo;
			this.result = result;
		}

		public String getCurrencyTo() {
			return currencyTo;
		}

		public void setCurrencyTo(String currencyTo) {
			this.currencyTo = currencyTo;
		}

		public BigDecimal getResult() {
			return result;
		}

		public void setResult(BigDecimal result) {
			this.result = result;
		}

	}
}
