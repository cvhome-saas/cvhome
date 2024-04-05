package com.asrevo.cvhome.store.core.services.payments;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.order.Order;
import com.asrevo.cvhome.store.core.entity.payments.Transaction;
import com.asrevo.cvhome.store.core.entity.payments.TransactionType;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.repositories.payments.TransactionRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;


@Service("transactionService")
public class TransactionServiceImpl extends SalesManagerEntityServiceImpl<Long, Transaction> implements TransactionService {


    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        super(transactionRepository);
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void create(Transaction transaction) throws ServiceException {

        //parse JSON string
        String transactionDetails = transaction.toJSONString();
        if (!StringUtils.isBlank(transactionDetails)) {
            transaction.setDetails(transactionDetails);
        }

        super.create(transaction);


    }

    @Override
    public List<Transaction> listTransactions(Order order) throws ServiceException {

        List<Transaction> transactions = transactionRepository.findByOrder(order.getId());
        ObjectMapper mapper = new ObjectMapper();
        for (Transaction transaction : transactions) {
            if (!StringUtils.isBlank(transaction.getDetails())) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, String> objects = mapper.readValue(transaction.getDetails(), Map.class);
                    transaction.setTransactionDetails(objects);
                } catch (Exception e) {
                    throw new ServiceException(e);
                }
            }
        }

        return transactions;
    }

    /**
     * Authorize
     * AuthorizeAndCapture
     * Capture
     * Refund
     * <p>
     * Check transactions
     * next transaction flow is
     * Build map of transactions map
     * filter get last from date
     * get last transaction type
     * verify which step transaction it if
     * check if target transaction is in transaction map we are in trouble...
     */
    public Transaction lastTransaction(Order order, MerchantStore store) throws ServiceException {

        List<Transaction> transactions = transactionRepository.findByOrder(order.getId());
        //ObjectMapper mapper = new ObjectMapper();

        //TODO order by date
        TreeMap<String, Transaction> map = transactions.stream()
                .collect(

                        Collectors.toMap(
                                Transaction::getTransactionTypeName, transaction -> transaction, (o1, o2) -> o1, TreeMap::new)


                );


        //get last transaction
        Entry<String, Transaction> last = map.lastEntry();

        String currentStep = last.getKey();

        System.out.println("Current step " + currentStep);

        //find next step

        return last.getValue();


    }

    @Override
    public Transaction getCapturableTransaction(Order order)
            throws ServiceException {
        List<Transaction> transactions = transactionRepository.findByOrder(order.getId());
        ObjectMapper mapper = new ObjectMapper();
        Transaction capturable = null;
        for (Transaction transaction : transactions) {
            if (transaction.getTransactionType().name().equals(TransactionType.AUTHORIZE.name())) {
                if (!StringUtils.isBlank(transaction.getDetails())) {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, String> objects = mapper.readValue(transaction.getDetails(), Map.class);
                        transaction.setTransactionDetails(objects);
                        capturable = transaction;
                    } catch (Exception e) {
                        throw new ServiceException(e);
                    }
                }
            }
            if (transaction.getTransactionType().name().equals(TransactionType.CAPTURE.name())) {
                break;
            }
            if (transaction.getTransactionType().name().equals(TransactionType.REFUND.name())) {
                break;
            }
        }

        return capturable;
    }

    @Override
    public Transaction getRefundableTransaction(Order order)
            throws ServiceException {
        List<Transaction> transactions = transactionRepository.findByOrder(order.getId());
        Map<String, Transaction> finalTransactions = new HashMap<String, Transaction>();
        Transaction finalTransaction = null;
        for (Transaction transaction : transactions) {
            if (transaction.getTransactionType().name().equals(TransactionType.AUTHORIZECAPTURE.name())) {
                finalTransactions.put(TransactionType.AUTHORIZECAPTURE.name(), transaction);
                continue;
            }
            if (transaction.getTransactionType().name().equals(TransactionType.CAPTURE.name())) {
                finalTransactions.put(TransactionType.CAPTURE.name(), transaction);
                continue;
            }
            if (transaction.getTransactionType().name().equals(TransactionType.REFUND.name())) {
                //check transaction id
                Transaction previousRefund = finalTransactions.get(TransactionType.REFUND.name());
                if (previousRefund != null) {
                    Date previousDate = previousRefund.getTransactionDate();
                    Date currentDate = transaction.getTransactionDate();
                    if (previousDate.before(currentDate)) {
                        finalTransactions.put(TransactionType.REFUND.name(), transaction);
                    }
                } else {
                    finalTransactions.put(TransactionType.REFUND.name(), transaction);
                }
            }
        }

        if (finalTransactions.containsKey(TransactionType.AUTHORIZECAPTURE.name())) {
            finalTransaction = finalTransactions.get(TransactionType.AUTHORIZECAPTURE.name());
        }

        if (finalTransactions.containsKey(TransactionType.CAPTURE.name())) {
            finalTransaction = finalTransactions.get(TransactionType.CAPTURE.name());
        }

        if (finalTransaction != null && !StringUtils.isBlank(finalTransaction.getDetails())) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, String> objects = mapper.readValue(finalTransaction.getDetails(), Map.class);
                finalTransaction.setTransactionDetails(objects);
            } catch (Exception e) {
                throw new ServiceException(e);
            }
        }

        return finalTransaction;
    }

    @Override
    public List<Transaction> listTransactions(Date startDate, Date endDate) throws ServiceException {

        return transactionRepository.findByDates(startDate, endDate);
    }

}
