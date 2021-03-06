package edu.ualberta.med.biobank.validator.constraint;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import edu.ualberta.med.biobank.validator.constraint.impl.NotUsedValidator;

/**
 * Asserts that the annotated bean is not used by the given class through the
 * given property.
 * <p>
 * The message need not contain a list of objects that reference the 'NotUsed'
 * object because the person who catches the exception has the necessary data to
 * go and get that list.
 * 
 * @author Jonathan Ferland
 */
@Documented
@Constraint(validatedBy = { NotUsedValidator.class })
@Target({ TYPE, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
public @interface NotUsed {
    String message() default "";

    Class<?> by();

    String property();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Defines several {@code @NotUsed} annotations on the same element.
     */
    @Target({ TYPE, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
    @Retention(RUNTIME)
    @Documented
    public @interface List {
        NotUsed[] value();
    }
}
