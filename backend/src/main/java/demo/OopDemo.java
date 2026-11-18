package demo;

import com.fairbid.exception.InvalidBidException;
import com.fairbid.interfaces.Biddable;
import com.fairbid.interfaces.Sellable;
import com.fairbid.model.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * ============================================================================
 * FairBid: Standalone In-Memory OOP Viva Demonstration
 * ============================================================================
 * This console application demonstrates all mandatory OOP principles
 * WITHOUT requiring a database connection or running web server:
 *
 * 1. Abstraction: Abstract User class and Sellable / Biddable interfaces.
 * 2. Encapsulation: Private model attributes with validated getters and setters.
 * 3. Inheritance: Farmer extends User, Buyer extends User (super constructor calls).
 * 4. Polymorphism: Method overriding (getDashboardPath, getProfileSummary) and
 *    dynamic method dispatch via parent User and interface references.
 * 5. Collections & Sorting: In-memory List<Bid> sorted by amount DESC, then time ASC.
 * 6. Exception Handling: Custom InvalidBidException caught and handled cleanly.
 * 7. Precise Financial Math: BigDecimal calculations for the Tomato produce worked example.
 * ============================================================================
 */
public class OopDemo {

    public static void main(String[] args) {
        System.out.println("====================================================================");
        System.out.println("  FAIRBID – FARMER PRODUCE AUCTION SYSTEM (OOP VIVA DEMO)  ");
        System.out.println("====================================================================\n");

        // ---------------------------------------------------------------------
        // 1. INHERITANCE & CONSTRUCTORS: User -> Farmer, User -> Buyer
        // ---------------------------------------------------------------------
        System.out.println("[OOP CONCEPT 1 & 2: INHERITANCE & ENCAPSULATION]");
        Farmer farmer = new Farmer(
                1,
                "Ramesh Kumar",
                "farmer1@fairbid.com",
                "$2a$12$olUQo7C7Bt9Y7yMQ7KWHeeGFhRmbDEMYSTB6d3tRUOG.LE3Nc8AFi",
                "9876543210",
                "Thanjavur, Tamil Nadu",
                new Timestamp(System.currentTimeMillis()),
                "Green Valley Organics",
                "Papanasam",
                "Thanjavur"
        );

        Buyer buyer1 = new Buyer(
                4,
                "Priya Agro Traders",
                "buyer1@fairbid.com",
                "$2a$12$iJg194/J3xOw2wYzWMJ45.1.QfNdPdH4nyHs1ncf./4TCPtEmSxU.",
                "9123456780",
                "Chennai, Tamil Nadu",
                new Timestamp(System.currentTimeMillis()),
                "Priya Agro Traders Pvt Ltd",
                "Wholesale Distributor"
        );

        Buyer buyer2 = new Buyer(
                5,
                "Apex Food Processors",
                "buyer2@fairbid.com",
                "$2a$12$iJg194/J3xOw2wYzWMJ45.1.QfNdPdH4nyHs1ncf./4TCPtEmSxU.",
                "9123456781",
                "Ahmedabad, Gujarat",
                new Timestamp(System.currentTimeMillis()),
                "Apex Food Processors India",
                "Food Processor"
        );

        System.out.println("Created Farmer: " + farmer.getFullName() + " from " + farmer.getDistrict());
        System.out.println("Created Buyer 1: " + buyer1.getFullName() + " (" + buyer1.getBuyerType() + ")");
        System.out.println("Created Buyer 2: " + buyer2.getFullName() + " (" + buyer2.getBuyerType() + ")\n");

        // ---------------------------------------------------------------------
        // 2. POLYMORPHISM: Dynamic Method Dispatch via Base User Reference
        // ---------------------------------------------------------------------
        System.out.println("[OOP CONCEPT 3: POLYMORPHISM (DYNAMIC METHOD DISPATCH)]");
        List<User> userList = new ArrayList<>();
        userList.add(farmer);
        userList.add(buyer1);

        for (User u : userList) {
            // JVM resolves getDashboardPath() and getProfileSummary() at runtime
            System.out.println("Polymorphic Call -> Role: " + u.getRole()
                    + " | Dashboard: " + u.getDashboardPath()
                    + " | Summary: " + u.getProfileSummary());
        }
        System.out.println();

        // ---------------------------------------------------------------------
        // 3. ABSTRACTION & INTERFACES: Sellable and Biddable
        // ---------------------------------------------------------------------
        System.out.println("[OOP CONCEPT 4: INTERFACES (Sellable & Biddable)]");
        Sellable seller = farmer;
        Biddable bidder = buyer1;
        System.out.println("Farmer referenced via Sellable interface: " + (seller instanceof Sellable));
        System.out.println("Buyer referenced via Biddable interface: " + (bidder instanceof Biddable));
        System.out.println();

        // ---------------------------------------------------------------------
        // 4. DOMAIN WORKED EXAMPLE: 100 kg Tomatoes, Min ₹25/kg, Agent ₹22/kg
        // ---------------------------------------------------------------------
        System.out.println("[OOP CONCEPT 5: DOMAIN MODEL & PRICE COMPARISON MATH]");
        ProduceLot tomatoLot = new ProduceLot(
                1,
                farmer.getId(),
                "Organic Hybrid Tomatoes",
                "Vegetables",
                new BigDecimal("100.00"),
                "kg",
                new BigDecimal("25.00"),
                new BigDecimal("22.00"),
                LotStatus.IN_AUCTION,
                new Timestamp(System.currentTimeMillis())
        );

        BigDecimal qty = tomatoLot.getQuantity();
        BigDecimal minPrice = tomatoLot.getMinPricePerUnit();
        BigDecimal agentPrice = tomatoLot.getAgentOfferPerUnit();

        BigDecimal expectedAuctionRevenue = qty.multiply(minPrice);
        BigDecimal agentRevenue = qty.multiply(agentPrice);
        BigDecimal grossDifference = expectedAuctionRevenue.subtract(agentRevenue);

        System.out.println("Produce: " + tomatoLot.getCropName() + " (" + qty + " " + tomatoLot.getUnit() + ")");
        System.out.println("Farmer Minimum Price: ₹" + minPrice + "/unit -> Expected Auction Min Revenue = ₹" + expectedAuctionRevenue);
        System.out.println("Local Agent Offer:   ₹" + agentPrice + "/unit -> Total Agent Value       = ₹" + agentRevenue);
        System.out.println("Gross Advantage of Platform: ₹" + grossDifference + " (" + (grossDifference.signum() >= 0 ? "Platform Higher" : "Agent Higher") + ")");
        System.out.println("Note: Final decision ALWAYS belongs to the farmer; auction revenue is not guaranteed.\n");

        // ---------------------------------------------------------------------
        // 5. COLLECTIONS & COMPARATORS: Auction Bidding & Tie-Breaking
        // ---------------------------------------------------------------------
        System.out.println("[OOP CONCEPT 6: COLLECTIONS & COMPARATOR TIE-BREAKING]");
        Auction auction = new Auction(
                1,
                tomatoLot.getId(),
                1,
                minPrice,
                new Timestamp(System.currentTimeMillis() - 3600000),
                new Timestamp(System.currentTimeMillis() + 7200000),
                AuctionStatus.OPEN,
                null
        );

        long now = System.currentTimeMillis();
        // Bid 1 by Buyer 1: ₹26.00 at T+0s
        Bid bid1 = new Bid(1, 1, buyer1.getId(), new BigDecimal("26.00"), new Timestamp(now - 30000), BidStatus.VALID);
        // Bid 2 by Buyer 2: ₹28.50 at T+10s
        Bid bid2 = new Bid(2, 1, buyer2.getId(), new BigDecimal("28.50"), new Timestamp(now - 20000), BidStatus.VALID);
        // Bid 3 by Buyer 1: ₹28.50 at T+15s (Same amount as Bid 2, but placed LATER)
        Bid bid3 = new Bid(3, 1, buyer1.getId(), new BigDecimal("28.50"), new Timestamp(now - 15000), BidStatus.VALID);

        auction.addBid(bid1);
        auction.addBid(bid2);
        auction.addBid(bid3);

        System.out.println("Bids in Auction:");
        for (Bid b : auction.getBids()) {
            System.out.println(" - Bid #" + b.getId() + " by Buyer #" + b.getBuyerId()
                    + " for ₹" + b.getAmountPerUnit() + " at " + b.getPlacedAt());
        }

        // Winner selection: Highest price wins; tie broken by EARLIEST bid
        Bid winningBid = auction.determineHighestValidBid();
        System.out.println("\n>>> Evaluated Winning Bid via Comparator:");
        System.out.println("Winner Bid ID: #" + winningBid.getId()
                + " | Buyer ID: #" + winningBid.getBuyerId()
                + " | Amount: ₹" + winningBid.getAmountPerUnit()
                + " | Placed At: " + winningBid.getPlacedAt());
        System.out.println("(Bid #2 won over Bid #3 because Bid #2 was placed earlier at equal amount of ₹28.50)\n");

        // ---------------------------------------------------------------------
        // 6. EXCEPTION HANDLING: Custom Domain Exceptions
        // ---------------------------------------------------------------------
        System.out.println("[OOP CONCEPT 7: CUSTOM EXCEPTION HANDLING]");
        try {
            BigDecimal lowBidAmount = new BigDecimal("20.00");
            System.out.println("Attempting to place bid of ₹" + lowBidAmount + " (below minimum price ₹" + minPrice + ")...");
            if (lowBidAmount.compareTo(minPrice) < 0) {
                throw new InvalidBidException("Bid amount ₹" + lowBidAmount + " is below minimum acceptable price ₹" + minPrice);
            }
        } catch (InvalidBidException ex) {
            System.out.println("Successfully caught expected exception: [" + ex.getClass().getSimpleName() + "] " + ex.getMessage());
        }

        System.out.println("\n====================================================================");
        System.out.println("  OOP VIVA DEMONSTRATION COMPLETE – ALL CONCEPTS VERIFIED  ");
        System.out.println("====================================================================");
    }
}
