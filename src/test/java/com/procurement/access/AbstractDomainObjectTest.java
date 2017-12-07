package com.procurement.access;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by Maxim Sambulat.
 */
public abstract class AbstractDomainObjectTest extends JsonUtil {

    protected <T> void compare(final Class<T> target, final String file) {
        final String resource = getResource(file);
        final T obj = toObject(target, resource);

        final String actual = toJson(obj);

        assertEquals(resource, actual);
    }

    protected <T> void compareObj(final T objOrigin, final Class<T> target, final String file) {
        final String resource = getResource(file);
        final T obj = toObject(target, resource);

        assertEquals(objOrigin, obj);
    }
}
