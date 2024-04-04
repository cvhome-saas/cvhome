package com.asrevo.cvhome.store.core.services.payments;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.order.Order;
import com.asrevo.cvhome.store.core.entity.payments.Transaction;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;


import java.util.Date;
import java.util.List;




public interface TransactionService extends SalesManagerEntityService<Long, Transaction> {

	/**
	 * Obtain a previous transaction that has type authorize for a give order
	 * @param order
	 * @return
	 * @throws ServiceException
	 */
	Transaction getCapturableTransaction(Order order) throws ServiceException;

	Transaction getRefundableTransaction(Order order) throws ServiceException;

	List<Transaction> listTransactions(Order order) throws ServiceException;
	
	List<Transaction> listTransactions(Date startDate, Date endDate) throws ServiceException;
	
	Transaction lastTransaction(Order order, MerchantStore store) throws ServiceException;



}