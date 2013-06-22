package net.alpenblock.bungeeperms.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DelegateDeserialization {
    /**
* Which class should be used as a delegate for this classes deserialization
*
* @return Delegate class
*/
    public Class<? extends ConfigurationSerializable> value();
}