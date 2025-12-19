package com.Investube.mvc.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class KrxIndexResponse {
    @JsonProperty("OutBlock_1")
    private List<KrxIndexItem> OutBlock_1;

    public List<KrxIndexItem> getOutBlock_1() {
        return OutBlock_1;
    }

    public void setOutBlock_1(List<KrxIndexItem> outBlock_1) {
        OutBlock_1 = outBlock_1;
    }

    public static class KrxIndexItem {
        @JsonProperty("BAS_DD")
        private String BAS_DD;          // 기준일자
        
        @JsonProperty("IDX_CLSS")
        private String IDX_CLSS;        // 계열구분
        
        @JsonProperty("IDX_NM")
        private String IDX_NM;          // 지수명
        
        @JsonProperty("CLSPRC_IDX")
        private String CLSPRC_IDX;      // 종가
        
        @JsonProperty("CMPPREVDD_IDX")
        private String CMPPREVDD_IDX;   // 대비
        
        @JsonProperty("FLUC_RT")
        private String FLUC_RT;         // 등락률
        
        @JsonProperty("OPNPRC_IDX")
        private String OPNPRC_IDX;      // 시가
        
        @JsonProperty("HGPRC_IDX")
        private String HGPRC_IDX;       // 고가
        
        @JsonProperty("LWPRC_IDX")
        private String LWPRC_IDX;       // 저가
        
        @JsonProperty("ACC_TRDVOL")
        private String ACC_TRDVOL;      // 거래량
        
        @JsonProperty("ACC_TRDVAL")
        private String ACC_TRDVAL;      // 거래대금
        
        @JsonProperty("MKTCAP")
        private String MKTCAP;          // 상장시가총액

        // Getters and Setters
        public String getBAS_DD() {
            return BAS_DD;
        }

        public void setBAS_DD(String BAS_DD) {
            this.BAS_DD = BAS_DD;
        }

        public String getIDX_CLSS() {
            return IDX_CLSS;
        }

        public void setIDX_CLSS(String IDX_CLSS) {
            this.IDX_CLSS = IDX_CLSS;
        }

        public String getIDX_NM() {
            return IDX_NM;
        }

        public void setIDX_NM(String IDX_NM) {
            this.IDX_NM = IDX_NM;
        }

        public String getCLSPRC_IDX() {
            return CLSPRC_IDX;
        }

        public void setCLSPRC_IDX(String CLSPRC_IDX) {
            this.CLSPRC_IDX = CLSPRC_IDX;
        }

        public String getCMPPREVDD_IDX() {
            return CMPPREVDD_IDX;
        }

        public void setCMPPREVDD_IDX(String CMPPREVDD_IDX) {
            this.CMPPREVDD_IDX = CMPPREVDD_IDX;
        }

        public String getFLUC_RT() {
            return FLUC_RT;
        }

        public void setFLUC_RT(String FLUC_RT) {
            this.FLUC_RT = FLUC_RT;
        }

        public String getOPNPRC_IDX() {
            return OPNPRC_IDX;
        }

        public void setOPNPRC_IDX(String OPNPRC_IDX) {
            this.OPNPRC_IDX = OPNPRC_IDX;
        }

        public String getHGPRC_IDX() {
            return HGPRC_IDX;
        }

        public void setHGPRC_IDX(String HGPRC_IDX) {
            this.HGPRC_IDX = HGPRC_IDX;
        }

        public String getLWPRC_IDX() {
            return LWPRC_IDX;
        }

        public void setLWPRC_IDX(String LWPRC_IDX) {
            this.LWPRC_IDX = LWPRC_IDX;
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
    }
}
