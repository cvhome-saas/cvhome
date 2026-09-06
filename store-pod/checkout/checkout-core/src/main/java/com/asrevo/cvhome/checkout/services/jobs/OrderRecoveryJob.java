package com.asrevo.cvhome.checkout.services.jobs;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.asrevo.cvhome.checkout.config.CheckoutProperties;
import com.asrevo.cvhome.checkout.entity.Order;
import com.asrevo.cvhome.checkout.repositories.OrderRepository;
import com.asrevo.cvhome.checkout.services.order.OrderStepRunner;
import com.asrevo.cvhome.errors.BaseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Finishes what a request could not: any order still owing a remote step after {@code checkout.recovery.stale-after}.
 * One loop covers a crash after the order row, a crash after the reservation, a paid order whose commit found inventory
 * down, and a cancelled order whose release did.
 *
 * <p>
 * Runs on every replica with no lock. The claim is {@link Order#recoveryAttempted}: two replicas picking the same id
 * both try it, the second loses the optimistic lock and skips. Even if both got past that, the remotes are idempotent
 * by ref and the second apply loses its lock too.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderRecoveryJob {

    private final OrderRepository orders;

    private final OrderStepRunner steps;

    private final CheckoutProperties properties;

    private final TransactionTemplate transactions;

    private final Clock clock;

    @Scheduled(fixedDelayString = "${checkout.recovery.interval:30s}")
    public void recover() {
        Instant now = clock.instant();
        List<Long> stale = orders.findStalePendingActionIds(now.minus(properties.getRecovery().getStaleAfter()),
                PageRequest.of(0, properties.getRecovery().getBatchSize()));
        for (Long orderId : stale) {
            recoverOne(orderId);
        }
    }

    /**
     * @return whether a step was attempted (false when the claim was lost or the order gave up)
     */
    public boolean recoverOne(Long orderId) {
        if (!claim(orderId)) {
            return false;
        }
        try {
            steps.runUntilSettled(orderId, 1);
        } catch (BaseException e) {
            log.warn("Order {}: recovery attempt failed, will retry: {}", orderId, e.getMessage());
        }
        return true;
    }

    private boolean claim(Long orderId) {
        try {
            return Boolean.TRUE.equals(transactions.execute(status -> {
                Order order = orders.findById(orderId).orElse(null);
                if (order == null || !order.hasPendingAction() || order.isNeedsAttention()) {
                    return false;
                }
                Instant now = clock.instant();
                if (order.getPendingActionAttempts() >= properties.getRecovery().getMaxAttempts()) {
                    order.recoveryGaveUp(now);
                    orders.saveAndFlush(order);
                    log.error("Order {} still owes {} after {} attempts; flagged for attention", orderId,
                            order.getPendingAction(), order.getPendingActionAttempts());
                    return false;
                }
                order.recoveryAttempted(now);
                orders.saveAndFlush(order);
                return true;
            }));
        } catch (ObjectOptimisticLockingFailureException e) {
            log.debug("Order {} claimed by another replica", orderId);
            return false;
        }
    }
}
