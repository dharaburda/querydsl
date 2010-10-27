/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.query.codegen;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.mysema.codegen.JavaWriter;
import com.mysema.codegen.model.ClassType;
import com.mysema.codegen.model.SimpleType;
import com.mysema.codegen.model.Type;
import com.mysema.codegen.model.TypeCategory;
import com.mysema.codegen.model.Types;

public class BeanSerializerTest {
    
    private Type typeModel;
    
    private EntityType type;

    private Writer writer = new StringWriter();

    @Before
    public void setUp(){
        typeModel = new SimpleType(TypeCategory.ENTITY, "com.mysema.query.DomainClass", "com.mysema.query", "DomainClass", false,false);
        type = new EntityType("Q", typeModel);                    
    }
    
    @Test
    public void Annotations() throws IOException{
        type.addAnnotation(new QueryEntityImpl());
        
        BeanSerializer serializer = new BeanSerializer();
        serializer.serialize(type, SimpleSerializerConfig.DEFAULT, new JavaWriter(writer));
        String str = writer.toString();
        
        assertTrue(str.contains("import com.mysema.query.annotations.QueryEntity;"));
        assertTrue(str.contains("@QueryEntity"));
    }
    
    @Test
    public void Annotated_Property() throws IOException{
        Property property = new Property(type, "entityField", type);
        property.addAnnotation(new QueryEntityImpl());
        type.addProperty(property);
        
        BeanSerializer serializer = new BeanSerializer();
        serializer.serialize(type, SimpleSerializerConfig.DEFAULT, new JavaWriter(writer));
        String str = writer.toString();
        
        assertTrue(str.contains("import com.mysema.query.annotations.QueryEntity;"));
        assertTrue(str.contains("@QueryEntity"));
    }
    
    @Test
    public void Properties() throws IOException{
        // property
        type.addProperty(new Property(type, "entityField", type));
        type.addProperty(new Property(type, "collection", new SimpleType(Types.COLLECTION, typeModel)));
        type.addProperty(new Property(type, "listField", new SimpleType(Types.LIST, typeModel)));
        type.addProperty(new Property(type, "setField", new SimpleType(Types.SET, typeModel)));
        type.addProperty(new Property(type, "arrayField", new ClassType(TypeCategory.ARRAY, String[].class)));
        type.addProperty(new Property(type, "mapField", new SimpleType(Types.MAP, typeModel, typeModel)));

        for (Class<?> cl : Arrays.<Class<?>>asList(Boolean.class, Comparable.class, Integer.class, Date.class, java.sql.Date.class, java.sql.Time.class)){
            Type classType = new ClassType(TypeCategory.get(cl.getName()), cl);
            type.addProperty(new Property(type, StringUtils.uncapitalize(cl.getSimpleName()), classType));
        }

        BeanSerializer serializer = new BeanSerializer();
        serializer.serialize(type, SimpleSerializerConfig.DEFAULT, new JavaWriter(writer));
        String str = writer.toString();
        System.err.println(str);
        for (String prop : Arrays.asList(
                "String[] arrayField;",
                "Boolean boolean_;",
                "java.util.Collection<DomainClass> collection;",
                "Comparable comparable;",
                "java.util.Date date;",
                "DomainClass entityField;",
                "Integer integer;",
                "List<DomainClass> listField;",
                "Map<DomainClass, DomainClass> mapField;",
                "Set<DomainClass> setField;",
                "java.sql.Time time;")){
            assertTrue(prop + " was not contained", str.contains(prop));
        }
    }
    
}
