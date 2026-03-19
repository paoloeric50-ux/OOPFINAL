package com.motorph.payroll.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.HashMap;
import java.util.Map;

/*
 * MapJsonConverter.java - JPA Attribute Converter for Map<String, Object>
 *
 * The problem: the payslip has "earnings" and "deductions" fields that are
 * Maps (key-value pairs). A regular SQL database column can't store a Map directly.
 *
 * The solution: convert the Map to a JSON string before saving it to the DB,
 * and convert the JSON string back to a Map when reading it from the DB.
 *
 * Example:
 *   Map  → {"basic_pay": 15000.0, "overtime_pay": 500.0}  (stored as text in DB)
 *   Text → Map<String, Object>  (loaded back when reading from DB)
 *
 * JPA will automatically call this converter for any field annotated with
 * @Convert(converter = MapJsonConverter.class)
 *
 * Author: Group 5 - MO-IT101
 */
@Converter
public class MapJsonConverter implements AttributeConverter<Map<String, Object>, String> {

    // ObjectMapper from Jackson library handles JSON conversion
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /*
     * convertToDatabaseColumn() - Map → JSON String
     * Called before saving/updating a record in the database.
     * Returns "{}" as a safe fallback if something goes wrong.
     */
    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        if (attribute == null) return null;
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            return "{}";  // empty JSON object as fallback
        }
    }

    /*
     * convertToEntityAttribute() - JSON String → Map
     * Called after reading a record from the database.
     * Returns an empty map if the stored value is null or can't be parsed.
     */
    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return new HashMap<>();
        try {
            return objectMapper.readValue(dbData, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();  // return empty map on parse error
        }
    }
}
