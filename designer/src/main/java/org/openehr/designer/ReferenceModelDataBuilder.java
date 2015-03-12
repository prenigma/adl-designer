/*
 * ADL2-core
 * Copyright (c) 2013-2014 Marand d.o.o. (www.marand.com)
 *
 * This file is part of ADL2-core.
 *
 * ADL2-core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openehr.designer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openehr.adl.rm.RmModel;
import org.openehr.adl.rm.RmType;
import org.openehr.adl.rm.RmTypeAttribute;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author markopi
 */
public class ReferenceModelDataBuilder {

    private final Map<String, Type> typeMap;

    public ReferenceModelDataBuilder() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JavaType rootType = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, Type.class);
        typeMap = mapper.readValue(getClass().getClassLoader().getResourceAsStream("org/openehr/designer/rm/openehr.json"), rootType);
    }


    @Nullable
    private Type getTypeInfo(RmType type) {
        while (type != null) {
            Type t = typeMap.get(type.getRmType());
            if (t != null) return t;
            type = type.getParent();
        }
        return null;
    }

    private Optional<Object> getTypeInfoProperty(RmType type, String property) {
        try {
            Field f = Type.class.getDeclaredField(property);
            f.setAccessible(true);
            while (type != null) {
                Type t = typeMap.get(type.getRmType());
                if (t != null) {
                    Object fieldValue = f.get(t);
                    if (fieldValue != null) return Optional.of(fieldValue);
                }
                type = type.getParent();
            }
            return Optional.empty();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    @Nullable
    private Attribute getAttributeInfo(RmTypeAttribute rmAttribute) {
        RmType type = rmAttribute.getOwner();
        while (type != null) {
            Type t = typeMap.get(type.getRmType());
            if (t != null) {
                if (t.attributes != null) {
                    Attribute a = t.attributes.get(rmAttribute.getAttributeName());
                    if (a != null) return a;
                }
            }
            type = type.getParent();
        }
        return null;
    }

    public ReferenceModelData build(RmModel rmModel) {
        ReferenceModelData result = new ReferenceModelData();
        result.setName("openEHR");
        result.setVersion("1.0.2");
        result.setTypes(new LinkedHashMap<>());

        for (RmType type : rmModel.getAllTypes()) {

            ReferenceModelData.Type t = new ReferenceModelData.Type();
            t.setName(type.getRmType());
            t.setParent(type.getParent() != null ? type.getParent().getRmType() : null);
            getTypeInfoProperty(type, "rootType").ifPresent(f -> t.setRootType((Boolean) f));
            getTypeInfoProperty(type, "finalType").ifPresent(f -> t.setFinalType((Boolean) f));
//            t.setFinalType(type.isFinalType());
//            t.setRootType(type.isRootType());
            t.setDataAttribute(type.getDataAttribute());
//            if (type.getDisplay() != null) {
//                t.setDisplay(type.getDisplay().toString());
//            }
            if (!type.getAttributes().isEmpty()) {
                t.setAttributes(new LinkedHashMap<>());
                for (RmTypeAttribute attribute : type.getAttributes().values()) {
                    Attribute attrInfo = getAttributeInfo(attribute);
                    if (attrInfo != null && attrInfo.ignore) continue;

                    ReferenceModelData.Attribute a = new ReferenceModelData.Attribute();
                    a.setName(attribute.getAttributeName());
                    a.setExistence(ReferenceModelData.Multiplicity.of(attribute.getExistence()));
                    a.setType(attribute.getTargetType() != null ? attribute.getTargetType().getRmType() : null);
                    t.getAttributes().put(a.getName(), a);
                }
            }
            result.getTypes().put(t.getName(), t);
        }
        return result;
    }

    static class Type {
        @JsonProperty
        Boolean rootType;
        @JsonProperty
        Boolean finalType;
        @JsonProperty
        Map<String, Attribute> attributes;
    }

    static class Attribute {
        @JsonProperty
        boolean ignore;
    }
}
