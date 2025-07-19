package com.banda.parser;


public record ParseResult(String fileName, byte[] fileContent, String contentType) {}
