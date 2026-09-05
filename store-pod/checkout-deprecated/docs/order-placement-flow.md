### Order Placement and Finalization Flow

The following diagram illustrates the current implementation of the order placement lifecycle, covering inventory reservation, payment initiation, and asynchronous status updates.

```mermaid
graph TD
    %% Initial Phase
    Start([Checkout Request]) --> Auth{Auth Check}
    Auth -- Fail --> Error401[HTTP 401 Unauthorized]
    Auth -- Success --> CreateOrder[orderFacade.saveOrder<br/>Status: CREATED]
    
    CreateOrder --> ReserveInv{Reserve Inventory}
    
    %% Reservation Outcomes
    ReserveInv -- Failed --> ResFailed[Status: CANCELLED<br/>Payment: FAILED<br/>Inventory: RESERVATION_FAILED]
    ReserveInv -- Success --> ResSuccess[Status: PENDING_PAYMENT<br/>Payment: PENDING<br/>Inventory: RESERVED]
    
    ResSuccess --> InitPayment[Initiate Payment Gateway]
    
    %% Immediate Payment Response
    InitPayment --> PayResponse{Payment Response}
    
    PayResponse -- PAID --> CommitInv[Commit Reservation]
    CommitInv -- Success --> Confirmed[Status: CONFIRMED<br/>Payment: PAID<br/>Inventory: COMMITTED]
    CommitInv -- Failed --> RetryCommit[Status: PENDING_PAYMENT<br/>Payment: PAID<br/>Inventory: RESERVED<br/>Manual Intervention Required]
    
    PayResponse -- PAY_LATER --> COD[Status: PENDING<br/>Payment: PENDING<br/>Inventory: RESERVED]
    
    PayResponse -- PENDING --> AwaitPay[Status: PENDING_PAYMENT<br/>Payment: PENDING<br/>Inventory: RESERVED]
    
    PayResponse -- FAILED --> ReleaseInv[Release Reservation]
    ReleaseInv --> CancelledPay[Status: CANCELLED<br/>Payment: FAILED<br/>Inventory: RELEASED]

    %% Asynchronous Updates (Webhooks / Admin)
    AwaitPay -.-> UpdateStatus[Webhook / Admin Action]
    COD -.-> UpdateStatus
    
    UpdateStatus --> AsyncStatus{Async Status}
    
    AsyncStatus -- PAID --> CommitInv
    AsyncStatus -- FAILED / EXPIRED / CANCELLED --> ReleaseInv
    AsyncStatus -- REJECTED --> ReleaseInv
    AsyncStatus -- WAITING_VERIFICATION --> WaitVerif[Payment: WAITING_VERIFICATION]
    
    %% Fulfillment Phase
    Confirmed --> Processing[Status: PROCESSING]
    Processing --> Shipped[Status: SHIPPED]
    Shipped --> Delivering[Status: DELIVERING]
    Delivering --> Delivered[Status: DELIVERED]
    Delivered --> Completed([Status: COMPLETED])

    %% Transitions
    WaitVerif -.-> UpdateStatus
    
    %% Terminal States
    ResFailed --> End([End])
    CancelledPay --> End
    Error401 --> End
```

### Status Definitions and Mappings

#### Order Status (`OrderStatus`)
| Status | Description |
| :--- | :--- |
| `CREATED` | Initial state when the order record is first saved. |
| `PENDING_PAYMENT` | Inventory is reserved, awaiting successful payment confirmation. |
| `PENDING` | Used for Pay Later (COD) or Manual Transfer before confirmation. |
| `CONFIRMED` | Payment is successful and inventory reservation is committed. |
| `PROCESSING` | Order is being prepared for shipment. |
| `SHIPPED` / `DELIVERING` | Order is in transit. |
| `DELIVERED` / `COMPLETED` | Order has reached the customer. |
| `CANCELLED` | Terminal state for failed reservations, failed payments, or expired sessions. |

#### Payment Status (`PaymentStatus`)
- **Internal (Checkout):** `PENDING`, `PAID`, `FAILED`, `EXPIRED`, `CANCELLED`, `WAITING_VERIFICATION`, `REJECTED`.
- **Gateway (Payment):** Adds `PAY_LATER` (mapped to `PENDING` internally) and `PROCESSING`.

#### Inventory Status (`InventoryStatus`)
| Status | Description |
| :--- | :--- |
| `RESERVED` | Items are temporarily held in the catalog. |
| `COMMITTED` | Reservation finalized after successful payment. |
| `RELEASED` | Reservation cancelled and items returned to stock. |
| `RESERVATION_FAILED` | Initial attempt to hold items failed. |

### Key Logic Observations
1. **Atomic Reservation:** Order placement always attempts to reserve inventory before initiating payment.
2. **Orchestration:** `OrderPlacementFacade` coordinates between `OrderFacade` (local state), `OrderInventoryOrchestrator` (catalog interaction), and `IPaymentGatewayService` (payment).
3. **Propagation:** When an external payment gateway updates a transaction (via Webhook), the `PaymentGatewayService` propagates the status back to the Checkout service, which then triggers either a `commit` or `release` of the inventory reservation.
4. **Resiliency:** If a reservation commit fails after a successful payment, the system retains the order in `PENDING_PAYMENT` with `PAID` payment status to allow for manual reconciliation or retry, preventing data loss.
