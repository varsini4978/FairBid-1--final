package com.fairbid.interfaces;

import com.fairbid.model.Bid;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interface 2: Biddable
 * Defines buyer bidding operations on open auctions.
 * Implemented by Buyer (domain validator & bidder) and AuctionManager (auction engine).
 */
public interface Biddable {

    /**
     * Submits a competitive bid on an open auction round.
     *
     * @param auctionId Target auction ID
     * @param buyerId ID of the authenticated buyer
     * @param amount Bid price per unit (must be strictly greater than current highest bid)
     * @return Persisted Bid record
     */
    Bid placeBid(int auctionId, int buyerId, BigDecimal amount);

    /**
     * Retrieves the bidding history for a buyer across all auctions.
     *
     * @param buyerId ID of the buyer
     * @return List of bids placed by this buyer
     */
    List<Bid> getBidHistory(int buyerId);
}
