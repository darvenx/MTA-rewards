package com.training.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    // Pointcut for all repository methods
    @Before("execution(* com.training.repository.*.*(..))")
    public void logBeforeRepository(JoinPoint joinPoint) {
        logger.info("[Repository] Entering: {} with args: {}",
                joinPoint.getSignature().toShortString(),
                joinPoint.getArgs());
    }

    @AfterReturning(pointcut = "execution(* com.training.repository.*.*(..))", returning = "result")
    public void logAfterRepository(JoinPoint joinPoint, Object result) {
        logger.info("[Repository] Exiting: {} with result: {}",
                joinPoint.getSignature().toShortString(),
                result);
    }

    // Pointcut for all service methods
    @Before("execution(* com.training.service..*.*(..))")
    public void logBeforeService(JoinPoint joinPoint) {
        logger.info("[Service] Entering: {} with args: {}",
                joinPoint.getSignature().toShortString(),
                joinPoint.getArgs());
    }

    @AfterReturning(pointcut = "execution(* com.training.service..*.*(..))", returning = "result")
    public void logAfterService(JoinPoint joinPoint, Object result) {
        logger.info("[Service] Exiting: {} with result: {}",
                joinPoint.getSignature().toShortString(),
                result);
    }

    // Pointcut for all controller methods
    @Before("execution(* com.training.controller.*.*(..))")
    public void logBeforeController(JoinPoint joinPoint) {
        logger.info("[Controller] Entering: {} with args: {}",
                joinPoint.getSignature().toShortString(),
                joinPoint.getArgs());
    }

    @AfterReturning(pointcut = "execution(* com.training.controller.*.*(..))", returning = "result")
    public void logAfterController(JoinPoint joinPoint, Object result) {
        logger.info("[Controller] Exiting: {} with result: {}",
                joinPoint.getSignature().toShortString(),
                result);
    }
}
