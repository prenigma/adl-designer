/*
 * ADL Designer
 * Copyright (c) 2013-2014 Marand d.o.o. (www.marand.com)
 *
 * This file is part of ADL2-tools.
 *
 * ADL2-tools is free software: you can redistribute it and/or modify
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

package org.openehr.designer.json;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.openehr.jaxb.am.ArchetypeTerm;
import org.openehr.jaxb.am.CodeDefinitionSet;
import org.openehr.jaxb.rm.StringDictionaryItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Marko Pipan
 */
public class CodeDefinitionListToMapConverter extends StdConverter<List, Map> {

    @Override
    public Map convert(List value) {
        Map<String, Map<String, Map<String, String>>> result = new LinkedHashMap<>();
        for (CodeDefinitionSet cds : (List<CodeDefinitionSet>)value) {
            result.put(cds.getLanguage(), archetypeTermListToMap(cds.getItems()));
        }
        return result;
    }

    private Map<String, Map<String, String>> archetypeTermListToMap(List<ArchetypeTerm> terms) {
        Map<String, Map<String, String>> result = new LinkedHashMap<>();
        for (ArchetypeTerm term : terms) {
            result.put(term.getCode(), stringDictionaryItemListToMap(term.getItems()));
        }
        return result;
    }

    private Map<String, String> stringDictionaryItemListToMap(List<StringDictionaryItem> items) {
        Map<String, String> result = new LinkedHashMap<>();
        for (StringDictionaryItem item : items) {
            result.put(item.getId(), item.getValue());
        }
        return result;
    }
}
