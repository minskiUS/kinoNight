package org.home.kinonight.aop;

import lombok.AllArgsConstructor;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.home.kinonight.component.KinoManagerBot;
import org.home.kinonight.model.ExceptionDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

@Aspect
@Component
@AllArgsConstructor
public class BotExceptionHandler {

    private KinoManagerBot kinoManagerBot;

    @Pointcut("execution(* org.home.kinonight.service..*(..))")
    public void allMethods() {
    }

    @AfterThrowing(pointcut = "allMethods()", throwing = "e")
    public void handleExceptions(Exception e) {
        Field field = null;
        try {
            field = e.getClass().getSuperclass().getDeclaredField("exceptionDetails");
            field.setAccessible(true);
            ExceptionDetails exceptionDetails = (ExceptionDetails) ReflectionUtils.getField(field, e);
            if (exceptionDetails == null) {
                throw new RuntimeException();
            }
            kinoManagerBot.sendExceptionMessageToUser(exceptionDetails);
        } catch (NoSuchFieldException | RuntimeException ex) {
            throw new RuntimeException(ex.getMessage());
        } finally {
            if (field != null) {
                field.setAccessible(false);
            }
        }
    }
}
