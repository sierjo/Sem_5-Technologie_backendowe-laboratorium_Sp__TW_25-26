package com.project.jobportal.dto;

import java.util.List;

//класс для десериализации json ответа с Api
public class NbpExchangeRateDTO {
    private String table;
    private String currency;
    private String code;
    private List<RateDetails> rates;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<RateDetails> getRates() {
        return rates;
    }

    public void setRates(List<RateDetails> rates) {
        this.rates = rates;
    }

    @Override
    public String toString() {
        return "NbpExchangeRateDTO{" +
                "table='" + table + '\'' +
                ", currency='" + currency + '\'' +
                ", code='" + code + '\'' +
                ", rates=" + rates +
                '}';
    }

    public static class RateDetails{
        private String no;
        private String effectiveDate;
        private double mid; //средний курс

        public String getNo() {
            return no;
        }

        public void setNo(String no) {
            this.no = no;
        }

        public String getEffectiveDate() {
            return effectiveDate;
        }

        public void setEffectiveDate(String effectiveDate) {
            this.effectiveDate = effectiveDate;
        }

        public double getMid() {
            return mid;
        }

        public void setMid(double mid) {
            this.mid = mid;
        }

        @Override
        public String toString() {
            return "RateDetails{" +
                    "no='" + no + '\'' +
                    ", effectiveDate='" + effectiveDate + '\'' +
                    ", mid=" + mid +
                    '}';
        }
    }
}
