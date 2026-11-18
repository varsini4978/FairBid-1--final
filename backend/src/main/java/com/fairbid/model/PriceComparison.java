package com.fairbid.model;

import java.math.BigDecimal;

/**
 * Domain model representing transparent financial comparison between
 * auction route and local agent offer route.
 * Employs BigDecimal for precise financial math.
 */
public class PriceComparison {
    private int lotId;
    private String cropName;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal minPricePerUnit;
    private BigDecimal agentOfferPerUnit;

    // Gross revenues
    private BigDecimal expectedAuctionRevenueMin; // quantity * minPrice
    private BigDecimal agentRevenue;              // quantity * agentPrice
    private BigDecimal grossDifference;           // auctionRevenue - agentRevenue

    // Optional route expenses
    private BigDecimal platformTransportCost;
    private BigDecimal platformOtherExpenses;
    private BigDecimal agentTransportCost;
    private BigDecimal agentOtherExpenses;

    // Net earnings
    private BigDecimal netAuctionEarnings;
    private BigDecimal netAgentEarnings;
    private BigDecimal netDifference;

    // Active bidding context
    private BigDecimal currentHighestBid;
    private BigDecimal revenueAtHighestBid;

    // Disclaimers & Advice
    private String revenueMinLabel;
    private String neutralNote;

    public PriceComparison() {
        this.platformTransportCost = BigDecimal.ZERO;
        this.platformOtherExpenses = BigDecimal.ZERO;
        this.agentTransportCost = BigDecimal.ZERO;
        this.agentOtherExpenses = BigDecimal.ZERO;
        this.revenueMinLabel = "Expected revenue at minimum price (not guaranteed – the final price depends on bidding)";
        this.neutralNote = "The final decision ALWAYS belongs to the farmer. FairBid provides market comparison to inform your choice, but never mandates an external sale.";
    }

    public int getLotId() {
        return lotId;
    }

    public void setLotId(int lotId) {
        this.lotId = lotId;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getMinPricePerUnit() {
        return minPricePerUnit;
    }

    public void setMinPricePerUnit(BigDecimal minPricePerUnit) {
        this.minPricePerUnit = minPricePerUnit;
    }

    public BigDecimal getAgentOfferPerUnit() {
        return agentOfferPerUnit;
    }

    public void setAgentOfferPerUnit(BigDecimal agentOfferPerUnit) {
        this.agentOfferPerUnit = agentOfferPerUnit;
    }

    public BigDecimal getExpectedAuctionRevenueMin() {
        return expectedAuctionRevenueMin;
    }

    public void setExpectedAuctionRevenueMin(BigDecimal expectedAuctionRevenueMin) {
        this.expectedAuctionRevenueMin = expectedAuctionRevenueMin;
    }

    public BigDecimal getAgentRevenue() {
        return agentRevenue;
    }

    public void setAgentRevenue(BigDecimal agentRevenue) {
        this.agentRevenue = agentRevenue;
    }

    public BigDecimal getGrossDifference() {
        return grossDifference;
    }

    public void setGrossDifference(BigDecimal grossDifference) {
        this.grossDifference = grossDifference;
    }

    public BigDecimal getPlatformTransportCost() {
        return platformTransportCost;
    }

    public void setPlatformTransportCost(BigDecimal platformTransportCost) {
        this.platformTransportCost = (platformTransportCost != null) ? platformTransportCost : BigDecimal.ZERO;
    }

    public BigDecimal getPlatformOtherExpenses() {
        return platformOtherExpenses;
    }

    public void setPlatformOtherExpenses(BigDecimal platformOtherExpenses) {
        this.platformOtherExpenses = (platformOtherExpenses != null) ? platformOtherExpenses : BigDecimal.ZERO;
    }

    public BigDecimal getAgentTransportCost() {
        return agentTransportCost;
    }

    public void setAgentTransportCost(BigDecimal agentTransportCost) {
        this.agentTransportCost = (agentTransportCost != null) ? agentTransportCost : BigDecimal.ZERO;
    }

    public BigDecimal getAgentOtherExpenses() {
        return agentOtherExpenses;
    }

    public void setAgentOtherExpenses(BigDecimal agentOtherExpenses) {
        this.agentOtherExpenses = (agentOtherExpenses != null) ? agentOtherExpenses : BigDecimal.ZERO;
    }

    public BigDecimal getNetAuctionEarnings() {
        return netAuctionEarnings;
    }

    public void setNetAuctionEarnings(BigDecimal netAuctionEarnings) {
        this.netAuctionEarnings = netAuctionEarnings;
    }

    public BigDecimal getNetAgentEarnings() {
        return netAgentEarnings;
    }

    public void setNetAgentEarnings(BigDecimal netAgentEarnings) {
        this.netAgentEarnings = netAgentEarnings;
    }

    public BigDecimal getNetDifference() {
        return netDifference;
    }

    public void setNetDifference(BigDecimal netDifference) {
        this.netDifference = netDifference;
    }

    public BigDecimal getCurrentHighestBid() {
        return currentHighestBid;
    }

    public void setCurrentHighestBid(BigDecimal currentHighestBid) {
        this.currentHighestBid = currentHighestBid;
    }

    public BigDecimal getRevenueAtHighestBid() {
        return revenueAtHighestBid;
    }

    public void setRevenueAtHighestBid(BigDecimal revenueAtHighestBid) {
        this.revenueAtHighestBid = revenueAtHighestBid;
    }

    public String getRevenueMinLabel() {
        return revenueMinLabel;
    }

    public void setRevenueMinLabel(String revenueMinLabel) {
        this.revenueMinLabel = revenueMinLabel;
    }

    public String getNeutralNote() {
        return neutralNote;
    }

    public void setNeutralNote(String neutralNote) {
        this.neutralNote = neutralNote;
    }
}
