package com.asrevo.cvhome.store.core.services.generic;

/**
 * Indique que le service doit être rendu transactionnel via un aspect.
 * <p>
 * Cela permet de simplifier la configuration Spring de la partie transactionnelle car
 * il suffit alors de déclarer le pointcut de l'aspect sur
 * this(com.asrevo.cvhome.store.core.services.common.generic.ITransactionalAspectAwareService)
 */
public interface TransactionalAspectAwareService {

}
