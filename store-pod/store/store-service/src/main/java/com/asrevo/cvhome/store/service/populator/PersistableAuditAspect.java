package com.asrevo.cvhome.store.service.populator;

import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

/**
 * Create audit section
 *
 * @author carlsamson
 */
@Aspect
@Configuration
@Slf4j
public class PersistableAuditAspect {


    @AfterReturning(value = "execution(* populate(..))",
            returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {

        try {
            if (result instanceof Auditable) {
                Auditable entity = (Auditable) result;
                AuditSection audit = entity.getAuditSection();
                if (entity.getAuditSection() == null) {
                    audit = new AuditSection();
                }
                audit.setDateModified(new Date());
//	@TODO ASHRAF

/*
					Authentication auth = SecurityContextHolder.getContext().getAuthentication();
					if(auth!=null) {
						if(auth instanceof UsernamePasswordAuthenticationToken) {//api only is captured
							com.asrevo.cvhome.store.store.security.user.JWTUser user = (com.asrevo.cvhome.store.store.security.user.JWTUser)auth.getPrincipal();
							audit.setModifiedBy(user.getUsername());
						}
					}
*/
                //TODO put in log audit log trail
                entity.setAuditSection(audit);
            }
        } catch (Throwable e) {
            log.error("Error while setting audit values{}", e.getMessage());
        }

    }


}
