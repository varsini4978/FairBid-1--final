package com.fairbid.service;

import com.fairbid.dao.AgentOfferDAO;
import com.fairbid.dao.AuctionDAO;
import com.fairbid.dao.BidDAO;
import com.fairbid.dao.ProduceLotDAO;
import com.fairbid.exception.ResourceNotFoundException;
import com.fairbid.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service calculating transparent financial comparisons between
 * the competitive auction platform route and the offline local agent offer route.
 * Employs BigDecimal throughout for exact monetary calculations.
 */
public class ComparisonService {
    private final ProduceLotDAO produceLotDAO;
    private final AuctionDAO auctionDAO;
    private final BidDAO bidDAO;
    private final AgentOfferDAO agentOfferDAO;

    public ComparisonService() {
        this.produceLotDAO = new ProduceLotDAO();
        this.auctionDAO = new AuctionDAO();
        this.bidDAO = new BidDAO();
        this.agentOfferDAO = new AgentOfferDAO();
    }

    public ComparisonService(ProduceLotDAO produceLotDAO, AuctionDAO auctionDAO, BidDAO bidDAO, AgentOfferDAO agentOfferDAO) {
        this.produceLotDAO = produceLotDAO;
        this.auctionDAO = auctionDAO;
        this.bidDAO = bidDAO;
        this.agentOfferDAO = agentOfferDAO;
    }

    /**
     * Calculates comparative revenue and net earnings for a produce lot.
     *
     * Worked Example:
     * Tomatoes: 100.00 kg
     * Minimum Price: ₹25.00/kg -> Expected Auction Min Revenue = 100 * 25 = ₹2,500.00
     * Local Agent: ₹22.00/kg   -> Agent Offer Value = 100 * 22 = ₹2,200.00
     * Gross Difference: ₹2,500 - ₹2,200 = ₹300.00 advantage on FairBid.
     *
     * @param lotId Target produce lot ID
     * @param platformTransport Transportation expenses if sold on platform
     * @param platformOther Other expenses for platform sale
     * @param agentTransport Transportation expenses for agent sale
     * @param agentOther Other expenses for agent sale
     * @return Fully populated PriceComparison domain object
     */
    public PriceComparison compare(int lotId, BigDecimal platformTransport, BigDecimal platformOther,
                                  BigDecimal agentTransport, BigDecimal agentOther) {
        ProduceLot lot = produceLotDAO.findById(lotId);
        if (lot == null) {
            throw new ResourceNotFoundException("Produce lot not found with ID: " + lotId);
        }

        BigDecimal pTransport = platformTransport != null ? platformTransport : BigDecimal.ZERO;
        BigDecimal pOther = platformOther != null ? platformOther : BigDecimal.ZERO;
        BigDecimal aTransport = agentTransport != null ? agentTransport : BigDecimal.ZERO;
        BigDecimal aOther = agentOther != null ? agentOther : BigDecimal.ZERO;

        BigDecimal quantity = lot.getQuantity();
        BigDecimal minPrice = lot.getMinPricePerUnit();
        BigDecimal agentPrice = lot.getAgentOfferPerUnit() != null ? lot.getAgentOfferPerUnit() : BigDecimal.ZERO;

        // Gross revenues
        BigDecimal expectedAuctionRevenueMin = quantity.multiply(minPrice).setScale(2, RoundingMode.HALF_UP);
        BigDecimal agentRevenue = quantity.multiply(agentPrice).setScale(2, RoundingMode.HALF_UP);
        BigDecimal grossDiff = expectedAuctionRevenueMin.subtract(agentRevenue).setScale(2, RoundingMode.HALF_UP);

        // Net earnings = Revenue - (Transport + Other Expenses)
        BigDecimal totalPlatformExpenses = pTransport.add(pOther);
        BigDecimal totalAgentExpenses = aTransport.add(aOther);

        BigDecimal netAuction = expectedAuctionRevenueMin.subtract(totalPlatformExpenses).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netAgent = agentRevenue.subtract(totalAgentExpenses).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netDiff = netAuction.subtract(netAgent).setScale(2, RoundingMode.HALF_UP);

        PriceComparison pc = new PriceComparison();
        pc.setLotId(lot.getId());
        pc.setCropName(lot.getCropName());
        pc.setQuantity(quantity);
        pc.setUnit(lot.getUnit());
        pc.setMinPricePerUnit(minPrice);
        pc.setAgentOfferPerUnit(agentPrice);

        pc.setExpectedAuctionRevenueMin(expectedAuctionRevenueMin);
        pc.setAgentRevenue(agentRevenue);
        pc.setGrossDifference(grossDiff);

        pc.setPlatformTransportCost(pTransport);
        pc.setPlatformOtherExpenses(pOther);
        pc.setAgentTransportCost(aTransport);
        pc.setAgentOtherExpenses(aOther);

        pc.setNetAuctionEarnings(netAuction);
        pc.setNetAgentEarnings(netAgent);
        pc.setNetDifference(netDiff);

        // Check if there is an active auction with an even higher current bid
        Auction activeAuction = auctionDAO.findActiveByLotId(lotId);
        if (activeAuction != null) {
            java.util.List<Bid> bids = bidDAO.findBidsByAuctionId(activeAuction.getId());
            activeAuction.setBids(bids);
            Bid highest = activeAuction.determineHighestValidBid();
            if (highest != null && highest.getAmountPerUnit().compareTo(minPrice) > 0) {
                pc.setCurrentHighestBid(highest.getAmountPerUnit());
                pc.setRevenueAtHighestBid(quantity.multiply(highest.getAmountPerUnit()).setScale(2, RoundingMode.HALF_UP));
            }
        }

        return pc;
    }
}
