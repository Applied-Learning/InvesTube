package com.Investube.mvc.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class KrxStockResponse {
    @JsonProperty("OutBlock_1")
    private List<KrxStockItem> OutBlock_1;

    public List<KrxStockItem> getOutBlock_1() {
        return OutBlock_1;
    }

    public void setOutBlock_1(List<KrxStockItem> outBlock_1) {
        OutBlock_1 = outBlock_1;
    }

    public static class KrxStockItem {
        @JsonProperty("BAS_DD")
        private String BAS_DD;          // 기준일자
        
        @JsonProperty("ISU_CD")
        private String ISU_CD;          // 종목코드
        
        @JsonProperty("ISU_NM")
        private String ISU_NM;          // 종목명
        
        @JsonProperty("MKT_NM")
        private String MKT_NM;          // 시장구분
        
        @JsonProperty("SECT_TP_NM")
        private String SECT_TP_NM;      // 소속부
        
        @JsonProperty("TDD_CLSPRC")
        private String TDD_CLSPRC;      // 종가
        
        @JsonProperty("CMPPREVDD_PRC")
        private String CMPPREVDD_PRC;   // 대비
        
        @JsonProperty("FLUC_RT")
        private String FLUC_RT;         // 등락률
        
        @JsonProperty("TDD_OPNPRC")
        private String TDD_OPNPRC;      // 시가
        
        @JsonProperty("TDD_HGPRC")
        private String TDD_HGPRC;       // 고가
        
        @JsonProperty("TDD_LWPRC")
        private String TDD_LWPRC;       // 저가
        
        @JsonProperty("ACC_TRDVOL")
        private String ACC_TRDVOL;      // 거래량
        
        @JsonProperty("ACC_TRDVAL")
        private String ACC_TRDVAL;      // 거래대금
        
        @JsonProperty("MKTCAP")
        private String MKTCAP;          // 시가총액
        
        @JsonProperty("LIST_SHRS")
        private String LIST_SHRS;       // 상장주식수

        // Getters and Setters
        public String getBAS_DD() {
            return BAS_DD;
        }

        public void setBAS_DD(String BAS_DD) {
            this.BAS_DD = BAS_DD;
        }

        public String getISU_CD() {
            return ISU_CD;
        }

        public void setISU_CD(String ISU_CD) {
            this.ISU_CD = ISU_CD;
        }

        public String getISU_NM() {
            return ISU_NM;
        }

        public void setISU_NM(String ISU_NM) {
            this.ISU_NM = ISU_NM;
        }

        public String getMKT_NM() {
            return MKT_NM;
        }

        public void setMKT_NM(String MKT_NM) {
            this.MKT_NM = MKT_NM;
        }

        public String getSECT_TP_NM() {
            return SECT_TP_NM;
        }

        public void setSECT_TP_NM(String SECT_TP_NM) {
            this.SECT_TP_NM = SECT_TP_NM;
        }

        public String getTDD_CLSPRC() {
            return TDD_CLSPRC;
        }

        public void setTDD_CLSPRC(String TDD_CLSPRC) {
            this.TDD_CLSPRC = TDD_CLSPRC;
        }

        public String getCMPPREVDD_PRC() {
            return CMPPREVDD_PRC;
        }

        public void setCMPPREVDD_PRC(String CMPPREVDD_PRC) {
            this.CMPPREVDD_PRC = CMPPREVDD_PRC;
        }

        public String getFLUC_RT() {
            return FLUC_RT;
        }

        public void setFLUC_RT(String FLUC_RT) {
            this.FLUC_RT = FLUC_RT;
        }

        public String getTDD_OPNPRC() {
            return TDD_OPNPRC;
        }

        public void setTDD_OPNPRC(String TDD_OPNPRC) {
            this.TDD_OPNPRC = TDD_OPNPRC;
        }

        public String getTDD_HGPRC() {
            return TDD_HGPRC;
        }

        public void setTDD_HGPRC(String TDD_HGPRC) {
            this.TDD_HGPRC = TDD_HGPRC;
        }

        public String getTDD_LWPRC() {
            return TDD_LWPRC;
        }

        public void setTDD_LWPRC(String TDD_LWPRC) {
            this.TDD_LWPRC = TDD_LWPRC;
        }

        public String getACC_TRDVOL() {
            return ACC_TRDVOL;
        }

        public void setACC_TRDVOL(String ACC_TRDVOL) {
            this.ACC_TRDVOL = ACC_TRDVOL;
        }

        public String getACC_TRDVAL() {
            return ACC_TRDVAL;
        }

        public void setACC_TRDVAL(String ACC_TRDVAL) {
            this.ACC_TRDVAL = ACC_TRDVAL;
        }

        public String getMKTCAP() {
            return MKTCAP;
        }

        public void setMKTCAP(String MKTCAP) {
            this.MKTCAP = MKTCAP;
        }

        public String getLIST_SHRS() {
            return LIST_SHRS;
        }

        public void setLIST_SHRS(String LIST_SHRS) {
            this.LIST_SHRS = LIST_SHRS;
        }
    }
}
