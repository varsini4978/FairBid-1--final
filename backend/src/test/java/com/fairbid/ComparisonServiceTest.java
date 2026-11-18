package com.fairbid;

import com.fairbid.dao.AgentOfferDAO;
import com.fairbid.dao.AuctionDAO;
import com.fairbid.dao.BidDAO;
import com.fairbid.dao.ProduceLotDAO;
import com.fairbid.model.LotStatus;
import com.fairbid.model.PriceComparison;
import com.fairbid.model.ProduceLot;
import com.fairbid.service.ComparisonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying financial math in ComparisonService,
 * specifically the required Tomatoes worked example.
 */
public class ComparisonServiceTest {

    private ComparisonService comparisonService;

    @BeforeEach
    void setUp() {
        // Stub DAO returning the Tomatoes worked example
        ProduceLotDAO mockLotDAO = new ProduceLotDAO() {
            @Override
            public ProduceLot findById(int id) {
                return new ProduceLot(
                        1, 1, "Organic Hybrid Tomatoes", "Vegetables",
                        new BigDecimal("100.00"), "kg",
                        new BigDecimal("25.00"), new BigDecimal("22.00"),
                        LotStatus.IN_AUCTION, new Timestamp(System.currentTimeMillis())
                );
            }
        };

        AuctionDAO mockAuctionDAO = new AuctionDAO() {
            @Override
            public com.fairbid.model.Auction findActiveByLotId(int lotId) {
                return null;
            }
        };

        BidDAO mockBidDAO = new BidDAO();
        AgentOfferDAO mockOfferDAO = new AgentOfferDAO();

        comparisonService = new ComparisonService(mockLotDAO, mockAuctionDAO, mockBidDAO, mockOfferDAO);
    }

    @Test
    @DisplayName("Tomatoes worked example: 100 kg * ₹25 = ₹2,500; Agent 100 kg * ₹22 = ₹2,200; Difference ₹300")
    void testTomatoesWorkedExampleGrossMath() {
        PriceComparison pc = comparisonService.compare(1, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        assertNotNull(pc);
        assertEquals(new BigDecimal("100.00"), pc.getQuantity());
        assertEquals(new BigDecimal("25.00"), pc.getMinPricePerUnit());
        assertEquals(new BigDecimal("22.00"), pc.getAgentOfferPerUnit());

        // Expected Auction Min Revenue = 100 * 25 = 2,500.00
        assertEquals(new BigDecimal("2500.00"), pc.getExpectedAuctionRevenueMin());

        // Agent Revenue = 100 * 22 = 2,200.00
        assertEquals(new BigDecimal("2200.00"), pc.getAgentRevenue());

        // Gross Difference = 2,500 - 2,200 = 300.00
        assertEquals(new BigDecimal("300.00"), pc.getGrossDifference());

        // Neutral note must state final decision belongs to the farmer
        assertTrue(pc.getNeutralNote().contains("farmer"));
        assertTrue(pc.getRevenueMinLabel().contains("not guaranteed"));
    }

    @Test
    @DisplayName("Net earnings calculation with route-specific transport and other expenses")
    void testRouteExpensesMath() {
        BigDecimal platformTransport = new BigDecimal("150.00");
        BigDecimal platformOther = new BigDecimal("50.00");
        BigDecimal agentTransport = new BigDecimal("80.00");
        BigDecimal agentOther = new BigDecimal("20.00");

        PriceComparison pc = comparisonService.compare(1, platformTransport, platformOther, agentTransport, agentOther);

        // Net Auction = 2500 - (150 + 50) = 2300.00
        assertEquals(new BigDecimal("2300.00"), pc.getNetAuctionEarnings());

        // Net Agent = 2200 - (80 + 20) = 2100.00
        assertEquals(new BigDecimal("2100.00"), pc.getNetAgentEarnings());

        // Net Difference = 2300 - 2100 = 200.00 advantage on platform
        assertEquals(new BigDecimal("200.00"), pc.getNetDifference());
    }
}
