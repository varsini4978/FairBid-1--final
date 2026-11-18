package com.fairbid.interfaces;

import com.fairbid.model.AgentDecision;
import com.fairbid.model.Auction;
import com.fairbid.model.ProduceLot;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Interface 1: Sellable
 * Defines producer operations for listing produce, revising prices, reopening rounds,
 * and deciding on local agent offers.
 * Implemented by Farmer (domain object builder & validator) and AuctionManager (auction engine).
 */
public interface Sellable {

    /**
     * Lists a new agricultural produce lot on the platform.
     *
     * @param lot ProduceLot domain object containing crop, quantity, and pricing details
     * @return Persisted ProduceLot with generated ID
     */
    ProduceLot listProduce(ProduceLot lot);

    /**
     * Revises the minimum acceptable price for an unsold produce lot.
     *
     * @param lotId ID of the produce lot
     * @param farmerId ID of the owning farmer (for authorization)
     * @param newMinPrice Revised minimum price per unit (must be > 0)
     */
    void reviseMinimumPrice(int lotId, int farmerId, BigDecimal newMinPrice);

    /**
     * Reopens an unsold produce lot into a new auction round (round_number + 1).
     *
     * @param lotId ID of the produce lot
     * @param farmerId ID of the owning farmer
     * @param newEndTime Closing timestamp for the new auction round
     * @return Newly created open Auction round
     */
    Auction reopenAuction(int lotId, int farmerId, LocalDateTime newEndTime);

    /**
     * Records the farmer's final decision regarding a local agent's offline offer.
     *
     * @param lotId ID of the produce lot
     * @param farmerId ID of the owning farmer
     * @param decision ACCEPTED or REJECTED
     * @param transportCost Optional transportation cost for agent route
     * @param otherExpenses Optional other expenses for agent route
     */
    void decideOnAgentOffer(int lotId, int farmerId, AgentDecision decision, BigDecimal transportCost, BigDecimal otherExpenses);
}
